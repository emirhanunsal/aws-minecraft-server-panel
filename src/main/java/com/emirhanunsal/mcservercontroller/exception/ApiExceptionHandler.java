package com.emirhanunsal.mcservercontroller.exception;

import com.emirhanunsal.mcservercontroller.dto.ServerResponse;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(LambdaInvocationException.class)
    ResponseEntity<ServerResponse> lambdaFailure(LambdaInvocationException exception) {
        log.warn("event=api_lambda_failure error_type={}", exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ServerResponse(false, ServerState.ERROR, "Unable to communicate with the Minecraft server controller", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ServerResponse> invalidInput(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ServerResponse(false, ServerState.ERROR, "Invalid request", null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ServerResponse> unexpected(Exception exception) {
        log.error("event=api_unexpected_failure error_type={}", exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServerResponse(false, ServerState.ERROR, "An unexpected error occurred", null));
    }
}
