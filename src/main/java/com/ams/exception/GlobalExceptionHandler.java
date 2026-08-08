package com.ams.exception;

import com.ams.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                )
        );
    }
}
