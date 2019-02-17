package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class BotInstaller {

    public static final byte[] bytesToMainApplication = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0
    };

    private static final byte[] bytesToSettings = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5
    };

    private final List<Manager> managers;

    public static int SEP;

    public final Lazy<Boolean> invalid;

    public final Lazy<Long> mainApplicationAddress;

    public final Lazy<Long> mainAddress;

    public final Lazy<Long> screenManagerAddress;
    public final Lazy<Long> guiManagerAddress;

    public final Lazy<Long> userDataAddress;
    public final Lazy<Long> settingsAddress;


    private long timer;

    public BotInstaller() {
        this.managers = new ArrayList<>();

        this.mainApplicationAddress = new Lazy<>();
        this.mainAddress = new Lazy<>();
        this.screenManagerAddress = new Lazy<>();
        this.guiManagerAddress = new Lazy<>();
        this.userDataAddress = new Lazy<>();
        this.settingsAddress = new Lazy<>();

        this.invalid = new Lazy<>(true);
        this.invalid.add(value -> {
            if (value) timer = System.currentTimeMillis();
        });
    }

    public void add(Manager manager) {
        this.managers.add(manager);
    }

    public void init() {
        for (Manager manager : managers) {
            manager.install(this);
        }
    }

    public boolean isInvalid() {
        if (invalid.value) return true;

        if (API.readMemoryLong(mainApplicationAddress.value + 1344) == mainAddress.value) {

            checkUserData();

            return false;
        } else {
            invalid.send(true);
            return true;
        }
    }

    public void verify() {
        if (install0()) {
            invalid.send(false);
        } else {
            invalid.send(true);
        }
    }

    private void checkUserData() {
        if (userDataAddress.value != 0) return;

        int id = API.readMemoryInt(API.readMemoryLong(screenManagerAddress.value + 240) + 56);

        if (id == 0) return;

        long[] address = API.queryMemoryInt(id, 10);

        for (long value : address) {

            int level = API.readMemoryInt(value + 4);
            int speed = API.readMemoryInt(value + 8);
            int bool = API.readMemoryInt(value + 12);
            int val = API.readMemoryInt(value + 16);
            int cargo = API.readMemoryInt(API.readMemoryLong(value - 48 + 240) + 40);
            int maxCargo = API.readMemoryInt(API.readMemoryLong(value - 48 + 248) + 40);

            if (level >= 0 && level <= 32
                    && speed > 50 && speed < 2000
                    && (bool == 1 || bool == 2)
                    && val == 0
                    && cargo >= 0
                    && maxCargo >= 100 && maxCargo < 100_000) {
                userDataAddress.send(value - 48);
                break;
            }

        }

    }

    private boolean install0() {

        if (!API.isValid()) return false;

        long[] address = API.queryMemory(bytesToMainApplication, 1);

        checkInvalid();

        if (address.length != 1) return false;

        mainApplicationAddress.send(address[0] - 228);

        address = API.queryMemory(bytesToSettings, 1);

        if (address.length != 1) return false;

        settingsAddress.send(address[0] - 237);

        SEP = API.readMemoryInt(mainApplicationAddress.value + 4);

        long temp = API.readMemoryLong(mainApplicationAddress.value + 1344);

        if (temp == 0) return false;

        mainAddress.send(temp);

        temp = API.readMemoryLong(mainAddress.value + 504);

        if (temp == 0) return false;

        screenManagerAddress.send(temp);

        temp = API.readMemoryLong(mainAddress.value + 512);

        if (temp == 0) return false;

        guiManagerAddress.send(temp);

        //reset address
        userDataAddress.send(0L);

        return true;
    }

    private void checkInvalid() {
        if (System.currentTimeMillis() - timer > 180000) {
            API.refresh();
            timer = System.currentTimeMillis();
        }
    }

}
