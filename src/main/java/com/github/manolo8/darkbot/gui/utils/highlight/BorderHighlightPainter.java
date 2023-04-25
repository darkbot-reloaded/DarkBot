package com.github.manolo8.darkbot.gui.utils.highlight;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import java.awt.*;

/**
 * Simple highlight painter that draws a border around the highlight
 */
public class BorderHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

    public BorderHighlightPainter(Color color) {
        super(color);
    }

    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
        g.setColor(getColor());

        Rectangle r;
        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
            // Contained in view, can just use bounds.
            r = bounds instanceof Rectangle ? (Rectangle) bounds : bounds.getBounds();
        } else {
            try {
                Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                r = shape instanceof Rectangle ? (Rectangle) shape : shape.getBounds();
            } catch (BadLocationException e) {
                r = null;
            }
        }

        if (r != null) g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        return r;
    }

}
