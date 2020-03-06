package com.github.manolo8.darkbot.gui.players;

import com.formdev.flatlaf.ui.FlatSliderUI;
import com.formdev.flatlaf.util.UIScale;
import com.github.manolo8.darkbot.config.PlayerTag;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

public class PlayerTagUtils {

    public static PlayerTag createTag(Component parent) {
        JPanel panel = new JPanel(new MigLayout("ins 0, wrap 2", "[]3px[grow]"));
        JTextField name = new JTextField(20);
        JSlider color = new JSlider(0, 255);
        color.setUI(new PlayerTagUtils.ColoredSliderUI(0.6f, 0.6f));

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

    public static class ColoredSliderUI extends FlatSliderUI {
        private static final int TRACK_HEIGHT = 8;
        private static final int THUMB_WIDTH = 20;
        private Color[] gradient;
        public ColoredSliderUI(float sat, float val) {
            gradient = IntStream.rangeClosed(0, 255).mapToObj(i -> Color.getHSBColor(i / 255f, sat, val)).toArray(Color[]::new);
        }

        protected Dimension getThumbSize() {
            return new Dimension(UIScale.scale(THUMB_WIDTH), UIScale.scale(THUMB_WIDTH));
        }

        public void paintTrack(Graphics g2d) {
            int drawY = trackRect.y + (trackRect.height / 2) - TRACK_HEIGHT / 2;
            for (int i = 0; i < trackRect.width; i++) {
                g2d.setColor(gradient[(int) (i * 255f / trackRect.width)]);
                g2d.drawRect(trackRect.x + i, drawY, 1, TRACK_HEIGHT);
            }
        }

        @Override
        public void paintThumb(Graphics g) {
            g.setColor(gradient[slider.getValue()]);
            g.fillOval( thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height );
        }
    }

}
