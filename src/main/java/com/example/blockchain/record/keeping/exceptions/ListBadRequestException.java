package com.example.blockchain.record.keeping.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ListBadRequestException extends RuntimeException {
    private final List<String> errors;

    public ListBadRequestException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
}
