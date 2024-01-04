package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.login.LoginData;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class SidLogin extends JPanel implements LoginScreen {
    private final JTextField sv = new JTextField(4), sid = new JTextField(16);

    public SidLogin() {
        super(new MigLayout("wrap 2", "[]8px:push[]", "push[][]push"));
        add(new JLabel(I18n.get("gui.login.sid.server")));
        add(sv);
        add(new JLabel(I18n.get("gui.login.sid.sid")));
        add(sid);
    }

    @Override
    public LoginForm.Message tryLogin(LoginData login, Consumer<LoginForm.Message> publish) {
        login.setSid(sid.getText(), sv.getText() + ".darkorbit.com");
        return null;
    }
}
