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
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
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
        String username = params.getAutoLoginValue(StartupParams.PropertyKey.USERNAME);
        String password = params.getAutoLoginValue(StartupParams.PropertyKey.PASSWORD);

        if (username != null && (password == null || password.isEmpty())) {
            password = getPassword(username, params.getAutoLoginMasterPassword());

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
        } catch (IOException e) {
            System.err.println("IOException trying to perform auto login, servers may be down");
            e.printStackTrace();
        } catch (WrongCredentialsException e) {
            System.err.println("Wrong credentials, check your username and password");
        }

        if (loginData.getPreloaderUrl() == null || loginData.getParams() == null) {
            System.err.println("Could not find preloader url or parameters, exiting bot.");
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
            try {
                usernameLogin(loginData, "lp");
            } catch (IOException ex) {
                throw new LoginException("Failed to load frontpage", "domain may not be available", ex);
            }
        }
    }

    private static void usernameLogin(LoginData loginData, String domain) throws IOException {
        URL url = new URL("https://" + domain + ".darkorbit.com/");
        String frontPage = IOUtils.read(Http.create(url.toString()).getInputStream());

        Map<String, String> extraPostParams = Collections.emptyMap();
        CaptchaAPI solver = CaptchaAPI.getInstance();
        if (solver != null) {
            try {
                extraPostParams = solver.solveCaptcha(url, frontPage);
            } catch (Throwable t) {
                System.out.println("Captcha solver failed to resolve login captcha");
                t.printStackTrace();
                throw new LoginException("Captcha-Solver failed", "check the logs for details", t);
            }
        } else if (frontPage.contains("class=\"bgcdw_captcha\"")) {
            throw new LoginException("reCaptcha detected", "Captcha-Solver not configured");
        }

        String loginUrl = getLoginUrl(frontPage);

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        try {
            Http http = Http.create(loginUrl, Method.POST)
                        .setParam("username", loginData.getUsername())
                        .setParam("password", loginData.getPassword());
            extraPostParams.forEach(http::setParam);
            http.closeInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        CookieHandler.setDefault(null);

        HttpCookie cookie = cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equalsIgnoreCase("dosid"))
                .filter(c -> c.getDomain().matches(".*\\d+.*"))
                .findFirst().orElseThrow(() -> new WrongCredentialsException("Wrong credentials",
                        "failed to find dosid cookie, check your username & password"));

        loginData.setSid(cookie.getValue(), cookie.getDomain());
    }

    public static void findPreloader(LoginData loginData) throws IOException {
        Http req = Http.create("https://" + loginData.getUrl() + "/indexInternal.es?action=internalMapRevolution", false)
                .setRawHeader("Cookie", "dosid=" + loginData.getSid());

        String flashEmbed = req.consumeInputStream(inputStream ->
                new BufferedReader(new InputStreamReader(inputStream))
                        .lines()
                        .filter(l -> l.contains("flashembed("))
                        .findFirst()
                        .orElseThrow(() ->
                                new WrongCredentialsException("FlashEmbed not found", "try again later")));

        Matcher m = DATA_PATTERN.matcher(flashEmbed);
        if (m.find()) loginData.setPreloader(m.group(1), replaceParameters(m.group(2)));
        else throw new WrongCredentialsException("FlashEmbed parsing failed", "try again later");
    }

    private static String replaceParameters(String params) {
        // update it here so on refresh can be used 2d or 3d
        //FORCED_PARAMS.put("display2d", ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.API_CONFIG.USE_3D ? "1" : "2");

        params = params.replaceAll("\"", "").replaceAll(",", "&").replaceAll(": ", "=");
        for (Map.Entry<String, String> replaces : FORCED_PARAMS.entrySet()) {
            params = params.replaceAll(replaces.getKey() + "=[^&]+", replaces.getKey() + "=" + replaces.getValue());
        }
        return params;
    }

    private static String getLoginUrl(String in) {
        Matcher match = LOGIN_PATTERN.matcher(in);
        if (match.find()) return match.group(1).replace("&amp;", "&");

        throw new LoginException("Failed to get login URL", "Failed to get login URL in frontpage");
    }

    public static Credentials loadCredentials() {
        Path file = Paths.get("credentials.json");
        if (!Files.exists(file)) return Credentials.create();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
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

    public static class LoginException extends RuntimeException {
        private final String title;

        public LoginException(String title, String message) {
            super(message);
            this.title = title;
        }

        public LoginException(String title, String message, Throwable cause) {
            super(message, cause);
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class WrongCredentialsException extends LoginException {

        public WrongCredentialsException(String title, String message) {
            super(title, message);
        }
    }
}
