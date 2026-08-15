package com.emirhanunsal.mcservercontroller.client;

import com.emirhanunsal.mcservercontroller.config.AwsProperties;
import com.emirhanunsal.mcservercontroller.dto.LambdaResult;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import com.emirhanunsal.mcservercontroller.exception.LambdaInvocationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LambdaMinecraftClientTest {
    private LambdaClient sdk;
    private LambdaMinecraftClient client;

    @BeforeEach void setUp() {
        sdk = mock(LambdaClient.class);
        client = new LambdaMinecraftClient(sdk, new ObjectMapper(), new AwsProperties("eu-central-1", new AwsProperties.Lambda("test-function")));
    }

    @Test void parsesProxyResponseBody() {
        LambdaResult result = client.parseResponse("{\"statusCode\":200,\"body\":\"{\\\"state\\\":\\\"running\\\",\\\"minecraftAddress\\\":\\\"mc.example.com\\\"}\"}");
        assertEquals(ServerState.RUNNING, result.state()); assertEquals("mc.example.com", result.minecraftAddress());
    }

    @Test void parsesDirectResponseAndMapsStates() {
        assertEquals(ServerState.STARTING, client.parseResponse("{\"state\":\"pending\"}").state());
        assertEquals(ServerState.STOPPING, client.parseResponse("{\"state\":\"shutting-down\"}").state());
        assertEquals(ServerState.UNKNOWN, client.parseResponse("{\"state\":\"rebooting\"}").state());
    }

    @Test void rejectsMalformedAndEmptySemanticResponses() {
        assertThrows(LambdaInvocationException.class, () -> client.parseResponse("not-json"));
        assertThrows(LambdaInvocationException.class, () -> client.parseResponse("{}"));
    }

    @Test void invokesLambdaSynchronously() {
        when(sdk.invoke(any(software.amazon.awssdk.services.lambda.model.InvokeRequest.class))).thenReturn(InvokeResponse.builder().statusCode(200).payload(SdkBytes.fromUtf8String("{\"state\":\"stopped\"}")).build());
        assertEquals(ServerState.STOPPED, client.invoke("status").state());
        verify(sdk).invoke(argThat((software.amazon.awssdk.services.lambda.model.InvokeRequest r) ->
                r.invocationTypeAsString().equals("RequestResponse") && r.payload().asUtf8String().contains("status")));
    }

    @Test void wrapsAwsAndFunctionErrors() {
        when(sdk.invoke(any(software.amazon.awssdk.services.lambda.model.InvokeRequest.class))).thenThrow(LambdaException.builder().message("denied").build());
        assertThrows(LambdaInvocationException.class, () -> client.invoke("start"));
        reset(sdk);
        when(sdk.invoke(any(software.amazon.awssdk.services.lambda.model.InvokeRequest.class))).thenReturn(InvokeResponse.builder().functionError("Unhandled").payload(SdkBytes.fromUtf8String("{}")).build());
        assertThrows(LambdaInvocationException.class, () -> client.invoke("stop"));
    }
}
