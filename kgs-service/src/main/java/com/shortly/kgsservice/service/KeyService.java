package com.shortly.kgsservice.service;

import com.shortly.kgsservice.constant.Constant;
import com.shortly.kgsservice.enumaration.StatusType;
import com.shortly.kgsservice.model.ShortlyKey;
import com.shortly.kgsservice.repository.ShortlyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ShortlyKeyRepository shortlyKeyRepository;
    private final GeneratorService generatorService;

    public String getKey() {

        // 1. Check queue length
        Long queueLen = redisTemplate.opsForList().size(Constant.REDIS_QUEUE_NAME);

        if (queueLen != null && queueLen < Constant.QUEUE_LENGTH) {
            log.info("Queue length is low, generating more keys");
            generatorService.generateKeys(Constant.KEY_COUNT);
        }

        // 2. Pop key from Redis (RPOP)
        String keyVal = redisTemplate.opsForList().rightPop(Constant.REDIS_QUEUE_NAME);

        if( keyVal == null) {
            throw new RuntimeException("No key available in Redis");
        }

        // 3. Update MongoDB status -> USED
        Optional<ShortlyKey> optionalKey = shortlyKeyRepository.findByKey(keyVal);

        if(optionalKey.isEmpty()) {
            redisTemplate.opsForList().leftPush(Constant.REDIS_QUEUE_NAME, keyVal);
            throw new RuntimeException("Key not found in DB, rollback to Redis");
        }

        ShortlyKey shortlyKey = optionalKey.get();
        shortlyKey.setStatus(StatusType.USED);
        shortlyKeyRepository.save(shortlyKey);

        log.info("Key status updated in database");

        return keyVal;
    }
 }
