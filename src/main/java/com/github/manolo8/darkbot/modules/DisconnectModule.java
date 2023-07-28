package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.modules.utils.SafetyFinder;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import eu.darkbot.util.Popups;

public class DisconnectModule extends TemporalModule {

    private final Long pauseTime;
    private final String reason;

    private Main main;
    private HeroManager hero;
    private SafetyFinder safety;

    private Gui lostConnection;
    private Gui logout;
    private long logoutStart;

    private Long pauseUntil = null;
    private boolean refreshing = false;
    private boolean closeBot = false;
    private boolean popupOpened = false;

    /**
     * @param pauseTime null for infinite pause, otherwise pause for that amount of
     *                  MS.
     */
    public DisconnectModule(Long pauseTime, String reason) {
        this.reason = reason;
        this.pauseTime = pauseTime;
    }

    /**
     * @param pauseTime null for infinite pause, otherwise pause for that amount of
     *                  MS.
     */
    public DisconnectModule(Long pauseTime, String reason, boolean closeBot) {
        this(pauseTime, reason);
        this.closeBot = closeBot;
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
    public void uninstall() {
        safety.uninstall();
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public void tick() {
        showClosePopup();
        // Just in case refresh was super quick, don't go back to normal tick.
        if (refreshing) {
            tickStopped();
            return;
        }
        main.guiManager.pet.setEnabled(false);
        safety.setRefreshing(true);
        safety.tick();
        if (hero.locationInfo.isMoving() || safety.state() != SafetyFinder.Escaping.WAITING)
            return;
        if (!logout.visible)
            logoutStart = System.currentTimeMillis();
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
        showClosePopup();
        if (main.isRunning()) {
            if (!lostConnection.visible)
                return;
            // Bot done. Pause "forever" (unless a behaviour restarts it).
            if (pauseTime == null)
                main.setRunning(false);
            else if (pauseTime == 0)
                goBack();
            else {
                pauseUntil = System.currentTimeMillis() + pauseTime;
                main.setRunning(false);

                if (closeBot) {
                    System.out.println("Exit by the Disconnect module, exiting");
                    System.exit(0);
                }
            }
        } else if (pauseUntil != null && System.currentTimeMillis() > pauseUntil - 10_000) {
            if (!refreshing) {
                System.out.println("Disconnect module, refreshing after pause, getting back to work.");
                Main.API.handleRefresh();
                refreshing = true;
            } else if (System.currentTimeMillis() > pauseUntil) {
                goBack();
                main.setRunning(true);
            }
        }
    }

    @Override
    public String status() {
        return I18n.get("module.disconnect.status", reason, safety.status());
    }

    @Override
    public String stoppedStatus() {
        if (pauseUntil == null)
            return I18n.get("module.disconnect.status_stopped", reason);
        else
            return I18n.get("module.disconnect.status_paused", reason,
                    Time.toString(Math.max(0, pauseUntil - System.currentTimeMillis())));
    }

    private void showClosePopup() {
        if (!closeBot || popupOpened) {
            return;
        }

        this.popupOpened = true;
        JButton okBtn = new JButton(I18n.get("module.disconnect.popup.ok_btn"));
        JButton cancelBtn = new JButton(I18n.get("module.disconnect.popup.cancel_btn"));
        okBtn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(okBtn).setVisible(false);
        });
        cancelBtn.addActionListener(e -> {
            closeBot = false;
            SwingUtilities.getWindowAncestor(cancelBtn).setVisible(false);
        });

        Popups.of(I18n.get("module.disconnect.popup.title"),
                new JOptionPane(I18n.get("module.disconnect.popup.message"), JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION, null, new Object[] { okBtn, cancelBtn }))
                .showAsync();
    }
}
