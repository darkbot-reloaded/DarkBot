package com.github.manolo8.darkbot.gui.utils;

import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.awt.*;

/**
 * @deprecated Migrate to eu.darkbot.util.Popups
 */
@Deprecated
public class Popups {

    public static void showMessageAsync(String title, String content, int type) {
        of(title, content).messageType(type).showAsync();
    }

    public static void showMessageAsync(String title, Object content, int type) {
        of(title, content).messageType(type).showAsync();
    }

    public static void showMessageAsync(String title, JOptionPane pane) {
        of(title, pane).showAsync();
    }

    public static void showMessageSync(String title, JOptionPane pane) {
        of(title, pane).showSync();
    }

    public static void showMessageSync(Component parent, String title, JOptionPane pane) {
        of(title, pane).parent(parent).showSync();
    }

    public static Builder of(String title, Object message) {
        return (Builder) new Builder().title(title).message(message);
    }

    public static Builder of(String title, Object message, @MagicConstant(intValues = {
            JOptionPane.ERROR_MESSAGE,
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.WARNING_MESSAGE,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.PLAIN_MESSAGE}) int messageType) {
        return (Builder) new Builder().title(title).message(message).messageType(messageType);
    }

    public static Builder of(String title, JOptionPane pane) {
        return (Builder) new Builder(pane).title(title);
    }

    public static class Builder extends eu.darkbot.util.Popups.Builder {
        public Builder() {
            super();
        }

        public Builder(JOptionPane pane) {
            super(pane);
        }
    }


}
