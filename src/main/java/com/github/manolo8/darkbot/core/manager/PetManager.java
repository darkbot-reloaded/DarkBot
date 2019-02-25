package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;

public class PetManager extends Gui {

    private static final int MAIN_BUTTON_X = 30, MODULES_X_MAX = 260, MODULE_Y = 120;

    private long repairPetTime;
    private long togglePetTime;
    private long selectModuleTime;
    private int moduleStatus = -2; // -2 no module, -1 selecting module, >= 0 module selected
    private int activity;
    private Main main;
    private boolean enabled = true;

    PetManager(Main main) {
        this.main = main;
    }

    public void tick() {
        if (!main.isRunning() || !main.config.PET.ENABLED) return;
        if (dead()) {
            if (show(true)) tryRevive();
            return;
        }
        if (active() != enabled) {
            if (show(true)) togglePet();
            return;
        }
        if (!enabled) {
            show(false);
            return;
        }
        if (moduleStatus != main.config.PET.MODULE && show(true)) this.selectModule();
        else if (moduleSelected()) show(false);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean dead() {
        return main.hero.health.hp == 0;
    }

    public void tickActive() {
        this.activity = dead() ? 0 : System.currentTimeMillis() - this.togglePetTime > 1000 && main.hero.pet.locationInfo.isMoving() ? 25 : activity;
        if (main.hero.locationInfo.isMoving()) activity--;
    }

    public boolean active() {
        return activity > 0;
    }

    private boolean moduleSelected() {
        return System.currentTimeMillis() - this.selectModuleTime > 1000L;
    }

    private void tryRevive() {
        if (System.currentTimeMillis() - this.repairPetTime > 2000L) {
            click(MAIN_BUTTON_X, MODULE_Y);
            this.repairPetTime = System.currentTimeMillis();
        }
    }

    private void togglePet() {
        if (System.currentTimeMillis() - this.togglePetTime > 2000L) {
            activity = activity > 0 ? 0 : 25;
            click(MAIN_BUTTON_X, MODULE_Y);
            this.moduleStatus = -2;
            this.togglePetTime = System.currentTimeMillis();
        }
    }

    private void selectModule() {
        if (System.currentTimeMillis() - this.selectModuleTime > 1000L) {
            if (moduleStatus != -1) {
                click(MODULES_X_MAX - 5, MODULE_Y);
                this.moduleStatus = -1;
            } else {
                click(MODULES_X_MAX - 30, MODULE_Y + 40 + (20 * this.main.config.PET.MODULE));
                this.moduleStatus = this.main.config.PET.MODULE;
            }
            this.selectModuleTime = System.currentTimeMillis();
        }
    }

}
