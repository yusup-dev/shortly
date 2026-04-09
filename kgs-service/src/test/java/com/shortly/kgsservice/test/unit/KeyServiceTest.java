package com.shortly.kgsservice.test.unit;

import com.shortly.kgsservice.constant.Constant;
import com.shortly.kgsservice.enumaration.StatusType;
import com.shortly.kgsservice.model.ShortlyKey;
import com.shortly.kgsservice.repository.ShortlyKeyRepository;
import com.shortly.kgsservice.service.GeneratorService;
import com.shortly.kgsservice.service.KeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KeyServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private GeneratorService generatorService;

    @Mock
    private ShortlyKeyRepository shortlyKeyRepository;

    @InjectMocks
    private KeyService keyService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }


    @Test
    void shouldReturnKeySuccessfully() {

        // Mock queue length
        when(listOperations.size(Constant.REDIS_QUEUE_NAME)).thenReturn(20L);

        // Mock Redis pop
        when(listOperations.rightPop(Constant.REDIS_QUEUE_NAME)).thenReturn("abc123");

        // Mock DB
        ShortlyKey shortlyKey = ShortlyKey.builder()
                .key("abc123")
                .status(StatusType.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
        when(shortlyKeyRepository.findByKey("abc123"))
                .thenReturn(Optional.of(shortlyKey));

        String result = keyService.getKey();

        assertEquals("abc123", result);
        verify(shortlyKeyRepository).save(any());
    }

    @Test
    void shouldGenerateKeysWhenQueueLow() {
        when(listOperations.size(Constant.REDIS_QUEUE_NAME)).thenReturn(5L);
        when(listOperations.rightPop(Constant.REDIS_QUEUE_NAME)).thenReturn("xyz789");

        ShortlyKey shortlyKey = ShortlyKey.builder()
                .key("abc123")
                .status(StatusType.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
        when(shortlyKeyRepository.findByKey("xyz789"))
                .thenReturn(Optional.of(shortlyKey));

        keyService.getKey();

        verify(generatorService).generateKeys(anyInt());
    }

    @Test
    void shouldRollbackToRedisWhenDbFail() {

        when(listOperations.size(Constant.REDIS_QUEUE_NAME)).thenReturn(20L);
        when(listOperations.rightPop(Constant.REDIS_QUEUE_NAME)).thenReturn("fail123");

        when(shortlyKeyRepository.findByKey("fail123"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            keyService.getKey();
        });

        verify(listOperations).leftPush(Constant.REDIS_QUEUE_NAME, "fail123");
    }

    @Test
    void shouldThrowWhenRedisEmpty() {

        when(listOperations.size(Constant.REDIS_QUEUE_NAME)).thenReturn(20L);
        when(listOperations.rightPop(Constant.REDIS_QUEUE_NAME)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            keyService.getKey();
        });
    }


}