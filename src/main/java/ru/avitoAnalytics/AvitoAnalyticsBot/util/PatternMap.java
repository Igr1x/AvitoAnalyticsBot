package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class PatternMap<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final Map<Predicate<K>, V> patternMap = new HashMap<>();

    public void put(K key, V value) {
        map.put(key, value);
    }

    public void putPattern(Predicate<K> pattern, V value) {
        patternMap.put(pattern, value);
    }

    public V get(K key) {
        V value = map.get(key);
        if (value != null) {
            return value;
        }
        for (Map.Entry<Predicate<K>, V> entry : patternMap.entrySet()) {
            if (entry.getKey().test(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        if (map.containsKey(key)) return true;
        for (Map.Entry<Predicate<K>, V> entry : patternMap.entrySet()) {
            if (entry.getKey().test(key)) {
                return true;
            }
        }
        return false;
    }
}
