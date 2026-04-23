package com.example.aisales_backend.exception;

public class SentimentAnalysisException extends RuntimeException {
    public SentimentAnalysisException(String message) {
        super(message);
    }

    public SentimentAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
