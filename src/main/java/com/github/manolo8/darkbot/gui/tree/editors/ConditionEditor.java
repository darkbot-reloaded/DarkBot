package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Comparator;
import java.util.Objects;

public class ConditionEditor extends JTextField implements OptionEditor<Condition> {

    private boolean init = false;
    private Condition field;
    private Object highlight;

    private String lastParsed;
    private SyntaxException lastEx;

    private final SyntaxInfo popup = new SyntaxInfo();

    public ConditionEditor() {
        setMargin(new Insets(0, 5, 0, 5));
        this.getDocument().addDocumentListener((GeneralDocumentListener) e -> {
            if (init) {
                Condition val = updateDisplay();
                if (val != null || (getText() != null && getText().isEmpty())) field = val;
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!popup.isOpen()) updateDisplay();
            }
        });
    }

    @Override
    public JComponent getEditorComponent(Condition value, ValueHandler<Condition> handler) {
        this.init = false;

        this.field = value;
        setText(Objects.toString(value, ""));
        setColumns(30);

        this.init = true;

        return this;
    }

    @Override
    public Condition getEditorValue() {
        return field;
    }

    public Condition updateDisplay() {
        if (highlight != null) {
            getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
        if (getText() == null) return null;

        if (getText().equals(lastParsed)) {
            handleSyntaxEx(lastEx);
            return null;
        }

        lastParsed = getText();

        try {
            Condition cond = ValueParser.parseCondition(getText());
            handleSyntaxEx(lastEx = null);
            cond.get(HeroManager.instance.main);
            return cond;
        } catch (SyntaxException e) {
            handleSyntaxEx(lastEx = e);
            return null;
        }
    }

    private void handleSyntaxEx(SyntaxException e) {
        if (e == null) {
            setHighlight(0, getText().length(), UIUtils.GREEN_HIGHLIGHT);
            popup.update(null, 0, null);
            return;
        }

        int s = getText().lastIndexOf(e.getAt()), start = Math.max(0, s);

        setHighlight(start, getText().length(), UIUtils.RED_HIGHLIGHT);

        try {
            Rectangle rect = getUI().modelToView(this, start);
            popup.update(e, start, new Point((int) rect.getX(), (int) rect.getMaxY()));
        } catch (BadLocationException ble) {
            ble.printStackTrace();
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

    }

    private class InsertButton extends JButton {

        public InsertButton(String insert, int at, boolean inline) {
            super(insert);
            // Allow minimum size, otherwise L&F forces big buttons
            putClientProperty("JComponent.minimumWidth", 0);
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