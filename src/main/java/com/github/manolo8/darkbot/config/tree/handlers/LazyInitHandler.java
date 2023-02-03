package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.utils.itf.LazyValue;
import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LazyInitHandler<T> extends DefaultHandler<T> {

    protected final Map<String, LazyValue<?>> lazyMetadata = new HashMap<>();

    public LazyInitHandler() {
        this(null);
    }

    public LazyInitHandler(@Nullable Field field) {
        super(field);
    }

    private void check(String key) {
        if (lazyMetadata.isEmpty()) return;
        LazyValue<?> supplier = lazyMetadata.remove(key);
        if (supplier != null) metadata.put(key, supplier.get());
    }

    @Override
    public <V> @Nullable V getMetadata(String key) {
        check(key);
        return super.getMetadata(key);
    }

    @Override
    public <V> @Nullable V getOrCreateMetadata(String key, Supplier<V> builder) {
        check(key);
        return super.getOrCreateMetadata(key, builder);
    }
}
