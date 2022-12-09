package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import eu.darkbot.api.API;
import eu.darkbot.api.utils.NativeAction;
import eu.darkbot.util.TimeUtils;
import eu.darkbot.util.Timer;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SettingsGui extends Gui implements API.Singleton {
    private static final int KEYBIND_X_OFFSET = 365;
    private static final int KEYBIND_Y_OFFSET = 105;

    private boolean fake = false, reset;
    private final Main main;

    private final Timer assignKeyBindTimer = Timer.get(TimeUtils.MINUTE);

    public SettingsGui(Main main) {
        this.main = main;
        assignKeyBindTimer.activate(10_000);
    }

    @Override
    public void update() {
        if (!main.isRunning()) return;
        super.update();

        if (Main.API.hasCapability(GameAPI.Capability.DIRECT_POST_ACTIONS) && (reset || !assignKeyBindTimer.isArmed())) {
            if (show(true)) {
                Main.API.postActions(assignKeyBinds(!reset, fake));

                reset = fake = false;
                assignKeyBindTimer.activate();
                System.out.println("Trying to reset empty key-binds!");
            }
        } else if ((assignKeyBindTimer.getRemainingFuse() - 57 * TimeUtils.SECOND) < 0) // wait 3 seconds
            show(false);
    }

    public void revalidateKeyBinds() {
        if (assignKeyBindTimer.isInactive())
            assignKeyBindTimer.disarm();
    }

    private long[] assignKeyBinds(boolean emptyOnly, boolean assignEmpty) {
        List<Long> actions = openKeyBindsTab();

        Arrays.stream(SettingsProxy.KeyBind.values())
                .sorted(Comparator.comparingInt(SettingsProxy.KeyBind::getSettingIdx))
                .forEach(keyBind -> {
                    int idx = keyBind.getSettingIdx();
                    int idxModulo = idx % 8;

                    if (!emptyOnly || main.facadeManager.settings.getCharCode(keyBind) == null)
                        addKeyBind(actions, (idx != 0 && idxModulo == 0) ? 8 : idxModulo, assignEmpty ? 0 : keyBind.getDefaultKey());

                    if (idx != 0 && idxModulo == 0)
                        actions.add(scrollDown());
                });

        return saveAndCloseAction(actions);
    }

    public void setKeyBinds(boolean fake) {
        this.fake = fake;
        this.reset = true;
    }

    private List<Long> openKeyBindsTab() {
        List<Long> actions = new ArrayList<>();

        actions.add(NativeAction.Mouse.CLICK.of((int) getX2() - 50, y + 30)); // key binds tab
        actions.add(NativeAction.Mouse.CLICK.of(x + 200, y + 200)); // get focus on key binds tab

        // scroll up few times
        for (int i = 0; i < 5; i++)
            actions.add(scrollUp());

        return actions;
    }

    private long[] saveAndCloseAction(List<Long> actions) {
        actions.add(NativeAction.Mouse.CLICK.of(x + 65, y + 485)); // Save settings
        actions.add(NativeAction.Mouse.CLICK.of((int) minimized.x + 5, (int) minimized.y + 5)); // Close settings

        return actions.stream().mapToLong(l -> l).toArray();
    }

    private void addKeyBind(List<Long> actions, int offset, int key) {
        actions.add(NativeAction.Mouse.CLICK.of(x + KEYBIND_X_OFFSET, y + KEYBIND_Y_OFFSET + offset * 30));
        actions.add(NativeAction.Key.CLICK.of(fake ? 0 : key));
    }

    private long scrollUp() {
        return NativeAction.MouseWheel.up(x + 200, y + 200);
    }

    private long scrollDown() {
        return NativeAction.MouseWheel.down(x + 200, y + 200);
    }
}
