package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;

import javax.swing.*;
import java.util.List;

public class AutoLoginForm extends JPanel {
    private final JLabel infoLb = new JLabel();
    private final LoginData loginData = new LoginData();

    private AutoLogin login;

    public AutoLoginForm(StartupParams params) {
        login = new AutoLogin(this, params);
        add(infoLb);
        startLogin();
    }

    public LoginData getResult() {
        return loginData;
    }

    public void setInfoText(LoginForm.Message val) {
        infoLb.setText(val.text);
        infoLb.setToolTipText(val.description);
        UIUtils.setRed(infoLb, val.error);
    }

    protected synchronized void startLogin() {
        setInfoText(new LoginForm.Message(false, "Logging in (1/2)", null));
        new AutoLoginForm.LoginTask().execute();
    }

    private class LoginTask extends SwingWorker<LoginData, LoginForm.Message> {
        private boolean failed = false;

        @Override
        protected void process(List<LoginForm.Message> chunks) {
            setInfoText(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            if (!failed) SwingUtilities.getWindowAncestor(AutoLoginForm.this).setVisible(false);
        }

        @Override
        protected LoginData doInBackground() {
            try {
                publish(new LoginForm.Message(false, "Logging in (1/2)", null));
                LoginForm.Message msg = login.tryLogin(loginData);
                if (msg != null) {
                    publish(msg);
                    failed = true;
                    return null;
                }
                publish(new LoginForm.Message(false, "Loading spacemap (2/2)", null));
                LoginUtils.findPreloader(loginData);
                return loginData;
            } catch (Exception e) {
                publish(new LoginForm.Message(true, "Failed to login!", IssueHandler.createDescription(e)));
                failed = true;
            }
            return null;
        }
    }
}
