package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class BotInstaller {

    public static final byte[] bytes = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0
    };

    public static int SEP;

    private final List<Manager> managers;

    public final Lazy<Boolean> invalid;

    public final Lazy<Long> mainApplicationAddress;

    public final Lazy<Long> mainAddress;

    public final Lazy<Long> screenManagerAddress;
    public final Lazy<Long> guiManagerAddress;

    public final Lazy<Long> userDataAddress;
    public final Lazy<Long> settingsAddress;

    public BotInstaller() {
        this.managers = new ArrayList<>();

        this.mainApplicationAddress = new Lazy<>();
        this.mainAddress = new Lazy<>();
        this.screenManagerAddress = new Lazy<>();
        this.guiManagerAddress = new Lazy<>();
        this.userDataAddress = new Lazy<>();
        this.settingsAddress = new Lazy<>();

        this.invalid = new Lazy<>(true);
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
        boolean success = install0();

        if (success) {
            invalid.send(false);
        } else {
            invalid.send(true);
        }
    }

    private void checkUserData() {
//        if (userDataAddress.value != 0) return;
//
//        int id = API.readMemoryInt(API.readMemoryLong(screenManagerAddress.value + 240) + 56);
//
//        if (id == 0) return;
//
//        long[] address = API.queryMemoryLong(id, 100);
//
//        for (long value : address) {
//
//            int level = API.readMemoryInt(value + 4);
//            int speed = API.readMemoryInt(value + 8);
//            int bool = API.readMemoryInt(value + 12);
//
//
//            if (level >= 0 && level <= 32 && speed > 50 && speed < 2000 && (bool == 1 || bool == 0)) {
//                userDataAddress.send(value - 48);
//                break;
//            }
//
//        }

    }

    private boolean install0() {

        if (!API.attachToWindow()) {
            return false;
        }

        long[] address = API.queryMemory(bytes, 1);

        if (address.length != 1) {
            return false;
        }

        mainApplicationAddress.send(address[0] - 228);

        address = API.queryMemory(new byte[]{0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5}, 1);

        if (address.length != 1) {
            return false;
        }

        settingsAddress.send(address[0] - 237);

        SEP = API.readMemoryInt(mainApplicationAddress.value + 4);

        mainAddress.send(API.readMemoryLong(mainApplicationAddress.value + 1344));

        screenManagerAddress.send(API.readMemoryLong(mainAddress.value + 504));
        guiManagerAddress.send(API.readMemoryLong(mainAddress.value + 512));

        //reset user data address
        userDataAddress.send(0L);

        return true;
    }

}
