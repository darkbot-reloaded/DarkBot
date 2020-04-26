package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.login.Credentials;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SavedLogins extends JPanel implements LoginScreen {
    public LoginForm loginForm;

    private char[] password;
    private Credentials credentials = LoginUtils.loadCredentials();
    private boolean loaded = false;

    private DefaultListModel<Credentials.User> model = new DefaultListModel<>();
    private JList<Credentials.User> users = new JList<>(model);

    public SavedLogins(LoginForm loginForm) {
        super(new MigLayout("ins 0, gap 0, wrap 2, height 46px!", "[26px!][grow]", "[26px!][26px!][26px!]"));
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
    }

    public boolean isLoaded() {
        return loaded;
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
        JPasswordField pass = new JPasswordField(10);

        int result = JOptionPane.showConfirmDialog(this, new Object[]{"Input your master password:", pass},
                "Darkbot Master password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.YES_OPTION) return null;

        return pass.getPassword();
    }

    private char[] createMasterPassword() {
        JPasswordField pass = new JPasswordField(10);

        JOptionPane.showMessageDialog(this, new Object[]{
                "Master password for darkbot to encrypt your credentials.\n" +
                        "You can use a blank (empty) password:", pass},
                "Darkbot Master password", JOptionPane.PLAIN_MESSAGE);

        return pass.getPassword();
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
