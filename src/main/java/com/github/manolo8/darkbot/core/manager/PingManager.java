package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import eu.darkbot.api.API;


public class PingManager implements Manager, API.Singleton {
    private static final int PING_INTERVAL = 15_000;

    private final IntArray lastPings = IntArray.ofVector();

    public int ping = -1;
    private long pingStatsAddress;

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(value -> {
            pingStatsAddress = 0;
            lastPings.update(0);
        });
    }

    public void tick() {
        if (pingStatsAddress != 0) updatePing();
        else {
            ping = -1;
            this.pingStatsAddress = Main.API.searchClassClosure(closure ->
                    Main.API.readInt(closure + 48) == PING_INTERVAL);
        }
    }

    private void updatePing() {
        if (lastPings.address == 0) {
            ping = -1;
            lastPings.update(Main.API.readLong(pingStatsAddress, 56, 40));
            return;
        }

        lastPings.update();
        if (lastPings.size < 0 || lastPings.size > 100) return;

        int ping = lastPings.getLast();
        if (ping < -10 || ping > 50_000) return;

        this.ping = ping;
    }
}
