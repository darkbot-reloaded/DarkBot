package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.utils.Lazy;

import static com.github.manolo8.darkbot.Main.API;

public class BotInstaller {
    private static final byte[] bytesToMainApplication =
            new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0,
                    0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};

    private static final byte[] bytesToSettings =
            new byte[]{0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 5};

    public final Lazy<Boolean> invalid             = new Lazy<>(true);
    public final Lazy<Long> mainApplicationAddress = new Lazy<>();
    public final Lazy<Long> mainAddress            = new Lazy<>();
    public final Lazy<Long> screenManagerAddress   = new Lazy<>();
    public final Lazy<Long> guiManagerAddress      = new Lazy<>();
    public final Lazy<Long> heroInfoAddress        = new Lazy<>();
    public final Lazy<Long> settingsAddress        = new Lazy<>();

    public static int SEP;

    private long timer;

    public BotInstaller(Manager... managers) {
        this.invalid.add(value -> {
            if (value) timer = System.currentTimeMillis();
        });

        for (Manager manager : managers) manager.install(this);
    }

    public boolean isInvalid() {
        if (invalid.value) {
            checkInvalid();
            invalid.send(tryInstall());
            return true;
        }

        if (API.readMemoryLong(mainApplicationAddress.value + 1344) == mainAddress.value) {
            if (heroInfoAddress.value == 0) checkUserData();
            return false;
        }

        invalid.send(true);
        return true;
    }

    private void checkUserData() {
        int id = API.readMemoryInt(API.readMemoryLong(screenManagerAddress.value + 240) + 56);
        if (id == 0) return;

        long[] address = API.queryMemoryInt(id, 10);
        for (long value : address) {
            int level    = API.readMemoryInt(value + 4);
            int speed    = API.readMemoryInt(value + 8);
            int bool     = API.readMemoryInt(value + 12);
            int val      = API.readMemoryInt(value + 16);
            int cargo    = API.readMemoryInt(API.readMemoryLong(value - 48 + 240) + 40);
            int maxCargo = API.readMemoryInt(API.readMemoryLong(value - 48 + 248) + 40);

            if (level >= 0 && level <= 32
                    && speed > 50 && speed < 2000
                    && (bool == 1 || bool == 2)
                    && val == 0
                    && cargo >= 0
                    && maxCargo >= 100 && maxCargo < 100_000) {
                heroInfoAddress.send(value - 48);
                break;
            }
        }
    }

    /**
     * Attempts to install, returns if the bot is still invalid
     * @return True if invalid and should retry, false otherwise.
     */
    private boolean tryInstall() {
        if (!API.isValid()) return true;
        long[] query;
        long temp;

        if ((query = API.queryMemory(bytesToMainApplication, 1)).length != 1) return true;
        this.mainApplicationAddress.send(query[0] - 228);
        BotInstaller.SEP = API.readMemoryInt(mainApplicationAddress.value + 4);

        if ((query = API.queryMemory(bytesToSettings, 1)).length != 1) return true;
        this.settingsAddress.send(query[0] - 237);

        if ((temp = API.readMemoryLong(mainApplicationAddress.value + 1344)) == 0) return true;
        this.mainAddress.send(temp);

        if ((temp = API.readMemoryLong(mainAddress.value + 504)) == 0) return true;
        this.screenManagerAddress.send(temp);

        if ((temp = API.readMemoryLong(mainAddress.value + 512)) == 0) return true;
        this.guiManagerAddress.send(temp);

        //reset address
        this.heroInfoAddress.send(0L);
        return false;
    }

    private void checkInvalid() {
        if (timer == 0 || System.currentTimeMillis() - timer < 180000) return;

        API.handleRefresh();
        timer = System.currentTimeMillis();
        System.out.println("Triggering refresh: bot installer was invalid for too long");
    }
}
