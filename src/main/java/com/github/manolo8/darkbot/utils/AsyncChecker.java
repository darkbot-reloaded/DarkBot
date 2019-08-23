package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.Main;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class AsyncChecker<T> {
    private final CompletableFuture<T> futureValue;
    private T value = null;

    protected AsyncChecker(String url, Supplier<T> fallback) {
        Type[] types = ReflectionUtils.findGenericParameters(getClass(), AsyncChecker.class);
        if (types == null || types.length == 0)
            throw new UnsupportedOperationException("Can't create async checker without found type");
        this.futureValue = CompletableFuture.supplyAsync(() -> this.getRemote(url, types[0]))
                .exceptionally(exc -> fallback.get());
    }

    private T getRemote(String url, Type type) {
        try {
            return Main.GSON.fromJson(new InputStreamReader(new URL(url).openConnection().getInputStream(), StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public T get() {
        if (value == null) value = futureValue.join();
        return value;
    }
}
