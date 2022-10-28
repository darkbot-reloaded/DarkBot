package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;

public class BotInstaller implements API.Singleton {
    private static final byte[] bytesToMainApplication =
            new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0,
                    0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};

    private static final byte[] bytesToSettings =
            new byte[]{0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5};

    public static long SCRIPT_OBJECT_VTABLE, STRING_OBJECT_VTABLE;
    public final Lazy<Long> mainApplicationAddress = new Lazy<>();
    public final Lazy<Boolean> invalid = new Lazy<>(true);
    public final Lazy<Long> mainAddress = new Lazy<>();
    public final Lazy<Long> screenManagerAddress = new Lazy<>();
    public final Lazy<Long> guiManagerAddress = new Lazy<>();
    public final Lazy<Long> heroInfoAddress = new Lazy<>();
    public final Lazy<Long> settingsAddress = new Lazy<>();
    public final Lazy<Long> connectionManagerAddress = new Lazy<>();
    public static int SEP;

    private long timer;

    public BotInstaller() {
        this.invalid.add(value -> {
            if (value) timer = System.currentTimeMillis();
        });
    }

    public void install(Manager... managers) {
        for (Manager manager : managers)
            manager.install(this);
    }

    public boolean isInvalid() {
        if (invalid.get()) {
            checkInvalid();
            invalid.send(tryInstall());
            return true;
        }

        if (API.readMemoryLong(mainApplicationAddress.get() + 1344) == mainAddress.get()) {
            if (heroInfoAddress.get() == 0) checkUserData();

            if (connectionManagerAddress.get() == 0) {
                long connMgr = API.readMemoryLong(mainAddress.get() + 560);
                if (connMgr != 0) connectionManagerAddress.send(connMgr);
            }
            return false;
        }

        invalid.send(true);
        return true;
    }

    private void checkUserData() {
        int heroId = API.readMemoryInt(API.readMemoryLong(screenManagerAddress.get() + 240) + 56);
        if (heroId == 0) return;

        long address = API.searchClassClosure(closure -> {
            int level = API.readMemoryInt(closure + 52);
            int speed = API.readMemoryInt(closure + 56);
            int bool = API.readMemoryInt(closure + 60);
            int val = API.readMemoryInt(closure + 64);
            int cargo = API.readMemoryInt(API.readMemoryLong(closure + 272) + 40);
            int maxCargo = API.readMemoryInt(API.readMemoryLong(closure + 280) + 40);

            return heroId == API.readMemoryInt(closure + 0x30)
                   && level >= 0 && level <= 100
                   && speed > 50 && speed < 2000
                   && (bool == 1 || bool == 2)
                   && val == 0
                   && cargo >= 0
                   && maxCargo >= 100 && maxCargo < 100_000;
        });
        if (address != 0) heroInfoAddress.send(address);
    }

    /**
     * Attempts to install, returns if the bot is still invalid
     * @return True if invalid and should retry, false otherwise.
     */
    private boolean tryInstall() {
        if (!API.isValid()) return true;
        long temp;

        if ((temp = API.queryMemory(bytesToMainApplication)) == 0) return true;
        this.mainApplicationAddress.send(temp - 228);
        BotInstaller.SEP = API.readMemoryInt(mainApplicationAddress.get() + 4);

        if ((temp = API.searchClassClosure(this::settingsPattern)) == 0) return true;
        this.settingsAddress.send(temp);

        if ((temp = API.readMemoryLong(mainApplicationAddress.get() + 1344)) == 0) return true;
        this.mainAddress.send(temp);

        if ((temp = API.readMemoryLong(mainAddress.get() + 504)) == 0) return true;
        this.screenManagerAddress.send(temp);

        if ((temp = API.readMemoryLong(mainAddress.get() + 512)) == 0) return true;
        this.guiManagerAddress.send(temp);

        SCRIPT_OBJECT_VTABLE = API.readLong(screenManagerAddress.get());
        // vtable -> traits -> core -> string cache -> cpp_vtable
        STRING_OBJECT_VTABLE = API.readLong(screenManagerAddress.get(), 0x10, 0x28, 0x8, 0x3e8, 0x0);

        //reset address
        this.heroInfoAddress.send(0L);
        this.connectionManagerAddress.send(0L);
        return false;
    }

    private boolean settingsPattern(long address) {
        return API.readMemoryInt(address + 48) == -1
               && API.readMemoryInt(address + 52) == 0
               && API.readMemoryInt(address + 56) == 2
               && API.readMemoryInt(address + 60) == 1;
        // address + 280 - starts old pattern here
    }

    private void checkInvalid() {
        if (timer == 0 || System.currentTimeMillis() - timer < 180000) return;

        API.handleRefresh();
        timer = System.currentTimeMillis();
        System.out.println("Triggering refresh: bot installer was invalid for too long");
    }
}
