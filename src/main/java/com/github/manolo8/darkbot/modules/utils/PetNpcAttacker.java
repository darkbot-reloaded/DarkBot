package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.FakeNpc;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.objects.facades.SettingsProxy.KeyBind.ACTIVE_PET;
import static com.github.manolo8.darkbot.core.objects.facades.SettingsProxy.KeyBind.ATTACK_ROCKET;

public class PetNpcAttacker extends NpcAttacker {

    public PetNpcAttacker(Main main) {
        super(main);
    }

    public String status() {
        return target != null ? "Killing npc" + (hero.isAttacking(target) ? " S" : "") + (ability != null ? " A" : "") : "Idle";
    }

    public boolean castingAbility() {
        return ability != null;
    }

    public boolean hasTarget() {
        return target != null && !target.removed;
    }

    public void doKillTargetTick() {
        if (target == null || target instanceof FakeNpc) return;
        if (!mapManager.isTarget(target)) {
            lockAndSetTarget();
            return;
        }
        tryAttackOrFix();
    }

    protected void tryAttackOrFix() {
        boolean bugged = !target.health.hpDecreasedIn(3000) && hero.locationInfo.distance(target) < 350
                && System.currentTimeMillis() > (5000 + laserTime + (fixedTimes * 5000L));
        if (!hero.pet.isAttacking(target) && System.currentTimeMillis() > laserTime) {
            if (bugged) {
                API.keyboardClick(keybinds.getCharCode(ACTIVE_PET));
                fixedTimes++;
            } else if (hero.locationInfo.distance(target) > 800) {
                API.keyboardClick(keybinds.getCharCode(ATTACK_ROCKET));
                laserTime = System.currentTimeMillis() + 5000;
            }
        }
    }

    @Override
    public double modifyRadius(double radius) {
        if (hero.getLocalTarget() == target && !hero.pet.isAttacking(target) && System.currentTimeMillis() > laserTime) return 850;
        return radius;
    }
}
