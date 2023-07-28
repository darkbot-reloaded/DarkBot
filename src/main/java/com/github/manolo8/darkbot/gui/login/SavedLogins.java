package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.login.Credentials;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
                    loginForm.setInfoText(new LoginForm.Message(true, I18n.get("gui.login.saved.no_password"),
                            I18n.get("gui.login.saved.no_password")));
                } else {
                    credentials.decrypt(password);
                    loaded = true;

                    // do not ask for master password again if was empty
                    if (password.length == 0) {
                        ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD = true;
                    }
                }
            } else {
                loaded = true;
            }
        } catch (Exception e) {
            loginForm.setInfoText(new LoginForm.Message(true, I18n.get("gui.login.saved.failed_decrypt"),
                    IssueHandler.createDescription(e)));
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
            loginForm.setInfoText(new LoginForm.Message(true, I18n.get("gui.login.saved.failed_save"),
                    IssueHandler.createDescription(e)));
        }
    }

    private char[] requestMasterPassword() {
        if (ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD) return new char[]{};

        JPasswordField pass = new JPasswordField(10);

        int result = Popups.of(I18n.get("gui.login.saved.master_pwd.title"),
                        new Object[]{I18n.get("gui.login.saved.master_pwd.prompt"), pass})
                .optionType(JOptionPane.OK_CANCEL_OPTION)
                .showOptionSync();


        if (result == JOptionPane.YES_OPTION) return pass.getPassword();
        return null;
    }

    private char[] createMasterPassword() {
        if (ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD) return new char[]{};
        JPanel panel = new JPanel(new MigLayout("ins 0", "[]push[]"));
        JLabel label = new JLabel(I18n.get("gui.login.saved.master_pwd.title"));
        JPasswordField pass = new JPasswordField(26);
        JCheckBox check = new JCheckBox(I18n.get("gui.login.saved.master_pwd.toggle"));
        JButton button = new JButton("OK");
        panel.add(label, "span");
        panel.add(pass, "span");
        panel.add(check);
        panel.add(button);
        button.setEnabled(false);

        check.addItemListener(e -> {
            pass.setEditable(e.getStateChange() != ItemEvent.SELECTED);
            button.setEnabled(pass.getPassword().length > 0 || e.getStateChange() == ItemEvent.SELECTED);
        });
        pass.getDocument().addDocumentListener((GeneralDocumentListener) e -> 
                button.setEnabled(pass.getPassword().length > 0 || check.isSelected()));
        button.addActionListener(e -> SwingUtilities.getWindowAncestor(button).setVisible(false));

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = pane.createDialog(this, I18n.get("gui.login.saved.master_pwd.title"));
        dialog.getRootPane().setDefaultButton(button);
        dialog.setVisible(true);

        if (check.isSelected() || pass.getPassword().length == 0) {
            ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.OTHER.DISABLE_MASTER_PASSWORD = true;
            return new char[]{};
        } else {
            return pass.getPassword();
        }
    }

    @Override
    public LoginForm.Message tryLogin(LoginData login) {
        Credentials.User user = users.getSelectedValue();
        if (user == null) return new LoginForm.Message(true,
                I18n.get("gui.login.error.no_user"), I18n.get("gui.login.error.no_user.desc"));

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
            int result = JOptionPane.showConfirmDialog(this, newUser, I18n.get("gui.login.saved.add_login"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
            int result = JOptionPane.showConfirmDialog(this, editor, I18n.get("gui.login.saved.edit_login"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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