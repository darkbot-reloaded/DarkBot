package com.github.manolo8.darkbot;

import com.bulenkov.darcula.DarculaLaf;
import com.github.manolo8.darkbot.extensions.util.VerifierChecker;

import javax.swing.*;

public class Bot {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        VerifierChecker.getAuthApi().setupAuth();
        new Main();
    }
}
