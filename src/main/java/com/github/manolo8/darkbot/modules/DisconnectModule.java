package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.modules.utils.SafetyFinder;

public class DisconnectModule extends TemporalModule {

    private final boolean stopBot;

    private Main main;
    private HeroManager hero;
    private SafetyFinder safety;

    private Gui lostConnection;
    private Gui logout;
    private long logoutStart;

    public DisconnectModule(boolean stopBot) {
        this.stopBot = stopBot;
    }

    @Override
    public void install(Main main) {
        super.install(main);
        this.main = main;
        this.hero = main.hero;
        this.safety = new SafetyFinder(main);

        this.lostConnection = main.guiManager.lostConnection;
        this.logout = main.guiManager.logout;
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public void tick() {
        main.guiManager.pet.setEnabled(false);
        safety.setRefreshing(true);
        safety.tick();
        if (hero.locationInfo.isMoving() || safety.state() != SafetyFinder.Escaping.WAITING) return;
        if (!logout.visible) logoutStart = System.currentTimeMillis();
        logout.show(true);
        // Prevent bug where logout gets to 0 and doesn't log out, just force a reload
        if (System.currentTimeMillis() - logoutStart > 25_000) {
            logoutStart = System.currentTimeMillis() + 90_000;
            System.out.println("Disconnect module, refreshing due to logout not finishing bug.");
            Main.API.handleRefresh();
        }
    }

    @Override
    public void tickStopped() {
        if (lostConnection.visible) {
            if (stopBot) main.setRunning(false);
            else goBack();
        }
    }

    @Override
    public String status() {
        return "Disconnecting: " + safety.status();
    }
}
