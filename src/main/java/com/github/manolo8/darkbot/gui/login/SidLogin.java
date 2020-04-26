package com.github.manolo8.darkbot.gui.login;

import com.github.manolo8.darkbot.utils.login.LoginData;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class SidLogin extends JPanel implements LoginScreen {
    private JTextField sv = new JTextField(4), sid = new JTextField(16);

    public SidLogin() {
        super(new MigLayout("wrap 2, height 48px!", "[]8px:push[]", "push[][]push"));
        add(new JLabel("Server"));
        add(sv);
        add(new JLabel("SID"));
        add(sid);
    }

    @Override
    public LoginForm.Message tryLogin(LoginData login) {
        login.setSid(sid.getText(), sv.getText() + ".darkorbit.com");
        return null;
    }
}
