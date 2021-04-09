package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.SystemUtils;

import java.awt.event.ActionEvent;
import java.net.URI;

public class PluginSidePanel extends IssueList {

    private final DonationButton donationButton;

    public PluginSidePanel(Plugin plugin) {
        super(false, plugin.getIssues(), plugin.getUpdateIssues());
        this.donationButton = plugin.getDefinition().donation != null ?
                new DonationButton(plugin.getDefinition()) : null;
        setupUI();
    }

    @Override
    protected void setupUI() {
        super.setupUI();
        if (getComponents().length == 0 && donationButton != null)
            add(donationButton);

        setVisible(getComponents().length > 0);
    }

    public static class DonationButton extends MainButton {

        private final URI donationURI;

        public DonationButton(PluginDefinition plDef) {
            super(UIUtils.getIcon("heart"), "Donate " + plDef.author);
            this.donationURI = plDef.donation;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (donationURI != null)
                SystemUtils.openUrl(donationURI);
        }
    }
}
