package com.example.blockchain.record.keeping.exceptions;

import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;

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

    @ExceptionHandler(NotFoundRequestException.class)
    public ResponseEntity<?> handleNotFoundRequestException(NotFoundRequestException ex) {
        return ApiResponseBuilder.notFound(ex.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", 401, "message", "Token expired"));
    }
}
