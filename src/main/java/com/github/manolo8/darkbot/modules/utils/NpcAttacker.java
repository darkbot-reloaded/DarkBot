package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.manager.EffectManager;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.utils.Drive;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.objects.facades.SettingsProxy.KeyBind.*;

public class NpcAttacker {

    protected Main main;
    protected MapManager mapManager;
    protected HeroManager hero;
    protected Drive drive;
    protected final SettingsProxy keybinds;
    protected final CategoryBar bar;

    public Npc target;
    protected Long ability; // Time at which offensive ability can be triggered

    // General delays
    protected long clickDelay; // When can we mouse-click again? (lock)
    protected long laserTime; // When can we cast attack again? (ctrl or attack key)
    protected long usedRsb; // When did we last RSB?

    // Attacking bug fixing
    protected long isAttacking; // Time until we consider the attack valid (not-bugged)
    protected int fixedTimes; // Amount of times we bugged

    protected boolean firstAttack; // If the initial attack was casted
    protected boolean sab; // If shooting SAB right now
    protected boolean rsb; // If shooting RSB right nos

    public NpcAttacker(Main main) {
        this.main = main;
        this.mapManager = main.mapManager;
        this.hero = main.hero;
        this.drive = hero.drive;
        this.keybinds = main.facadeManager.settings;
        this.bar = main.facadeManager.slotBars.categoryBar;
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
        return fixedTimes > 5;
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
        if (hero.target == target && firstAttack) {
            // On npc death, lock goes away before the npc does, sometimes the bot would try to lock the dead npc.
            // This adds a bit of delay when any cause makes you lose the lock, until you try to re-lock.
            clickDelay = System.currentTimeMillis();
        }

        fixedTimes = 0;
        laserTime = 0;
        firstAttack = false;
        if (hero.locationInfo.distance(target) < 800 && System.currentTimeMillis() - clickDelay > 500) {
            hero.setTarget(target);
            target.trySelect(false);
            clickDelay = System.currentTimeMillis();
            if (main.config.LOOT.SHIP_ABILITY != null) ability = clickDelay + 4000;
        }
    }

    protected void tryAttackOrFix() {
        if (System.currentTimeMillis() < laserTime) return;

        if (!firstAttack) {
            firstAttack = true;
            sendAttack(1500, 5000, true);
        } else if (shouldSab() != sab || shouldRsb() != rsb) {
            sendAttack(250, 5000, true);
        } else if (!hero.isAttacking(target) || !hero.isAiming(target)) {
            sendAttack(1500, 5000, false);
        } else if (target.health.hpDecreasedIn(1500) || target.hasEffect(EffectManager.Effect.NPC_ISH)
                || hero.locationInfo.distance(target) > 700) {
            isAttacking = Math.max(isAttacking, System.currentTimeMillis() + 2000);
        } else if (System.currentTimeMillis() > isAttacking) {
            sendAttack(1500, ++fixedTimes * 3000L, false);
        }
    }

    private void sendAttack(long minWait, long bugTime, boolean normal) {
        laserTime = System.currentTimeMillis() + minWait;
        isAttacking = Math.max(isAttacking, laserTime + bugTime);
        if (normal) API.keyboardClick(getAttackKey());
        else if (API instanceof DarkBoatAdapter) API.keyboardClick(keybinds.getCharCode(ATTACK_LASER));
        else target.trySelect(true);
    }

    public double modifyRadius(double radius) {
        if (target.health.hpPercent() < 0.25 && target.npcInfo.extra.has(NpcExtra.AGGRESSIVE_FOLLOW)) radius *= 0.75;
        if (target != hero.target || !hero.isAttacking(target) || castingAbility()) radius = Math.min(550, radius);
        else if (!target.locationInfo.isMoving() || target.health.hpPercent() < 0.25) radius = Math.min(600, radius);

        return radius + bar.findItemById("ability_zephyr_mmt").map(i -> i.quantity).orElse(0d) * 5;
    }

    private boolean shouldSab() {
        return main.config.LOOT.SAB.ENABLED && hero.health.shieldPercent() < main.config.LOOT.SAB.PERCENT
                && target.health.shield > main.config.LOOT.SAB.NPC_AMOUNT;
    }

    private boolean shouldRsb() {
        if (!main.config.LOOT.RSB.ENABLED || !target.npcInfo.extra.has(NpcExtra.USE_RSB)) return false;
        if (usedRsb < System.currentTimeMillis() - main.config.LOOT.RSB.AMMO_REFRESH) usedRsb = System.currentTimeMillis();

        return usedRsb > System.currentTimeMillis() - 50;
    }

    private Character getAttackKey() {
        if (rsb = shouldRsb()) return main.config.LOOT.RSB.KEY;
        if (sab = shouldSab()) return main.config.LOOT.SAB.KEY;
        return this.target == null || this.target.npcInfo.attackKey == null ?
                main.config.LOOT.AMMO_KEY : this.target.npcInfo.attackKey;
    }

}
