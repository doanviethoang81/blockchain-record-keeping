package com.example.blockchain.record.keeping.exceptions;

public class NotFoundRequestException extends RuntimeException {
    public NotFoundRequestException(String message) {
        super(message);
    }
}
