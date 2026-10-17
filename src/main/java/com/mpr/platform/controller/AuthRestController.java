package com.mpr.platform.controller;

import com.mpr.platform.model.User;
import com.mpr.platform.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthRestController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            String email = payload.get("email");
            String password = payload.get("password");
            String role = payload.getOrDefault("role", "CUSTOMER");

            User user = userService.registerUser(name, email, password, role);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", user.getId());
            resp.put("name", user.getName());
            resp.put("email", user.getEmail());
            resp.put("role", user.getRole());
            resp.put("message", "User registered successfully");
            return new ResponseEntity<>(resp, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            String username = payload.get("email") != null ? payload.get("email") : payload.get("username");
            String password = payload.get("password");

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_CUSTOMER");

            Map<String, Object> resp = new HashMap<>();
            resp.put("username", auth.getName());
            resp.put("role", role.replace("ROLE_", ""));
            resp.put("status", "Authenticated");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email/username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
