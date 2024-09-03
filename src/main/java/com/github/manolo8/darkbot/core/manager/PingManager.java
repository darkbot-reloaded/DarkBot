package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.FlashListInt;
import eu.darkbot.api.API;
import eu.darkbot.util.Timer;

public class PingManager implements Manager, API.Singleton {
    private static final int PING_INTERVAL = 15_000;

    private final FlashListInt lastPings = FlashListInt.ofVector().noAuto();

    public int ping = -1;
    private long pingStatsAddress;

    private final Timer addressSearchTimer = Timer.get(5000);

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(value -> {
            pingStatsAddress = 0;
            lastPings.update(0);
        });

        botInstaller.heroInfoAddress.add(value -> {
            if (value == 0) addressSearchTimer.disarm();
            else addressSearchTimer.activate();
        });
    }

    public void tick() {
        if (pingStatsAddress != 0) updatePing();
        else if (addressSearchTimer.isArmed() && addressSearchTimer.isInactive()) {
            ping = -1;
            this.pingStatsAddress = Main.API.searchClassClosure(closure ->
                    Main.API.readInt(closure + 48) == PING_INTERVAL);
        }
    }

    private void updatePing() {
        if (lastPings.getAddress() == 0) {
            ping = -1;
            lastPings.update(Main.API.readLong(pingStatsAddress, 56, 40));
            return;
        }

        lastPings.update();
        if (lastPings.isEmpty() || lastPings.size() > 100) return;

        int ping = lastPings.getLastElement();
        if (ping < -10 || ping > 50_000) return;

        this.ping = ping;
    }
}
