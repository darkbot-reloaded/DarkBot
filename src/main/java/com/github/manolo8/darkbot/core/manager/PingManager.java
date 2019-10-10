package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.VectorInt;

import java.util.Arrays;

import static com.github.manolo8.darkbot.Main.API;

public class PingManager implements Manager {

    private volatile VectorInt lastPings = null;

    public int ping = -1;
    private int currSize;

    private long lastCheck = System.currentTimeMillis();
    private volatile long time;

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(value -> reset());
    }

    public void tick() {
        if (time == -1) return;
        if (!updatePing()) {
            reset();
            if (System.currentTimeMillis() - time < 5000) return;
            time = -1;
            searchPingManager();
        }
    }

    private void reset() {
        ping = -1;
        currSize = 0;
        lastPings = null;
    }

    private boolean updatePing() {
        if (lastPings == null || lastPings.address == 0) return false;
        lastPings.update();
        if (lastPings.size <= 0) return false;

        if (currSize != lastPings.size) lastCheck = System.currentTimeMillis();
        else if (System.currentTimeMillis() - lastCheck > 5000) return false;

        this.currSize = lastPings.size;

        int ping = lastPings.elements[lastPings.size - 1];
        if (ping < -10 || ping > 50_000) return false;

        this.ping = ping;
        return true;
    }

    private void searchPingManager() {
        Arrays.stream(API.queryMemoryLong(1000, 10_000))
                .unordered()
                .parallel()
                .filter(val -> API.readMemoryInt(val + 16) == 0)
                .map(val -> API.readMemoryLong(val + 8)) // Get ping manager address
                .distinct()
                .filter(addr -> addr != 0) // Check it being ping manager
                .filter(addr -> API.readMemoryLong(addr) != 0)
                .filter(addr -> API.readMemoryLong(addr + 12) != 0)
                .filter(addr -> API.readMemoryLong(addr + 36) != 0)
                .map(addr -> API.readMemoryLong(addr + 40)) // Get last pings array
                .distinct()
                .filter(addr -> addr != 0) // Check array
                .filter(addr -> API.readMemoryInt(addr + 64) > 0) // array size > 0
                .mapToObj(VectorInt::new)
                .peek(VectorInt::update)
                .filter(vec -> vec.size > 0)
                .filter(vec -> Arrays.stream(vec.elements).allMatch(ping -> ping > -1_000 && ping < 50_000))
                .findAny()
                .ifPresent(pings -> this.lastPings = pings);

        time = System.currentTimeMillis();
    }
}
