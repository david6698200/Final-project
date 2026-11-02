package com.bugtracker;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
