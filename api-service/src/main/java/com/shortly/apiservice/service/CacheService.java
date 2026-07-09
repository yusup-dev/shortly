package com.shortly.apiservice.service;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    <T>Optional<T> get(String key, Class<T> clazz);
    <T> void put(String key, T value, Duration ttl);
    Optional<byte[]> getBytes(String key);
    void putBytes(String key, byte[] value, Duration ttl);
    void evict(String key);
}
