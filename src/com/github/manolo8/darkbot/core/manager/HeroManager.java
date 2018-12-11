package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Manager;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.objects.Statistics;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.manager.MapManager.mapAddress;
import static java.lang.Math.*;
import static java.lang.StrictMath.sin;

public class HeroManager extends Ship implements Manager {

    public static HeroManager instance;
    private final Main main;
    private final MapManager mapManager;

    private long staticAddress;
    private long settingsAddress;

    public Pet pet;

    public Statistics statistics;
    public Location going;
    public Ship target;

    public int config;

    private long configTime;
    private int attempts;

    public HeroManager(Main main) {
        instance = this;

        this.main = main;
        this.mapManager = main.mapManager;
        this.statistics = new Statistics(0);
        this.pet = new Pet(0, 0);
    }

    @Override
    public void install(BotManager botManager) {
        this.staticAddress = botManager.screenManagerAddress + 240;
        this.settingsAddress = botManager.settingsAddress;
        this.statistics.update(botManager.userDataAddress);
    }

    @Override
    public void stop() {
        if (going != null) {
            stop(true);
        }
    }

    public void tick() {
        long address = API.readMemoryLong(staticAddress);

        if (this.address != address) {
            update(address);
        }

        update();
    }

    @Override
    public void update() {

        super.update();

        pet.update();

        config = API.readMemoryInt(settingsAddress + 56);

        //        statistics.update();

        if (pet.isInvalid()) {
            pet.update(API.readMemoryLong(address + 176));
        }
    }

    @Override
    public void update(long address) {
        super.update(address);

        pet.update(API.readMemoryLong(address + 176));
        id = API.readMemoryInt(address + 56);
    }

    public void checkMove() {

        if (going == null || !location.isLoaded()) {
            return;
        }

        double distance = going.distance(location);

        //50 = error margin

        if (distance > 50) {

            if (((!location.isMoving()) && !tryPressMouse())) {
                return;
            }

            double chunk = min(200, distance);
            double angle = going.angle(location);

            mapManager.translateMouseMove(
                    cos(angle) * chunk + location.x,
                    sin(angle) * chunk + location.y
            );

        } else {
            stop(true);
        }
    }

    private boolean tryPressMouse() {

        double x, y;
        int trying = 0;

        while (++trying != 12) {

            double angle = (Math.PI / 6) * (attempts++ % 12);
            x = cos(angle) * 150 + location.x;
            y = sin(angle) * 150 + location.y;

            if (mapManager.canClick(x, y)) {
                mapManager.translateMousePress(x, y);
                return true;
            }

        }

        return false;
    }

    public void stop(boolean at) {
        if (at) mapManager.translateMouseMove(location.x, location.y);
        API.mouseRelease();
        going = null;
    }

    public void click(Location location) {
        mapManager.translateMouseClick(location.x, location.y);
    }

    public void move(Entity entity) {
        move(entity.location);
    }

    public void move(Location location) {
        move(location.x, location.y);
    }

    public void move(double x, double y) {
        going = new Location(x, y);
    }

    public void moveRandom() {
        move(random() * mapManager.internalWidth, random() * mapManager.internalHeight);
    }

    public boolean isMoving() {
        return going != null || location.isMoving();
    }

    public boolean isTarget(Entity entity) {
        return API.readMemoryLong(API.readMemoryLong(mapAddress + 120) + 40) == entity.address;
    }

    public void setTarget(Ship entity) {
        this.target = entity;
        API.writeMemoryLong(API.readMemoryLong(mapAddress + 120) + 40, entity.address);
    }

    public long timeTo(Location to) {
        return (long) (location.distance(to) * 1000 / shipInfo.speed);
    }

    public int nextMap() {
        return API.readMemoryInt(API.readMemoryInt(settingsAddress + 204));
    }

    public void attackMode() {
        if (config != main.config.OFFENSIVE_CONFIG && System.currentTimeMillis() - configTime > 6000) {
            API.button('c');
            API.button(main.config.OFFENSIVE_FORMATION);
            configTime = System.currentTimeMillis();
        }
    }

    public void runMode() {
        if (config != main.config.RUN_CONFIG && System.currentTimeMillis() - configTime > 6000) {
            API.button(main.config.RUN_FORMATION);
            API.button('c');
            configTime = System.currentTimeMillis();
        }
    }

    public boolean willStop(int distance) {
        return going == null || going.distance(location) < distance;
    }
}