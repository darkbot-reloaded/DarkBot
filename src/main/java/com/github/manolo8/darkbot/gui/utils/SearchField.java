package com.github.manolo8.darkbot.gui.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.PatternSyntaxException;

public class SearchField extends JTextField implements GeneralDocumentListener {

    private static final Icon SEARCH_ICON = UIUtils.getIcon("search");
    private static final Border MARGIN_BORDER = new EmptyBorder(0, SEARCH_ICON.getIconWidth(), 0, 0);
    private final BiConsumer<SearchField, String> onChange;

    private boolean valid = true;

    public SearchField(Consumer<String> onTextChange) {
        this((sf, t) -> onTextChange.accept(t));
    }

    public SearchField(BiConsumer<SearchField, String> onChange) {
        this.onChange = onChange;
    }

    public static SearchField forTable(@NotNull TableRowSorter<?> sorter) {
        return forTable(sorter, null);
    }

    public static SearchField forTable(@NotNull TableRowSorter<?> sorter, @Nullable Document document) {
        SearchField field = new SearchField((sf, text) -> {
            try {
                if (sorter instanceof MultiTableRowSorter)
                    ((MultiTableRowSorter<?>) sorter).putRowFilter("search", RowFilter.regexFilter("(?i)" + text, 0));
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
                sf.setValid(true);
            } catch (PatternSyntaxException ex) {
                sf.setValid(false);
            }
        });
        if (document != null) field.setDocument(document);
        else field.update((DocumentEvent) null);
        return field;
    }


    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals("document") && oldValue != newValue) {
            if (oldValue != null) ((Document) oldValue).removeDocumentListener(this);
            if (newValue != null) ((Document) newValue).addDocumentListener(this);
        }
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void update(DocumentEvent e) {
        if (onChange != null) onChange.accept(this, getText());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int margin = (getHeight() - SEARCH_ICON.getIconHeight()) / 2;
        SEARCH_ICON.paintIcon(this, graphics, margin, margin);
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(new CompoundBorder(border, MARGIN_BORDER));
    }

    public void setValid(boolean valid) {
        if (this.valid == valid) return; // No change
        this.valid = valid;
        putClientProperty("JComponent.outline", valid ? null : "error");
    }

}
