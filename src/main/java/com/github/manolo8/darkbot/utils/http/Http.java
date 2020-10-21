package com.github.manolo8.darkbot.utils.http;

import com.github.manolo8.darkbot.utils.IOUtils;
import com.github.manolo8.darkbot.utils.ThrowFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility for HTTP connections.
 * Use it like builder, just one time for instance
 */
public class Http {
    private static String DEFAULT_USER_AGENT = "Mozilla/5.0";

    public static String getDefaultUserAgent() {
        return DEFAULT_USER_AGENT;
    }

    public static void setDefaultUserAgent(String defaultUserAgent) {
        DEFAULT_USER_AGENT = defaultUserAgent;
    }

    protected String url;
    protected final Method method;
    protected final boolean followRedirects;

    //Discord doesn't handle java's user agent...
    protected String userAgent = DEFAULT_USER_AGENT;
    protected ParamBuilder params;
    protected List<Runnable> suppliers;
    protected Map<String, String> headers = new LinkedHashMap<>();

    protected Http(String url, Method method, boolean followRedirects) {
        this.url = url;
        this.method = method;
        this.followRedirects = followRedirects;
    }

    /**
     * Creates new instance of Http with provided url.
     * Request method is {@link Method#GET} and follows redirects by default.
     *
     * @param url to connect
     * @return new Http
     */
    public static Http create(String url) {
        return new Http(url, Method.GET, true);
    }

    /**
     * Creates new instance of Http with provided url and request method.
     * Follows redirects by default.
     *
     * @param url    to connect
     * @param method of request
     * @return new Http
     */
    public static Http create(String url, Method method) {
        return new Http(url, method, true);
    }

    /**
     * Creates new instance of Http with provided url and follow redirects.
     * Request method is {@link Method#GET} by default.
     *
     * @param url             to connect
     * @param followRedirects should follow redirects, response code 3xx
     * @return new Http
     */
    public static Http create(String url, boolean followRedirects) {
        return new Http(url, Method.GET, followRedirects);
    }

    /**
     * Creates new instance of Http with provided arguments.
     *
     * @param url             to connect
     * @param method          of request
     * @param followRedirects should follow redirects, response code 3xx
     * @return new Http
     */
    public static Http create(String url, Method method, boolean followRedirects) {
        return new Http(url, method, followRedirects);
    }

    /**
     * Adds action which will be executed at the end of the connection
     *
     * @param action to execute
     * @return current instance of Http
     */
    public Http addSupplier(Runnable action) {
        if (suppliers == null) suppliers = new ArrayList<>();
        this.suppliers.add(action);
        return this;
    }

    /**
     * Sets or overrides connection header.
     * Encoded via {@link java.net.URLEncoder#encode(String, String)} in UTF-8
     * To set header without encoding look {@link Http#setRawHeader(String, String)}
     * <p>
     * Equivalent to {@link HttpURLConnection#setRequestProperty(String, String)}
     *
     * @param key   of header
     * @param value of header
     * @return current instance of Http
     */
    public Http setHeader(String key, String value) {
        this.headers.put(ParamBuilder.encode(key), ParamBuilder.encode(value));
        return this;
    }

    /**
     * Sets or overrides connection header without encoding.
     * Equivalent to {@link HttpURLConnection#setRequestProperty(String, String)}
     *
     * @param key   of header
     * @param value of header
     * @return current instance of Http
     */
    public Http setRawHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Sets or overrides parameter for POST as body or for GET as
     * additional query url only if current url doesn't contains '?' char.
     * Is encoded via {@link java.net.URLEncoder#encode(String, String)}
     *
     * @param key   of parameter
     * @param value of parameter
     * @return current instance of Http
     */
    public Http setParam(Object key, Object value) {
        if (this.params == null)
            this.params = ParamBuilder.create(ParamBuilder.encode(key), ParamBuilder.encode(value));
        else this.params.set(key, value);
        return this;
    }

    /**
     * Sets or overrides parameter for POST as body or for GET as
     * additional query url only if current url doesn't contains '?' char.
     * Be aware, this wont be encoded via {@link java.net.URLEncoder#encode(String, String)}
     *
     * @param key   of parameter
     * @param value of parameter
     * @return current instance of Http
     */
    public Http setRawParam(Object key, Object value) {
        if (this.params == null)
            this.params = ParamBuilder.create(key, value);
        else this.params.setRaw(key, value);
        return this;
    }

    /**
     * Sets user agent used in connection.
     * Default is "Mozilla/5.0".
     *
     * @param userAgent to use.
     * @return current instance of Http
     */
    public Http setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Connects, gets and converts InputStream to String then closes stream.
     * <b>Creates new connection on each call</b>
     *
     * @return body of request as String
     * @throws IOException of {@link IOUtils#read(InputStream)}
     */
    public String getContent() throws IOException {
        return IOUtils.read(getInputStream(), true);
    }

    /**
     * Gets and closes InputStream of current connection.
     * <b>Creates new connection on each call</b>
     *
     * @throws IOException of {@link Http#getInputStream()}
     */
    public void closeInputStream() throws IOException {
        getInputStream().close();
    }

    /**
     * Gets InputStream of current connection.
     * <b>Creates new connection on each call</b>
     *
     * @return InputStream of connection
     * @throws IOException of {@link Http#getConnection()}
     */
    public InputStream getInputStream() throws IOException {
        return getConnection().getInputStream();
    }

    /**
     * Gets InputStream of current connection and applies consumer then closes InputStream.
     * <b>Creates new connection on each call</b>
     *
     * <pre>{@code
     *         try {
     *             String result = Http.create("https://example.com")
     *                     .consumeInputStream(IOUtils::read);
     *         } catch (IOException e) {
     *             System.out.println("Something went wrong");
     *         }
     * }</pre>
     *
     * @param function    function which will consume InputStream
     * @param <R>         type of return
     * @return <R> of your expression or null on exception.
     */
    @SuppressWarnings("unchecked")
    public <R, X extends Throwable> R consumeInputStream(ThrowFunction<InputStream, R, X> function) throws X {
        try (InputStream is = getInputStream()) {
            return function.apply(is);
        } catch (Throwable t) {
            throw (X) t;
        }
    }

    /**
     * Gets {@link HttpURLConnection} with provided params,
     * request method, and body.
     * <b>Creates new connection on each call</b>
     *
     * @return HttpURLConnection
     * @throws IOException of connection
     */
    public HttpURLConnection getConnection() throws IOException {
        return getConnection(null);
    }

    /**
     * Gets {@link HttpURLConnection} with provided params,
     * request method, and body.
     * <b>Creates new connection on each call</b>
     *
     * @param customSettings custom settings of connection which they
     *                       will be consumed after initializing http.
     * @return HttpURLConnection
     * @throws IOException of connection
     */
    public HttpURLConnection getConnection(Consumer<HttpURLConnection> customSettings) throws IOException {
        if (method == Method.GET && params != null && !url.contains("?"))
            url += "?" + params.toString();

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);
        if (customSettings != null) customSettings.accept(conn);

        conn.setInstanceFollowRedirects(followRedirects);
        conn.setRequestProperty("User-Agent", userAgent);
        if (!headers.isEmpty()) headers.forEach(conn::setRequestProperty);

        if (method == Method.POST && params != null) {
            byte[] data = params.getBytes();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data);
            }
        }
        if (suppliers != null) suppliers.forEach(Runnable::run);

        return conn;
    }
}