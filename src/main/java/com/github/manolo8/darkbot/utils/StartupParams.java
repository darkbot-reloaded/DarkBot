package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class StartupParams {
    private static final String COMMAND_PREFIX = "-";

    /**
     * Command-line argument for auto-login, requires username and a path to a plaintext file with the master password.
     * Example usage: {@code -login NotABot C:\Users\Owner\masterpw.txt}
     *
     * @see #username
     * @see #masterPasswordPath
     */
    private static final String LOGIN_COMMAND = COMMAND_PREFIX + "login";
    private boolean autoLogin = false;
    /**
     * If you have a username containing special characters and are having issues passing in your username from RunBot.bat
     * <ol>
     *     <li>Change your Windows locale to UTF-8
     *     <li>add this flag -Dsun.jnu.encoding=UTF-8
     * </ol>
     * Example RunBot.bat file: {@code START javaw -jar -Dsun.jnu.encoding=UTF-8 DarkBot.jar -login ᑎOTᗩᗷOT C:\Users\Owner\masterpw.txt}
     * @see <a href="https://stackoverflow.com/a/53995490">https://stackoverflow.com/questions/7660651/passing-command-line-unicode-argument-to-java-code/9043883</a>
     */
    private String username;
    /**
     * Plaintext file containing the master password, make sure it has only 1 line
     * Leave the file empty if you have an empty master password
     */
    private Path masterPasswordPath;

    /**
     * Command-line argument for auto-start, has no parameters.
     */
    private static final String START_COMMAND = COMMAND_PREFIX + "start";
    private boolean autoStart = false;

    private String[] args;

    public StartupParams(String[] args) {
        this.args = args;
        parse(args);
        System.out.println(this);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case LOGIN_COMMAND:
                    if (i + 2 >= args.length) {
                        System.err.println("Missing arguments for auto-login, usage: -login username path/to/masterpass.txt");
                        System.exit(0);
                    }
                    username = args[++i];
                    masterPasswordPath = Paths.get(args[++i]);
                    autoLogin = true;
                    break;
                case START_COMMAND:
                    autoStart = true;
                    break;
            }
        }
    }

    public char[] getMasterPassword() throws IOException {
        List<String> lines = Files.readAllLines(masterPasswordPath, StandardCharsets.UTF_8);
        if (lines.size() != 1) {
            System.err.println("Master password file contains multiple lines, make sure it only contains 1 line");
            System.exit(0);
        }
        return lines.get(0).toCharArray();
    }

    public boolean getAutoLogin() {
        return autoLogin;
    }

    public String getUsername() {
        return username;
    }

    public boolean getAutoStart() {
        return autoStart;
    }

    @Override
    public String toString() {
        return "StartupParams: " + Arrays.toString(args);
    }
}
