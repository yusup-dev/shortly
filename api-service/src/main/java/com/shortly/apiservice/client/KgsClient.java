package com.shortly.apiservice.client;

import com.shortly.proto.key.Empty;
import com.shortly.proto.key.KeyResponse;
import com.shortly.proto.key.KeyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KgsClient {

    private final KeyServiceGrpc.KeyServiceBlockingStub stub;

    public KgsClient() {
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("localhost", 9090)
                    .usePlaintext()
                    .build();

            this.stub = KeyServiceGrpc.newBlockingStub(channel);

            log.info("KGS Client Initialized");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getKey() {
        try {
            Empty request = Empty.newBuilder().build();

            KeyResponse response = stub
                    .withDeadlineAfter(2, TimeUnit.SECONDS)
                    .getKey(request);

            return response.getKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get key from KGS", e);
        }
    }
}
