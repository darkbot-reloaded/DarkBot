package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.utils.login.LoginData;

public interface LoginScreen {
    /**
     * Try to perform login in this login screen
     * @param login The login data to update
     * @return null if it went right, message to display otherwise
     */
    LoginForm.Message tryLogin(LoginData login);
}
