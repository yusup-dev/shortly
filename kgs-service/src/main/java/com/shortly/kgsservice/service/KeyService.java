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

        int retry = 0;

        while (retry < 3) {

            Long queueLen = redisTemplate.opsForList()
                    .size(Constant.REDIS_QUEUE_NAME);

            log.info("Queue size: {}", queueLen);

            // =========================
            // 1. GENERATE IF LOW
            // =========================
            if (queueLen == null || queueLen < Constant.QUEUE_LENGTH) {
                log.info("Queue is low or empty, generating keys...");
                generatorService.generateKeys(Constant.KEY_COUNT);
            }

            // =========================
            // 2. POP FROM REDIS
            // =========================
            String keyVal = redisTemplate.opsForList()
                    .rightPop(Constant.REDIS_QUEUE_NAME);

            if (keyVal == null) {
                log.warn("No key in Redis, regenerating...");
                generatorService.generateKeys(Constant.KEY_COUNT);
                retry++;
                continue;
            }

            // =========================
            // 3. CHECK IN MONGO
            // =========================
            Optional<ShortlyKey> optionalKey =
                    shortlyKeyRepository.findByKey(keyVal);

            if (optionalKey.isEmpty()) {

                log.error("Key {} exists in Redis but NOT in Mongo!", keyVal);

                // ⚠️ jangan balikin ke Redis (biar tidak loop terus)
                // regenerate instead
                generatorService.generateKeys(Constant.KEY_COUNT);

                retry++;
                continue;
            }

            // =========================
            // 4. MARK AS USED
            // =========================
            ShortlyKey shortlyKey = optionalKey.get();
            shortlyKey.setStatus(StatusType.USED);
            ShortlyKey save = shortlyKeyRepository.save(shortlyKey);


            log.info("Key {} marked as USED", keyVal);
            log.info("Save {} mark", save);

            return keyVal;
        }

        // =========================
        // 5. FAIL SAFE
        // =========================
        throw new RuntimeException("Failed to get valid key after retries");
    }
 }
