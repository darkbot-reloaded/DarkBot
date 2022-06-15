package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.gui.utils.FloatingDialog;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ApiSettingsPanel extends JPanel {

    private final Config.BotSettings.APIConfig apiConfig;
    private final JSpinner w, h;

    public ApiSettingsPanel(Config.BotSettings.APIConfig display, JComponent parent) {
        super(new MigLayout("wrap 6"));
        this.apiConfig = display;

        if (Main.API.hasCapability(GameAPI.Capability.WINDOW_POSITION)) {
            JCheckBox c = new JCheckBox();
            c.setSelected(apiConfig.attachToBot);

            c.addItemListener(l -> apiConfig.attachToBot = l.getStateChange() == ItemEvent.SELECTED);

            add(new JLabel("Attach window: "), "span 3, align left");
            add(c, "span 3");
        }

        if (Main.API.hasCapability(GameAPI.Capability.HANDLER_CLEAR_CACHE)) {
            JButton b = new JButton("Clear Cache");
            b.addActionListener(l -> Main.API.clearCache());
            add(b, "span 3, grow");
        }

        if (Main.API.hasCapability(GameAPI.Capability.HANDLER_CLEAR_RAM)) {
            JButton b = new JButton("Clear Ram");
            b.addActionListener(l -> {
                System.gc();
                Main.API.emptyWorkingSet();
            });
            add(b, "span 3, grow");
        }

        if (Main.API.hasCapability(GameAPI.Capability.HANDLER_VOLUME)) {
            JSlider s = new JSlider(0, 100);
            s.setValue(display.volume);
            s.addChangeListener(l -> Main.API.setVolume(display.volume = s.getValue()));
            s.setPreferredSize(new Dimension(90, 12));

            add(new JLabel("Volume: "), "span 3, align left");
            add(s, "span 3, align right");
        }

        if (Main.API.hasCapability(GameAPI.Capability.HANDLER_TRANSPARENCY)) {
            JSlider s = new JSlider(10, 100);
            s.setValue(display.transparency);
            s.addChangeListener(l -> Main.API.setTransparency(display.transparency = s.getValue()));
            s.setPreferredSize(new Dimension(90, 12));

            add(new JLabel("Transparency: "), "span 3, align left");
            add(s, "span 3, align right");
        }

        if (Main.API.hasCapability(GameAPI.Capability.HANDLER_GAME_QUALITY)) {
            JSlider s = new JSlider(0, 3);
            s.setValue(display.gameQuality.ordinal());
            s.addChangeListener(l -> Main.API.setQuality(display.gameQuality = GameAPI.Handler.GameQuality.values()[s.getValue()]));
            s.setPreferredSize(new Dimension(90, 12));

            add(new JLabel("Game Quality: "), "span 3, align left");
            add(s, "span 3, align right");
        }

        setBorder(UIUtils.getBorder());

        add(w = createSpinner(display.width, 480, 4096), "span 2, align left");
        add(new JLabel("x"), "span 2, align right");
        add(h = createSpinner(display.height, 320, 2160), "span 2, align right");

        JButton apply = new JButton("Apply");
        apply.addActionListener(a -> updateSize());
        add(apply, "span 3, grow");

        JButton def = new JButton("Default");
        def.setDefaultCapable(false);
        def.addActionListener(a -> {
            w.setValue(1280);
            h.setValue(800);
            updateSize();
        });
        add(def, "span 3, grow");
        add(UIUtils.setRed(new JLabel(I18n.get("bot.issue.resolution")), true), "span");

        FloatingDialog.show(this, parent);
        getRootPane().setDefaultButton(apply);
    }

    private JSpinner createSpinner(int val, int min, int max) {
        JSpinner sp = new JSpinner(new SpinnerNumberMinMaxFix(val, min, max, 100));
        ((JSpinner.NumberEditor) sp.getEditor()).getTextField().setColumns(5);
        return sp;
    }

    private void updateSize() {
        Main.API.setSize(apiConfig.width = (int) w.getValue(), apiConfig.height = (int) h.getValue());
        ConfigEntity.changed();
    }

}
