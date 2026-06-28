package com.mmotors.controller;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "M-Motors API"));
    }

    @GetMapping("/sentry-test")
    public void sentryTest() {
        try {
            throw new RuntimeException("Test Sentry — M-Motors API opérationnelle");
        } catch (RuntimeException e) {
            Sentry.captureException(e);
            throw e;
        }
    }
}
