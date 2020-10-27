package com.github.manolo8.darkbot.gui.utils.window;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;

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
            Config.BotSettings.Window frame = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.MAIN_GUI_WINDOW;
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension sizeIn = mainFrame.getSize();
                resizeFrame.setSize(sizeIn);
                mainPanel.setSize(sizeIn);
                frame.width = mainFrame.getWidth();
                frame.height = mainFrame.getHeight();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                setMaximizedInsets(mainFrame);
                frame.x = mainFrame.getX();
                frame.y = mainFrame.getY();
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

}
