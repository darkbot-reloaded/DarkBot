package com.github.manolo8.darkbot.gui.tree.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.PluginAPI;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class NpcTableModel extends GenericTableModel<NpcInfo> implements Consumer<Boolean> {

    private boolean grouped = true;

    public NpcTableModel(PluginAPI api) {
        super(api, null, NpcInfo.class);
        rebuildTable();
    }

    @Override
    public void accept(Boolean grouped) {
        if (this.grouped == grouped) return;
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> accept(grouped));
            return;
        }
        this.grouped = grouped;
        rebuildTable();
    }

    @Override
    protected Row<NpcInfo> createRow(String name, NpcInfo data) {
        return new NpcTableModel.NpcRow(name, data);
    }

    @Override
    public String toTableName(String name) {
        return grouped ? Strings.simplifyName(name) : name;
    }

    @Override
    protected void setValue(Row<NpcInfo> row, Column column, Object value) {
        Field field = column.field;
        if (field == null) throw new UnsupportedOperationException("Can't edit default column");

        if (value instanceof NpcInfo.ExtraNpcInfo) {
            NpcInfo.ExtraNpcInfo extra = (NpcInfo.ExtraNpcInfo) value;

            for (NpcInfo info : ((NpcTableModel.NpcRow) row).getInfos())
                info.extra.copy(extra);
        } else {
            for (NpcInfo info : ((NpcTableModel.NpcRow) row).getInfos())
                ReflectionUtils.set(field, info, value);
        }

        ConfigEntity.changed();
    }

    public static class NpcRow extends GenericTableModel.Row<NpcInfo> {

        private Collection<NpcInfo> infos;

        public NpcRow(String name, NpcInfo data) {
            super(name, data);
            this.infos = Collections.singleton(data);
        }

        @Override
        public NpcTableModel.NpcRow update(NpcInfo info) {
            if (infos.contains(info)) return this;

            if (infos instanceof Set) infos = new ArrayList<>(infos);

            info.copyOf(data);
            infos.add(info);
            return this;
        }

        public Collection<NpcInfo> getInfos() {
            return this.infos;
        }

        public NpcInfo getInfo() {
            return this.data;
        }
    }
}
