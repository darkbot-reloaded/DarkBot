package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpUtils {

    private String url, userAgent = "Mozilla/5.0";
    private boolean followRedirects = true;

    private Map<String, String> headers = new LinkedHashMap<>(), params = new LinkedHashMap<>();

    private HttpUtils(String url) {
        this.url = url;
    }

    /**
     * Crates new instance of HttpUtils
     */
    public static HttpUtils create(String url) {
        return new HttpUtils(url);
    }

    /**
     * Creates HttpURLConnections of current instance and
     *
     * @return result as String
     */
    public String getContent() {
        try {
            return IOUtils.read(getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getAndCloseInputStream() {
        try {
            getConnection().getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates HttpURLConnections of current instance and
     *
     * @return result as InputStream
     */
    public InputStream getInputStream() {
        try {
            return getConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return HttpURLConnection of current instance
     */
    public HttpURLConnection getConnection() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(followRedirects);
            conn.setRequestProperty("User-Agent", userAgent);

            if (!headers.isEmpty()) headers.forEach(conn::setRequestProperty);

            if (!params.isEmpty()) {
                byte[] data = getParamsBytes();
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                conn.getOutputStream().write(data);
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
            }

            return conn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * conn.setRequestProperty(key, value)
     */
    public HttpUtils setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Put any param to make POST request
     */
    public HttpUtils setParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    /**
     * "Mozilla/5.0" is default
     */
    public HttpUtils setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * true by default
     */
    public HttpUtils setFollowRedirects(boolean value) {
        this.followRedirects = value;
        return this;
    }

    private byte[] getParamsBytes() {
        return this.params.entrySet().stream()
                .map(Object::toString)
                .collect(Collectors.joining("&"))
                .getBytes(StandardCharsets.UTF_8);
    }
}
