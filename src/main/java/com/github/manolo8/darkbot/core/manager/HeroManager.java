package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.utils.Drive;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.entities.other.SelectableItem;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.objects.facades.SettingsProxy.KeyBind.JUMP_GATE;
import static com.github.manolo8.darkbot.core.objects.facades.SettingsProxy.KeyBind.TOGGLE_CONFIG;

public class HeroManager extends Ship implements Manager, HeroAPI {

    public static HeroManager instance;
    public final Main main;
    private final SettingsManager settings;
    private final SettingsProxy keybinds;
    private final List<Portal> portals;

    private long staticAddress;

    public final Pet pet;
    public final Drive drive;

    public Map map;

    public Ship target;

    private Entity inGameTarget;

    private Configuration configuration = Configuration.UNKNOWN;
    public int config;
    public int formationId;
    private long configTime;
    private Character formation = null;
    private long formationTime;
    private long portalTime;

    public HeroManager(Main main, PluginAPI pluginAPI) {
        instance = this;

        this.main = super.main = main;
        this.settings = main.settingsManager;
        this.keybinds = main.facadeManager.settings;
        this.portals = main.mapManager.entities.portals;
        this.drive = pluginAPI.requireInstance(Drive.class);
        main.status.add(drive::toggleRunning);
        this.pet = new Pet();
        this.map = main.starManager.byId(-1);
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
    }

    @Override
    public void update() {
        super.update();
        config = settings.config;
        configuration = Configuration.of(config);
        formationId = super.formationId;

        long petAddress = API.readMemoryLong(address + 176);
        if (petAddress != pet.address) pet.update(petAddress);
        pet.update();


        long targetPtr = API.readMemoryLong(main.mapManager.mapAddress, 120, 40);

        if (targetPtr == 0) inGameTarget = null;
        else inGameTarget =  main.mapManager.entities.allEntities.stream()
                .flatMap(Collection::stream)
                .filter(entity -> entity.address == targetPtr)
                .findAny().orElse(null);
    }

    @Override
    public void update(long address) {
        super.update(address);

        pet.update(API.readMemoryLong(address + 176));
        clickable.setRadius(0);
        id = API.readMemoryInt(address + 56);
    }

    public void setTarget(Ship entity) {
        this.target = entity;
    }

    public boolean hasTarget() {
        return this.target != null && !target.removed;
    }

    public long timeTo(double distance) {
        return (long) (distance * 1000 / shipInfo.speed);
    }

    public void jumpPortal(Portal portal) {
        if (portal.removed) return;
        if (System.currentTimeMillis() - portalTime < 500) return; // Minimum delay
        if ((System.currentTimeMillis() - portalTime > 20000 || isNotJumping(portal)) &&
                (portal.clickable.enabled || portals.stream().noneMatch(p -> p != portal && p.clickable.enabled))) {
            API.keyboardClick(keybinds.getCharCode(JUMP_GATE));
            portalTime = System.currentTimeMillis();
        }
    }

    // Consider not jumping if still on current map and nextMap is either unset or not the portal target map
    private boolean isNotJumping(Portal portal) {
        return !portal.isJumping && map.id == settings.currMap &&
                (settings.nextMap == -1 || portal.target == null || settings.nextMap != portal.target.id);
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
        int formationCheck = main.config.GENERAL.FORMATION_CHECK;

        if (this.config != con && System.currentTimeMillis() - configTime > 5500L) {
            Main.API.keyboardClick(keybinds.getCharCode(TOGGLE_CONFIG));
            this.configTime = System.currentTimeMillis();
        }
        boolean checkFormation = formationCheck > 0 && (System.currentTimeMillis() - formationTime) > formationCheck * 1000;

        if ((this.formation != form || checkFormation) && System.currentTimeMillis() - formationTime > 3500L) {
            Main.API.keyboardClick(this.formation = form);
            if (formation != null) this.formationTime = System.currentTimeMillis();
        }
        return isInMode(con, form);
    }

    public boolean isInMode(Config.ShipConfig config) {
        return isInMode(config.CONFIG, config.FORMATION);
    }

    public boolean isInMode(int config, Character formation) {
        return this.config == config && this.formation == formation;
    }

    @Nullable
    @Override
    public Entity getTarget() {
        return inGameTarget;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }


    public void toggleConfiguration() {
        if (System.currentTimeMillis() - configTime <= 5500L) return;

        Main.API.keyboardClick(keybinds.getCharCode(TOGGLE_CONFIG));
        this.configTime = System.currentTimeMillis();
    }

    public void setFormation(SelectableItem.Formation formation) {
        if (formation == getFormation() ||
                System.currentTimeMillis() - formationTime <= 3500L) return;

        SlotBarsProxy slotBars = main.facadeManager.slotBars;

        slotBars.filterItem(HeroItemsAPI.Category.DRONE_FORMATIONS, formation::matches)
                .filter(slotBars::isSelectable)
                .ifPresent(slotBars::selectItem);

        this.formationTime = System.currentTimeMillis();
    }

    @Override
    public boolean isInMode(Mode mode) {
        return mode.getConfiguration() == getConfiguration() && mode.getFormation() == getFormation();
    }

    @Override
    public boolean setMode(Mode mode) {
        if (mode.getConfiguration() != getConfiguration()) toggleConfiguration();
        setFormation(mode.getFormation());

        return isInMode(mode);
    }

    @Override
    public boolean setAttackMode(eu.darkbot.api.entities.Npc target) {
        return attackMode((Npc) target);
    }

    @Override
    public boolean setRoamMode() {
        return roamMode();
    }

    @Override
    public boolean setRunMode() {
        return runMode();
    }
}
