package com.womenconcern.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/public/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok", "service", "Women Concern API");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "username", jwt.getClaimAsString("preferred_username"),
                "email",    jwt.getClaimAsString("email"),
                "name",     jwt.getClaimAsString("name"),
                "roles",    jwt.getClaim("realm_access")
        );
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminTest() {
        return Map.of("message", "Admin access confirmed");
    }
}