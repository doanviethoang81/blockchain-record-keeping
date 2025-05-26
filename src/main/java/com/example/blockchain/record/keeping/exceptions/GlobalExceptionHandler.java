package com.example.blockchain.record.keeping.exceptions;

import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Bạn không có quyền thực hiện hành động này!");
    }

    @ExceptionHandler(ListBadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(ListBadRequestException ex) {
        return ApiResponseBuilder.listBadRequest(ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        return ApiResponseBuilder.badRequest(ex.getMessage());
    }
}
