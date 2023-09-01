package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.utils.login.LoginData;

import java.util.function.Consumer;

public interface LoginScreen {
    /**
     * Try to perform login in this login screen
     * @param login The login data to update
     * @param publish A way to publish intermediate messages during potentially long operations
     * @return null if it went right, message to display otherwise
     */
    LoginForm.Message tryLogin(LoginData login, Consumer<LoginForm.Message> publish);
}
