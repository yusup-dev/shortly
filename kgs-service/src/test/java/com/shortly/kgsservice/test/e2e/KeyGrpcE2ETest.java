package com.shortly.kgsservice.test.e2e;

import com.shortly.kgsservice.constant.Constant;
import com.shortly.kgsservice.enumaration.StatusType;
import com.shortly.kgsservice.model.ShortlyKey;
import com.shortly.kgsservice.repository.ShortlyKeyRepository;
import com.shortly.proto.key.Empty;
import com.shortly.proto.key.KeyResponse;
import com.shortly.proto.key.KeyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
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
public class KeyGrpcE2ETest {

    private static ManagedChannel channel;
    private static KeyServiceGrpc.KeyServiceBlockingStub stub;

    @Autowired
    private ShortlyKeyRepository shortlyKeyRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {

        // Setup gRPC client (port default: 9090)
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        stub = KeyServiceGrpc.newBlockingStub(channel);

        // Clear Mongo
        shortlyKeyRepository.deleteAll();

        // Clear Redis
        redisTemplate.delete(Constant.REDIS_QUEUE_NAME);

        // Insert ke Mongo
        ShortlyKey key = ShortlyKey.builder()
                .key("e2e123")
                .status(StatusType.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();

        shortlyKeyRepository.save(key);

        // Push ke Redis
        redisTemplate.opsForList()
                .leftPush(Constant.REDIS_QUEUE_NAME, "e2e123");
    }

    @AfterAll
    static void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Test
    void shouldGetKeyViaGrpcAndUpdateDatabase() {

        // Call gRPC
        KeyResponse response = stub.getKey(Empty.newBuilder().build());

        // Assert response
        assertEquals("e2e123", response.getKey());

        // Validate DB updated
        ShortlyKey updated = shortlyKeyRepository.findByKey("e2e123")
                .orElseThrow();

        assertEquals(StatusType.USED, updated.getStatus());
    }
}