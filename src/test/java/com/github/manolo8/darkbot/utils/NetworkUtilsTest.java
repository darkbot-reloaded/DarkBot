package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Authenticator;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

public class NetworkUtilsTest {

    @Test
    public void testDisabledProxy() {
        Config config = new Config();
        config.BOT_SETTINGS.API_CONFIG.PROXY.ENABLED = false;
        config.BOT_SETTINGS.API_CONFIG.PROXY.HOST = "disabled.example.com";
        NetworkUtils.applyConfig(config);

        Assertions.assertFalse(NetworkUtils.isProxyEnabled());
        Assertions.assertNull(ProxySelector.getDefault());
        Assertions.assertNull(Authenticator.getDefault());
    }

    @Test
    public void testHttpProxyApplied() {
        Config config = new Config();
        config.BOT_SETTINGS.API_CONFIG.PROXY.ENABLED = true;
        config.BOT_SETTINGS.API_CONFIG.PROXY.TYPE = Config.BotSettings.APIConfig.Proxy.ProxyType.HTTP;
        config.BOT_SETTINGS.API_CONFIG.PROXY.HOST = "http-proxy.example.com";
        config.BOT_SETTINGS.API_CONFIG.PROXY.PORT = 8080;
        NetworkUtils.applyConfig(config);

        Assertions.assertTrue(NetworkUtils.isProxyEnabled());
        Assertions.assertEquals(Proxy.Type.HTTP, NetworkUtils.getProxyType());
        Assertions.assertEquals("http-proxy.example.com", NetworkUtils.getProxyHost());
        Assertions.assertEquals(8080, NetworkUtils.getProxyPort());
        Assertions.assertNull(Authenticator.getDefault());

        ProxySelector selector = ProxySelector.getDefault();
        Assertions.assertNotNull(selector);

        List<Proxy> proxies = selector.select(URI.create("https://darkorbit.com/"));
        Assertions.assertEquals(1, proxies.size());
        Assertions.assertEquals(NetworkUtils.getProxy(), proxies.get(0));

        List<Proxy> bypassed = selector.select(URI.create("http://127.0.0.1:7777/"));
        Assertions.assertEquals(Proxy.NO_PROXY, bypassed.get(0));
    }

    @Test
    public void testSocksProxyWithAuth() {
        Config config = new Config();
        config.BOT_SETTINGS.API_CONFIG.PROXY.ENABLED = true;
        config.BOT_SETTINGS.API_CONFIG.PROXY.TYPE = Config.BotSettings.APIConfig.Proxy.ProxyType.SOCKS;
        config.BOT_SETTINGS.API_CONFIG.PROXY.HOST = "socks-proxy.example.com";
        config.BOT_SETTINGS.API_CONFIG.PROXY.PORT = 1080;
        config.BOT_SETTINGS.API_CONFIG.PROXY.USERNAME = "user";
        config.BOT_SETTINGS.API_CONFIG.PROXY.PASSWORD = "pass";
        NetworkUtils.applyConfig(config);

        Assertions.assertTrue(NetworkUtils.isProxyEnabled());
        Assertions.assertEquals(Proxy.Type.SOCKS, NetworkUtils.getProxyType());
        Assertions.assertEquals("user", NetworkUtils.getProxyUser());
        Assertions.assertNotNull(Authenticator.getDefault());

        List<Proxy> proxies = ProxySelector.getDefault().select(URI.create("https://darkorbit.com/"));
        Assertions.assertEquals(NetworkUtils.getProxy(), proxies.get(0));
    }
}
