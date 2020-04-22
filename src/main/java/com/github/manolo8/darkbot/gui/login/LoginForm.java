package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.LoginData;
import com.github.manolo8.darkbot.utils.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class LoginForm extends JPanel {

    private JTabbedPane tabbedPane = new JTabbedPane();
    private UserLogin user = new UserLogin();
    private SidLogin sid = new SidLogin();
    private JLabel info = new JLabel("");
    private JButton login = new JButton("Log in");

    public LoginForm(Consumer<LoginData> callback) {
        super(new MigLayout("wrap 2, ins 0", "[]10px:push[]"));
        tabbedPane.addTab("User & Pass", user);
        tabbedPane.addTab("SID login", sid);

        login.addActionListener(ac -> new LoginTask(new LoginData(), callback).execute());

        add(tabbedPane, "span 2");
        add(info, "gapleft 8px, grow 0");
        add(login, "gapright 8px");
    }

    public void setDialog(JDialog dialog) {
        dialog.getRootPane().setDefaultButton(login);
    }

    private static class UserLogin extends JPanel {
        private JTextField user = new JTextField(16),
                pass = new JPasswordField(16);
        UserLogin() {
            super(new MigLayout("wrap 2, height 30px!", "[]8px:push[]"));
            add(new JLabel("Username"));
            add(user);
            add(new JLabel("Password"));
            add(pass);
        }
    }
    private static class SidLogin extends JPanel {
        public JTextField sv = new JTextField(4),
                sid = new JTextField(16);
        SidLogin() {
            super(new MigLayout("wrap 2, height 30px!", "[]8px:push[]"));
            add(new JLabel("Server"));
            add(sv);
            add(new JLabel("SID"));
            add(sid);
        }
    }

    private class LoginTask extends SwingWorker<LoginData, String> {

        private LoginData loginData;
        private boolean failed = false;
        private Consumer<LoginData> callback;

        public LoginTask(LoginData loginData, Consumer<LoginData> callback) {
            this.loginData = loginData;
            this.callback = callback;
        }

        @Override
        protected void process(List<String> chunks) {
            String val = chunks.get(chunks.size() - 1);
            info.setText(val);
            Font baseFont = info.getFont();
            if (val.startsWith("Failed")) {
                info.setForeground(UIUtils.RED.brighter().brighter());
                info.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD));
            } else {
                info.setForeground(null);
                info.setFont(baseFont.deriveFont(baseFont.getStyle() & ~Font.BOLD));
            }
        }

        @Override
        protected void done() {
            super.done();
            if (!failed) {
                callback.accept(loginData);
                SwingUtilities.getWindowAncestor(LoginForm.this).setVisible(false);
            }
        }

        @Override
        protected LoginData doInBackground() {
            try {
                LoginData ld = new LoginData();
                if (tabbedPane.getSelectedComponent() instanceof UserLogin) {
                    publish("Logging in (1/2)");
                    ld.setCredentials(user.user.getText(), user.pass.getText());
                    LoginUtils.usernameLogin(ld);
                } else {
                    ld.setSid(sid.sid.getText(), sid.sv.getText() + ".darkorbit.com");
                }
                publish("Loading spacemap (2/2)");
                LoginUtils.findPreloader(ld);

            } catch (Exception e) {
                publish("Failed to login!");
                failed = true;
            }
            return loginData;
        }
    }

}
