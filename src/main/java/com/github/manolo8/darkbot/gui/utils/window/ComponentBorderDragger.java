package com.github.manolo8.darkbot.gui.utils.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class ComponentBorderDragger extends MouseAdapter {

    private JFrame frame;

    private byte direction;
    protected static final byte NORTH = 1;
    protected static final byte WEST = 2;
    protected static final byte SOUTH = 4;
    protected static final byte EAST = 8;

    private Cursor sourceCursor;

    private static Map<Byte, Integer> cursors = new HashMap<>();

    static {
        cursors.put((byte) 1, Cursor.N_RESIZE_CURSOR);
        cursors.put((byte) 2, Cursor.W_RESIZE_CURSOR);
        cursors.put((byte) 4, Cursor.S_RESIZE_CURSOR);
        cursors.put((byte) 8, Cursor.E_RESIZE_CURSOR);
        cursors.put((byte) 3, Cursor.NW_RESIZE_CURSOR);
        cursors.put((byte) 9, Cursor.NE_RESIZE_CURSOR);
        cursors.put((byte) 6, Cursor.SW_RESIZE_CURSOR);
        cursors.put((byte) 12, Cursor.SE_RESIZE_CURSOR);
    }

    private Insets dragInsets;
    private Dimension minSize;

    private Point basePoint;

    public ComponentBorderDragger(JFrame frame, Insets dragInsets,
                                  Dimension minSize) {
        super();
        this.frame = frame;
        this.dragInsets = dragInsets;
        this.minSize = minSize;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (direction == 0) {
            return;
        }

        Point newPoint = e.getPoint();
        int x, y, width, height, newBasePointX, newBasePointY;
        x = frame.getX();
        y = frame.getY();
        width = frame.getWidth();
        height = frame.getHeight();
        newBasePointX = newPoint.x;
        newBasePointY = newPoint.y;

        if ((direction & EAST) == EAST) {
            int newWidth;
            newWidth = Math.max(minSize.width, width + newPoint.x
                    - basePoint.x);
            width = newWidth;
        }
        if ((direction & SOUTH) == SOUTH) {
            int novoAlto;
            novoAlto = Math.max(minSize.height, height + newPoint.y
                    - basePoint.y);
            height = novoAlto;
        }
        if ((direction & WEST) == WEST) {
            int newWidth, newX;
            newWidth = Math.max(minSize.width, width - newPoint.x
                    + basePoint.x);
            newX = Math.min(x + width - minSize.width, x + newPoint.x
                    - basePoint.x);

            // Changing coordenates of new base point to refer to the new component position
            newBasePointX -= newX - x;
            x = newX;
            width = newWidth;
        }
        if ((direction & NORTH) == NORTH) {
            int newHeigth, newY;
            newHeigth = Math.max(minSize.height, height - newPoint.y
                    + basePoint.y);
            newY = Math.min(y + height - minSize.height, y + newPoint.y
                    - basePoint.y);
            // Changing coordenates of new base point to refer to the new component position
            newBasePointY -= newY - y;
            y = newY;
            height = newHeigth;
        }
        frame.setBounds(x, y, width, height);
        basePoint = new Point(newBasePointX, newBasePointY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Component originator = e.getComponent();
        if (direction == 0) {
            sourceCursor = originator.getCursor();
        }
        calculateDirection(e.getPoint(), e.getComponent().getSize());
        setCursor(e.getComponent());
        basePoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().validate();
            frame.getContentPane().repaint();
        });
    }

    private void setCursor(Component component) {
        if (direction == 0) {
            component.setCursor(sourceCursor);
        } else {
            int cursorType = cursors.get(direction);
            Cursor cursor = Cursor.getPredefinedCursor(cursorType);
            component.setCursor(cursor);
        }
    }

    private void calculateDirection(Point point, Dimension componentSize) {
        this.direction = calculateDirection(point, componentSize, 2);
    }

    private byte calculateDirection(Point point, Dimension componentSize, double leniency) {
        byte direction = 0;
        if (point.x < dragInsets.left * leniency) direction |= WEST;
        if (point.y < dragInsets.top * leniency) direction |= NORTH;
        if (point.x > componentSize.width - dragInsets.right * leniency) direction |= EAST;
        if (point.y > componentSize.height - dragInsets.bottom * leniency) direction |= SOUTH;
        return direction;
    }
}