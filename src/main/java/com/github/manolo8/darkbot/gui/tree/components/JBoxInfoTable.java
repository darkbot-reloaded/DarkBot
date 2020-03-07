package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;

import javax.swing.*;
import java.util.Arrays;

public class JBoxInfoTable extends InfoTable<GenericTableModel, BoxInfo> implements OptionEditor {

    public JBoxInfoTable(Config.Collect collect) {
        super(BoxInfo.class, collect.BOX_INFOS, collect.ADDED_BOX, BoxInfo::new);

        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING),
                new RowSorter.SortKey(3, SortOrder.ASCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));
    }

}
