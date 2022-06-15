package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.SettingsManager;
import com.github.manolo8.darkbot.core.objects.facades.HighlightProxy;
import com.github.manolo8.darkbot.extensions.features.handlers.LaserSelectorHandler;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.items.Item;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.impl.managers.AbstractAttackImpl;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.Nullable;

public class AttackAPIImpl extends AbstractAttackImpl implements Listener {

    private final LaserSelectorHandler laserHandler;
    private final HighlightProxy highlight;
    private final SettingsManager settingsManager;
    private final MapManager mapManager;

    private final Timer buggedTimer = Timer.get(20_000), highLightTimer = Timer.get(500);

    public AttackAPIImpl(HeroItemsAPI heroItems, HeroAPI hero, LaserSelectorHandler laserHandler,
                         HighlightProxy highlight, SettingsManager settingsManager, MapManager mapManager,
                         ConfigAPI config) {
        super(heroItems, hero);
        this.laserHandler = laserHandler;
        this.highlight = highlight;
        this.settingsManager = settingsManager;
        this.mapManager = mapManager;

//        this.shipAbility = config.requireConfig("loot.ship_ability");
//        this.shipAbilityHealth = config.requireConfig("loot.ship_ability.min");
    }

    @Override
    protected boolean isAttackViaSlotBarEnabled() {
        return settingsManager.attackViaSlotbar;
    }

    @Override
    protected SelectableItem.Laser getBestLaserAmmo() {
        return laserHandler.getBest();
    }

    @Override
    public String getStatus() {
//        String supplierName = Optional.ofNullable(laserHandler.getLastUsedSupplier())
//                .filter(s -> isLocked())
//                .map(Object::getClass)
//                .map(c -> c.getAnnotation(Feature.class))
//                .map(Feature::name)
//                .orElse("");

        return hasTarget() ? ("Targeting " + target.getClass().getSimpleName()
                + " [" + (isLocked() ? "L " : "")
                + (isAttacking() ? "S " : "")
                + (isCastingAbility() ? "A " : "")
                + ("B" + Math.round(buggedTimer.getRemainingFuse() / 1000.0)) + "]")
                : "Waiting";
    }

    @Override
    public boolean isBugged() {
        return buggedTimer.isInactive();
    }

    @Override
    public void setTarget(@Nullable Lockable target) {
        if (this.target != target) {
            buggedTimer.activate();
            highLightTimer.disarm();
        }

        super.setTarget(target);
    }

    // TODO: 11.07.2022 use offensive abilities on its own (venom/cyborg or diminisher)
    @Override
    public boolean isCastingAbility() {
        return false;
    }

    @Override
    public void tryLockTarget() {
        if (target instanceof FakeNpc) return;
        super.tryLockTarget();
    }

    @Override
    public boolean isLocked() {
        return super.isLocked() && mapManager.isTarget((Entity) this.target);
    }

    @Override
    public void tryLockAndAttack() {
        super.tryLockAndAttack();

        /*if (lastTarget != target) {
            lastTarget = target;
            buggedTimer.activate();
            highLightTimer.disarm();
        }*/

        if ((isAttacking() && ((target.hasEffect(EntityEffect.NPC_ISH) || target.hasEffect(EntityEffect.ISH))
                || target.getHealth().hpDecreasedIn(3_000)))
                || hero.distanceTo(target) > 1_000)
            buggedTimer.activate();
    }

    @Override
    public boolean isAttacking() {
        if (!attacked)
            highLightTimer.disarm();

        if (highlight.isAttacking())
            highLightTimer.activate();

        return super.isAttacking() && highLightTimer.isActive();
    }

    @Override
    public double modifyRadius(double radius) {
        if (target.getHealth().hpPercent() < 0.25 && hasExtraFlag(NpcExtra.AGGRESSIVE_FOLLOW))
            radius *= 0.75;

        if (!isAttacking() && target.isMoving() && MathUtils.angleDiff(target.getLocationInfo().getAngle(), hero.angleTo(target)) > 1.6)
            radius = Math.min(560, radius);

        if (isCastingAbility())
            radius = Math.min(560, radius);

        if (!target.isMoving() || target.getHealth().hpPercent() < 0.25)
            radius = Math.min(600, radius);

        return radius + heroItems.getItem(SelectableItem.Ability.ZEPHYR_MMT, ItemFlag.AVAILABLE)
                .map(Item::getQuantity)
                .orElse(0d) * 5;
    }

    @EventHandler
    public void onRunningToggle(BotAPI.RunningToggleEvent event) {
        buggedTimer.activate();
    }
}
