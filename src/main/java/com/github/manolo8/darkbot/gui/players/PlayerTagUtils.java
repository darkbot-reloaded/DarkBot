package com.github.manolo8.darkbot.gui.players;

import com.bulenkov.darcula.ui.DarculaSliderUI;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.GraphicsUtil;
import com.github.manolo8.darkbot.config.PlayerTag;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.stream.IntStream;

public class PlayerTagUtils {

    public static PlayerTag createTag(Component parent) {
        JPanel panel = new JPanel(new MigLayout("ins 0, wrap 2", "[]3px[grow]"));
        JTextField name = new JTextField(20);
        JSlider color = new JSlider(0, 255);
        color.setUI(new PlayerTagUtils.ColoredSliderUI(color, 0.6f, 0.6f));

        panel.add(new JLabel("Tag "));
        panel.add(name, "grow");
        panel.add(new JLabel("Color"));
        panel.add(color, "grow");

        JButton createTag = new JButton("Create Tag");
        createTag.addActionListener(event -> SwingUtilities.getWindowAncestor(panel).setVisible(false));

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new Object[]{createTag}, createTag);
        JDialog dialog = pane.createDialog(parent, "Create Player tag");

        pane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();

        if (pane.getValue() == null || name.getText().trim().isEmpty()) return null;

        return PlayerTag.getTag(name.getText(), Color.getHSBColor(color.getValue() / 255f, 0.6f, 0.6f));
    }

    public static class ColoredSliderUI extends DarculaSliderUI {
        private Color[] gradient;
        public ColoredSliderUI(JSlider slider, float sat, float val) {
            super(slider);
            gradient = IntStream.rangeClosed(0, 255).mapToObj(i -> Color.getHSBColor(i / 255f, sat, val)).toArray(Color[]::new);
        }

        public void paintTrack(Graphics g2d) {
            Graphics2D g = (Graphics2D) g2d;
            Rectangle trackBounds = trackRect;
            final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);

            int height = 8;
            int cy = (trackBounds.height / 2) - height / 2;

            g.translate(trackBounds.x, trackBounds.y + cy);
            for (int i = 0; i < trackRect.width; i++) {
                g.setColor(gradient[(int) (i * 255f / trackRect.width)]);
                g.drawRect(i, 0, 1, height);
            }

            g.translate(-trackBounds.x, -(trackBounds.y + cy));

            config.restore();
        }

        @Override
        public void paintThumb(Graphics g) {
            final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
            Rectangle knobBounds = thumbRect;
            int w = knobBounds.width;
            int h = knobBounds.height;

            g.translate(knobBounds.x, knobBounds.y);

            double r = slider.getOrientation() == JSlider.HORIZONTAL ? h : w;
            g.setColor(getThumbBorderColor());
            ((Graphics2D) g).fill(new Ellipse2D.Double(0, 0, r, r));
            g.setColor(gradient[slider.getValue()]);
            ((Graphics2D) g).fill(new Ellipse2D.Double(1, 1, r - 2, r - 2));

            g.translate(-knobBounds.x, -knobBounds.y);
            config.restore();
        }
    }

}
