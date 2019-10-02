package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.Drive;

import static com.github.manolo8.darkbot.Main.API;

public class NpcAttacker {

    protected MapManager mapManager;
    protected Config config;
    protected HeroManager hero;
    protected Drive drive;

    public Npc target;
    protected Long ability;

    protected long laserTime;
    protected long fixTimes;
    protected long clickDelay;
    protected boolean sab;

    public NpcAttacker(Main main) {
        this.mapManager = main.mapManager;
        this.hero = main.hero;
        this.config = main.config;
        this.drive = hero.drive;
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

        if (ability != null && ability < System.currentTimeMillis()) {
            if (target.health.maxHp < config.LOOT.SHIP_ABILITY_MIN) ability = null;
            else if (hero.locationInfo.distance(target) < 575) {
                API.keyboardClick(config.LOOT.SHIP_ABILITY);
                ability = null;
            }
        }

        tryAttackOrFix();
    }

    void lockAndSetTarget() {
        if (hero.locationInfo.distance(target) > 700 || System.currentTimeMillis() - clickDelay < 400) return;
        hero.setTarget(target);
        setRadiusAndClick(true);
        clickDelay = System.currentTimeMillis();
        fixTimes = 0;
        laserTime = clickDelay + 50;
        if (config.LOOT.SHIP_ABILITY != null) ability = clickDelay + 4000;
    }

    protected void tryAttackOrFix() {
        boolean bugged = hero.isAttacking(target)
                && (!hero.isAiming(target) || (!target.health.hpDecreasedIn(3000) && hero.locationInfo.distance(target) < 650))
                && System.currentTimeMillis() > (laserTime + fixTimes * 3000);
        boolean sabChanged = shouldSab() != sab;
        if ((sabChanged || !hero.isAttacking(target) || bugged) && System.currentTimeMillis() > laserTime) {
            laserTime = System.currentTimeMillis() + 750;
            if (!bugged || sabChanged) API.keyboardClick(getAttackKey());
            else {
                setRadiusAndClick(false);
                fixTimes++;
            }
        }
    }

    public double modifyRadius(double radius) {
        if (target != hero.target || !hero.isAttacking(target) || castingAbility()) return Math.min(550, radius);
        if (!target.locationInfo.isMoving() || target.health.hpPercent() < 0.25) return Math.min(600, radius);
        return radius;
    }

    private boolean shouldSab() {
        return config.LOOT.SAB.ENABLED && hero.health.shieldPercent() < config.LOOT.SAB.PERCENT
                && target.health.shield > config.LOOT.SAB.NPC_AMOUNT;
    }

    private char getAttackKey() {
        if (sab = shouldSab()) return this.config.LOOT.SAB.KEY;
        return this.target == null || this.target.npcInfo.attackKey == null ?
                this.config.LOOT.AMMO_KEY : this.target.npcInfo.attackKey;
    }

    private void setRadiusAndClick(boolean single) {
        target.clickable.setRadius(800);
        drive.clickCenter(single, target.locationInfo.now);
        target.clickable.setRadius(0);
    }

}
