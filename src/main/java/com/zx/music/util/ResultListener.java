package com.zx.music.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ResultListener<T> {
    private final Map<String, CompletableFuture<T>> map = new HashMap<>();


    public synchronized boolean register(String key, BiConsumer<T, ? super Throwable> action) {
        if (map.containsKey(key)) {
            map.get(key).whenComplete(action);
            return false;
        }
        map.put(key, new CompletableFuture<>());
        return true;
    }

    public synchronized void release(String key, T result) {
        CompletableFuture<T> future = map.remove(key);
        if (future != null) {
            future.complete(result);
        }
    }
}
