package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;
import java.util.List;

public class LoginForm extends JPanel {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final JLabel infoLb = new JLabel("");
    private final JButton loginBtn = new JButton("Log in");

    private final LoginData loginData = new LoginData();

    public LoginForm() {
        super(new MigLayout("wrap 2, ins 0", "[]10px:push[]", "[]8px[]"));
        tabbedPane.addTab("User & Pass", new UserLogin());
        tabbedPane.addTab("SID login", new SidLogin());
        SavedLogins saved =  new SavedLogins(this);
        tabbedPane.addTab("Saved", saved);
        tabbedPane.setEnabledAt(2, saved.isLoaded());
        if (saved.getLogins() > 0) tabbedPane.setSelectedComponent(saved);

        loginBtn.addActionListener(ac -> startLogin());

        add(tabbedPane, "span 2, growx, height 112px!");
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
        UIUtils.setRed(infoLb, val.error);
    }

    public static class Message {
        private final boolean error;
        private final String text;
        private final String description;

        public Message(boolean error, String text, String description) {
            this.error = error;
            this.text = text;
            this.description = description;
        }
    }

    private boolean canLogIn = true;
    protected synchronized void startLogin() {
        if (!canLogIn) return;
        loginBtn.setEnabled(canLogIn = false);
        setInfoText(new Message(false, "Logging in (1/2)", null));
        new LoginTask().execute();
    }

    protected synchronized void endLogin() {
        loginBtn.setEnabled(canLogIn = true);
    }

    private class LoginTask extends SwingWorker<LoginData, Message> {
        private boolean failed = false;

        @Override
        protected void process(List<Message> chunks) {
            setInfoText(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            if (!failed) SwingUtilities.getWindowAncestor(LoginForm.this).setVisible(false);
            endLogin();
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
                if (e instanceof SSLHandshakeException) //?
                    publish(new Message(true, "Too old Java!", IssueHandler.createDescription(e)));
                else if (e instanceof LoginUtils.LoginException)
                    publish(new Message(true, ((LoginUtils.LoginException) e).titleMessage, IssueHandler.createDescription(e)));
                else
                    publish(new Message(true, "Failed to login!", IssueHandler.createDescription(e)));

                failed = true;
            }
            return null;
        }
    }

}
