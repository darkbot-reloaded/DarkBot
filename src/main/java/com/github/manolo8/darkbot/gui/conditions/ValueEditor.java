package com.github.manolo8.darkbot.gui.conditions;

import com.github.manolo8.darkbot.config.actions.Value;

import javax.swing.*;

public class ValueEditor<T> extends JPanel {

    private Value<T> value;


    public ValueEditor() {

    }

    public void display(Value<T> condition) {
        removeAll();

    }


}
