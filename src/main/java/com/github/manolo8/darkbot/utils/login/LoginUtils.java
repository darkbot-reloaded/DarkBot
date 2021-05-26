package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.login.LoginForm;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.*;
import com.github.manolo8.darkbot.utils.http.Http;
import com.github.manolo8.darkbot.utils.http.Method;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginUtils {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("\"bgcdw_login_form\" action=\"(.*)\"");
    private static final Pattern DATA_PATTERN = Pattern.compile("\"src\": \"([^\"]*)\".*}, \\{(.*)}");

    private static final Map<String, String> FORCED_PARAMS = new HashMap<>();
    static {
        String lang = I18n.getLocale().getLanguage();
        if (!lang.isEmpty() && ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.API_CONFIG.FORCE_GAME_LANGUAGE)
            FORCED_PARAMS.put("lang", lang);
        FORCED_PARAMS.put("display2d", "2");
        FORCED_PARAMS.put("autoStartEnabled", "1");
    }

    public static LoginData performUserLogin(StartupParams params) {
        if (params.getAutoLogin()) return LoginUtils.performAutoLogin(params);

        LoginForm panel = new LoginForm();

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        Popups.showMessageSync("Login", pane, panel::setDialog);

        LoginData loginData = panel.getResult();
        if (loginData.getPreloaderUrl() == null || loginData.getParams() == null) {
            System.out.println("Closed login panel, exited without logging in");
            System.exit(0);
        }
        return loginData;
    }

    public static LoginData performAutoLogin(StartupParams params) {
        String username = params.get(StartupParams.PropertyKey.USERNAME);
        String password = params.get(StartupParams.PropertyKey.PASSWORD);

        if (username != null && (password == null || password.isEmpty())) {
            password = getPassword(username, params.getMasterPassword());

            if (password == null)
                System.err.println("Password for user couldn't be retrieved. Check that the user exists and master password is correct.");
        }

        if (username == null || password == null || password.isEmpty()) {
            System.err.println("Credentials file requires username and either a password or a master password");
            System.exit(-1);
        }

        LoginData loginData = new LoginData();
        loginData.setCredentials(username, password);

        try {
            System.out.println("Auto logging in (1/2)");
            usernameLogin(loginData);
            System.out.println("Loading spacemap (2/2)");
            findPreloader(loginData);
        } catch (WrongCredentialsException e) {
            System.err.println("Wrong credentials, check your username and password");
        }

        if (loginData.getPreloaderUrl() == null || loginData.getParams() == null) {
            System.err.println("Could not find preloader url or parameters");
            System.exit(-1);
        }
        return loginData;
    }

    private static String getPassword(String username, char[] masterPassword) {
        Credentials credentials = loadCredentials();
        try {
            credentials.decrypt(masterPassword);
        } catch (Exception e) {
            System.err.println("Couldn't retreive logins, check your startup properties file");
            e.printStackTrace();
            return null;
        }
        return credentials.getUsers()
                .stream()
                .filter(usr -> username.equals(usr.u))
                .findFirst()
                .map(usr -> usr.p)
                .orElse(null);
    }

    public static void usernameLogin(LoginData loginData) {
        try {
            usernameLogin(loginData, "www");
        } catch (Exception e) {
            usernameLogin(loginData, "lp");
        }
    }

    public static void usernameLogin(LoginData loginData, String domain) {
        String frontPage = IOUtils.read(Http.create("https://" + domain + ".darkorbit.com/").getInputStream());

        try {
            CaptchaAPI captcha = CaptchaAPI.getInstance();
            gResponse = captcha.solveCaptcha(frontPage);
        }catch (Exception e){
            e.printStackTrace();
        }

        String loginUrl = getLoginUrl(frontPage);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        try {
            if(gResponse.isEmpty())
                Http.create(loginUrl, Method.POST)
                        .setParam("username", loginData.getUsername())
                        .setParam("password", loginData.getPassword())
                        .closeInputStream();
            else
                Http.create(loginUrl, Method.POST)
                        .setParam("username", loginData.getUsername())
                        .setParam("password", loginData.getPassword())
                        .setParam("g-recaptcha-response", gResponse)
                        .closeInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CookieHandler.setDefault(null);

        HttpCookie cookie = cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equalsIgnoreCase("dosid"))
                .filter(c -> c.getDomain().matches(".*\\d+.*"))
                .findFirst().orElseThrow(WrongCredentialsException::new);

        loginData.setSid(cookie.getValue(), cookie.getDomain());
    }

    public static void findPreloader(LoginData loginData) {
        Http req = Http.create("https://" + loginData.getUrl() + "/indexInternal.es?action=internalMapRevolution", false)
                .setRawHeader("Cookie", "dosid=" + loginData.getSid());

        String flashEmbed = req.consumeInputStream(inputStream ->
                new BufferedReader(new InputStreamReader(inputStream))
                        .lines()
                        .filter(l -> l.contains("flashembed("))
                        .findFirst()
                        .orElseThrow(WrongCredentialsException::new));

        Matcher m = DATA_PATTERN.matcher(flashEmbed);
        if (m.find()) loginData.setPreloader(m.group(1), replaceParameters(m.group(2)));
        else throw new WrongCredentialsException("Can't parse flashembed vars");
    }

    private static String replaceParameters(String params) {
        params = params.replaceAll("\"", "").replaceAll(",", "&").replaceAll(": ", "=");
        for (Map.Entry<String, String> replaces : FORCED_PARAMS.entrySet()) {
            params = params.replaceAll(replaces.getKey() + "=[^&]+", replaces.getKey() + "=" + replaces.getValue());
        }
        return params;
    }

    private static String getLoginUrl(String in) {
        Matcher match = LOGIN_PATTERN.matcher(in);
        if (match.find()) return match.group(1).replace("&amp;", "&");
        throw new WrongCredentialsException();
    }

    private static String getLoginUrl(InputStream in) {
        return new BufferedReader(new InputStreamReader(in)).lines()
                .map(LOGIN_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1).replace("&amp;", "&"))
                .findFirst().orElseThrow(WrongCredentialsException::new);
    }

    public static Credentials loadCredentials() {
        Path file = Paths.get("credentials.json");
        if (!Files.exists(file)) return Credentials.create();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("credentials.json"))) {
            return Credentials.GSON.fromJson(reader, Credentials.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Credentials.create();
    }

    public static void saveCredentials(Credentials c, char[] passwd) throws GeneralSecurityException {
        Path file = Paths.get("credentials.json");

        c.encrypt(passwd);

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            Credentials.GSON.toJson(c, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class WrongCredentialsException extends IllegalArgumentException {

        public WrongCredentialsException() {
            this("Wrong login data");
        }

        public WrongCredentialsException(String s) {
            super(s);
        }
    }

}
