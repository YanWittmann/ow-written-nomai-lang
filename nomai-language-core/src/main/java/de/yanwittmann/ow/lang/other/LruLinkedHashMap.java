package de.yanwittmann.ow.lang.other;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public LruLinkedHashMap(int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
