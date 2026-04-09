package com.shortly.kgsservice.test.integration;

import com.shortly.kgsservice.constant.Constant;
import com.shortly.kgsservice.enumaration.StatusType;
import com.shortly.kgsservice.model.ShortlyKey;
import com.shortly.kgsservice.repository.ShortlyKeyRepository;
import com.shortly.kgsservice.service.KeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class KeyServiceIntegrationTest {

    @Autowired
    private KeyService keyService;

    @Autowired
    private ShortlyKeyRepository shortlyKeyRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {

        // clear MongoDB
        shortlyKeyRepository.deleteAll();

        // clear Redis
        redisTemplate.delete(Constant.REDIS_QUEUE_NAME);

        // insert ke Mongo
        ShortlyKey key = ShortlyKey.builder()
                .key("integration123")
                .status(StatusType.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();

        shortlyKeyRepository.save(key);

        // push ke Redis
        redisTemplate.opsForList()
                .leftPush(Constant.REDIS_QUEUE_NAME, "integration123");
    }

    @Test
    void shouldGetKeyFromRedisAndUpdateDb() {

        String result = keyService.getKey();

        assertEquals("integration123", result);

        ShortlyKey updated = shortlyKeyRepository.findByKey("integration123")
                .orElseThrow();

        assertEquals(StatusType.USED, updated.getStatus());
    }
}