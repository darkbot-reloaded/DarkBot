package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.objects.Map;

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

    public Map map;
    public Pet pet;

    public Location going;
    public Ship target;

    public int config;

    private long configTime;

    public HeroManager(Main main) {
        super(0);
        instance = this;

        this.main = main;
        this.mapManager = main.mapManager;
        this.pet = new Pet(0);
        this.map = new Map(0, "LOADING", new Portal[0]);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> staticAddress = value + 240);
        botInstaller.settingsAddress.add(value -> this.settingsAddress = value);
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

        long petAddress = API.readMemoryLong(address + 176);

        if (petAddress != pet.address) {
            pet.update(petAddress);
        }
    }

    @Override
    public void update(long address) {
        super.update(address);

        pet.update(API.readMemoryLong(address + 176));
        clickable.setRadius(0);
        id = API.readMemoryInt(address + 56);
    }

    public void checkMove() {

        if (going == null || !location.isLoaded()) {
            return;
        }

        double distance = going.distance(location);

        if (!location.isMoving()) {
            mapManager.translateMousePress(location.x, location.y);
        }

        if (distance > 50) {

            distance = min(distance, 200);

            double angle = going.angle(location);

            mapManager.translateMouseMove(
                    cos(angle) * distance + location.x,
                    sin(angle) * distance + location.y
            );

        } else {
            stop(true);
        }

    }

    public void stop(boolean at) {
        if (going != null) {
            if (at) mapManager.translateMouseMoveRelease(going.x, going.y);
            else API.mouseRelease((int) location.x, (int) location.y);
            going = null;
        }
    }

    public void click(Location location) {
        mapManager.translateMouseClick(location.x, location.y);
    }

    public void clickCenter() {
        click(location);
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
//        API.writeMemoryLong(API.readMemoryLong(mapAddress + 120) + 40, entity.address);
    }

    public long timeTo(Location to) {
        return (long) (location.distance(to) * 1000 / shipInfo.speed);
    }

    public boolean isOutOfMap() {
        return main.mapManager.isOutOfMap(location.x, location.y);
    }

    public int nextMap() {
        return API.readMemoryInt(API.readMemoryInt(settingsAddress + 204));
    }

    public void attackMode() {
        if (config != main.config.OFFENSIVE_CONFIG && System.currentTimeMillis() - configTime > 6000) {
            API.keyboardClick('c');
            API.keyboardClick(main.config.OFFENSIVE_FORMATION);
            configTime = System.currentTimeMillis();
        }
    }

    public void runMode() {
        if (config != main.config.RUN_CONFIG && System.currentTimeMillis() - configTime > 6000) {
            API.keyboardClick(main.config.RUN_FORMATION);
            API.keyboardClick('c');
            configTime = System.currentTimeMillis();
        }
    }
}