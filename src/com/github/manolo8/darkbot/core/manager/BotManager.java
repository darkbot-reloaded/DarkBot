package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.def.Manager;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class BotManager {

    private final byte[] bytes = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0
    };

    public static int SEP;

    private List<Manager> managers;

    private boolean invalid;

    public long mainApplicationAddress;

    public long mainAddress;

    public long screenManagerAddress;
    public long guiManagerAddress;

    public long userDataAddress;
    public long settingsAddress;

    public BotManager() {
        this.managers = new ArrayList<>();
        invalid = true;
    }

    public void add(Manager manager) {
        this.managers.add(manager);
    }

    public boolean isInvalid() {
        if (invalid) return true;

        if (API.readMemoryLong(mainApplicationAddress + 1344) == mainAddress) {
            return false;
        } else {
            if (!invalid) stop();
            invalid = true;
            return true;
        }
    }

    public void install() {
        boolean success = install0();

        if (success) {
            for (Manager manager : managers) {
                manager.install(this);
            }
            API.free();
            invalid = false;
        } else {
            invalid = true;
        }
    }

    private boolean install0() {

        if (!API.attachToWindow()) {
            return false;
        }

        List<Long> address = API.queryMemory(bytes);

        if (address.size() != 1) {
            return false;
        }

        mainApplicationAddress = address.get(0) - 228;

        address = API.queryMemory(new byte[]{0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5});

        if (address.size() != 1) {
            return false;
        }

        settingsAddress = address.get(0) - 237;

        SEP = API.readMemoryInt(mainApplicationAddress + 4);

        mainAddress = API.readMemoryLong(mainApplicationAddress + 1344);

        screenManagerAddress = API.readMemoryLong(mainAddress + 504);
        guiManagerAddress = API.readMemoryLong(mainAddress + 512);

//        int userId = API.readMemoryInt(API.readMemoryLong(screenManagerAddress + 240) + 56);
//
//        if (userId == 0) {
//            return false;
//        }
//
//        address = API.queryMemory(userId);
//
//        userDataAddress = 0;
//
//        for (long value : address) {
//
//            int level = API.readMemoryInt(value + 4);
//            int speed = API.readMemoryInt(value + 8);
//            int bool = API.readMemoryInt(value + 12);
//
//
//            if (level >= 0 && level <= 32 && speed > 50 && speed < 2000 && (bool == 1 || bool == 0)) {
//                userDataAddress = value - 48;
//            }
//        }

        return true;
    }

    private void stop() {
        for (Manager manager : managers) {
            manager.install(this);
        }
    }
}
