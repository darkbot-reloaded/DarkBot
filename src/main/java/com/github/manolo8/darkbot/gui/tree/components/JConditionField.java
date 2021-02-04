package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueParser;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class JConditionField extends JTextField implements OptionEditor {

    private ConfigField field;
    private Object highlight;
    private Point popup;
    private String message;

    public JConditionField() {
        this.getDocument().addDocumentListener((GeneralDocumentListener) e ->  {
            if (field != null) {
                Condition val = getValue();
                if (val != null) field.set(val);
            }
        });
        ((DefaultHighlighter) getHighlighter()).setDrawsLayeredHighlights(true);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        setText(Objects.toString(field.get(), ""));
        getValue();
        setColumns(40);
        this.field = field;
    }

    public Condition getValue() {
        if (getText() == null) return null;
        clearHighlight();

        Condition cond = null;
        try {
            cond = ValueParser.parseCondition(getText());
            setHighlight(0, getText().length(),UIUtils.GREEN_HIGHLIGHT);

        } catch (SyntaxException e) {
            int start = e.getStart(getText());
            if (start < 0) start = 0;

            setHighlight(start, getText().length(),UIUtils.RED_HIGHLIGHT);

            message = e.getMessage();
            if (e.getExpected() != null) message += "\nExpected: " + e.getExpected();

            try {
                Rectangle rect = getUI().modelToView(this, start);
                popup = new Point((int) rect.getX(), (int) rect.getMaxY());
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }

        if (field != null) showTooltip(this);

        return cond;
    }

    private void setHighlight(int start, int end, Color color) {
        if (highlight != null) getHighlighter().removeHighlight(highlight);
        try {
            highlight = getHighlighter().addHighlight(start, end,
                    new DefaultHighlighter.DefaultHighlightPainter(color));
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        if (popup != null) return popup;
        return super.getToolTipLocation(event);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (message != null) return message;
        return super.getToolTipText(event);
    }

    private void showTooltip(Component component) {
        final ToolTipManager ttm = ToolTipManager.sharedInstance();
        final int oldDelay = ttm.getInitialDelay();
        ttm.setInitialDelay(0);
        ttm.mouseMoved(new MouseEvent(component, 0, 0, 0,
                0, 0, // X-Y of the mouse for the tool tip
                0, false));
        SwingUtilities.invokeLater(() -> ttm.setInitialDelay(oldDelay));
    }

    private void clearHighlight() {
        popup = null;
        message = null;
        if (highlight != null) {
            getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
    }

}
