package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.types.suppliers.PetGearSupplier;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class PetManager extends Gui {

    private static final int MAIN_BUTTON_X = 30, MODULES_X_MAX = 260, MODULE_Y = 120;

    private long togglePetTime, selectModuleTime;
    private long activeUntil;
    private Main main;
    private List<Ship> ships;
    private Ship target;
    private Pet pet;
    private boolean enabled = false;

    private ObjArray guiSprites = ObjArray.ofSprite();

    private ObjArray modulesArr = ObjArray.ofArrObj();
    private ObjArray currentArr = ObjArray.ofArrObj();
    private ObjArray currSpriteWrapper = ObjArray.ofSprite();
    private ObjArray currSprite = ObjArray.ofSprite();

    private ObjArray gearsArr = ObjArray.ofArrObj();
    private List<Gear> gearList = new ArrayList<>();

    private ObjArray locatorWrapper = ObjArray.ofArrObj(), locatorNpcList = ObjArray.ofArrObj();
    private List<Gear> locatorList = new ArrayList<>();


    private ModuleStatus selection = ModuleStatus.NOTHING;
    private Gear currentModule;   // The Module used, like Passive mode, kamikaze, or enemy locator
    private Gear currentSubModule;// The submodule used, like an npc inside enemy locator.
    private NpcInfo selectedNpc;

    private Integer gearOverride = null;
    private long gearOverrideTime = 0;

    private enum ModuleStatus {
        NOTHING,
        DROPDOWN,
        SUB_DROPDOWN,
        SELECTED
    }

    PetManager(Main main) {
        this.main = main;
        this.ships = main.mapManager.entities.ships;
        this.pet = main.hero.pet;

        PetGearSupplier.GEARS = gearList;
    }

    public void tick() {
        if (!main.isRunning() || !main.config.PET.ENABLED) return;
        if (active() != enabled) {
            if (show(true)) clickToggleStatus();
            return;
        }
        if (!enabled) {
            show(false);
            return;
        }
        updatePetTarget();
        int moduleId = main.config.PET.MODULE_ID;
        if (main.config.PET.COMPATIBILITY_MODE && main.config.PET.MODULE < gearList.size()) {
            moduleId = gearList.get(main.config.PET.MODULE).id;
        }

        if (gearOverrideTime > System.currentTimeMillis() && gearOverride != null) {
            moduleId = gearOverride;
        }

        if (target != null && !(target instanceof Npc) && target.playerInfo.isEnemy()) {
            moduleId = PetGearSupplier.Gears.PASSIVE.getId();
        }

        int submoduleId = -1, submoduleIdx = -1;
        if (moduleId == PetGearSupplier.Gears.ENEMY_LOCATOR.getId()) {
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

        if ((selection != ModuleStatus.SELECTED
                || (currentModule != null && currentModule.id != moduleId)
                || (currentSubModule == null && submoduleIdx != -1)
                || (currentSubModule != null && currentSubModule.id != submoduleId)) && show(true))
            this.selectModule(moduleId, submoduleIdx);
        else if (System.currentTimeMillis() > this.selectModuleTime) show(false);
    }

    private class NpcPick {
        private NpcInfo npc;
        private Gear gear;
        public NpcPick(String npcName, NpcInfo npc) {
            this.npc = npc;
            String fuzzyName = npcName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            this.gear = locatorList.stream().filter(l -> fuzzyName.equals(l.fuzzyName)).findFirst().orElse(null);
        }
    }
    public NpcInfo getTrackedNpc() {
        return selectedNpc;
    }

    private void updatePetTarget() {
        if (target == null || target.removed || !pet.isAttacking(target))
            target = ships.stream().filter(s -> pet.isAttacking(s)).findFirst().orElse(null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOverride(Integer gearId) {
        this.gearOverride = gearId;
        this.gearOverrideTime = System.currentTimeMillis() + 5000;
    }

    private boolean active() {
        if (!pet.removed) activeUntil = System.currentTimeMillis() + 1000;
        return System.currentTimeMillis() < activeUntil;
    }

    private void clickToggleStatus() {
        if (System.currentTimeMillis() - this.togglePetTime > 5000L) {
            click(MAIN_BUTTON_X, MODULE_Y);
            this.selection = ModuleStatus.NOTHING;
            this.togglePetTime = System.currentTimeMillis();
        }
    }

    private int moduleIdToIndex(int moduleId) {
        for (int i = 0; i < gearList.size(); i++) {
            if (gearList.get(i).id == moduleId) return i;
        }
        return 0;
    }

    private void selectModule(int moduleId, int submoduleIdx) {
        if (System.currentTimeMillis() < this.selectModuleTime) return;
        this.selectModuleTime = System.currentTimeMillis() + 150;

        switch (selection) {
            case SELECTED:
            case NOTHING:
                click(MODULES_X_MAX - 5, MODULE_Y);
                selection = ModuleStatus.DROPDOWN;
                break;
            case DROPDOWN:
                if (submoduleIdx != -1) {
                    hover(MODULES_X_MAX - 10, MODULE_Y + 35 + (22 * moduleIdToIndex(moduleId)));
                    selection = ModuleStatus.SUB_DROPDOWN;
                } else {
                    click(MODULES_X_MAX - 30, MODULE_Y + 35 + (22 * moduleIdToIndex(moduleId)));
                    selection = ModuleStatus.SELECTED;
                }
                break;
            case SUB_DROPDOWN:
                selection = ModuleStatus.SELECTED;
                if (submoduleIdx == -1) return;
                click(MODULES_X_MAX + 100, MODULE_Y + 35 + (22 * moduleIdToIndex(moduleId)) + (22 * submoduleIdx));
        }
        this.selectModuleTime = System.currentTimeMillis() + (selection == ModuleStatus.SELECTED ? 5000 : 150);
    }

    @Override
    public void update() {
        super.update();
        if (address == 0) return;

        guiSprites.update(address);
        long gearsSprite = API.readMemoryLong(guiSprites.getLast() + 216);
        gearsArr.update(API.readMemoryLong(API.readMemoryLong(gearsSprite + 176) + 224));
        gearsArr.sync(gearList, Gear::new, null);

        locatorWrapper.update(API.readMemoryLong(gearsSprite + 168));
        locatorNpcList.update(API.readMemoryLong(locatorWrapper.get(0) + 224));

        locatorNpcList.sync(locatorList, Gear::new, null);

        modulesArr.update(API.readMemoryLong(address + 400));

        for (int i = 0; i < modulesArr.size; i++) {
            if (API.readMemoryInt(modulesArr.get(i) + 172) != 54) continue;
            currentArr.update(API.readMemoryLong(modulesArr.get(i) + 184));
            break;
        }
        for (int i = 0; i < currentArr.size; i++) {
            if (API.readMemoryInt(currentArr.get(i) + 168) != 72) continue;
            currSpriteWrapper.update(currentArr.get(i));
            break;
        }
        currSprite.update(API.readMemoryLong(API.readMemoryLong(currSpriteWrapper.get(0) + 216) + 176));
        long currGearCheck = API.readMemoryLong(currSprite.get(1), 216, 152, 0x10);

        currentModule = findGear(gearList, currGearCheck);
        if (currentModule != null) currentSubModule = null;
        else {
            currentSubModule = findGear(locatorList, currGearCheck);
            if (currentSubModule != null) currentModule = byId(currentSubModule.parentId);
        }
    }

    public Gear findGear(List<Gear> gears, long check) {
        for (Gear gear : gears) if (gear.check == check) return gear;
        return null;
    }

    public Gear byId(int id) {
        for (Gear gear : gearList) if (gear.id == id) return gear;
        return null;
    }

    public static class Gear extends UpdatableAuto {
        public int id, parentId;
        public long check;
        public String name, fuzzyName;

        @Override
        public void update() {
            this.id = API.readMemoryInt(address + 172);
            this.parentId = API.readMemoryInt(address + 176); //assume, -1 if none
            this.name = API.readMemoryString(API.readMemoryLong(address + 200));
            this.fuzzyName = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            this.check = API.readMemoryLong(address, 208, 152, 0x10);
        }
    }

}
