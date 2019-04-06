package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.objects.Gui;

public class PetManager extends Gui {

    private static final int MAIN_BUTTON_X = 30, MODULES_X_MAX = 260, MODULE_Y = 120;

    private long togglePetTime, selectModuleTime;
    private int moduleStatus = -2; // -2 no module, -1 selecting module, >= 0 module selected
    private Main main;
    private Pet pet;
    private boolean enabled = false;

    PetManager(Main main) {
        this.main = main;
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
        if (moduleStatus != main.config.PET.MODULE && show(true)) this.selectModule();
        else if (moduleSelected()) show(false);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean active() {
        return !pet.removed;
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
