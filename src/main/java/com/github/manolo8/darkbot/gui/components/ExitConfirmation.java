package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitConfirmation extends JPanel {

    public ExitConfirmation() {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[]"));

        add(new Question("Are you sure you want to exit?"), "grow");
        add(new YesButton());
        add(new CancelButton());
        setVisible(false);
        setToolTipText("Confirm exiting the bot, can be disabled in settings > bot settings");
    }

    private static class Question extends JLabel {
        private Question(String text) {
            super(text);
            setBorder(UIUtils.getBorderWithInsets(true));
        }
    }

    private static class YesButton extends MainButton {

        private YesButton() {
            super("Yes");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Exit confirmation button pressed, exiting");
            System.exit(0);
        }
    }

    private class CancelButton extends MainButton {

        private CancelButton() {
            super("Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ExitConfirmation.this.setVisible(false);
        }



    }


}
