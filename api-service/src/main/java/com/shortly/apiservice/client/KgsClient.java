package com.shortly.apiservice.client;

import com.shortly.proto.key.Empty;
import com.shortly.proto.key.KeyResponse;
import com.shortly.proto.key.KeyServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class KgsClient {

    private final ManagedChannel channel;
    private KeyServiceGrpc.KeyServiceBlockingStub stub;


    @PostConstruct
    public void init() {
        this.stub = KeyServiceGrpc.newBlockingStub(channel);
        log.info("KGS Client Initialized");
    }

    @Retry(name = "kgsRetry", fallbackMethod = "fallbackKey")
    @CircuitBreaker(name = "kgsCircuitBreaker", fallbackMethod = "fallbackKey")
    public String getKey() {
        log.info("Calling KGS service...");

        KeyResponse response = stub
                .withDeadlineAfter(2, TimeUnit.SECONDS)
                .getKey(Empty.newBuilder().build());

        return response.getKey();
    }

    public String fallbackKey(Throwable t){
        log.error("KGS failed, fallback triggered", t);

        return generateFallbackKey();
    }

    private String generateFallbackKey() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}
