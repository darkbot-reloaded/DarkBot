package com.github.manolo8.darkbot;

import com.bulenkov.darcula.DarculaLaf;
import com.github.manolo8.darkbot.utils.AuthAPI;

import javax.swing.*;

public class Bot {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        AuthAPI.getInstance().setupAuth();
        new Main();
    }
}
