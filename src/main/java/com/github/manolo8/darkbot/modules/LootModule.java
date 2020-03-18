package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;
import com.github.manolo8.darkbot.modules.utils.SafetyFinder;

import java.util.Comparator;
import java.util.List;

import static java.lang.Double.min;
import static java.lang.Math.random;

@Feature(name = "Npc Killer", description = "Npc-only module. Will never pick up resources.")
public class LootModule implements Module {

    private Main main;

    private List<Ship> ships;
    private List<Npc> npcs;

    private HeroManager hero;
    private Drive drive;

    private Config config;

    protected NpcAttacker attack;
    protected SafetyFinder safety;
    protected long refreshing;

    @Override
    public void install(Main main) {
        this.main = main;
        this.attack = new NpcAttacker(main);
        this.safety = new SafetyFinder(main);

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.ships = main.mapManager.entities.ships;
        this.npcs = main.mapManager.entities.npcs;
        this.config = main.config;
    }

    @Override
    public void uninstall() {
        safety.uninstall();
    }

    @Override
    public String status() {
        return safety.state() != SafetyFinder.Escaping.NONE ? safety.status() :
                attack.hasTarget() ? attack.status() : "Roaming";
    }

    @Override
    public boolean canRefresh() {
        if (!attack.hasTarget()) refreshing = System.currentTimeMillis() + 10000;
        return !attack.hasTarget() && safety.state() == SafetyFinder.Escaping.WAITING;
    }

    @Override
    public void tick() {
        if (checkDangerousAndCurrentMap()) {
            main.guiManager.pet.setEnabled(true);

            if (findTarget()) {
                moveToAnSafePosition();
                ignoreInvalidTarget();
                attack.doKillTargetTick();
            } else {
                hero.roamMode();
                if (!drive.isMoving()) drive.moveRandom();
            }
        }
    }

    protected boolean checkDangerousAndCurrentMap() {
        safety.setRefreshing(System.currentTimeMillis() <= refreshing);
        return safety.tick() && checkMap();
    }

    protected boolean checkMap() {
        if (this.config.GENERAL.WORKING_MAP != this.hero.map.id && !main.mapManager.entities.portals.isEmpty()) {
            this.main.setModule(new MapModule())
                    .setTarget(this.main.starManager.byId(this.main.config.GENERAL.WORKING_MAP));
            return false;
        }
        return true;
    }

    protected boolean findTarget() {
        return (attack.target = closestNpc(hero.locationInfo.now)) != null;
    }

    protected void ignoreInvalidTarget() {
        double closestDist = drive.closestDistance(attack.target.locationInfo.now);
        if (!main.mapManager.isTarget(attack.target)) {
            if (closestDist > 600) {
                attack.target.setTimerTo(1000);
                hero.setTarget(attack.target = null);
            }
        } else if (!(attack.target.npcInfo.extra.has(NpcExtra.IGNORE_OWNERSHIP) || main.mapManager.isCurrentTargetOwned())
                || (hero.locationInfo.distance(attack.target) > config.LOOT.NPC_DISTANCE_IGNORE) // Too far away from ship
                || (closestDist > 650 && attack.target.health.hpPercent() > 0.90)   // Too far into obstacle and full hp
                || (closestDist > 500 && !attack.target.locationInfo.isMoving() // Inside obstacle, waiting & and regen shields
                        && (attack.target.health.shIncreasedIn(1000) || attack.target.health.shieldPercent() > 0.99))) {
            attack.target.setTimerTo(5000);
            hero.setTarget(attack.target = null);
        } else if (attack.target.playerInfo.username.contains("Invoke") && attack.target.npcInfo.extra.has(NpcExtra.PASSIVE)
                && attack.target == hero.target && !attack.castingAbility()) {
            attack.target.setTimerTo(600_000);
            hero.setTarget(attack.target = null);
        }
    }

    protected  boolean backwards = false;
    protected void moveToAnSafePosition() {
        Npc target = attack.target;
        Location direction = drive.movingTo();
        Location heroLoc = hero.locationInfo.now;
        Location targetLoc = target.locationInfo.destinationInTime(400);

        double distance = heroLoc.distance(target.locationInfo.now);
        double angle = targetLoc.angle(heroLoc);
        double radius = target.npcInfo.radius;
        boolean noCircle = target.npcInfo.extra.has(NpcExtra.NO_CIRCLE);

        radius = attack.modifyRadius(radius);
        if (radius > 750) noCircle = false;

        double angleDiff;
        if (noCircle) {
            double dist = targetLoc.distance(direction);
            double minRad = Math.max(0, Math.min(radius - 200, radius * 0.5));
            if (dist <= radius && dist >= minRad) {
                setConfig(direction);
                return;
            }
            distance = minRad + random() * (radius - minRad - 10);
            angleDiff = (random() * 0.1) - 0.05;
        } else {
            double maxRadFix = target.npcInfo.radius / 2,
                    radiusFix = (int) Math.max(Math.min(radius - distance, maxRadFix), -maxRadFix);
            distance = (radius += radiusFix);
            // Moved distance + speed - distance to chosen radius same angle, divided by radius
            angleDiff = Math.max((hero.shipInfo.speed * 0.625) + (min(200, target.locationInfo.speed) * 0.625)
                    - heroLoc.distance(Location.of(targetLoc, angle, radius)), 0) / radius;
        }
        direction = getBestDir(targetLoc, angle, angleDiff, distance);

        while (!drive.canMove(direction) && distance < 10000)
            direction.toAngle(targetLoc, angle += backwards ? -0.3 : 0.3, distance += 2);
        if (distance >= 10000) direction.toAngle(targetLoc, angle, 500);

        setConfig(direction);

        drive.move(direction);
    }

    protected Location getBestDir(Location targetLoc, double angle, double angleDiff, double distance) {
        int iteration = 1;
        double forwardScore = 0, backScore = 0;
        do {
            forwardScore += score(Location.of(targetLoc, angle + (angleDiff * iteration), distance));
            backScore += score(Location.of(targetLoc, angle - (angleDiff * iteration), distance));
            // Toggle direction if either one of the directions is perfect, or one is 300 better.
            if (forwardScore < 0 != backScore < 0 || Math.abs(forwardScore - backScore) > 300) break;
        } while (iteration++ < config.LOOT.MAX_CIRCLE_ITERATIONS);

        if (iteration <= config.LOOT.MAX_CIRCLE_ITERATIONS) backwards = backScore > forwardScore;
        return Location.of(targetLoc, angle + angleDiff * (backwards ? -1 : 1), distance);
    }

    protected double score(Location loc) {
        return (drive.canMove(loc) ? 0 : -1000) - npcs.stream() // Consider barrier as bad as 1000 radius units.
                .filter(n -> attack.target != n)
                .mapToDouble(n -> Math.max(0, n.npcInfo.radius - n.locationInfo.now.distance(loc)))
                .sum();
    }

    protected void setConfig(Location direction) {
        if (!attack.hasTarget()) hero.roamMode();
        else if (config.LOOT.RUN_CONFIG_IN_CIRCLE
                && attack.target.health.hpPercent() < 0.25
                && hero.locationInfo.now.distance(direction) > attack.target.npcInfo.radius * 2) hero.runMode();
        else if (hero.locationInfo.now.distance(direction) > attack.target.npcInfo.radius * 3) hero.roamMode();
        else hero.attackMode(attack.target);
    }

    protected boolean isAttackedByOthers(Npc npc) {
        for (Ship ship : this.ships) {
            if (ship.address == hero.address || ship.address == hero.pet.address
                    || !ship.isAttacking(npc)) continue;
            if (!npc.npcInfo.extra.has(NpcExtra.IGNORE_ATTACKED)) npc.setTimerTo(20_000);
            return true;
        }
        return false;
    }

    protected Npc closestNpc(Location location) {
        int extraPriority = attack.hasTarget() &&
                (hero.target == attack.target || hero.locationInfo.distance(attack.target) < 600)
                ? 20 - (int)(attack.target.health.hpPercent() * 10) : 0;
        return this.npcs.stream()
                .filter(n -> (n == attack.target && hero.isAttacking(attack.target)) ||
                        ((!config.GENERAL.ROAMING.ONLY_KILL_PREFERRED || main.mapManager.preferred.contains(n.locationInfo.now))
                                && shouldKill(n)
                                && drive.closestDistance(n.locationInfo.now) < 500))
                .min(Comparator.<Npc>comparingInt(n -> n.npcInfo.priority - (n == attack.target ? extraPriority : 0))
                        .thenComparing(n -> n.health.hpPercent())
                        .thenComparing(n -> n.locationInfo.now.distance(location))).orElse(null);
    }

    protected boolean shouldKill(Npc n) {
        boolean attacked = this.isAttackedByOthers(n);
        return n.npcInfo.kill && !n.isInTimer() &&
                (n.npcInfo.extra.has(NpcExtra.IGNORE_ATTACKED) || !attacked) && // Either ignore attacked, or not being attacked
                (!n.npcInfo.extra.has(NpcExtra.ATTACK_SECOND) || attacked) &&   // Either don't want to attack second, or being attacked
                (n.playerInfo.username.contains("Invoke") || !n.npcInfo.extra.has(NpcExtra.PASSIVE) || n.isAttacking(hero));
    }

}
