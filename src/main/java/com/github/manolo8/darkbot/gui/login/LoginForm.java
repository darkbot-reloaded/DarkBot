package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginForm extends JPanel {

    private JTabbedPane tabbedPane = new JTabbedPane();

    private JLabel infoLb = new JLabel("");
    private JButton loginBtn = new JButton("Log in");

    private LoginData loginData = new LoginData();

    public LoginForm() {
        super(new MigLayout("wrap 2, ins 0", "[]10px:push[]", "[]8px[]"));
        tabbedPane.addTab("User & Pass", new UserLogin());
        tabbedPane.addTab("SID login", new SidLogin());
        SavedLogins saved =  new SavedLogins(this);
        tabbedPane.addTab("Saved", saved);
        tabbedPane.setEnabledAt(2, saved.isLoaded());

        loginBtn.addActionListener(ac -> new LoginTask().execute());

        add(tabbedPane, "span 2");
        add(infoLb, "gapleft 8px, grow 0");
        add(loginBtn, "gapright 8px");
    }

    public void setDialog(JDialog dialog) {
        dialog.getRootPane().setDefaultButton(loginBtn);
    }

    public LoginData getResult() {
        return loginData;
    }

    public void setInfoText(Message val) {
        infoLb.setText(val.text);
        infoLb.setToolTipText(val.description);
        Font baseFont = infoLb.getFont();
        if (val.error) {
            infoLb.setForeground(UIUtils.RED.brighter().brighter());
            infoLb.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD));
        } else {
            infoLb.setForeground(null);
            infoLb.setFont(baseFont.deriveFont(baseFont.getStyle() & ~Font.BOLD));
        }
    }

    public static class Message {
        private boolean error;
        private String text;
        private String description;

        public Message(boolean error, String text, String description) {
            this.error = error;
            this.text = text;
            this.description = description;
        }
    }

    private class LoginTask extends SwingWorker<LoginData, Message> {
        private boolean failed = false;

        @Override
        protected void process(List<Message> chunks) {
            setInfoText(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            super.done();
            if (!failed) SwingUtilities.getWindowAncestor(LoginForm.this).setVisible(false);
        }

        @Override
        protected LoginData doInBackground() {
            try {
                publish(new Message(false, "Logging in (1/2)", null));
                Message msg = ((LoginScreen) tabbedPane.getSelectedComponent()).tryLogin(loginData);
                if (msg != null) {
                    publish(msg);
                    failed = true;
                    return null;
                }
                publish(new Message(false, "Loading spacemap (2/2)", null));
                LoginUtils.findPreloader(loginData);
                return loginData;
            } catch (Exception e) {
                publish(new Message(true, "Failed to login!", IssueHandler.createDescription(e)));
                failed = true;
            }
            return null;
        }
    }

}
