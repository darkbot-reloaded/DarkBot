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
     * <b>Usage:</b> <tt>-login <i>username</i> <i>filepath</i></tt><br>
     * &#x09 This command will auto login to the account with this <i>username</i><br>
     * &#x09<i>username</i> and <i>filepath</i> are required arguments<br>
     * &#x09<i>username</i> refers to the account you want to login to<br>
     * &#x09<i>filepath</i> refers to the filepath to your txt file containing your master password
     * <p>
     * <b>Example usage:</b>
     * <blockquote><pre>
     *     -login NotABot C:\Users\Owner\masterpw.txt
     * </pre></blockquote>
     */
    private static final String LOGIN_COMMAND = COMMAND_PREFIX + "login";
    public boolean AUTO_LOGIN = false;
    public String USERNAME;
    /**
     * Sample content of master password file:
     * <blockquote><pre>
     *     password="mypassword12345"
     * </pre></blockquote>
     * Leave it empty if you didnt set a master password like this:
     * <blockquote><pre>
     *     password=""
     * </pre></blockquote>
     */
    public File MASTER_PASSWORD_FILE;

    /**
     * <b>Usage:</b> <tt>-start<br>
     * &#x09This command takes no arguments and will auto start the bot
     * <p>
     * <b>Example usage:</b>
     * <blockquote><pre>
     *     -start
     * </pre></blockquote>
     */
    private static final String START_COMMAND = COMMAND_PREFIX + "start";
    public boolean AUTO_START = false;

    public void parse(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case LOGIN_COMMAND:
                        USERNAME = args[++i];
                        MASTER_PASSWORD_FILE = new File(args[++i]);
                        AUTO_LOGIN = true;
                        break;
                    case START_COMMAND:
                        AUTO_START = true;
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.err.println("Invalid program arguments");
            Popups.showMessageSync("Command Line Error",
                    new JOptionPane(new Object[]{"Invalid program arguments"}, JOptionPane.ERROR_MESSAGE));
        }
    }

    public char[] getMasterPassword() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(MASTER_PASSWORD_FILE));

        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }

        return sb.substring(sb.indexOf("\"") + 1, sb.lastIndexOf("\"")).toCharArray();
    }
}
