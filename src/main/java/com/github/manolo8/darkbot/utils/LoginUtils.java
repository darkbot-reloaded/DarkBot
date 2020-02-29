package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.core.DarkFlash;
import com.github.manolo8.darkbot.gui.utils.Popups;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginUtils {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("\"bgcdw_login_form\" action=\"(.*)\"");
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
            setLoginData(sid.getText(), sv.getText() + ".darkorbit.com");
            SwingUtilities.getWindowAncestor(panel).setVisible(false);
        });

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{login}, login);
        Popups.showMessageSync("Sid Login", pane);

        if (loginData == null) System.exit(0);
        return this;
    }

    public DarkFlash.LoginData getLoginData() {
        return loginData;
    }

    private void login(String username, String password) {
        String loginUrl = getLoginUrl(HttpUtils.create("https://www.darkorbit.com/").getInputStream());
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        HttpUtils.create(loginUrl)
                .setParam("username", username)
                .setParam("password", password)
                .getAndCloseInputStream();

        CookieHandler.setDefault(null);

        HttpCookie cookie = cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equalsIgnoreCase("dosid"))
                .filter(c -> c.getDomain().matches(".*\\d+.*"))
                .findFirst().orElseThrow(WrongCredentialsException::new);

        setLoginData(cookie.getValue(), cookie.getDomain());
    }

    private void setLoginData(String sid, String url) {
        url = "https://" + url + "/";
        sid = "dosid=" + sid;

        InputStream in = HttpUtils.create(url + "indexInternal.es?action=internalMapRevolution")
                .setFollowRedirects(false)
                .setHeader("Cookie", sid)
                .getInputStream();

        String flashEmbed = new BufferedReader(new InputStreamReader(in))
                .lines()
                .filter(l -> l.contains("flashembed("))
                .findFirst()
                .orElseThrow(WrongCredentialsException::new);

        Matcher m = DATA_PATTERN.matcher(flashEmbed);
        if (m.find()) loginData = new DarkFlash.LoginData(sid, url, m.group(1), m.group(2)
                .replaceAll("\"", "")
                .replaceAll(",", "&")
                .replaceAll(": ", "="));
        else throw new WrongCredentialsException("Can't parse flashembed vars");
    }

    private String getLoginUrl(InputStream in) {
        return new BufferedReader(new InputStreamReader(in)).lines()
                .map(LOGIN_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1).replace("&amp;", "&"))
                .findFirst().orElseThrow(WrongCredentialsException::new);
    }

    private static class WrongCredentialsException extends IllegalArgumentException {

        public WrongCredentialsException() {
            this("Wrong login data");
        }

        public WrongCredentialsException(String s) {
            super(s);
            Popups.showMessageAsync("Error", s, JOptionPane.ERROR_MESSAGE);
        }
    }
}
