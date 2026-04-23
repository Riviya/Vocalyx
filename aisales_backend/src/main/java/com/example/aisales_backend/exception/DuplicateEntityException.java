package com.example.aisales_backend.exception;

public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String entity, String field, String value) {
        super(String.format("%s with %s '%s' already exists", entity, field, value));
    }
}
