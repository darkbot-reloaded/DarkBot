package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.ToolTipHeader;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.table.TableCharEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableCharRenderer;
import com.github.manolo8.darkbot.gui.utils.table.TableDoubleEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableDoubleRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Deprecated
public abstract class InfoTable<T extends TableModel, E> extends JTable implements OptionEditor {
    private final JComponent component;
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

        setDefaultRenderer(Double.class, new TableDoubleRenderer());
        setDefaultRenderer(Character.class, new TableCharRenderer());

        setDefaultEditor(Double.class, new TableDoubleEditor());
        setDefaultEditor(Character.class, new TableCharEditor());

        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        if (model instanceof GenericTableModel) {
            setTableHeader(new ToolTipHeader(getColumnModel(), (GenericTableModel<?>) model));
        }
        getTableHeader().setReorderingAllowed(false);

        MultiTableRowSorter<T> sorter = new MultiTableRowSorter<>(model);
        setRowSorter(sorter);

        component = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][][]", "[][grow]"));

        component.add(SearchField.forTable(sorter), "grow, cell 0 0");
        component.add(new JScrollPane(this), "grow, span, cell 0 1");

        if (data != null && listener != null) {
            this.data = data;
            this.listener = listener;
            if (supplier != null) {
                this.supplier = supplier;
                getComponent().add(addButton(), "cell 2 0");
                getComponent().add(removeButton(), "cell 3 0");
            }
        }

        component.setPreferredSize(new Dimension(500, 270));
    }

    protected MainButton addButton() {
        return new AddButton();
    }

    protected MainButton removeButton() {
        return new RemoveButton();
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
                Popups.of("Error", "Name already in use", JOptionPane.ERROR_MESSAGE).showAsync();
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
