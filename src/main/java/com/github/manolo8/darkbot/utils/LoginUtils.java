package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.core.DarkFlash;
import com.github.manolo8.darkbot.gui.utils.Popups;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginUtils {
    private static final Pattern DATA_PATTERN = Pattern.compile("\"src\": \"([^\"]*)\".*}, \\{(.*)}");

    private DarkFlash.LoginData loginData;

    public LoginUtils performSidLogin() {
        JPanel panel = new JPanel(new MigLayout("ins 0", "[]3px[]10px[]3px[]"));
        JTextField sv = new JTextField(5), sid = new JTextField(20);
        panel.add(new JLabel("Server"));
        panel.add(sv);
        panel.add(new JLabel("SID"));
        panel.add(sid);

        JButton login = new JButton("Log in");
        login.addActionListener(event -> {
            performLogin(sv.getText(), sid.getText());
            SwingUtilities.getWindowAncestor(panel).setVisible(false);
        });

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{login}, login);
        Popups.showMessageSync("Sid Login", pane);

        if (loginData == null) performSidLogin();
        return this;
    }

    public DarkFlash.LoginData getLoginData() {
        return loginData;
    }

    private void performLogin(String sv, String sid) {
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
        this.loginData = new DarkFlash.LoginData(sv, sid, preloader, params, url);
    }

}
