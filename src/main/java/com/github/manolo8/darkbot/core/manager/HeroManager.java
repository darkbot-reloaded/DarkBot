package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Drive;

import static com.github.manolo8.darkbot.Main.API;

public class HeroManager extends Ship implements Manager {

    public static HeroManager instance;
    public final Main main;

    private long staticAddress;
    private long settingsAddress;

    public final Pet pet;
    public final Drive drive;

    public Map map;

    public Ship target;

    public int config;
    private long configTime;
    private Character formation = null;
    private long formationTime;
    private long portalTime;

    public HeroManager(Main main) {
        super(0);
        instance = this;

        this.main = main;
        this.drive = new Drive(this, main.mapManager);
        main.status.add(drive::toggleRunning);
        this.pet = new Pet(0);
        this.map = main.starManager.byId(-1);
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

        config = API.readMemoryInt(settingsAddress + 52);

        long petAddress = API.readMemoryLong(address + 176);
        if (petAddress != pet.address) pet.update(petAddress);
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

    public boolean hasTarget() {
        return this.target != null && !target.removed;
    }

    public long timeTo(double distance) {
        return (long) (distance * 1000 / shipInfo.speed);
    }

    // 208 -> next map, 212 -> curr map, 216 -> prev map
    private int nextMap() {
        return API.readMemoryInt(settingsAddress + 204);
    }
    private int currMap() {
        return API.readMemoryInt(settingsAddress + 208);
    }

    public void jumpPortal(Portal portal) {
        if (portal.removed) return;
        if (System.currentTimeMillis() - portalTime > 10000 || (System.currentTimeMillis() - portalTime > 1000 &&
                map.id == currMap() && (nextMap() == -1 || portal.target == null || nextMap() != portal.target.id))) {
            API.keyboardClick('j');
            portalTime = System.currentTimeMillis();
        }
    }

    public boolean attackMode() {
        return setMode(main.config.GENERAL.OFFENSIVE);
    }

    public boolean attackMode(Npc target) {
        if (target == null) return attackMode();
        Config.ShipConfig config = this.main.config.GENERAL.OFFENSIVE;
        return setMode(config.CONFIG, target.npcInfo.attackFormation != null ?
                target.npcInfo.attackFormation : config.FORMATION);
    }

    public boolean runMode() {
        return setMode(this.main.config.GENERAL.RUN);
    }

    public boolean roamMode() {
        return setMode(main.config.GENERAL.ROAM);
    }

    public boolean setMode(Config.ShipConfig config) {
        return setMode(config.CONFIG, config.FORMATION);
    }

    public boolean setMode(int con, Character form) {
        int formationCheck = main.config.GENERAL.FORMATION_CHECK;

        if (this.config != con && System.currentTimeMillis() - configTime > 5500L) {
            Main.API.keyboardClick('c');
            this.configTime = System.currentTimeMillis();
        }
        boolean checkFormation = formationCheck > 0 && (System.currentTimeMillis() - formationTime) > formationCheck * 1000;

        if ((this.formation != form || checkFormation) && System.currentTimeMillis() - formationTime > 3500L) {
            Main.API.keyboardClick(this.formation = form);
            if (formation != null) this.formationTime = System.currentTimeMillis();
        }
        return isInMode(con, form);
    }

    public boolean isInMode(Config.ShipConfig config) {
        return isInMode(config.CONFIG, config.FORMATION);
    }

    public boolean isInMode(int config, Character formation) {
        return this.config == config && this.formation == formation;
    }

}
