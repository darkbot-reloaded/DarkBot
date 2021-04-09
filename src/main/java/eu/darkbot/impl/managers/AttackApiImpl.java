package eu.darkbot.impl.managers;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import eu.darkbot.api.entities.other.SelectableItem;
import eu.darkbot.api.entities.utils.Attackable;
import eu.darkbot.api.managers.AttackAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.objects.Item;
import org.jetbrains.annotations.Nullable;

public class AttackApiImpl implements AttackAPI {

    private final SettingsProxy settingsProxy;
    private final HeroItemsAPI heroItems;
    private final HeroAPI hero;

    private Attackable target;
    private long lockTryTime, attackTryTime;

    public AttackApiImpl(SettingsProxy settingsProxy,
                         HeroItemsAPI heroItems,
                         HeroAPI hero) {
        this.settingsProxy = settingsProxy;
        this.heroItems = heroItems;
        this.hero = hero;
    }

    @Override
    public @Nullable Attackable getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable Attackable attackable) {
        this.target = attackable;
    }

    @Override
    public boolean isLocked() {
        return hasTarget() && hero.getTarget() == target;
    }

    @Override
    public void tryLockTarget() {
        if (!hasTarget() || isLocked() || lockTryTime > System.currentTimeMillis()) return;

        this.lockTryTime = System.currentTimeMillis() + (this.target.trySelect(false) ? 500 : 150);
    }

    @Override
    public boolean isAttacking() {
        return hasTarget() && hero.isAttacking(target);
    }

    @Override
    public void laserAttack() {
        if (!hasTarget() || isAttacking() || attackTryTime > System.currentTimeMillis() - 150) return;

        if (!isLocked()) tryLockTarget();
        else {
            Character laserAttackChar = settingsProxy.getCharCode(SettingsProxy.KeyBind.ATTACK_LASER);
            if (laserAttackChar != null) Main.API.keyboardClick(laserAttackChar); //use slotbar instead

            attackTryTime = System.currentTimeMillis();
        }
    }

    @Override
    public void laserAbort() {
        if (!isAttacking() || attackTryTime > System.currentTimeMillis() - 150) return;

        Character laserAttackChar = settingsProxy.getCharCode(SettingsProxy.KeyBind.ATTACK_LASER);
        if (laserAttackChar != null) Main.API.keyboardClick(laserAttackChar);

        attackTryTime = System.currentTimeMillis();
    }

    private long rocketTryTime;
    @Override
    public void launchRocket() {
        Character rocketLaunchChar = settingsProxy.getCharCode(SettingsProxy.KeyBind.ATTACK_ROCKET);
        if (rocketLaunchChar == null ||
                !isLocked() ||
                hero.distanceTo(target) > 1000 ||
                rocketTryTime > System.currentTimeMillis() - 500) return;

        heroItems.getItems(HeroItemsAPI.Category.ROCKETS).stream()
                .filter(Item::isSelected)
                .filter(Item::isReady)
                .findAny()
                .ifPresent(i -> Main.API.keyboardClick(rocketLaunchChar));

        this.rocketTryTime = System.currentTimeMillis();
    }

    @Override
    public SelectableItem.Laser getLaser() {
        return heroItems.getItems(HeroItemsAPI.Category.LASERS).stream()
                .filter(Item::isSelected)
                .map(item -> SelectableItem.Laser.of(item.getId()))
                .findFirst().orElse(null);
    }

    @Override
    public SelectableItem.Rocket getRocket() {
        return heroItems.getItems(HeroItemsAPI.Category.ROCKETS).stream()
                .filter(Item::isSelected)
                .map(item -> SelectableItem.Rocket.of(item.getId()))
                .findFirst().orElse(null);
    }
}
