package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.Credentials;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;

public class AutoLogin implements LoginScreen {
    public AutoLoginForm loginForm;

    private StartupParams params;
    private char[] password;
    private final Credentials credentials = LoginUtils.loadCredentials();

    public AutoLogin(AutoLoginForm loginForm, StartupParams params) {
        this.loginForm = loginForm;
        this.params = params;

        try {
            if (!credentials.isEmpty()) {
                password = params.getMasterPassword();
                credentials.decrypt(password);
            }
        } catch (Exception e) {
            loginForm.setInfoText(new LoginForm.Message(true, "Couldn't login, check your master password txt file", IssueHandler.createDescription(e)));
        }
    }

    @Override
    public LoginForm.Message tryLogin(LoginData login) {
        Credentials.User user = credentials.getUsers().stream()
                .filter(usr -> usr.u.equals(params.getUsername()))
                .findFirst()
                .orElse(null);
        if (user == null) return new LoginForm.Message(true, "Incorrect program arguments", "Please make sure you entered the correct username and filepath");

        login.setCredentials(user.u, user.p);
        LoginUtils.usernameLogin(login);
        return null;
    }
}
