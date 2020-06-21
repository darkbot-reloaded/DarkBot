package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.Drive;

import static com.github.manolo8.darkbot.Main.API;

public class NpcAttacker {

    protected Main main;
    protected MapManager mapManager;
    protected HeroManager hero;
    protected Drive drive;

    public Npc target;
    protected Long ability;

    protected long laserTime;
    protected long fixTimes;
    protected long clickDelay;
    protected long useRsbUntil;
    protected boolean sab;
    protected boolean rsb;

    public NpcAttacker(Main main) {
        this.main = main;
        this.mapManager = main.mapManager;
        this.hero = main.hero;
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

    public boolean isBugged() {
        return fixTimes > 5;
    }

    public void doKillTargetTick() {
        if (target == null || target instanceof FakeNpc) return;
        if (!mapManager.isTarget(target)) {
            lockAndSetTarget();
            return;
        }

        if (ability != null && ability < System.currentTimeMillis()) {
            if (target.health.maxHp < main.config.LOOT.SHIP_ABILITY_MIN) ability = null;
            else if (hero.locationInfo.distance(target) < 575) {
                API.keyboardClick(main.config.LOOT.SHIP_ABILITY);
                ability = null;
            }
        }

        tryAttackOrFix();
    }

    void lockAndSetTarget() {
        if (hero.locationInfo.distance(target) > 800 || System.currentTimeMillis() - clickDelay < 400) return;
        hero.setTarget(target);
        setRadiusAndClick(true);
        clickDelay = System.currentTimeMillis();
        fixTimes = 0;
        laserTime = clickDelay + 50;
        if (main.config.LOOT.SHIP_ABILITY != null) ability = clickDelay + 4000;
    }

    protected void tryAttackOrFix() {
        boolean bugged = hero.isAttacking(target)
                && (!hero.isAiming(target) || (!target.health.hpDecreasedIn(3000) && hero.locationInfo.distance(target) < 700))
                && System.currentTimeMillis() > (laserTime + fixTimes * 3000);
        boolean ammoChanged = shouldSab() != sab || shouldRsb() != rsb;
        if ((ammoChanged || !hero.isAttacking(target) || bugged) && System.currentTimeMillis() > laserTime) {
            laserTime = System.currentTimeMillis() + 750;
            if (!bugged || ammoChanged) API.keyboardClick(getAttackKey());
            else {
                if (API instanceof DarkBotAPI) API.rawKeyboardClick((char) 0x11);
                else setRadiusAndClick(false);
                fixTimes++;
            }
        }
    }

    public double modifyRadius(double radius) {
        if (target.health.hpPercent() < 0.25 && target.npcInfo.extra.has(NpcExtra.AGGRESSIVE_FOLLOW)) radius *= 0.75;
        if (target != hero.target || !hero.isAttacking(target) || castingAbility()) return Math.min(550, radius);
        if (!target.locationInfo.isMoving() || target.health.hpPercent() < 0.25) return Math.min(600, radius);
        return radius;
    }

    private boolean shouldSab() {
        return main.config.LOOT.SAB.ENABLED && hero.health.shieldPercent() < main.config.LOOT.SAB.PERCENT
                && target.health.shield > main.config.LOOT.SAB.NPC_AMOUNT;
    }

    private boolean shouldRsb() {
        if (!main.config.LOOT.RSB.ENABLED || !target.npcInfo.extra.has(NpcExtra.USE_RSB)) return false;
        if (useRsbUntil < System.currentTimeMillis() - main.config.LOOT.RSB.AMMO_REFRESH) useRsbUntil = System.currentTimeMillis();

        return useRsbUntil > System.currentTimeMillis() - 50;
    }

    private char getAttackKey() {
        if (rsb = shouldRsb()) return main.config.LOOT.RSB.KEY;
        if (sab = shouldSab()) return main.config.LOOT.SAB.KEY;
        return this.target == null || this.target.npcInfo.attackKey == null ?
                main.config.LOOT.AMMO_KEY : this.target.npcInfo.attackKey;
    }

    private void setRadiusAndClick(boolean single) {
        target.clickable.setRadius(800);
        drive.clickCenter(single, target.locationInfo.now);
        target.clickable.setRadius(0);
    }

}
