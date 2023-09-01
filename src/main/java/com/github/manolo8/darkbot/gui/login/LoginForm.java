package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;
import java.util.List;

public class LoginForm extends JPanel {

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final JLabel infoLb = new JLabel("");
    private final JButton loginBtn = new JButton(I18n.get("gui.login.log_in.button"));

    private final LoginData loginData = new LoginData();

    public LoginForm() {
        super(new MigLayout("wrap 2, ins 0", "[]10px:push[]", "[]8px[]"));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabbedPane.addTab(I18n.get("gui.login.user_pass"), new UserLogin());
        tabbedPane.addTab(I18n.get("gui.login.sid"), new SidLogin());
        SavedLogins saved =  new SavedLogins(this);
        tabbedPane.addTab(I18n.get("gui.login.saved"), saved);
        tabbedPane.setEnabledAt(2, saved.isLoaded());
        if (saved.getLogins() > 0) tabbedPane.setSelectedComponent(saved);

        loginBtn.addActionListener(ac -> startLogin());

        add(tabbedPane, "span 2, growx, height 112px!");
        add(infoLb, "gapleft 8px, grow 0");
        add(loginBtn, "gapright 8px");
    }

    public JButton getLoginBtn() {
        return loginBtn;
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
        setInfoText(new Message(false, I18n.get("gui.login.info.logging_in"), null));
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
                loginData.reset();

                publish(new Message(false, I18n.get("gui.login.info.logging_in"), null));
                Message msg = ((LoginScreen) tabbedPane.getSelectedComponent()).tryLogin(loginData, this::publish);
                if (msg != null) {
                    publish(msg);
                    failed = true;
                    return null;
                }

                if (loginData.isNotInitialized()) {
                    publish(new Message(false, I18n.get("gui.login.info.loading_spacemap"), null));
                    LoginUtils.findPreloader(loginData);
                }
                return loginData;
            } catch (SSLHandshakeException e) {
                publish(new Message(true, I18n.get("gui.login.error.unsupported_java"),
                        IssueHandler.createDescription(e)));
            } catch (LoginUtils.LoginException e) {
                publish(new Message(true, e.getTitle(), IssueHandler.createDescription(e)));
            } catch (Exception e) {
                publish(new Message(true, I18n.get("gui.login.error.failed_login"),
                        IssueHandler.createDescription(e)));
            }
            failed = true;
            return null;
        }
    }

}
