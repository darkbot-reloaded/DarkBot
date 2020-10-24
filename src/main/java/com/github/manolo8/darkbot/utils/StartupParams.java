package com.github.manolo8.darkbot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

public class StartupParams {
    private static final String COMMAND_PREFIX = "-";

    /**
     * Command-line argument for auto-login
     * requires a path to a properties file containing a username and either a password or a master-password.
     * Example usage: {@code -login C:\Users\Owner\login.properties}
     *
     * @see #properties
     */
    private static final String LOGIN_COMMAND = COMMAND_PREFIX + "login";
    private boolean autoLogin = false;
    /**
     * Login properties file containing 3 keys: username, password, master-password
     * username is required to be defined and you can choose to define either password or master-password
     * If you have an empty master-password you can define that field to be empty
     * leave undefined fields empty
     */
    private Properties properties;

    /**
     * Command-line argument for auto-start, has no parameters.
     */
    private static final String START_COMMAND = COMMAND_PREFIX + "start";
    private boolean autoStart = false;

    private String[] args;

    public StartupParams(String[] args) throws IOException {
        this.args = args;
        System.out.println(this);

        // parsing program args
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case LOGIN_COMMAND:
                    if (i + 1 >= args.length) {
                        System.err.println("Missing arguments for auto-login, usage: -login path/to/startup.properties");
                    }
                    properties = loadLoginProperties(args[++i]);
                    autoLogin = true;
                    break;
                case START_COMMAND:
                    autoStart = true;
                    break;
            }
        }
    }

    private Properties loadLoginProperties(String path) throws IOException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8);
        Properties p = new Properties();
        p.load(reader);
        reader.close();

        System.out.println("Loaded startup properties file");
        p.list(System.out);

        return p;
    }

    public char[] getMasterPassword() {
        String masterPassword = get(PropertyKey.MASTER_PASSWORD);
        return masterPassword == null ? null : masterPassword.toCharArray();
    }

    private String get(String key) {
        return properties.getProperty(key);
    }

    public String get(PropertyKey key) {
        String value = get(key.name);
        if (value == null) {
            System.err.println("Unable to retrieve " + key.name + ", make sure you have defined this key inside your properties file");
            if (key.required) System.exit(-1);
        }
        return value;
    }

    public enum PropertyKey {
        USERNAME("username", true),
        PASSWORD("password", false),
        MASTER_PASSWORD("master-password", false);

        private final String name;
        private final boolean required;
        PropertyKey(String name, boolean required) {
            this.name = name;
            this.required = required;
        }
    }

    public boolean getAutoLogin() {
        return autoLogin;
    }

    public boolean getAutoStart() {
        return autoStart;
    }

    @Override
    public String toString() {
        return "StartupParams: " + Arrays.toString(args);
    }
}
