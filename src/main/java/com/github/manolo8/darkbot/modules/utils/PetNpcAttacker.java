package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;

import static com.github.manolo8.darkbot.Main.API;

public class PetNpcAttacker extends NpcAttacker {

    public PetNpcAttacker(Main main) {
        super(main);
    }

    public String status() {
        return target != null ? "Killing npc" + (hero.isAttacking(target) ? " S" : "") + (ability != null ? " A" : "") + (sab ? " SAB" : "") : "Idle";
    }

    public boolean castingAbility() {
        return ability != null;
    }

    public boolean hasTarget() {
        return target != null && !target.removed;
    }

    public void doKillTargetTick() {
        if (target == null) return;
        if (!mapManager.isTarget(target)) {
            lockAndSetTarget();
            return;
        }
        tryAttackOrFix();
    }

    protected void tryAttackOrFix() {
        boolean bugged = !target.health.hpDecreasedIn(3000) && hero.locationInfo.distance(target) < 350
                && System.currentTimeMillis() > (5000 + laserTime + (fixTimes * 5000));
        if (!hero.pet.isAttacking(target) && System.currentTimeMillis() > laserTime) {
            if (bugged) {
                API.keyboardClick('E');
                fixTimes++;
            } else if (hero.locationInfo.distance(target) > 800) {
                API.keyboardClick(' ');
                laserTime = System.currentTimeMillis() + 5000;
            }
        }
    }

    @Override
    public double modifyRadius(double radius) {
        if (hero.target == target && !hero.pet.isAttacking(target) && System.currentTimeMillis() > laserTime) return 850;
        return radius;
    }
}
