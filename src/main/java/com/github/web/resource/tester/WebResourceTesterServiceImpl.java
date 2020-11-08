package com.github.web.resource.tester;

import com.proto.web.resource.tester.TestResourceRequest;
import com.proto.web.resource.tester.TestResourceResponse;
import com.proto.web.resource.tester.WebResourceTesterServiceGrpc;
import io.grpc.stub.StreamObserver;

public class WebResourceTesterServiceImpl extends WebResourceTesterServiceGrpc.WebResourceTesterServiceImplBase {

    @Override
    public void testResource(TestResourceRequest request, StreamObserver<TestResourceResponse> responseObserver) {

    }
}
