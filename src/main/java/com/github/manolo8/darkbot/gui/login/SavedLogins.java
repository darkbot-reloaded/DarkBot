package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.login.Credentials;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.*;

public class SavedLogins extends JPanel implements LoginScreen {
    public LoginForm loginForm;

    private char[] password;
    private boolean loaded = false;
    private final Credentials credentials = LoginUtils.loadCredentials();

    private final DefaultListModel<Credentials.User> model = new DefaultListModel<>();
    private final JList<Credentials.User> users = new JList<>(model);

    public SavedLogins(LoginForm loginForm) {
        super(new MigLayout("ins 0, gap 0, wrap 2", "[26px!][grow]", "[26px!][26px!][26px!]"));
        this.loginForm = loginForm;

        users.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        users.setVisibleRowCount(-1);
        users.setFixedCellHeight(19);

        try {
            if (!credentials.isEmpty()) {
                password = requestMasterPassword();
                if (password == null) {
                    loginForm.setInfoText(new LoginForm.Message(true, "Didn't load credentials",
                            "Master password was cancelled. Saved credentials aren't available"));
                } else {
                    credentials.decrypt(password);
                    loaded = true;
                }
            } else {
                loaded = true;
            }
        } catch (Exception e) {
            loginForm.setInfoText(new LoginForm.Message(true, "Couldn't load credentials", IssueHandler.createDescription(e)));
        }

        users.setEnabled(loaded);

        add(new AddLogin(), "grow");
        add(new JScrollPane(users), "grow, span 1 3");
        add(new EditLogin(), "grow");
        add(new RemoveLogin(), "grow");

        updateList();
        users.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                int idx = users.locationToIndex(e.getPoint());
                if (idx == -1) return;
                if (users.getCellBounds(idx, idx).contains(e.getPoint())) loginForm.startLogin();
            }
        });
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getLogins() {
        return loaded ? model.getSize() : 0;
    }

    private void updateList() {
        if (!loaded) return; // Prevent list even trying

        model.clear();
        for (Credentials.User user : credentials.getUsers()) model.addElement(user);

        if (!credentials.getUsers().isEmpty() && password == null)
            password = createMasterPassword();

        try {
            LoginUtils.saveCredentials(credentials, password);
        } catch (Exception e) {
            loginForm.setInfoText(new LoginForm.Message(true, "Couldn't save credentials", IssueHandler.createDescription(e)));
        }
    }

    private char[] requestMasterPassword() {
        if (ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD) return new char[]{};

        JPasswordField pass = new JPasswordField(10);

        JOptionPane pane = new JOptionPane(new Object[]{"Input your master password:", pass},
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        Popups.showMessageSync("Darkbot Master password", pane);
        Object result = pane.getValue();

        if (result instanceof Integer && (Integer) result == JOptionPane.YES_OPTION) return pass.getPassword();
        return null;
    }

    private char[] createMasterPassword() {
        if (ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD) return new char[]{};
        JPanel panel = new JPanel(new MigLayout("ins 0"));
        JLabel label = new JLabel("Master password for darkbot to encrypt your credentials.");
        JPasswordField pass = new JPasswordField(26);
        JCheckBox check = new JCheckBox("Disable Master Password");
        JButton button = new JButton("OK");
        panel.add(label, "cell 0 0 2 1");
        panel.add(pass, "cell 0 1 2 1");
        panel.add(check, "cell 0 2 1 1");
        panel.add(button, "cell 1 2 1 1, gapleft push");

        button.setEnabled(false);

        check.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pass.setEditable(false);
                button.setEnabled(true);
            } else {
                pass.setEditable(true);
                if (new String(pass.getPassword()).isEmpty())
                    button.setEnabled(false);
            }
        });
        pass.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                button.setEnabled(!new String(pass.getPassword()).isEmpty() || check.isSelected());
            }
        });
        button.addPropertyChangeListener(evt -> panel.getRootPane().setDefaultButton(button));
        button.addActionListener(e -> SwingUtilities.getWindowAncestor(button).setVisible(false));

        JOptionPane.showOptionDialog(this, panel, "Darkbot Master password",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
        if (check.isSelected()) {
            ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD = true;
        }

        return check.isSelected() ? new char[]{} : pass.getPassword();
    }

    @Override
    public LoginForm.Message tryLogin(LoginData login) {
        Credentials.User user = users.getSelectedValue();
        if (user == null) return new LoginForm.Message(true, "No user selected", "Select the user to login with");

        login.setCredentials(user.u, user.p);
        LoginUtils.usernameLogin(login);
        return null;
    }

    private class AddLogin extends MainButton {
        public AddLogin() {
            super(UIUtils.getIcon("add"), true);
            setEnabled(loaded);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            UserLogin newUser = new UserLogin();
            int result = JOptionPane.showConfirmDialog(this, newUser, "Add new login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.YES_OPTION ||
                    newUser.getUsername().isEmpty() || newUser.getPassword().isEmpty()) return;

            credentials.getUsers().add(new Credentials.User(newUser.getUsername(), newUser.getPassword()));
            updateList();
        }
    }

    private class EditLogin extends MainButton {
        public EditLogin() {
            super(UIUtils.getIcon("edit"), true);
            setEnabled(loaded);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Credentials.User user = users.getSelectedValue();
            if (user == null) return;
            UserLogin editor = new UserLogin(user.u, user.p);
            int result = JOptionPane.showConfirmDialog(this, editor, "Edit login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.YES_OPTION ||
                    editor.getUsername().isEmpty() || editor.getPassword().isEmpty()) return;

            user.u = editor.getUsername();
            user.p = editor.getPassword();

            updateList();
        }
    }

    private class RemoveLogin extends MainButton {
        public RemoveLogin() {
            super(UIUtils.getIcon("remove"), true);
            setEnabled(loaded);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            credentials.getUsers().removeAll(users.getSelectedValuesList());
            updateList();
        }

    }

}
