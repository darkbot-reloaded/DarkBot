package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BackpageButton extends TitleBarButton<MainGui> {

    private final Main main;
    private JProgressBar progressBar;

    BackpageButton(Main main, MainGui frame) {
        super(UIUtils.getIcon("home"), frame);
        this.main = main;

        setLayout(new BorderLayout());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;

        setEnabled(false);
        new BackpageTask(main, this).start();
    }

    public JProgressBar createProgressBar(int maxValue) {
        progressBar = new JProgressBar(0, maxValue);
        SwingUtilities.invokeLater(() -> {
            add(progressBar, BorderLayout.SOUTH);
            revalidate();
        });
        return progressBar;
    }

    public void removeProgressBar() {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                remove(progressBar);
                revalidate();
                progressBar = null;
            });
        }
    }
}
