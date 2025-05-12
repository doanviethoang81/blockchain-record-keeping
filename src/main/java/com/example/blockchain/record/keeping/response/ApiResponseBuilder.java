package com.example.blockchain.record.keeping.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseBuilder {
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .data(data)
                        .build()
        );
    }

    //400 dữ liệu không hợp lệ
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<T>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    //401  khi người dùng chưa đăng nhập hoặc thông tin đăng nhập không hợp lệ.
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<T>builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    //403 không có đủ quyền
    public static <T> ResponseEntity<ApiResponse> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.<T>builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    //500
    public static <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<T>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> listBadRequest(String message, T data) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<T>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .data(data)
                        .build()
        );
    }



}
