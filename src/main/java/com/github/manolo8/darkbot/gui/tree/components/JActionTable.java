package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.ActionInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;

import javax.swing.*;
import java.util.Arrays;

@Deprecated
public class JActionTable extends InfoTable<GenericTableModel, ActionInfo> implements OptionEditor {

    public JActionTable(Config.ExtraActions extra) {
        super(ActionInfo.class, extra.ACTION_INFOS, extra.MODIFIED_ACTIONS, ActionInfo::new);

        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));
    }

}

