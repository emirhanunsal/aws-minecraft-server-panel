package com.emirhanunsal.mcservercontroller.client;

import com.emirhanunsal.mcservercontroller.config.AwsProperties;
import com.emirhanunsal.mcservercontroller.dto.LambdaResult;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import com.emirhanunsal.mcservercontroller.exception.LambdaInvocationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class LambdaMinecraftClient {
    private static final Logger log = LoggerFactory.getLogger(LambdaMinecraftClient.class);
    private static final Set<String> ACTIONS = Set.of("start", "stop", "status");
    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;
    private final AwsProperties properties;

    public LambdaMinecraftClient(LambdaClient lambdaClient, ObjectMapper objectMapper, AwsProperties properties) {
        this.lambdaClient = lambdaClient; this.objectMapper = objectMapper; this.properties = properties;
    }

    public LambdaResult invoke(String action) {
        if (!ACTIONS.contains(action)) throw new IllegalArgumentException("Unsupported action");
        log.info("event=lambda_invocation_start action={} function={}", action, properties.lambda().functionName());
        try {
            byte[] requestJson = objectMapper.writeValueAsBytes(java.util.Map.of("action", action));
            InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                    .functionName(properties.lambda().functionName()).invocationType(InvocationType.REQUEST_RESPONSE)
                    .payload(SdkBytes.fromByteArray(requestJson)).build());
            if (response.functionError() != null) throw new LambdaInvocationException("Lambda reported a function error");
            if (response.payload() == null || response.payload().asByteArray().length == 0) throw new LambdaInvocationException("Lambda returned an empty response");
            LambdaResult result = parseResponse(response.payload().asString(StandardCharsets.UTF_8));
            log.info("event=lambda_invocation_complete action={} state={}", action, result.state());
            return result;
        } catch (LambdaInvocationException e) { throw e;
        } catch (SdkException e) {
            log.warn("event=lambda_invocation_failed action={} error_type={}", action, e.getClass().getSimpleName());
            throw new LambdaInvocationException("AWS Lambda is unavailable", e);
        } catch (Exception e) {
            throw new LambdaInvocationException("Lambda returned an invalid response", e);
        }
    }

    LambdaResult parseResponse(String json) {
        try {
            JsonNode outer = objectMapper.readTree(json);
            if (outer == null || !outer.isObject()) throw new LambdaInvocationException("Lambda returned malformed JSON");
            if (outer.has("statusCode") && outer.path("statusCode").asInt() >= 400)
                throw new LambdaInvocationException("Lambda operation was unsuccessful");
            JsonNode body = outer.has("body") ? outer.get("body") : outer;
            if (body.isTextual()) body = objectMapper.readTree(body.asText());
            if (body == null || !body.isObject()) throw new LambdaInvocationException("Lambda response body is invalid");
            String rawState = body.path("state").asText(null);
            if (rawState == null) throw new LambdaInvocationException("Lambda response has no server state");
            return new LambdaResult(ServerState.fromAwsState(rawState), body.path("minecraftAddress").asText(null), body.path("message").asText(null));
        } catch (LambdaInvocationException e) { throw e;
        } catch (Exception e) { throw new LambdaInvocationException("Lambda returned malformed JSON", e); }
    }
}
