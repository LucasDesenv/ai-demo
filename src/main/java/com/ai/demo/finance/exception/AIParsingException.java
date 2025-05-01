package com.ai.demo.finance.exception;

public class AIParsingException extends RuntimeException {
    public AIParsingException(String message, Exception e) {
        super(message, e);
    }
}
