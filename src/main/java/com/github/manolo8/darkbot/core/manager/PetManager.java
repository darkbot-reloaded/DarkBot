package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.types.suppliers.PetGearSupplier;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.SpriteObject;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.handlers.PetGearSelectorHandler;
import com.github.manolo8.darkbot.gui.utils.Strings;
import eu.darkbot.api.extensions.selectors.GearSelector;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.game.other.EntityInfo;
import eu.darkbot.api.game.other.Health;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.game.other.LocationInfo;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.PetAPI;
import eu.darkbot.api.utils.Inject;
import eu.darkbot.api.utils.ItemNotEquippedException;
import eu.darkbot.util.TimeUtils;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.manolo8.darkbot.Main.API;

public class PetManager extends Gui implements PetAPI {

    private static final int MAIN_BUTTON_X = 30,
            MODULES_X_MAX = 260,
            MODULE_Y = 135,
            MODULE_Y_OFFSET = 17,
            MODULE_HEIGHT = 23,
            SUBMODULE_HEIGHT = 22;

    private final Main main;
    private final List<Ship> ships;
    private final Pet pet;
    private final PetGearSelectorHandler gearSelectorHandler;
    private final EventBrokerAPI eventBroker;

    private long togglePetTime, selectModuleTime;
    private long activeUntil;
    private Ship target;
    private boolean enabled = false;

    private final ObjArray gearsArr = ObjArray.ofArrObj();
    private final List<Gear> gearList = new ArrayList<>();
    private final List<PetGear> newGears = new ArrayList<>();

    private final ObjArray locatorWrapper = ObjArray.ofArrObj(), locatorNpcList = ObjArray.ofArrObj();
    private final List<Gear> locatorList = new ArrayList<>();

    private final List<Integer> petBuffsIds = new ArrayList<>();

    private final Set<Integer> currentSubmodules = new HashSet<>();
    private final Timer submodulesCheckTimer = Timer.get(TimeUtils.MINUTE * 5);
    private int submodulesSize;

    private ModuleStatus selection = ModuleStatus.NOTHING;
    private Gear currentModule;   // The Module used, like Passive mode, kamikaze, or enemy locator
    private Gear currentSubModule;// The submodule used, like an npc inside enemy locator.
    private long validUntil;
    private NpcInfo selectedNpc;

    private Integer gearOverride = null;
    private long gearOverrideTime = 0;
    private boolean repaired = true;

    private final Map<PetStatsType, PetStats> petStats = new EnumMap<>(PetStatsType.class);

    private enum ModuleStatus {
        NOTHING,
        DROPDOWN,
        SUB_DROPDOWN,
        SELECTED
    }

    public PetManager(Main main, MapManager mapManager, HeroManager hero,
                      PetGearSelectorHandler gearSelectorHandler, EventBrokerAPI eventBroker) {
        this.main = main;
        this.ships = mapManager.entities.ships;
        this.pet = hero.pet;
        this.gearSelectorHandler = gearSelectorHandler;
        this.eventBroker = eventBroker;

        PetGearSupplier.updateGears(gearList);
    }

    private PetGear getPetGearToUse() {
        PetGear gear = null;
        if (gearOverrideTime > System.currentTimeMillis() && gearOverride != null)
            gear = PetGear.of(gearOverride);

        if (gear != null)
            return gear;
        gear = main.config.PET.MODULE_ID;

        return gear == null ? PetGear.PASSIVE : gear;
    }

    public void tick() {
        if (!main.isRunning() || !main.config.PET.ENABLED) return;

        eu.darkbot.api.extensions.selectors.PetGearSupplier gearSupplier = gearSelectorHandler.getBestSupplier();

        Boolean enablePet = gearSupplier.enablePet();
        boolean enabled = enablePet != null ? enablePet : isEnabled();

        if (active() != enabled) {
            if (show(true)) clickToggleStatus();
            return;
        }
        if (!enabled) {
            show(false);
            return;
        }
        updatePetTarget();
        int moduleId = gearSupplier.get().getId();

        if (target != null && !(target instanceof Npc) && !target.playerInfo.isEnemy()) {
            moduleId = PetGear.PASSIVE.getId();
        }

        int submoduleId = -1, submoduleIdx = -1;
        if (moduleId == PetGear.ENEMY_LOCATOR.getId()) {
            NpcPick submodule = main.config.LOOT.NPC_INFOS.entrySet()
                    .stream()
                    .filter(e -> e.getValue().extra.has(NpcExtra.PET_LOCATOR))
                    .sorted(Comparator.comparingInt(e -> e.getValue().priority))
                    .map(entry -> new NpcPick(entry.getKey(), entry.getValue()))
                    .filter(p -> p.gear != null)
                    .findFirst()
                    .orElse(null);
            if (submodule != null) {
                selectedNpc = submodule.npc;
                submoduleId = submodule.gear.id;
                submoduleIdx = locatorList.indexOf(submodule.gear);
            }
        }
        if (submoduleId == -1) selectedNpc = null;

        if (selection != ModuleStatus.SELECTED
                || (currentModule != null && currentModule.id != moduleId)
                || (currentSubmodules.isEmpty() && submoduleIdx != -1)
                || (!currentSubmodules.isEmpty() && !currentSubmodules.contains(submoduleId))) {
            if (show(true)) this.selectModule(moduleId, submoduleIdx);
        } else if (System.currentTimeMillis() > this.selectModuleTime) show(false);
    }

    private class NpcPick {
        private final NpcInfo npc;
        private final Gear gear;
        public NpcPick(String npcName, NpcInfo npc) {
            this.npc = npc;
            String fuzzyName = npc.fuzzyName != null ? npc.fuzzyName : (npc.fuzzyName = Strings.fuzzyMatcher(npcName));
            this.gear = locatorList.stream().filter(l -> fuzzyName.equals(l.fuzzyName)).findFirst().orElse(null);
        }
    }
    public NpcInfo getTrackedNpc() {
        return selectedNpc;
    }

    private void updatePetTarget() {
        if (target == null || target.removed || !pet.isAttacking(target))
            target = ships.stream().filter(pet::isAttacking).findFirst().orElse(null);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOverride(PetGearSupplier.Gears gear) {
        setOverride(gear.getId());
    }

    public void setOverride(Integer gearId) {
        this.gearOverride = gearId;
        this.gearOverrideTime = gearId == null ? 0 : System.currentTimeMillis() + 6000;
    }

    public boolean hasGear(PetGearSupplier.Gears gear) {
        return hasGear(gear.getId());
    }

    @Override
    public boolean hasGear(int id) {
        return findGearById(id) != null;
    }

    public List<PetGear> getGears() {
        return newGears;
    }

    public PetStats getPetStats(PetStatsType type) {
        return petStats.get(type);
    }

    public boolean hasCooldown(PetBuff buff) {
        return hasCooldown(buff.getId());
    }

    @Override
    public boolean hasCooldown(int buffId) {
        return petBuffsIds.contains(buffId);
    }

    @Override
    public boolean isRepaired() {
        return repaired;
    }

    private boolean active() {
        if (!pet.removed) activeUntil = System.currentTimeMillis() + 1000;
        return System.currentTimeMillis() < activeUntil;
    }

    private void clickToggleStatus() {
        if (System.currentTimeMillis() - this.togglePetTime > 2000L) {
            click(MAIN_BUTTON_X, MODULE_Y);
            this.selection = ModuleStatus.NOTHING;
            this.togglePetTime = System.currentTimeMillis();
        }
    }

    private void selectModule(int moduleId, int submoduleIdx) {
        if (System.currentTimeMillis() < this.selectModuleTime) return;

        Gear gear = null;
        if (submoduleIdx == -1) {
            int moduleIdx = moduleIdToIndex(moduleId);
            if (moduleIdx < gearList.size()) gear = gearList.get(moduleIdx);
        } else {
            gear = locatorList.get(submoduleIdx);
        }

        if (gear != null) {
            long gearsSprite = getSpriteChild(address, -1);
            gear.setModule(gearsSprite);
            this.selection = ModuleStatus.SELECTED;
            this.selectModuleTime = System.currentTimeMillis() + 1000;
        }
    }

    private int getModuleY(int moduleId, boolean centered) {
        return MODULE_Y + MODULE_Y_OFFSET + (MODULE_HEIGHT * moduleIdToIndex(moduleId)) +
                (centered ? (MODULE_HEIGHT / 2) : 0);
    }

    private int moduleIdToIndex(int moduleId) {
        for (int i = 0; i < gearList.size(); i++) {
            if (gearList.get(i).id == moduleId) return i;
        }
        return 0;
    }

    private int repairCount;
    @Override
    public void update() {
        super.update();
        if (address == 0) return;

        long gearsSprite = getSpriteChild(address, -1);
        gearsArr.update(API.readMemoryLong(gearsSprite, 176, 224));
        gearsArr.syncAndReport(gearList, Gear::new);
        if (modulesChanged()) {
            newGears.clear();
            for (Gear gear : gearList)
                newGears.add(PetGear.of(gear.id));
        }
        PetGearSupplier.updateGears(gearList);

        updateNpcLocatorList(gearsSprite);

        long elementsListAddress = getElementsList(54);
        updateCurrentModule(elementsListAddress);

        updatePetBuffs(elementsListAddress);

        long element = getSpriteElement(elementsListAddress, 67);

        boolean wasRepaired = repaired;
        repaired = API.readMemoryLong(getSpriteChildWrapper(element, 0), 0x148) == 0;

        if (!wasRepaired && repaired) repairCount++;

        updatePetStats(elementsListAddress);
    }

    private boolean modulesChanged() {
        Iterator<Gear> it1 = gearList.iterator();
        Iterator<PetGear> it2 = newGears.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            Gear g1 = it1.next();
            PetGear g2 = it2.next();
            if (g1 == null || g2 == null || g1.id != g2.getId()) return true;
        }
        return it1.hasNext() || it2.hasNext();
    }

    @Deprecated
    public boolean hasBuff(PetBuff buff) {
        return hasBuff(buff.getId());
    }

    @Deprecated
    public boolean hasBuff(int buffId) {
        return petBuffsIds.contains(buffId);
    }

    @Deprecated
    public boolean isPetRepaired() {
        return repaired;
    }

    @Deprecated // Use hasGear instead
    public Gear byId(int id) {
        return findGearById(id);
    }

    private void updatePetBuffs(long elementsListAddress) {
        long temp = getSpriteElement(elementsListAddress, 70);
        temp = getSpriteChild(temp, 0);

        petBuffsIds.clear();
        forEachSpriteChild(temp, l -> petBuffsIds.add(API.readMemoryInt(l + 168)));
    }

    private void updateCurrentModule(long elementsListAddress) {
        long temp = getSpriteElement(elementsListAddress, 72);
        temp = API.readMemoryLong(getSpriteChild(temp, 0), 176); //get first sprite child then read 176 offset

        long currGearCheck = API.readMemoryLong(getSpriteChild(temp, 1), 152, 16);

        currentSubmodules.clear();
        currentModule = findGear(gearList, currGearCheck);
        if (currentModule == null) {
            Gear current = null;
            for (Gear gear : locatorList) {
                if (gear.check == currGearCheck) {
                    current = gear;
                    currentSubmodules.add(gear.id);
                }
            }

            currentSubModule = current;
            if (current != null) currentModule = findGearById(current.parentId);
        } else currentSubModule = null;

        if (currentSubmodules.size() > 1) {
            // check every 5 minutes if we have selected correct alien
            if (!submodulesCheckTimer.isArmed()) submodulesCheckTimer.activate();
            if (submodulesCheckTimer.tryDisarm()) selection = ModuleStatus.NOTHING;

            // recheck on size change with same check-address
            if (submodulesSize != currentSubmodules.size()) {
                submodulesCheckTimer.activate();
                submodulesSize = currentSubmodules.size();
                selection = ModuleStatus.NOTHING;
            }
        } else submodulesCheckTimer.disarm();
    }

    private final SpriteObject locatorTab = new SpriteObject();
    private void updateNpcLocatorList(long gearsSprite) {
        locatorWrapper.update(API.readMemoryLong(gearsSprite + 168));

        long locatorBaseAddr = locatorWrapper.get(0);
        if (locatorBaseAddr == 0) {
            locatorList.clear();
            return;
        }
        locatorTab.update(locatorBaseAddr);
        locatorTab.update();
        int oldSize = locatorNpcList.getSize();
        locatorNpcList.update(API.readMemoryLong(locatorBaseAddr + 224));

        // Sometimes the NPC list will be half-updated and there may be way less npcs than before.
        // If we have a recent update and list is smaller, we'll ignore updating for a bit
        if (locatorNpcList.getSize() < oldSize && validUntil > System.currentTimeMillis()) return;

        validUntil = System.currentTimeMillis() + 100;
        if (locatorNpcList.syncAndReport(locatorList, Gear::new)) {
            eventBroker.sendEvent(new LocatorNpcListChangeEvent(getLocatorNpcs()));
        }
    }

    private void updatePetStats(long elementsListAddress) {
        if (pet.removed) return;
        for (PetStatsType type : PetStatsType.values())
            petStats.computeIfAbsent(type, PetStats::new).update(elementsListAddress, type.id);
    }

    private Gear findGear(List<Gear> gears, long check) {
        for (Gear gear : gears) if (gear.check == check) return gear;
        return null;
    }

    private Gear findGearById(int id) {
        for (Gear gear : gearList) if (gear.id == id) return gear;
        return null;
    }

    public Gear getCurrentModule() {
        return currentModule;
    }

    public Gear getCurrentSubModule() {
        return currentSubModule;
    }

    public Integer getGearOverride() {
        return gearOverride;
    }

    @Override
    public @NotNull Collection<? extends NpcInfo> getLocatorNpcs() {
        if (locatorList.isEmpty()) return Collections.emptyList();

        return main.config.LOOT.NPC_INFOS.entrySet()
                .stream()
                .map(entry -> new NpcPick(entry.getKey(), entry.getValue()))
                .filter(p -> p.gear != null)
                .map(p -> p.npc)
                .collect(Collectors.toList());
    }

    public class PetStats implements PetStat {
        private double curr, total;

        public double getCurr() {
            return curr;
        }

        @Override
        public double getCurrent() {
            return curr;
        }

        @Override
        public double getTotal() {
            return total;
        }

        PetStats(PetManager.PetStatsType petStatsType) {
            this.curr = 0;
            this.total = 0;
        }

        private void update(long elementsListAddress, int id) {
            long address = getAddress(elementsListAddress, id);
            curr = API.readMemoryDouble(address, 0x118);
            total = API.readMemoryDouble(address, 0x120);
        }

        private long getAddress(long elementsListAddress, int id) {
            long element = getSpriteElement(elementsListAddress, id);
            return getSpriteChild(element, 0);
        }
    }

    public enum PetStatsType {
        HP(60), SHIELD(62), FUEL(63), XP(61), HEAT(109);

        private final int id;

        PetStatsType(int id) {
            this.id = id;
        }
    }

    // Represents a pet cool down
    public enum PetBuff {
        SINGULARITY,
        SPEED_LEECH,
        TRADE,
        WEAKEN_SHIELD,
        KAMIKAZE_CD,
        COMBO_REPAIR_CD,
        FRIENDLY_SACRIFICE,
        RETARGETING_CD,
        HP_LINK_CD,
        MEGA_MINE_CD;

        public int getId() {
            return ordinal() + 1;
        }
    }

    @Override
    public int getId() {
        return pet.getId();
    }

    @Override
    public boolean isValid() {
        return pet.isValid();
    }

    @Override
    public boolean isSelectable() {
        return pet.isSelectable();
    }

    @Override
    public boolean trySelect(boolean tryAttack) {
        return pet.trySelect(tryAttack);
    }

    @Override
    public LocationInfo getLocationInfo() {
        return pet.getLocationInfo();
    }

    @Override
    public Collection<Integer> getEffects() {
        return pet.getEffects();
    }

    @Override
    public void setMetadata(String key, Object value) {
        pet.setMetadata(key, value);
    }

    @Override
    public @Nullable Object getMetadata(String key) {
        return pet.getMetadata(key);
    }

    @Override
    public int getLevel() {
        return pet.getLevel();
    }

    @Override
    public int getOwnerId() {
        return pet.getOwnerId();
    }

    @Override
    public Optional<eu.darkbot.api.game.entities.Ship> getOwner() {
        return pet.getOwner();
    }

    @Override
    public int getShipId() {
        return pet.getShipId();
    }

    @Override
    public boolean isInvisible() {
        return pet.isInvisible();
    }

    @Override
    public boolean isBlacklisted() {
        return pet.isBlacklisted();
    }

    @Override
    public void setBlacklisted(long time) {
        pet.setBlacklisted(time);
    }

    @Override
    public Lock getLockType() {
        return pet.getLockType();
    }

    @Override
    public Health getHealth() {
        return pet.getHealth();
    }

    @Override
    public EntityInfo getEntityInfo() {
        return pet.getEntityInfo();
    }

    @Override
    public @Nullable Entity getTarget() {
        return pet.getTarget();
    }

    @Override
    public boolean isAttacking() {
        return pet.isAttacking();
    }

    @Override
    public boolean isMoving() {
        return pet.isMoving();
    }

    @Override
    public boolean isMoving(long inTime) {
        return pet.isMoving(inTime);
    }

    @Override
    public int getSpeed() {
        return pet.getSpeed();
    }

    @Override
    public double getAngle() {
        return pet.getAngle();
    }

    @Override
    public double getDestinationAngle() {
        return pet.getDestinationAngle();
    }

    @Override
    public boolean isAiming(Locatable other) {
        return pet.isAiming(other);
    }

    @Override
    public Optional<Location> getDestination() {
        return pet.getDestination();
    }

    @Override
    public boolean isActive() {
        return pet.address != 0;
    }

    @Override
    public int getRepairCount() {
        return repairCount;
    }

    @Override
    public PetGear getGear() {
        return currentModule == null ? null : PetGear.of(currentModule.id);
    }

    @Override
    public void setGear(Integer gearId) throws ItemNotEquippedException {
        if (gearId != null && !hasGear(gearId))
            throw new ItemNotEquippedException(PetGear.of(gearId), "Gear #" + gearId);

        this.setOverride(gearId);
    }

    @Override
    public void setGear(@Nullable PetGear petGear) throws ItemNotEquippedException {
        setGear(petGear != null ? petGear.getId() : null);
    }

    @Override
    public Optional<Location> getLocatorNpcLoc() {
        FakeNpc petPing = main.mapManager.entities.fakeNpc;

        if (petPing.isValid())
            return Optional.of(petPing.locationInfo);
        return Optional.empty();
    }

    public static class Gear extends Reporting implements Point {
        public int id, parentId;
        public long check;
        public String name, fuzzyName;

        private final SpriteObject sprite = new SpriteObject();

        @Override
        public boolean updateAndReport() {
            sprite.update(address);
            sprite.update();

            int id = API.readMemoryInt(address + 172);
            int parentId = API.readMemoryInt(address + 176); //assume, -1 if none
            String name = API.readMemoryString(API.readMemoryLong(address + 200));

            if (this.id == id && this.parentId == parentId && this.name.equals(name)) return false;

            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.fuzzyName = Strings.fuzzyMatcher(name);
            this.check = API.readMemoryLong(address, 208, 152, 0x10);
            return true;
        }

        @Override
        public double getX() {
            return sprite.getX();
        }

        @Override
        public double getY() {
            return sprite.getY();
        }

        public void setModule(long gearsSprite) {
            if (!API.hasCapability(GameAPI.Capability.DIRECT_CALL_METHOD)) return;

            Main.API.callMethodChecked(true, "23(handleClick)(2626)1016321600", 148, address);
            // hide gears list again
            Main.API.callMethodChecked(true, "23(hide)(26)008211400",
                    152, Main.API.readLong(gearsSprite, 176));
        }
    }

    @Override
    public PetStat getStat(Stat stat) {
        return petStats.get(PetStatsType.valueOf(stat.name()));
    }

    @Feature(name = "Default Gear Supplier", description = "Sets the fallback pet gear")
    public static class DefaultGearSupplier implements GearSelector, eu.darkbot.api.extensions.selectors.PetGearSupplier {

        private PetManager pet;

        @Inject
        public void setPetManager(PetManager pet) {
            this.pet = pet;
        }

        @Override
        public eu.darkbot.api.extensions.selectors.@NotNull PetGearSupplier getGearSupplier() {
            return this;
        }

        @Override
        public PetGear get() {
            return pet.getPetGearToUse();
        }

        @Override
        public @Nullable Boolean enablePet() {
            return pet.isEnabled();
        }
    }
}
