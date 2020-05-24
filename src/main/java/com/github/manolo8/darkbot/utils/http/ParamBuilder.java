package com.github.manolo8.darkbot.utils.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ParamBuilder is used to make POST bodies, queries for GET request etc.
 */
public class ParamBuilder {
    protected Map<Object, Object> params = new LinkedHashMap<>();

    protected ParamBuilder() { }
    protected ParamBuilder(Object key, Object value) { this.params.put(key, value); }
    protected ParamBuilder(Map<Object, Object> params) { this.params.putAll(params); }

    /**
     * Creates new instance of ParamBuilder.
     *
     * @return new ParamBuilder
     */
    public static ParamBuilder create() {
        return new ParamBuilder();
    }

    /**
     * Creates new instance of ParamBuilder with parameter
     * Be aware, this parameter wont be encoded via {@link URLEncoder}
     *
     * @param key   of parameter.
     * @param value of parameter.
     * @return new ParamBuilder
     */
    public static ParamBuilder create(Object key, Object value) {
        return new ParamBuilder(key, value);
    }

    /**
     * Creates new instance of ParamBuilder with values of params arg.
     * Be aware, this parameter wont be encoded via {@link URLEncoder}
     *
     * @param params map of parameters.
     * @return new ParamBuilder
     */
    public static ParamBuilder create(Map<Object, Object> params) {
        return new ParamBuilder(params);
    }

    /**
     * Set or overwrite a parameter.
     * Will be encoded via URLEncoder in UTF-8
     *
     * @param key   of parameter.
     * @param value of parameter.
     * @return current instance of ParamBuilder
     */
    public ParamBuilder set(Object key, Object value) {
        this.params.put(encode(key), encode(value));
        return this;
    }

    /**
     * Set or overwrite parameters.
     * Will be encoded via URLEncoder in UTF-8
     *
     * @param params map of parameters.
     * @return current instance of ParamBuilder
     */
    public ParamBuilder set(Map<Object, Object> params) {
        params.forEach(this::set);
        return this;
    }

    /**
     * Set or overwrite a parameter without URLEncoder.
     *
     * @param key   of parameter.
     * @param value of parameter.
     * @return current instance of ParamBuilder
     */
    public ParamBuilder setRaw(Object key, Object value) {
        this.params.put(key, value);
        return this;
    }

    /**
     * Set or overwrite parameters without URLEncoder.
     *
     * @param params map of parameters.
     * @return current instance of ParamBuilder
     */
    public ParamBuilder setRaw(Map<Object, Object> params) {
        this.params.putAll(params);
        return this;
    }

    /**
     * Creates bytes from current params in UTF_8 encoding.
     *
     * @return byte array
     */
    public byte[] getBytes() {
        return toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a String of parameters from objects.
     * Example: "key=value&anotherKey=anotherValue".
     *
     * @return String of current parameters
     */
    public String toString() {
        return this.params.entrySet().stream()
                .map(Object::toString)
                .collect(Collectors.joining("&"));
    }

    /**
     * Get current HashMap of parameters.
     *
     * @return map with current parameters.
     */
    public Map<Object, Object> getParams() {
        return this.params;
    }

    /**
     * Checks if Objects is instance of String
     * and encodes it via {@link URLEncoder#encode(String, String)} in UTF-8
     * else returns raw value.
     *
     * @param value Object to encode
     * @return encoded String or raw Object if is not a String / on exception.
     */
    public static Object encode(Object value) {
        if (!(value instanceof String)) return value;
        return encode((String) value);
    }

    /**
     * Encodes String via {@link URLEncoder#encode(String, String)} in UTF-8
     *
     * @param value to encode
     * @return encoded String or raw value on exception
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return value;
        }
    }
}