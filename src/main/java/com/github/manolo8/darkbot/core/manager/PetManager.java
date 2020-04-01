package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.suppliers.PetGearSupplier;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class PetManager extends Gui {

    private static final int MAIN_BUTTON_X = 30, MODULES_X_MAX = 260, MODULE_Y = 120;

    private long togglePetTime, selectModuleTime;
    private long activeUntil;
    private int moduleStatus = -2; // -2 no module, -1 selecting module, >= 0 module selected
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

    private Gear current;

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
        int moduleId = (target == null || target instanceof Npc || target.playerInfo.isEnemy()) ? main.config.PET.MODULE_ID : 1;
        if (moduleStatus != moduleId && show(true)) this.selectModule(moduleId);
        else if (moduleSelected()) show(false);
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

    private boolean active() {
        if (!pet.removed) activeUntil = System.currentTimeMillis() + 1000;
        return System.currentTimeMillis() < activeUntil;
    }

    private boolean moduleSelected() {
        return System.currentTimeMillis() - this.selectModuleTime > 1000L;
    }

    private void clickToggleStatus() {
        if (System.currentTimeMillis() - this.togglePetTime > 5000L) {
            click(MAIN_BUTTON_X, MODULE_Y);
            this.moduleStatus = -2;
            this.togglePetTime = System.currentTimeMillis();
        }
    }

    private int moduleIdToIndex(int moduleId) {
        for (int i = 0; i < gearList.size(); i++) {
            if (gearList.get(i).id == moduleId) return i;
        }
        return 0;
    }

    private void selectModule(int moduleId) {
        if (System.currentTimeMillis() - this.selectModuleTime > 1000L) {
            if (moduleStatus != -1) {
                click(MODULES_X_MAX - 5, MODULE_Y);
                this.moduleStatus = -1;
            } else {
                click(MODULES_X_MAX - 30, MODULE_Y + 40 + (20 * moduleIdToIndex(moduleId)));
                this.moduleStatus = moduleId;
            }
            this.selectModuleTime = System.currentTimeMillis();
        }
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
        long currentModule = API.readMemoryLong(API.readMemoryLong(currSprite.get(1) + 216) + 152);

        for (Gear gear : gearList) if (gear.check == currentModule) current = gear;
        for (Gear gear : locatorList) if (gear.check == currentModule) current = gear;
    }

    public static class Gear extends UpdatableAuto {
        public int id, parentId;
        public long check;
        public String name;

        @Override
        public void update() {
            this.id = API.readMemoryInt(address + 172);
            this.parentId = API.readMemoryInt(address + 176); //assume, -1 if none
            this.name = API.readMemoryString(API.readMemoryLong(address + 200));
            this.check = API.readMemoryLong(API.readMemoryLong(address + 208) + 152);
        }
    }

}
