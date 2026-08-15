package com.emirhanunsal.mcservercontroller.exception;

public class LambdaInvocationException extends RuntimeException {
    public LambdaInvocationException(String message) { super(message); }
    public LambdaInvocationException(String message, Throwable cause) { super(message, cause); }
}
