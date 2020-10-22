package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StartupParams {
    private static final String COMMAND_PREFIX = "-";

    /**
     * <b>NAME</b>
     * <pre>
     *     login - auto logins to the account with this <i>username</i>
     * </pre>
     * <b>SYNTAX</b>
     * <pre>
     *     -login <i>username</i> <i>filepath</i>
     * </pre>
     * <b>OPTIONS</b>
     * <pre>
     *     <i>username</i> and <i>filepath</i> are required arguments
     *     <i>username</i> refers to the account you want to login to
     *     <i>filepath</i> refers to the filepath to your txt file containing your master password
     * </pre>
     * <b>SAMPLE USAGE</b>
     * <pre>
     *     -login NotABot C:\Users\Owner\masterpw.txt
     * </pre>
     * @see #masterPasswordFile
     */
    private static final String LOGIN_COMMAND = COMMAND_PREFIX + "login";
    private boolean autoLogin = false;
    private String username;
    /**
     * This is a txt file containing your master file<br>
     * Sample content of master password file:
     * <pre>
     *     password="mypassword12345"
     * </pre>
     * Leave it empty if you didnt set a master password like this:
     * <pre>
     *     password=""
     * </pre>
     */
    private File masterPasswordFile;

    /**
     * <b>NAME</b>
     * <pre>
     *     start - auto starts bot
     * </pre>
     * <b>SYNTAX</b>
     * <pre>
     *     -start
     * </pre>
     * <b>OPTIONS</b>
     * <pre>
     *     This command takes no options
     * </pre>
     * <b>SAMPLE USAGE</b>
     * <pre>
     *     -start
     * </pre>
     */
    private static final String START_COMMAND = COMMAND_PREFIX + "start";
    private boolean autoStart = false;

    public StartupParams(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case LOGIN_COMMAND:
                        username = args[++i];
                        masterPasswordFile = new File(args[++i]);
                        autoLogin = true;
                        break;
                    case START_COMMAND:
                        autoStart = true;
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.err.println("[ERROR] Invalid program arguments");
            Popups.showMessageSync("Command Line Error",
                    new JOptionPane(new Object[]{"Invalid program arguments"}, JOptionPane.ERROR_MESSAGE));
            System.exit(0);
        }
    }

    public char[] getMasterPassword() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(masterPasswordFile));

        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }

        return sb.substring(sb.indexOf("\"") + 1, sb.lastIndexOf("\"")).toCharArray();
    }

    public boolean shouldAutoLogin() {
        return autoLogin;
    }

    public String getUsername() {
        return username;
    }

    public boolean shouldAutoStart() {
        return autoStart;
    }

}
