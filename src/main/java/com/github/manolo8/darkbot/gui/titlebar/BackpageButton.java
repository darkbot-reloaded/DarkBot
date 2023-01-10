package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

public class BackpageButton extends TitleBarButton<MainGui> {

    private final Main main;
    private JProgressBar progressBar;

    BackpageButton(Main main, MainGui frame) {
        super(UIUtils.getIcon("home"), frame);
        this.main = main;

        setToolTipText(I18n.get("gui.backpage_button"));
        setLayout(new BorderLayout());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;

        setEnabled(false);
        new BackpageTask(main, this).start();
    }

    public JProgressBar createProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        SwingUtilities.invokeLater(() -> {
            add(progressBar, BorderLayout.SOUTH);
            revalidate();
        });
        return progressBar;
    }

    public void removeProgressBar() {
        final JProgressBar progressBar = this.progressBar;
        this.progressBar = null;

        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                remove(progressBar);
                revalidate();
            });
        }
    }
}
