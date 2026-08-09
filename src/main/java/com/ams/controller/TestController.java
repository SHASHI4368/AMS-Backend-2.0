package com.ams.controller;

import com.ams.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path}/test")
public class TestController {
    @GetMapping("")
    public ResponseEntity<ApiResponse<Void>> test() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Test successful",
                        null
                )
        );
    }
}
