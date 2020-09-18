package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;

import javax.swing.*;
import java.util.Collection;

public interface ExtraMenuProvider {
    Collection<JComponent> getExtraMenuItems(Main main);
}
