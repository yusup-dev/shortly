package com.shortly.kgsservice.service;

import com.shortly.proto.key.Empty;
import com.shortly.proto.key.KeyResponse;
import com.shortly.proto.key.KeyServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class KeyServiceImpl extends KeyServiceGrpc.KeyServiceImplBase {

    private final KeyService keyService;

    @Override
    public void getKey(Empty request, StreamObserver<KeyResponse> responseObserver) {

        String key = keyService.getKey();

        KeyResponse response = KeyResponse.newBuilder()
                .setKey(key)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
