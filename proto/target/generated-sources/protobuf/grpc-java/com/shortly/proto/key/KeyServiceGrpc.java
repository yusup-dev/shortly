package com.shortly.proto.key;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.1)",
    comments = "Source: key.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class KeyServiceGrpc {

  private KeyServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "key.KeyService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.shortly.proto.key.Empty,
      com.shortly.proto.key.KeyResponse> getGetKeyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetKey",
      requestType = com.shortly.proto.key.Empty.class,
      responseType = com.shortly.proto.key.KeyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shortly.proto.key.Empty,
      com.shortly.proto.key.KeyResponse> getGetKeyMethod() {
    io.grpc.MethodDescriptor<com.shortly.proto.key.Empty, com.shortly.proto.key.KeyResponse> getGetKeyMethod;
    if ((getGetKeyMethod = KeyServiceGrpc.getGetKeyMethod) == null) {
      synchronized (KeyServiceGrpc.class) {
        if ((getGetKeyMethod = KeyServiceGrpc.getGetKeyMethod) == null) {
          KeyServiceGrpc.getGetKeyMethod = getGetKeyMethod =
              io.grpc.MethodDescriptor.<com.shortly.proto.key.Empty, com.shortly.proto.key.KeyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetKey"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shortly.proto.key.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shortly.proto.key.KeyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new KeyServiceMethodDescriptorSupplier("GetKey"))
              .build();
        }
      }
    }
    return getGetKeyMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static KeyServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<KeyServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<KeyServiceStub>() {
        @java.lang.Override
        public KeyServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new KeyServiceStub(channel, callOptions);
        }
      };
    return KeyServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static KeyServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<KeyServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<KeyServiceBlockingStub>() {
        @java.lang.Override
        public KeyServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new KeyServiceBlockingStub(channel, callOptions);
        }
      };
    return KeyServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static KeyServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<KeyServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<KeyServiceFutureStub>() {
        @java.lang.Override
        public KeyServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new KeyServiceFutureStub(channel, callOptions);
        }
      };
    return KeyServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getKey(com.shortly.proto.key.Empty request,
        io.grpc.stub.StreamObserver<com.shortly.proto.key.KeyResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetKeyMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service KeyService.
   */
  public static abstract class KeyServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return KeyServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service KeyService.
   */
  public static final class KeyServiceStub
      extends io.grpc.stub.AbstractAsyncStub<KeyServiceStub> {
    private KeyServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KeyServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new KeyServiceStub(channel, callOptions);
    }

    /**
     */
    public void getKey(com.shortly.proto.key.Empty request,
        io.grpc.stub.StreamObserver<com.shortly.proto.key.KeyResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetKeyMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service KeyService.
   */
  public static final class KeyServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<KeyServiceBlockingStub> {
    private KeyServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KeyServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new KeyServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.shortly.proto.key.KeyResponse getKey(com.shortly.proto.key.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetKeyMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service KeyService.
   */
  public static final class KeyServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<KeyServiceFutureStub> {
    private KeyServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KeyServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new KeyServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shortly.proto.key.KeyResponse> getKey(
        com.shortly.proto.key.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetKeyMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_KEY = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_KEY:
          serviceImpl.getKey((com.shortly.proto.key.Empty) request,
              (io.grpc.stub.StreamObserver<com.shortly.proto.key.KeyResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetKeyMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.shortly.proto.key.Empty,
              com.shortly.proto.key.KeyResponse>(
                service, METHODID_GET_KEY)))
        .build();
  }

  private static abstract class KeyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    KeyServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shortly.proto.key.Key.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("KeyService");
    }
  }

  private static final class KeyServiceFileDescriptorSupplier
      extends KeyServiceBaseDescriptorSupplier {
    KeyServiceFileDescriptorSupplier() {}
  }

  private static final class KeyServiceMethodDescriptorSupplier
      extends KeyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    KeyServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (KeyServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new KeyServiceFileDescriptorSupplier())
              .addMethod(getGetKeyMethod())
              .build();
        }
      }
    }
    return result;
  }
}
