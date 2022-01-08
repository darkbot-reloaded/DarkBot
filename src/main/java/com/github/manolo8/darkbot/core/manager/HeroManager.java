package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Player;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.handlers.ShipModeSelectorHandler;
import eu.darkbot.api.config.types.ShipMode;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;
import eu.darkbot.api.extensions.selectors.ShipModeSelector;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.items.Item;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.utils.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.objects.facades.SettingsProxy.KeyBind.*;

public class HeroManager extends Player implements Manager, HeroAPI {

    public static HeroManager instance;
    public final Main main;
    public final Pet pet;
    public final Drive drive;

    private final SettingsManager settings;
    private final SettingsProxy keybinds;
    private final Collection<? extends Portal> portals;
    private final HeroItemsAPI items;

    private final ShipModeSelectorHandler shipModeHandler;
    private final MutableShipMode shipMode = new MutableShipMode();

    public Map map;

    @Deprecated
    public Ship target;
    private Ship lastTarget; // Copy of target, to detect when it has been modified
    private Lockable localTarget;
    public int config;

    private long staticAddress;
    private Entity inGameTarget;
    private Configuration configuration = Configuration.UNKNOWN;
    private long configTime;
    private Character formation = null;
    private long formationTime;
    private long portalTime;

    public HeroManager(Main main,
                       SettingsManager settingsManager,
                       MapManager mapManager,
                       Drive drive,
                       FacadeManager facadeManager,
                       StarManager star,
                       HeroItemsAPI items,
                       ShipModeSelectorHandler shipModeHandler) {
        instance = this;

        this.main = super.main = main;
        this.settings = settingsManager;
        this.keybinds = facadeManager.settings;
        this.portals = mapManager.entities.getPortals();
        this.drive = drive;
        main.status.add(drive::toggleRunning);
        this.pet = new Pet();
        this.pet.main = main;
        this.map = star.byId(-1);

        this.items = items;

        this.shipModeHandler = shipModeHandler;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> staticAddress = value + 240);
    }

    public void tick() {
        long address = API.readMemoryLong(staticAddress);
        if (this.address != address) update(address);

        update();

        drive.checkMove();

        setConfigAndFormation(shipModeHandler.getBest());
    }

    @Override
    public void update() {
        super.update();
        config = settings.config;
        configuration = Configuration.of(config);

        long petAddress = API.readMemoryLong(address + 176);
        if (petAddress != pet.address) pet.update(petAddress);
        pet.update();


        long targetPtr = API.readMemoryLong(main.mapManager.mapAddress, 120, 40);

        if (targetPtr == 0) inGameTarget = null;
        else if (targetPtr == petAddress) inGameTarget = pet;
        else inGameTarget = main.mapManager.entities.allEntities.stream()
                .flatMap(Collection::stream)
                .filter(entity -> entity.address == targetPtr)
                .findAny().orElse(null);

        if (lastTarget != target) setLocalTarget(target);
    }

    @Override
    public void update(long address) {
        super.update(address);

        pet.update(API.readMemoryLong(address + 176));
        clickable.setRadius(0);
        id = API.readMemoryInt(address + 56);
    }

    public boolean hasTarget() {
        return this.localTarget != null && localTarget.isValid();
    }

    public long timeTo(double distance) {
        return super.timeTo(distance);
    }

    public void jumpPortal(Portal portal) {
        if (!portal.isValid()) return;
        if (System.currentTimeMillis() - portalTime < 500) return; // Minimum delay
        if ((System.currentTimeMillis() - portalTime > 20000 || isNotJumping(portal)) &&
                (portal.isSelectable() || portals.stream().noneMatch(p -> p != portal && p.isSelectable()))) {
            API.keyboardClick(keybinds.getCharCode(JUMP_GATE));
            portalTime = System.currentTimeMillis();
        }
    }

    // Consider not jumping if still on current map and nextMap is either unset or not the portal target map
    private boolean isNotJumping(Portal portal) {
        return !portal.isJumping() && map.id == settings.currMap &&
                (settings.nextMap == -1 || portal.getTargetMap().map(m -> m.getId() != settings.nextMap).orElse(true));
    }

    public boolean attackMode() {
        return setMode(main.config.GENERAL.OFFENSIVE);
    }

    public boolean attackMode(Npc target) {
        if (target == null) return attackMode();
        Config.ShipConfig config = this.main.config.GENERAL.OFFENSIVE;
        return setMode(config.CONFIG, target.npcInfo.attackFormation != null ?
                target.npcInfo.attackFormation : config.FORMATION);
    }

    public boolean runMode() {
        return setMode(this.main.config.GENERAL.RUN);
    }

    public boolean roamMode() {
        return setMode(main.config.GENERAL.ROAM);
    }

    public boolean setMode(Config.ShipConfig config) {
        return setMode(config.CONFIG, config.FORMATION);
    }

    public boolean setMode(int con, Character form) {
//        int formationCheck = main.config.GENERAL.FORMATION_CHECK;
//
//        if (this.config != con && System.currentTimeMillis() - configTime > 5500L) {
//            Main.API.keyboardClick(keybinds.getCharCode(TOGGLE_CONFIG));
//            this.configTime = System.currentTimeMillis();
//        }
//        boolean checkFormation = formationCheck > 0 && (System.currentTimeMillis() - formationTime) > formationCheck * 1000L;
//
//        if ((this.formation != form || checkFormation) && System.currentTimeMillis() - formationTime > 3500L) {
//            Main.API.keyboardClick(this.formation = form);
//            if (formation != null) this.formationTime = System.currentTimeMillis();
//        }

        Configuration conf = Configuration.of(con);
        if (conf == Configuration.UNKNOWN) return false; // unknown config? return, as we can't select unknown configuration.

        Item item = items.getItem(form, ItemCategory.DRONE_FORMATIONS);
        if (item == null) return false; // item not found & probably not possible to click, return

        this.formation = form;
        shipMode.set(conf, item.getAs(SelectableItem.Formation.class));

        return isInMode(shipMode);
    }

    public boolean isInMode(Config.ShipConfig config) {
        return isInMode(config.CONFIG, config.FORMATION);
    }

    public boolean isInMode(int config, Character formation) {
        return this.config == config && this.formation == formation;
    }

    /**
     * @param entity the target entity to set
     * @deprecated use {@link #setLocalTarget(Lockable)} instead
     */
    @Deprecated
    public void setTarget(Ship entity) {
        this.localTarget = this.target = this.lastTarget = entity;
    }

    private void toggleConfiguration() {
        if (System.currentTimeMillis() - configTime <= 5500L) return;

        if (keybinds.pressKeybind(TOGGLE_CONFIG))
            this.configTime = System.currentTimeMillis();
        //else toggle config key bind is missing?
    }

    private void setFormation(SelectableItem.Formation formation) {
        if (formation == null || formation == getFormation() ||
                System.currentTimeMillis() - formationTime <= 2500L) return;

        main.facadeManager.slotBars.useItem(formation, ItemFlag.NOT_SELECTED)
                .ifSuccessful(r -> formationTime = System.currentTimeMillis());
    }

    @Override
    public @UnknownNullability Lockable getLocalTarget() {
        return localTarget;
    }

    @Override
    public void setLocalTarget(@Nullable Lockable lockable) {
        if (lockable instanceof Ship) target = lastTarget = (Ship) lockable;
        else target = lastTarget = null;

        this.localTarget = lockable;
    }

    @Nullable
    @Override
    public Entity getTarget() {
        return inGameTarget;
    }

    @Override
    public GameMap getMap() {
        return map;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean isInMode(ShipMode mode) {
        return mode.getConfiguration() == getConfiguration() && mode.getFormation() == getFormation();
    }

    @Override
    public boolean setMode(@NotNull ShipMode mode) {
        this.shipMode.set(mode);
        return isInMode(mode);
    }

    private void setConfigAndFormation(ShipMode mode) {
        if (mode.getConfiguration() != null &&
                mode.getConfiguration() != getConfiguration()) toggleConfiguration();
        setFormation(mode.getFormation());
    }

    @Override
    public boolean setAttackMode(eu.darkbot.api.game.entities.Npc target) {
        return attackMode((Npc) target); //todo change with predefined ShipMode by user & set into selector
    }

    @Override
    public boolean setRoamMode() {
        return roamMode(); //todo change with predefined ShipMode by user & set into selector
    }

    @Override
    public boolean setRunMode() {
        return runMode(); //todo change with predefined ShipMode by user & set into selector
    }

    @Override
    public boolean triggerLaserAttack() {
        return keybinds.pressKeybind(ATTACK_LASER);
    }

    @Override
    public boolean launchRocket() {
        return keybinds.pressKeybind(ATTACK_ROCKET);
    }

    @Override
    public SelectableItem.Laser getLaser() {
        return items.getItems(ItemCategory.LASERS).stream()
                .filter(Item::isSelected)
                .map(item -> SelectableItem.Laser.of(item.getId()))
                .findFirst().orElse(null);
    }

    @Override
    public SelectableItem.Rocket getRocket() {
        return items.getItems(ItemCategory.ROCKETS).stream()
                .filter(Item::isSelected)
                .map(item -> SelectableItem.Rocket.of(item.getId()))
                .findFirst().orElse(null);
    }

    @Override
    public boolean hasPet() {
        return pet.isValid();
    }

    @Override
    public Optional<eu.darkbot.api.game.entities.Pet> getPet() {
        return hasPet() ? Optional.of(pet) : Optional.empty();
    }

    private static class MutableShipMode implements ShipMode {
        private Configuration configuration;
        private SelectableItem.Formation formation;

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public SelectableItem.Formation getFormation() {
            return formation;
        }

        public void set(ShipMode other) {
            this.configuration = other.getConfiguration();
            this.formation = other.getFormation();
        }

        public void set(Configuration configuration, SelectableItem.Formation formation) {
            this.configuration = configuration;
            this.formation = formation;
        }
    }

    @Feature(name = "Default Ship Mode Supplier", description = "Sets the fallback ship mode")
    public static class DefaultShipModeSupplier implements ShipModeSelector, PrioritizedSupplier<ShipMode> {

        private HeroManager hero;

        @Inject
        public void setHero(HeroManager hero) {
            this.hero = hero;
        }

        @Override
        public @NotNull PrioritizedSupplier<ShipMode> getShipModeSupplier() {
            return this;
        }

        @Override
        public ShipMode get() {
            return hero.shipMode; //default ship mode
        }
    }
}
