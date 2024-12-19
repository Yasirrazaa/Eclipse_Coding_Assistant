package com.copilot.groq.api;

public class GroqApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GroqApiException(String message) {
        super(message);
    }

    public GroqApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
