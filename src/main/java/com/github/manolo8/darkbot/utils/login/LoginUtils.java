package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.gui.login.LoginForm;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.utils.CaptchaAPI;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.IOUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.http.Http;
import eu.darkbot.util.http.Method;

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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginUtils {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("\"bgcdw_login_form\" action=\"(.*)\"");
    private static final Pattern DATA_PATTERN = Pattern.compile("\"src\": \"([^\"]*)\".*}, (\\{.*})");

    public static LoginData performUserLogin(StartupParams params) {
        if (params.getAutoLogin()) {
            try {
                return performAutoLogin(params.getAutoLoginProps());
            } catch (LoginException e) {
                System.err.println("Failed to perform autologin, falling back to login panel");
                e.printStackTrace();
            }
        }

        LoginForm panel = new LoginForm();

        Popups.of("Login", panel)
                .options()
                .border(BorderFactory.createEmptyBorder(0, 0, 5, 0))
                .defaultButton(panel.getLoginBtn())
                .showSync();

        LoginData loginData = panel.getResult();
        if (loginData.isNotInitialized()) {
            System.out.println("Closed login panel, exited without logging in");
            System.exit(0);
        }
        return loginData;
    }

    public static LoginData performAutoLogin(StartupParams.AutoLoginProps params) {
        String password = params.getPassword();
        if (!params.shouldSIDLogin() && params.getUsername() != null && Strings.isEmpty(password)) {
            password = getPassword(params.getUsername(), params.getMasterPassword());

            if (password == null)
                System.err.println("Password for user couldn't be retrieved. Check that the user exists and master password is correct.");
        }

        if (!params.shouldSIDLogin() && (params.getUsername() == null || Strings.isEmpty(password))) {
            System.err.println("Credentials file requires username & either a password or a master password, or/and server & sid");
            System.exit(-1);
        }

        LoginData loginData = new LoginData();
        loginData.setCredentials(params.getUsername(), password, null);

        System.out.println("Auto logging in using " + (params.shouldSIDLogin() ? "server & SID" : "user & password") + " (1/2)");
        try {
            if (params.shouldSIDLogin()) loginData.setSid(params.getSID(), params.getServer() + ".darkorbit.com");
            else usernameLogin(loginData);
            System.out.println("Loading spacemap (2/2)");
            findPreloader(loginData);
        } catch (IOException e) {
            System.err.println("IOException trying to perform auto login, servers may be down");
            e.printStackTrace();
        } catch (WrongCredentialsException e) {
            if (params.shouldSIDLogin()) {
                System.err.println("Expired SID in login properties file, attempting re-connect with user & pass");
                params.setSID("");
                return performAutoLogin(params);
            }
            System.err.println("Wrong credentials, check your username and password");
        }

        if (loginData.isNotInitialized()) {
            System.err.println("Could not find preloader url or parameters, exiting bot.");
            System.exit(-1);
        }

        if (!params.shouldSIDLogin() && params.isAllowStoreSID()) {
            params.setServer(loginData.getUrl().split("\\.")[0]);
            params.setSID(loginData.getSid());
            params.updateLoginFile();
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
        } catch (CaptchaException ex) {
            throw ex;
        } catch (Exception e) {
            try {
                usernameLogin(loginData, "lp");
            } catch (IOException ex) {
                throw LoginException.translated("gui.login.error.frontpage_fail", ex);
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
                throw LoginException.translated("gui.login.error.captcha_fail", t);
            }
        } else if (frontPage.contains("class=\"bgcdw_captcha\"")) {
            throw CaptchaException.translated("gui.login.error.captcha");
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
                .findFirst()
                .orElseThrow(() -> WrongCredentialsException.translated("gui.login.error.wrong_credentials"));

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
                        .orElseThrow(() -> WrongCredentialsException.translated("gui.login.error.no_flash_embed")));

        Matcher m = DATA_PATTERN.matcher(flashEmbed);
        if (m.find()) loginData.setPreloader(m.group(1), m.group(2));
        else throw WrongCredentialsException.translated("gui.login.error.flash_embed_fail");
    }

    private static String getLoginUrl(String in) {
        Matcher match = LOGIN_PATTERN.matcher(in);
        if (match.find()) return match.group(1).replace("&amp;", "&");

        throw LoginException.translated("gui.login.error.frontpage_fail");
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

        public static LoginException translated(String key) {
            return new LoginException(I18n.get(key), I18n.get(key + ".desc"));
        }

        public static LoginException translated(String key, Throwable cause) {
            return new LoginException(I18n.get(key), I18n.get(key + ".desc"), cause);
        }

        public String getTitle() {
            return title;
        }
    }

    public static class WrongCredentialsException extends LoginException {

        public WrongCredentialsException(String title, String message) {
            super(title, message);
        }

        public static WrongCredentialsException translated(String key) {
            return new WrongCredentialsException(I18n.get(key), I18n.get(key + ".desc"));
        }
    }

    public static class CaptchaException extends LoginException {

        public CaptchaException(String title, String message) {
            super(title, message);
        }

        public CaptchaException(String title, String message, Throwable cause) {
            super(title, message, cause);
        }

        public static CaptchaException translated(String key) {
            return new CaptchaException(I18n.get(key), I18n.get(key + ".desc"));
        }

        public static CaptchaException translated(String key, Throwable cause) {
            return new CaptchaException(I18n.get(key), I18n.get(key + ".desc"), cause);
        }
    }
}
