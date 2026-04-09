package com.shortly.kgsservice.service;

import com.shortly.kgsservice.constant.Constant;
import com.shortly.kgsservice.enumaration.StatusType;
import com.shortly.kgsservice.model.ShortlyKey;
import com.shortly.kgsservice.repository.ShortlyKeyRepository;
import com.shortly.kgsservice.utils.Base62;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratorService {

    private final ShortlyKeyRepository shortlyKeyRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void generateKeys(int count) {

        List<ShortlyKey> keys = new ArrayList<>();
        List<String> redisKeys = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            String key = Base62.generateRandomKey(6);

            keys.add(
                    ShortlyKey.builder()
                            .key(key)
                            .status(StatusType.AVAILABLE)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            redisKeys.add(key);
        }

        // Insert to MongoDB
        if (!keys.isEmpty()){
            shortlyKeyRepository.saveAll(keys);
        }

        // Push to redis (LPUSH)
        if(!redisKeys.isEmpty()) {
                redisTemplate.opsForList().leftPushAll(Constant.REDIS_QUEUE_NAME, redisKeys);
        }

        log.info("Successfully generated and stored keys: " + count);
    }
}
