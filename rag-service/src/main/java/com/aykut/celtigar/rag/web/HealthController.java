package com.aykut.celtigar.rag.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal health endpoint so the service is verifiably up during early phases.
 * Mirrors the Gateway's /healthz so every service answers the same probe.
 */
@RestController
public class HealthController {

    @GetMapping("/healthz")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "rag-service");
    }
}
