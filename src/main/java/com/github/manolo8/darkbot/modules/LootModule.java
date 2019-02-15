package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.Comparator;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class LootModule implements Module {

    private Main main;

    private List<Ship> ships;
    private List<Npc> npcs;

    private HeroManager hero;
    private Drive drive;

    private Config config;

    public Npc target;

    private int radiusFix;

    private long laserTime;
    private long clickDelay;
    private int times;
    private boolean sab;

    private boolean locked;
    private boolean circleRight;
    private boolean repairing;

    @Override
    public void install(Main main) {
        this.main = main;

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.ships = main.mapManager.entities.ships;
        this.npcs = main.mapManager.entities.npcs;
        this.config = main.config;
    }

    @Override
    public boolean canRefresh() {
        return target == null;
    }

    @Override
    public void tick() {

        if (checkDangerousAndCurrentMap()) {

            if (findTarget()) {
                moveToAnSafePosition();
                doKillTargetTick();
            } else if (!drive.isMoving()) {
                drive.moveRandom();
            }

        }

    }

    void doKillTargetTick() {
        if (main.mapManager.isTarget(target)) {

            if (main.mapManager.isCurrentTargetOwned()) {

                if (checkIfIsAttackingAndCanContinue()) {
                    checkSab();
                }

            } else {
                target.setTimerTo(5000);
                target = null;
            }

        } else {
            setTargetAndTryStartLaserAttack();
        }
    }

    boolean checkDangerousAndCurrentMap() {
        boolean mapWrong = this.config.WORKING_MAP != this.hero.map.id;
        boolean underAttack = this.isUnderAttack();
        boolean lowHp = this.hero.health.hpPercent() < this.config.GENERAL.SAFETY.REPAIR_HP &&
                (this.target == null || this.target.removed || this.target.health.hp == 0 || this.target.health.hp > 300000);
        if (mapWrong || lowHp || hasEnemies()) {
            this.hero.runMode();
            if (mapWrong || underAttack) {
                if (underAttack) Main.API.keyboardClick(config.GENERAL.SAFETY.SHIP_ABILITY);
                repairing = true;
                this.main.setModule(new MapModule()).setTargetAndBack(this.main.starManager.fromId(this.main.config.WORKING_MAP));
            } else {
                if (!this.config.LOOT.STOP_RUNNING_NO_SIGHT || lowHp) repairing = true;
                Portal portal = this.main.starManager.next(this.hero.map, this.hero.locationInfo, this.hero.map);
                if (portal == null) return true;
                if (portal.locationInfo.distance(this.hero) > 100.0) this.drive.move(portal);
            }
            return false;
        }
        repairing = repairing && this.hero.health.hpPercent() < this.config.GENERAL.SAFETY.REPAIR_TO_HP;
        return !repairing;
    }

    boolean findTarget() {
        if (target == null || target.removed) {
            target = closestNpc(hero.locationInfo.now);
            locked = false;
        }

        return target != null;
    }

    private void checkSab() {
        if (config.AUTO_SAB && hero.health.shieldPercent() < config.LOOT.SAB_PERCENT
                && target.health.shield > config.LOOT.SAB_NPC_AMOUNT) {

            if (!sab) {
                API.keyboardClick(config.AUTO_SAB_KEY);
                sab = true;
            }

        } else if (sab) {
            API.keyboardClick(getAttackKey());
            sab = false;
        }
    }

    private boolean checkIfIsAttackingAndCanContinue() {

        long laser = System.currentTimeMillis() - laserTime;

        boolean attacking = hero.isAttacking(target);
        boolean bugged = (!target.health.isDecreasedIn(1000) && laser > 1000);

        if ((!attacking || bugged) && hero.locationInfo.distance(target) < 800 && laser > 1500 + times * 10000) {
            setRadiusAndClick(2);
            times++;
            laserTime = System.currentTimeMillis();
        }

        return true;
    }

    private char getAttackKey() {
        return this.target == null || this.target.npcInfo.attackKey == null ?
                this.config.AMMO_KEY : this.target.npcInfo.attackKey;
    }

    private void setTargetAndTryStartLaserAttack() {
        if (hero.locationInfo.distance(target) < 800 && System.currentTimeMillis() - clickDelay > 1000) {

            hero.setTarget(target);

            setRadiusAndClick(1);
            API.keyboardClick(getAttackKey());
            clickDelay = System.currentTimeMillis();
            locked = true;
            times = 0;

        } else if (!locked) {
            target = null;
        }

    }

    private void setRadiusAndClick(int times) {
        target.clickable.setRadius(800);

        drive.clickCenter(times);

        target.clickable.setRadius(0);
    }

    void moveToAnSafePosition() {

        double hp = target.health.hpPercent();

        if (hp == 1) {
            drive.move(target);
        } else {

            LocationInfo heroInfo = hero.locationInfo;
            LocationInfo targetInfo = target.locationInfo;

            Location locationHero = heroInfo.now;
            Location locationCurrent = targetInfo.destinationInTime(200);

            double radius = target.npcInfo.radius;

            double distance = heroInfo.now.distance(targetInfo.now);
            double angle = locationCurrent.angle(locationHero);

            if (distance > radius) {
                radiusFix -= (distance - radius) / 2;
                radiusFix = (int) max(radiusFix, -target.npcInfo.radius / 2);
            } else {
                radiusFix += (radius - distance) / 6;
                radiusFix = (int) min(radiusFix, target.npcInfo.radius / 2);
            }

            if (distance > 2 * radius && target.health.hpPercent() < 0.25) {
                hero.runMode();
            } else {
                hero.attackMode();
            }

            radius += radiusFix;

            double speed = min(200, target.locationInfo.speed) * 0.625;
            double moveDistance = hero.shipInfo.speed * 0.625 + speed;

            double x = locationCurrent.x - cos(angle) * radius;
            double y = locationCurrent.y - sin(angle) * radius;

            boolean circle = (moveDistance = (moveDistance - (locationHero.distance(x, y)))) > 0;

            if (circle && !target.npcInfo.noCircle) {
                double add = moveDistance / radius;

                angle += add;

                x = locationCurrent.x - cos(angle) * radius;
                y = locationCurrent.y - sin(angle) * radius;
            }

            drive.move(x, y);
        }
    }

    private boolean isUnderAttack() {
        if (!config.LOOT.RUN_FROM_ENEMIES && config.LOOT.RUN_FROM_ENEMIES_SIGHT) return false;
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy() && ship.isAttacking(hero)) return true;
        }
        return false;
    }

    private boolean hasEnemies() {
        if (!config.LOOT.RUN_FROM_ENEMIES && config.LOOT.RUN_FROM_ENEMIES_SIGHT) return false;

        for (Ship ship : ships) {
            if (!ship.playerInfo.isEnemy()) continue;

            if (config.LOOT.RUN_FROM_ENEMIES_SIGHT && ship.locationInfo.distance(hero) < config.LOOT.MAX_SIGHT_DISTANCE)
                return true;

            if (ship.isAttacking(hero)) {
                ship.setTimerTo(400_000);
                return true;
            } else if (ship.isInTimer()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttackedByOthers(Npc npc) {

        for (Ship ship : ships) {
            if (ship.isAttacking(npc)) {
                npc.setTimerTo(5000);
                return true;
            }
        }

        return false;
    }

    private Npc closestNpc(Location location) {
        return this.npcs.stream()
                .filter(n -> n.npcInfo.kill)
                .filter(n -> !this.isAttackedByOthers(n))
                .min(Comparator.<Npc>comparingInt(n -> n.npcInfo.priority)
                        .thenComparing(n -> n.health.hpPercent())
                        .thenComparing(n -> n.locationInfo.now.distance(location))).orElse(null);
    }
}
