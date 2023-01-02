package com.github.manolo8.darkbot.gui.utils;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ButtonListRenderer extends JPanel implements ListCellRenderer<String>, SimpleMouseListener {
    private final Predicate<Integer> showButton;
    private final Consumer<Integer> buttonClick;

    private final JLabel name = new JLabelField();
    private final JButton button;

    private int rolloverIndex = -1;

    public boolean isOnButton() {
        return rolloverIndex != -1;
    }

    public ButtonListRenderer(Icon icon, Consumer<Integer> buttonClick, Predicate<Integer> showButton) {
        super(new BorderLayout());

        this.showButton = showButton;
        this.buttonClick = buttonClick;

        button = new JButton(icon);
        button.setFocusable(false);
        button.setRolloverEnabled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(24, 24));
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);

        setPreferredSize(new Dimension(0, 24));
        setMaximumSize(new Dimension(300, 24));
        name.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        add(button, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        name.setText(value);
        if (index < 0) return name;

        boolean isRollover = index == rolloverIndex;
        setBackground(isSelected && !isRollover ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected && !isRollover ? list.getSelectionForeground() : list.getForeground());
        name.setOpaque(false);

        button.setVisible(showButton.test(index));
        button.getModel().setRollover(index == rolloverIndex);
        button.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

        add(name);
        return this;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        int oldRollover = rolloverIndex;
        rolloverIndex = getButton(list, pt, index) != null ? index : -1;
        if (oldRollover != rolloverIndex) list.repaint(); // repaint all cells
    }

    @Override
    public void mousePressed(MouseEvent e) {
        e.getComponent().repaint(); // repaint all cells
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (button != null) {
                e.consume();
                buttonClick.accept(index);
            }
        }
        rolloverIndex = -1;
        list.repaint(); // repaint all cells
    }

    @Override
    public void mouseExited(MouseEvent e) {
        rolloverIndex = -1;
    }

    private <E> JButton getButton(JList<E> list, Point pt, int index) {
        E proto = list.getPrototypeCellValue();
        Component c = list.getCellRenderer().getListCellRendererComponent(list, proto, index, false, false);
        Rectangle r = list.getCellBounds(index, index);
        c.setBounds(r);
        pt.translate(-r.x, -r.y);
        return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
                .filter(JButton.class::isInstance).map(JButton.class::cast).orElse(null);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
    @Override
    public void repaint() {}
    @Override
    public void revalidate() {}
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}
    @Override
    public void repaint(Rectangle r) {}
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
