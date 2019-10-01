package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.Time;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DarkFlash extends AbstractDarkBotApi {
    private static final Pattern DATA_PATTERN = Pattern.compile("\"src\": \"([^\"]*)\".*}, \\{(.*)}");

    @Override
    public void refresh() {
        this.reloadSWF();
        Time.sleep(2000);
    }

    @Override
    public void createWindow() {
        JPanel panel = new JPanel(new MigLayout("ins 0", "[]3px[]10px[]3px[]"));
        JTextField sv = new JTextField(5), sid = new JTextField(20);
        panel.add(new JLabel("Server"));
        panel.add(sv);
        panel.add(new JLabel("SID"));
        panel.add(sid);

        JButton login = new JButton("Log in");
        login.addActionListener(event -> performLogin(panel, sv.getText(), sid.getText()));

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{login}, login);
        Popups.showMessageSync("Sid Login", pane);
    }

    private void performLogin(JPanel panel, String sv, String sid) {
        String url = "https://" + sv + ".darkorbit.com/";
        sid = "dosid=" + sid;
        String preloader;
        String params;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url + "indexInternal.es?action=internalMapRevolution")
                    .openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Cookie", sid);

            String flashEmbed = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines()
                    .filter(l -> l.contains("flashembed("))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("SID couldn't be used to log in"));

            Matcher m = DATA_PATTERN.matcher(flashEmbed);
            if (m.find()) {
                preloader = m.group(1);
                params = m.group(2).replaceAll("\"", "").replaceAll(",", "&").replaceAll(": ", "=");
            } else throw new IllegalArgumentException("SID couldn't be used to log in");
        } catch (Exception e) {
            Popups.showMessageAsync("Error", e.getMessage(), JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        setCookie(url, sid);
        load(preloader, params, url);
        SwingUtilities.getWindowAncestor(panel).setVisible(false);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void mouseClick(int x, int y) {
        this.mousePress(x, y);
    }

    @Override
    public void keyboardClick(char btn) {
        this.keyPress(btn);
    }

    public native void setCookie(String url, String cookie);

    public void load(String preloader, String params, String url) {
        System.out.println("Preloader: " + preloader);
        System.out.println("Params: " + params);
        System.out.println("Url: " + url);
        new Thread(() -> this.loadSWF(preloader, params, url)).start();
        new Thread(() -> {
            while ((window = USER_32.FindWindow(null, "DarkPlayer")) == null || !USER_32.IsWindow(window)) Time.sleep(100);
        }).start();
    }

    private native void loadSWF(String preloader, String params, String url);

    private native void reloadSWF();

    public native void mousePress(int x, int y);

    public native void keyPress(char btn);

    public native double readMemoryDouble(long address);

    public native long readMemoryLong(long address);

    public native int readMemoryInt(long address);

    public boolean readMemoryBoolean(long address) {
        return this.readMemoryInt(address) == 1;
    }

    public native byte[] readMemory(long address, int length);

    public native void writeMemoryDouble(long address, double value);

    public native void writeMemoryLong(long address, long value);

    public native void writeMemoryInt(long address, int value);

    public native long[] queryMemoryInt(int value, int maxQuantity);

    public native long[] queryMemoryLong(long value, int maxQuantity);

    public native long[] queryMemory(byte[] query, int maxQuantity);

    static {
        System.loadLibrary("DarkFlash");
    }
}
