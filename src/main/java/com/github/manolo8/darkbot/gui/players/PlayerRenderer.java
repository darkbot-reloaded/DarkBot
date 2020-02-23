package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.PlayerTag;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerRenderer extends JPanel implements ListCellRenderer<PlayerInfo> {

    private JLabel playername = new JLabel();
    private JLabel id = new JLabel();

    private Map<PlayerTag, Tag> tagCache = new HashMap<>();

    public PlayerRenderer() {
        super(new MigLayout("ins 4px 0px 4px 5px, fill, h 28px!", "[50px!]8px![120px!]8px:push[]8px!", "[]"));
        id.setFont(id.getFont().deriveFont(9f));
        id.setHorizontalAlignment(SwingConstants.RIGHT);
        id.setVerticalAlignment(SwingConstants.BOTTOM);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends PlayerInfo> list, PlayerInfo value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        removeAll();

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

        this.playername.setText(value.username);
        this.id.setText(String.valueOf(value.userId));

        add(id, "grow");
        add(playername, "grow");
        for (PlayerTag tag : value.getTags())
            add(tagCache.computeIfAbsent(tag, Tag::new), "grow");

        return this;
    }

    private static class Tag extends JLabel {
        private static final int ALPHA = 96 << 24;
        private static final Border MARGIN = new EmptyBorder(1, 5, 1, 5);

        public Tag(PlayerTag tag) {
            super(tag.name);
            setOpaque(true);
            setBorder(new CompoundBorder(BorderFactory.createLineBorder(tag.color), MARGIN));
            setBackground(new Color(tag.color.getRGB() + ALPHA, true));
        }
    }

    /**
     * Functions overridden for performance reasons:
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    /*
    // We can't override these or the tags start bugging due to layout manager.
    @Override
    public void validate() {}
    @Override
    public void invalidate() {}*/
    @Override
    public void repaint() {}
    @Override
    public void revalidate() {}
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}
    @Override
    public void repaint(Rectangle r) {}
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName == "text"
                || ((propertyName == "font" || propertyName == "foreground")
                && oldValue != newValue
                && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
