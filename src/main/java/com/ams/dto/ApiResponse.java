package com.ams.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T body
) {

}
