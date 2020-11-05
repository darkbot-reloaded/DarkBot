package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.utils.FloatingDialog;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ApiSettingsPanel extends JPanel {

    private final Config.BotSettings.APIConfig apiConfig;
    private final JSpinner w, h;

    public ApiSettingsPanel(Config.BotSettings.APIConfig display, JComponent parent) {
        super(new MigLayout("wrap 6"));
        this.apiConfig = display;

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
        ((JSpinner.NumberEditor) sp.getEditor()).getTextField().setColumns(3);
        return sp;
    }

    private void updateSize() {
        Main.API.setSize(apiConfig.width = (int) w.getValue(), apiConfig.height = (int) h.getValue());
        ConfigEntity.changed();
    }

}
