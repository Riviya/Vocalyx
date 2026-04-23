package com.example.aisales_backend.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entity, Long id) {
        super(String.format("%s not found with ID: %d", entity, id));
    }

    public EntityNotFoundException(String entity, String field, String value) {
        super(String.format("%s not found with %s: %s", entity, field, value));
    }
}
