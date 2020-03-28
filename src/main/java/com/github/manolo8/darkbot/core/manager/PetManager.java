package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Ship;
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
    private List<Gear> gearList = new ArrayList<>();
    private List<Gear> locatorList = new ArrayList<>();

    PetManager(Main main) {
        this.main = main;
        this.ships = main.mapManager.entities.ships;
        this.pet = main.hero.pet;
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
        int module = (target == null || target instanceof Npc || target.playerInfo.isEnemy()) ? main.config.PET.MODULE : 0;
        if (moduleStatus != module && show(true)) this.selectModule(module);
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

    private void selectModule(int module) {
        if (System.currentTimeMillis() - this.selectModuleTime > 1000L) {
            if (moduleStatus != -1) {
                click(MODULES_X_MAX - 5, MODULE_Y);
                this.moduleStatus = -1;
            } else {
                click(MODULES_X_MAX - 30, MODULE_Y + 40 + (20 * module));
                this.moduleStatus = module;
            }
            this.selectModuleTime = System.currentTimeMillis();
        }
    }

    private ObjArray gearsArr = ObjArray.ofArrObj();
    private void petModules() {
        gearsArr.update(API.readMemoryLong(API.readMemoryLong(getGearsSprite() + 176) + 224));
        gearsArr.update();

        gearList.clear();
        for (int i = 0; i < gearsArr.size; i++) {
            gearList.add(new Gear().update(gearsArr.get(i)));
        }
    }

    private ObjArray currSprite = ObjArray.ofSprite();
    private ObjArray currentArr = ObjArray.ofArrObj();
    private long currentModule() {
        long temp = API.readMemoryLong(address + 400);
        currentArr.update(temp);
        currentArr.update();

        for (int i = 0; i < currentArr.size; i++) {
            if (API.readMemoryInt(currentArr.get(i) + 172) == 54) {
                temp = currentArr.get(i);
                break;
            }
        }

        currentArr.update(API.readMemoryLong(temp + 184));
        currentArr.update();

        for (int i = 0; i < currentArr.size; i++) {
            if (API.readMemoryInt(currentArr.get(i) + 168) == 72) {
                temp = currentArr.get(i);
                break;
            }
        }

        currSprite.update(temp);
        currSprite.update();

        temp = API.readMemoryLong(API.readMemoryLong(currSprite.get(0) + 216) + 176);

        currSprite.update(temp);
        currSprite.update();

        return API.readMemoryLong(API.readMemoryLong(currSprite.get(1) + 216) + 152);
    }

    private ObjArray locatorArr = ObjArray.ofArrObj();
    private void findLocatorNpc() {
        locatorArr.update(API.readMemoryLong(getGearsSprite() + 168));
        locatorArr.update();
        locatorArr.update(API.readMemoryLong(locatorArr.get(0) + 224));
        locatorArr.update();

        locatorList.clear();
        for (int i = 0; i < locatorArr.size; i++) {
            locatorList.add(new Gear().update(locatorArr.get(i)));
        }
    }

    private ObjArray sprite = ObjArray.ofSprite();
    private long getGearsSprite() {
        sprite.update(address);
        sprite.update();
        return API.readMemoryLong(sprite.getLast() + 216);
    }

    static class Gear {
        public int id, parentId;
        public long check;
        public String name;

        public Gear update(long address) {
            this.id = API.readMemoryInt(address + 172);
            this.parentId = API.readMemoryInt(address + 176); //assume, -1 if none
            this.name = API.readMemoryString(API.readMemoryLong(address + 200));
            this.check = API.readMemoryLong(API.readMemoryLong(address + 208) + 152);
            return this;
        }
    }
}
