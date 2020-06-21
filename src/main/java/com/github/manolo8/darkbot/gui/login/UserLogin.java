package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class UserLogin extends JPanel implements LoginScreen {
    private JTextField username = new JTextField(16), password = new JPasswordField(16);

    public UserLogin() {
        super(new MigLayout("wrap 2", "[]8px:push[]", "push[][]push"));
        add(new JLabel("Username"));
        add(username);
        add(new JLabel("Password"));
        add(password);
    }

    public UserLogin(String username, String password) {
        this();
        this.username.setText(username);
        this.password.setText(password);
    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    @Override
    public LoginForm.Message tryLogin(LoginData login) {
        login.setCredentials(username.getText(), password.getText());
        LoginUtils.usernameLogin(login);
        return null;
    }

}
