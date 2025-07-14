package com.example.BookingApp.exception;

public class EventException extends RuntimeException {
    public EventException(String message) {
        super(message);
    }
    
    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}