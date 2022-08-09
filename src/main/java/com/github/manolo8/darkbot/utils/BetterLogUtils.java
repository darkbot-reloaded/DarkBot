package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;

public class BetterLogUtils extends LogUtils {
    private DatagramSocket socket ;
    //singleton
    private static BetterLogUtils instance;
    public static BetterLogUtils getInstance() {
        if (instance == null) instance = new BetterLogUtils();
        return instance;
    }
    private BetterLogUtils() {
        try {
            //get localhost IP address
            socket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void PrintLn(String message)
    {
        try {
            System.out.println(message);
            InetAddress adds = InetAddress.getLoopbackAddress();
            DatagramSocket ds = new DatagramSocket();
            String msg = "["+ ProcessHandle.current().pid()+"]"+"[" + LocalDateTime.now().format(LOG_DATE) + "] " + message + "\n";
            DatagramPacket dp = new DatagramPacket(msg.getBytes(),msg.length(), adds, 7504);
            ds.send(dp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
