package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Drive;

import static com.github.manolo8.darkbot.Main.API;

public class HeroManager extends Ship implements Manager {

    public static HeroManager instance;
    private final Main main;

    private long staticAddress;
    private long settingsAddress;

    public final Pet pet;
    public final Drive drive;

    public Map map;

    public Ship target;

    public int config;
    private long configTime;
    private char formation = (char) -1;
    private long formationTime;
    private long portalTime;

    public HeroManager(Main main) {
        super(0);
        instance = this;

        this.main = main;
        this.drive = new Drive(this, main.mapManager);
        main.status.add(b -> drive.stop(true));
        this.pet = new Pet(0);
        this.map = new Map(-1, "Loading", false, new Portal[0]);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> staticAddress = value + 240);
        botInstaller.settingsAddress.add(value -> this.settingsAddress = value);
    }

    public void tick() {

        long address = API.readMemoryLong(staticAddress);

        if (this.address != address) {
            update(address);
        }

        update();

        drive.checkMove();
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

    public void setTarget(Ship entity) {
        this.target = entity;
    }

    public long timeTo(double distance) {
        return (long) (distance * 1000 / shipInfo.speed);
    }

    public int nextMap() {
        return API.readMemoryInt(API.readMemoryInt(settingsAddress + 204));
    }

    public void jumpPortal(Portal portal) {
        if (portal.target.id != nextMap() && System.currentTimeMillis() - portalTime > 10000) {
            API.keyboardClick('j');
            portalTime = System.currentTimeMillis();
        }
    }

    public void attackMode() {
        setMode(this.main.config.GENERAL.OFFENSIVE);
    }

    public void runMode() {
        setMode(this.main.config.GENERAL.RUN);
    }

    public void roamMode() {
        setMode(main.config.GENERAL.ROAM);
    }

    private void setMode(Config.ShipConfig config) {
        if (this.config != config.CONFIG && System.currentTimeMillis() - configTime > 5500L) {
            Main.API.keyboardClick('c');
            this.configTime = System.currentTimeMillis();
        }
        if (this.formation != config.FORMATION && System.currentTimeMillis() - formationTime > 3500L) {
            Main.API.keyboardClick(this.formation = config.FORMATION);
            this.formationTime = System.currentTimeMillis();
        }
    }

}