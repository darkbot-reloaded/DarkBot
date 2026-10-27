package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.Config;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Applies the bot's proxy settings to all JVM network traffic ({@link java.net.URL},
 * {@link java.net.HttpURLConnection}, etc.) and exposes the active proxy for
 * {@link eu.darkbot.utils.KekkaPlayerProxyServer} to chain the game's traffic through.
 */
public class NetworkUtils {

    private static volatile Proxy currentProxy = Proxy.NO_PROXY;
    private static volatile Proxy.Type currentType = Proxy.Type.HTTP;
    private static volatile String currentHost = "";
    private static volatile int currentPort;
    private static volatile String currentUser = "";
    private static volatile String currentPass = "";

    private static volatile String appliedConfig = null;

    private NetworkUtils() {}

    /**
     * Reads the proxy settings from the given config and applies them globally.
     * This is cheap to call repeatedly, it only re-applies when the config changes.
     */
    public static void applyConfig(Config config) {
        if (config == null) return;
        Config.BotSettings.APIConfig.Proxy proxy = config.BOT_SETTINGS.API_CONFIG.PROXY;

        String host = proxy.HOST == null ? "" : proxy.HOST.trim();
        String user = proxy.USERNAME == null ? "" : proxy.USERNAME;
        String pass = proxy.PASSWORD == null ? "" : proxy.PASSWORD;
        int port = proxy.PORT;

        String signature = proxy.ENABLED + "|" + proxy.TYPE + "|" + host + "|" + port + "|" + user + "|" + pass;
        if (signature.equals(appliedConfig)) return;
        appliedConfig = signature;

        if (!proxy.ENABLED || host.isEmpty() || port <= 0) {
            currentProxy = Proxy.NO_PROXY;
            currentHost = "";
            currentPort = 0;
            currentUser = "";
            currentPass = "";
            ProxySelector.setDefault(null);
            Authenticator.setDefault(null);
            return;
        }

        currentType = proxy.TYPE == Config.BotSettings.APIConfig.Proxy.ProxyType.SOCKS
                ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
        currentProxy = new Proxy(currentType, new InetSocketAddress(host, port));
        currentHost = host;
        currentPort = port;
        currentUser = user;
        currentPass = pass;

        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (isBypassed(uri)) return Collections.singletonList(Proxy.NO_PROXY);
                return Collections.singletonList(currentProxy);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            }
        });

        if (!user.isEmpty()) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestorType() == RequestorType.PROXY)
                        return new PasswordAuthentication(currentUser, currentPass.toCharArray());
                    return null;
                }
            });
        } else {
            Authenticator.setDefault(null);
        }
    }

    private static boolean isBypassed(URI uri) {
        String host = uri.getHost();
        if (host == null) return true;
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    public static boolean isProxyEnabled() {
        return currentProxy != Proxy.NO_PROXY;
    }

    public static Proxy getProxy() {
        return currentProxy;
    }

    public static Proxy.Type getProxyType() {
        return currentType;
    }

    public static String getProxyHost() {
        return currentHost;
    }

    public static int getProxyPort() {
        return currentPort;
    }

    public static String getProxyUser() {
        return currentUser;
    }

    public static String getProxyPassword() {
        return currentPass;
    }
}
