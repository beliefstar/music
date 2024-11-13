package com.zx.music.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IpBlackHandler {

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    public void add(String ip) {
        cache.put(ip, ip);
    }

    public boolean contains(String ip) {
        return cache.getIfPresent(ip) != null;
    }

    public void remove(String ip) {
        cache.invalidate(ip);
    }
}
