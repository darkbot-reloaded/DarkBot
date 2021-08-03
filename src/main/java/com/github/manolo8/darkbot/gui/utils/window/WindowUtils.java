package com.github.manolo8.darkbot.gui.utils.window;

import com.github.manolo8.darkbot.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WindowUtils {

    public static void setupUndecorated(JFrame mainFrame, JPanel mainPanel) {
        mainFrame.setUndecorated(true);
        mainFrame.setLayout(null);

        FrameResize resizeFrame = new FrameResize(mainFrame, new Insets(5, 5, 5, 5));
        mainFrame.add(resizeFrame);
        mainFrame.add(mainPanel);

        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension sizeIn = mainFrame.getSize();
                resizeFrame.setSize(sizeIn);
                mainPanel.setSize(sizeIn);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                setMaximizedInsets(mainFrame);
            }
        });
        Dimension sizeIn = mainFrame.getSize();
        resizeFrame.setSize(sizeIn);
        mainPanel.setSize(sizeIn);
        setMaximizedInsets(mainFrame);

        mainFrame.addWindowStateListener(e -> RepaintManager.currentManager(mainFrame).addInvalidComponent(mainFrame.getRootPane()));
    }

    static boolean isMaximized(JFrame frame) {
        return (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
    }

    public static void toggleMaximized(JFrame frame) {
        setMaximized(frame, !isMaximized(frame));
    }

    public static void setMaximized(JFrame frame, boolean maximized) {
        if (maximized == isMaximized(frame)) return;
        setMaximizedInsets(frame);
        if (maximized) frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        else frame.setExtendedState(frame.getExtendedState() & ~Frame.MAXIMIZED_BOTH);
    }

    /**
     * Sets maximized bounds according to the display screen insets.
     */
    private static void setMaximizedInsets(JFrame frame) {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        Rectangle screenBounds = gc.getBounds();
        screenBounds.x = 0;
        screenBounds.y = 0;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        frame.setMaximizedBounds(new Rectangle((screenBounds.x + screenInsets.left),
                (screenBounds.y + screenInsets.top),
                screenBounds.width - ((screenInsets.left + screenInsets.right)),
                screenBounds.height - ((screenInsets.top + screenInsets.bottom))));
    }


    public static final int DEFAULT_WIDTH = 640, DEFAULT_HEIGHT = 480;

    public static void setWindowSize(JFrame frame, boolean saved, Config.BotSettings.BotGui.WindowPosition pos) {
        if (!saved || isOutsideScreen(pos)) {
            frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            frame.setLocationRelativeTo(null);
        } else {
            frame.setSize(pos.width, pos.height);
            frame.setLocation(pos.x, pos.y);
        }

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pos.width = frame.getWidth();
                pos.height = frame.getHeight();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                pos.x = frame.getX();
                pos.y = frame.getY();
            }
        });
    }

    // https://stackoverflow.com/a/39776624
    private static boolean isOutsideScreen(Config.BotSettings.BotGui.WindowPosition window) {
        Rectangle rec = new Rectangle(window.x, window.y, window.width, window.height);
        int windowArea = rec.width * rec.height;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds;
        int boundsArea = 0;

        for (GraphicsDevice gd : ge.getScreenDevices()) {
            bounds = gd.getDefaultConfiguration().getBounds();
            if (bounds.intersects(rec)) {
                bounds = bounds.intersection(rec);
                boundsArea = boundsArea + (bounds.width * bounds.height);
            }
        }
        return boundsArea != windowArea;
    }

}
