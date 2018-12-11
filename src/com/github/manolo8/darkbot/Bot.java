package com.github.manolo8.darkbot;

import com.github.manolo8.darkbot.core.def.WindowsAPI;
import com.github.manolo8.darkbot.core.objects.Vector;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Bot {

    private static User32 user32 = User32.INSTANCE;
    private static Kernel32 kernel32 = Kernel32.INSTANCE;

    public static void main(String[] args) throws IOException {
//
//        WinDef.HWND browser = user32.FindWindow(null, "BotBrowser");
//        WinDef.HWND flash = user32.FindWindowEx(browser, null, null, "");
//        flash = user32.FindWindowEx(flash, null, null, "");
//        flash = user32.FindWindowEx(flash, null, null, "");
//        flash = user32.FindWindowEx(flash, null, null, "");
//        flash = user32.FindWindowEx(flash, null, null, "");
//
//        char[] buffer = new char[128];
//
//        user32.GetClassName(flash, buffer, 128);
////MacromediaFlashPlayerActiveX
//        System.out.println(String.valueOf(buffer));

//
//        WindowsAPI api = new WindowsAPI();
//
//        api.attachToWindow();
//
//        Main.API = api;
//
//        System.out.println(api.readMemoryString(4817024756200L));



//        JFrame jFrame = new JFrame();
//
//        jFrame.add(new JPanel() {
//            @Override
//            public void paint(Graphics g) {
//                final BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
//                image.setRGB(0, 0, 500, 500, api.pixels(0, 0, 500, 500), 0, 500);
//                g.drawImage(image, 0, 0, null);
//            }
//        });
//
//        jFrame.setSize(640, 480);
//        jFrame.setVisible(true);
//        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//
//        while (true) {
//            jFrame.repaint();
//        }


        //
//        Main.API = api;
//
//        BotManager botManager = new BotManager();
//
//        botManager.install();
//
//        List<Long> values = api.queryMemory(botManager.mainAddress);
//
//        for (long value : values) {
//
//            if (api.readMemoryInt(value - 28) == BotManager.SEP) {
//
//                long petHandler = api.readMemoryLong(value + 16);
//
//                System.out.println(petHandler);
//            }
//
//        }
//
//        //23AE7D8D0A0
//        //2452021104800

        new Main();
    }
}
