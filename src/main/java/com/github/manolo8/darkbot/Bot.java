package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Bot {

    public static void main(String[] args) throws FileNotFoundException {
        if (System.console() == null
                && Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar")) {
            PrintStream output = new PrintStream(new FileOutputStream("logs/" + Time.filenameFriendly() + ".log"));
            System.setOut(new Time.PrintStreamWithDate(output));
            System.setErr(output);
        }
        try {
            UIManager.getFont("Label.font"); // Prevents a linux crash
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 0 );
            UIManager.put("Component.arc", 0 );
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new Main();
    }
}
