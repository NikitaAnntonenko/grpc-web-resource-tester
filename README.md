# Grpc Web Resource Tester

Implement gRPC server and client that checks web resources for response using http client.

Code tested with JUnit4 because now gRPC don't completely support JUnit 5 [here the issue](https://github.com/grpc/grpc-java/issues/5331)

For JUnit 5 need [GrpcCleanupExtension](https://github.com/asarkar/grpc-test) implementation.