package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.TableCharEditor;
import com.github.manolo8.darkbot.gui.utils.ToolTipHeader;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class InfoTable<T extends TableModel, E> extends JTable implements OptionEditor {
    private static final TableCharEditor CHAR_EDITOR = new TableCharEditor();

    private JComponent component;
    private Map<String, E> data;
    private Lazy<String> listener;
    private Supplier<E> supplier;

    /**
     * Creates a simple table from the model
     * @param model The model to use
     */
    public InfoTable(T model) {
        this(model, null, null, null);
    }

    /**
     * Creates a simple table from the data
     * @param clazz The type of elem
     * @param data The data that this table uses.
     */
    public InfoTable(Class<E> clazz, Map<String, E> data) {
        this(clazz, data, null, null);
    }

    /**
     * Creates a generic info table with a model backed by the data
     * @param clazz The type of element the table holds
     * @param data The data that this table uses.
     * @param listener Nullable, the listener to send modifications to. No Add/remove button if null.
     * @param supplier Nullable, the supplier for new instances of elements. No add button if null.
     */
    public InfoTable(Class<E> clazz, Map<String, E> data, Lazy<String> listener, Supplier<E> supplier) {
        this((T) new GenericTableModel<>(clazz, data, listener), data, listener, supplier);
    }

    /**
     * Creates an info table with the model & data provided
     * @param model The model to use
     * @param data Nullable, the data that this table uses. No Add/remove button if null.
     * @param listener Nullable, the listener to send modifications to. No Add/remove button if null.
     * @param supplier Nullable, the supplier for new instances of elements. No add button if null.
     */
    public InfoTable(T model, Map<String, E> data, Lazy<String> listener, Supplier<E> supplier) {
        super(model);
        getColumnModel().getColumn(0).setPreferredWidth(200);
        setDefaultEditor(Character.class, CHAR_EDITOR);

        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        if (model instanceof GenericTableModel) {
            setTableHeader(new ToolTipHeader(getColumnModel(), (GenericTableModel) model));
        }
        getTableHeader().setReorderingAllowed(false);

        TableRowSorter<T> sorter = new TableRowSorter<>((T) getModel());
        setRowSorter(sorter);

        component = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][][]", "[][grow]"));

        component.add(new JSearchField<>(sorter, extraFilters()), "grow, cell 0 0");
        component.add(new JScrollPane(this), "grow, span, cell 0 1");

        if (data != null && listener != null) {
            this.data = data;
            this.listener = listener;
            if (supplier != null) {
                this.supplier = supplier;
                getComponent().add(new AddButton(), "cell 2 0");
                getComponent().add(new RemoveButton(), "cell 3 0");
            }
        }

        component.setPreferredSize(new Dimension(550, 270));
    }

    protected RowFilter<T, Integer> extraFilters() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void edit(ConfigField ignore) {}

    private class AddButton extends MainButton {
        AddButton() {
            super(UIUtils.getIcon("add"));
            super.actionColor = UIUtils.GREEN;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(InfoTable.this.getComponent(), "Name (must be unique):", "New element", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            if (data.containsKey(name)) {
                Popups.showMessageAsync("Error", "Name already in use", JOptionPane.ERROR_MESSAGE);
                return;
            }
            data.put(name, supplier.get());
            listener.send(name);
        }
    }

    private class RemoveButton extends MainButton {
        RemoveButton() {
            super(UIUtils.getIcon("remove"));
            super.actionColor = UIUtils.RED;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> toRemove = Arrays.stream(InfoTable.super.getSelectedRows())
                    .mapToObj(row -> (String) getValueAt(row, 0))
                    .peek(data::remove)
                    .collect(Collectors.toList());
            toRemove.forEach(listener::send);
        }
    }

}
