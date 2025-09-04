package ru.ext.edu.postgres.blog.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityType, Long id) {
        super(String.format("%s with id %d not found", entityType, id));
    }
}