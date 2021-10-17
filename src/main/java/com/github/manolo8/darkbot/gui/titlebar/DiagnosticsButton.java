package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.DiagnosticsPanel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DiagnosticsButton extends TitleBarButton<JFrame> {

    private final Main main;

    DiagnosticsButton(Main main, JFrame frame) {
        super(UIUtils.getIcon("diagnostics"), frame);
        this.main = main;

        setToolTipText(I18n.get("gui.diagnostics"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new DiagnosticsPanel(main, this);
    }

}
