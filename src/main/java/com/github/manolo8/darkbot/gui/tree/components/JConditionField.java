package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Comparator;
import java.util.Objects;

public class JConditionField extends JTextField implements OptionEditor {

    private ConfigField field;
    private Object highlight;

    private final SyntaxInfo popup = new SyntaxInfo();

    public JConditionField() {
        setMargin(new Insets(0, 5, 0, 5));
        this.getDocument().addDocumentListener((GeneralDocumentListener) e -> {
            if (field != null) {
                Condition val = updateDisplay();
                if (val != null || (getText() != null && getText().isEmpty())) field.set(val);
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field != null && !popup.isOpen()) updateDisplay();
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        setText(Objects.toString(field.get(), ""));
        setColumns(30);
        this.field = field;
    }

    public Condition updateDisplay() {
        if (highlight != null) {
            getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
        if (getText() == null) return null;

        try {
            Condition cond = ValueParser.parseCondition(getText());
            setHighlight(0, getText().length(), UIUtils.GREEN_HIGHLIGHT);
            popup.update(null, 0, null);
            return cond;
        } catch (SyntaxException e) {
            int s = getText().lastIndexOf(e.getAt()), start = Math.max(0, s);

            setHighlight(start, getText().length(), UIUtils.RED_HIGHLIGHT);

            try {
                Rectangle rect = getUI().modelToView(this, start);
                popup.update(e, start, new Point((int) rect.getX(), (int) rect.getMaxY()));
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
            return null;
        }
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
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    private class SyntaxInfo extends JPanel {

        private final JPopupMenu popup = new JPopupMenu();
        private final JLabel message = new JLabel();
        private final JPanel expected = new JPanel();

        public SyntaxInfo() {
            super(new MigLayout("ins 0px 5px, fillx, gapy 0", "[grow]15px[grow]15px[grow]", "[]"));

            popup.setBorder(BorderFactory.createEmptyBorder());
            popup.setFocusable(false);
            popup.add(this);

            setBorder(UIUtils.getBorder());
        }

        public boolean isOpen() {
            return popup.isVisible();
        }

        public void update(SyntaxException syntax, int at, Point loc) {
            if (syntax == null) {
                popup.setVisible(false);
                return;
            }

            setLayout(new MigLayout("ins 0px 5px, fillx, gapy 0", "[grow]15px[grow]15px[grow]", "[]"));
            removeAll();

            if (syntax.getMessage() != null) {
                message.setText(syntax.getMessage());
                add(message, "gap 5px 5px 5px 0px, dock north, spanx, align left");
            }

            if (syntax.getExpected().length > 0) {
                expected.removeAll();
                expected.setLayout(new FlowLayout(FlowLayout.LEFT));
                expected.add(new JLabel("Expected: "));
                for (String val : syntax.getExpected())
                    expected.add(new InsertButton(val, at, true));

                add(expected, "dock north, align left");
            }

            syntax.getMetadata().sort(Comparator.comparing(Values.Meta::getName));
            for (Values.Meta<?> meta : syntax.getMetadata()) {
                add(syntax.isSingleMeta() ?
                        new JLabel(meta.getName()) :
                        new InsertButton(meta.getName() + "(", at, false), "grow");
                add(new JLabel(meta.getDescription()), "grow");
                add(new JLabel(meta.getExample()), "grow, wrap");
            }

            popup.setVisible(false);
            popup.show(JConditionField.this, loc.x, loc.y);
        }

    }

    private class InsertButton extends JButton {

        public InsertButton(String insert, int at, boolean inline) {
            super(insert);
            // Allow minimum size, otherwise L&F forces big buttons
            putClientProperty("JComponent.minimumWidth", 0);
            if (!inline) setHorizontalAlignment(SwingConstants.LEFT);

            setMargin(new Insets(2, 3, 2, 3));
            addActionListener(a -> {
                String text = JConditionField.this.getText();
                text = text.substring(0, at) + insert + text.substring(at);

                int lastLoc = at + insert.length();

                for (int i = 0; i < 100; i++) {
                    try {
                        ValueParser.parseCondition(text);
                    } catch (SyntaxException e) {
                        lastLoc = text.lastIndexOf(e.getAt());
                        if (e.getExpected().length != 1) break;
                        text = text.substring(0, lastLoc) + e.getExpected()[0] + text.substring(lastLoc);
                        lastLoc += e.getExpected()[0].length();
                    }
                }

                JConditionField.this.setText(text);
                JConditionField.this.setCaretPosition(lastLoc);
                JConditionField.this.requestFocus();
            });
        }

    }

}
