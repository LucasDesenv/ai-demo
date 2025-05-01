package com.ai.demo.finance.exception;

public class AICommunicationException extends RuntimeException {
    public AICommunicationException(String message, Exception e) {
        super(message, e);
    }
}
