package com.shortly.apiservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortly.apiservice.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        try {
            return Optional.ofNullable(objectMapper.readValue(value, clazz));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Error while parsing value. error message {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        try {
            return Optional.ofNullable(objectMapper.readValue(value, clazz));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Error while parsing value. error message {}", e.getMessage());
            return Optional.empty();
        }    }

    @Override
    public <T> void put(String key, T value) {
        try {
            String jasonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jasonValue);
        } catch (JsonProcessingException e) {
            log.error("Error while put value. error message {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            String jasonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jasonValue, ttl);
        } catch (JsonProcessingException e) {
            log.error("Error while put value. error message {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }
}
