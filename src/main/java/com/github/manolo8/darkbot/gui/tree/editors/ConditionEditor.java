package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.config.util.OptionEditor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Comparator;
import java.util.Objects;

public class ConditionEditor extends JTextField implements OptionEditor<Condition>, GeneralDocumentListener {

    private boolean init = false;
    private Condition condition;
    private Object highlight;
    private boolean valid = true;

    private String lastParsed;
    private SyntaxException lastEx;

    private final SyntaxInfo popup = new SyntaxInfo();

    public ConditionEditor() {
        setMargin(new Insets(0, 5, 0, 5));
        this.getDocument().addDocumentListener(this);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!popup.isOpen()) updateDisplay();
            }
        });
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Condition> condition) {
        this.init = false;

        this.condition = condition.getValue();
        setText(Objects.toString(this.condition, ""));
        setColumns(30);
        setEditable(!Boolean.TRUE.equals(condition.getMetadata("readonly")));

        this.init = true;

        return this;
    }

    public void setText(String text) {
        if (Objects.equals(text, getText())) return;
        super.setText(text);
    }

    @Override
    public Condition getEditorValue() {
        return condition;
    }

    @Override
    public boolean stopCellEditing() {
        if (valid) popup.close();
        return valid;
    }

    @Override
    public void cancelCellEditing() {
        popup.close();
    }

    public void update(DocumentEvent e) {
        if (!init) return;
        condition = updateDisplay();
    }

    public Condition updateDisplay() {
        String text = getText();
        if (text == null) text = "";

        // Don't re-parse, use cached exception
        if (text.equals(lastParsed)) {
            handleSyntaxEx(lastEx);
            return condition;
        }

        lastParsed = text;

        try {
            Condition cond = ValueParser.parseCondition(getText());
            handleSyntaxEx(lastEx = null);
            return cond;
        } catch (SyntaxException e) {
            handleSyntaxEx(lastEx = e);
            return null;
        }
    }

    private void handleSyntaxEx(SyntaxException e) {
        if (e == null) {
            setHighlight(0, getText().length(), true);
            popup.update(null, 0, null);
            return;
        }

        int s = getText().lastIndexOf(e.getAt()), start = Math.max(0, s);

        setHighlight(start, getText().length(), getText().isEmpty());

        try {
            Rectangle rect = getUI().modelToView(this, start);
            popup.update(e, start, new Point((int) rect.getX(), (int) rect.getMaxY()));
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }

    private void setHighlight(int start, int end, boolean valid) {
        if (this.valid != valid) {
            this.valid = valid;
            putClientProperty("JComponent.outline", valid ? null : "error");
        }
        if (highlight != null) {
            getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
        if (start < 0 || end <= 0 || !init) return; // No highlight
        try {
            highlight = getHighlighter().addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(
                    valid ? UIUtils.GREEN_HIGHLIGHT : UIUtils.RED_HIGHLIGHT));
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
        private final JLabel expectedLabel = new JLabel("Expected");
        private final JPanel expected = new JPanel(new MigLayout("ins 0px, wrap 6"));
        private final JPanel metadata = new JPanel(new MigLayout("ins 0px, gapy 0", "[grow]15px[grow]15px[grow]", "[]"));

        private SyntaxException lastEx;

        public SyntaxInfo() {
            super(new MigLayout("ins 5px, fillx, gapy 5px, wrap 1", "[grow]15px[grow]15px[grow]push", "[]"));

            popup.setBorder(BorderFactory.createEmptyBorder());
            popup.setFocusable(false);
            popup.add(this);

            setBorder(UIUtils.getBorder());

            add(message, "grow, hidemode 3");
            add(expected, "grow, hidemode 3");
            add(metadata, "grow, hidemode 3");
        }

        public boolean isOpen() {
            return popup.isVisible();
        }

        public void update(SyntaxException syntax, int at, Point loc) {
            // Do nothing on non initialized editor
            if (!init) return;
            if (syntax == null) {
                popup.setVisible(false);
                return;
            } else if (lastEx == syntax) {
                popup.show(ConditionEditor.this, loc.x, loc.y);
                return;
            }
            lastEx = syntax;

            if (syntax.getMessage() != null) message.setText(syntax.getMessage());
            message.setVisible(syntax.getMessage() != null);

            if (syntax.getExpected().length > 0) {
                expected.removeAll();
                expected.add(expectedLabel, "spany, gapright 10px");
                for (String val : syntax.getExpected())
                    expected.add(new InsertButton(val, at, true), "grow");
            }
            expected.setVisible(syntax.getExpected().length > 0);

            if (!syntax.getMetadata().isEmpty()) {
                metadata.removeAll();
                syntax.getMetadata().sort(Comparator.comparing(Values.Meta::getName));
                for (Values.Meta<?> meta : syntax.getMetadata()) {
                    metadata.add(syntax.isSingleMeta() ?
                            new JLabel(meta.getName()) :
                            new InsertButton(meta.getName() + "(", at, false), "grow");
                    metadata.add(new JLabel(meta.getDescription()), "grow");
                    metadata.add(new JLabel(meta.getExample()), "grow, wrap");
                }
            }
            metadata.setVisible(!syntax.getMetadata().isEmpty());

            popup.setVisible(false);
            popup.show(ConditionEditor.this, loc.x, loc.y);
        }

        public void close() {
            popup.setVisible(false);
        }

    }

    private class InsertButton extends JButton {

        public InsertButton(String insert, int at, boolean inline) {
            super(insert);
            // Allow minimum size, otherwise L&F forces big buttons
            putClientProperty("JComponent.minimumWidth", 0);
            setFocusable(false);
            if (!inline) setHorizontalAlignment(SwingConstants.LEFT);

            setMargin(new Insets(2, 3, 2, 3));
            addActionListener(a -> {
                String text = ConditionEditor.this.getText();
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

                ConditionEditor.this.setText(text);
                ConditionEditor.this.setCaretPosition(lastLoc);
                ConditionEditor.this.requestFocus();
            });
        }

    }

}