package com.github.manolo8.darkbot.gui.utils.highlight;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

public class HighlightHandler {

    private final JTextComponent text;
    private final Highlighter.HighlightPainter painter;
    private Object highlight;

    public HighlightHandler(JTextComponent text, Highlighter.HighlightPainter painter) {
        this.text = text;
        this.painter = painter;
    }

    public void setHighlight(Locatable node) {
        int start = -1, end = -1;
        if (node != null) {
            Position startPos = node.getStart();
            if (startPos != null) start = startPos.getOffset();

            Position endPos = node.getEnd();
            if (node.getEnd() != null) end = endPos.getOffset();
        }
        setHighlight(start, end);
    }

    public void setHighlight(int start, int end) {
        if (start != -1 && end != -1) {
            try {
                if (highlight == null) highlight = text.getHighlighter().addHighlight(start, end, painter);
                else text.getHighlighter().changeHighlight(highlight, start, end);
                return;
            } catch (BadLocationException ignored) {}
        }
        remove();
    }

    public void remove() {
        if (highlight != null) {
            text.getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
    }

}
