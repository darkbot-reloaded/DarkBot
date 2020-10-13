package com.github.manolo8.darkbot.core.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class WeakValueHashMap<K, V> {
    private final HashMap<K, WeakRef> references = new HashMap<>();
    private final ReferenceQueue<V> queue = new ReferenceQueue<>();

    public V put(K key, V value) {
        WeakRef valueRef = new WeakRef(key, value);
        return getReference(references.put(key, valueRef));
    }

    public V get(K key) {
        removeGarbageCollected();
        return getReference(references.get(key));
    }

    public void clear() {
        references.clear();
    }

    private V getReference(WeakRef weakRef) {
        return weakRef != null ? weakRef.get() : null;
    }

    @SuppressWarnings("unchecked")
    private void removeGarbageCollected() {
        WeakRef weakRef;

        while ((weakRef = (WeakRef) this.queue.poll()) != null)
            this.references.remove(weakRef.getKey());
    }

    private class WeakRef extends WeakReference<V> {
        private final K key;

        private WeakRef(K key, V value) {
            super(value, queue);
            this.key = key;
        }

        private K getKey() {
            return key;
        }
    }
}