package org.example.todo_app.controller;

import org.example.todo_app.model.AuthSession;
import org.example.todo_app.model.User;
import org.example.todo_app.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthSessionRepository authSessionRepository;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public AuthController(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    @PostMapping("/init")
    public ResponseEntity<?> initAuthSession() {
        String token = UUID.randomUUID().toString();

        AuthSession session = new AuthSession(token, "PENDING", LocalDateTime.now().plusMinutes(5));
        authSessionRepository.save(session);

        String botUrl = "https://t.me/" + botUsername + "?start=" + token;

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("botUrl", botUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus(@RequestParam("token") String token) {
        Optional<AuthSession> sessionOpt = authSessionRepository.findBySessionToken(token);
        Map<String, Object> response = new HashMap<>();

        if (sessionOpt.isEmpty()) {
            response.put("status", "INVALID");
            return ResponseEntity.badRequest().body(response);
        }

        AuthSession session = sessionOpt.get();

        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            session.setStatus("EXPIRED");
            authSessionRepository.save(session);
            response.put("status", "EXPIRED");
            return ResponseEntity.ok(response);
        }

        if ("CONFIRMED".equals(session.getStatus()) && session.getUser() != null) {
            User user = session.getUser();
            response.put("status", "CONFIRMED");
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("username", user.getUsername());
            response.put("chatId", user.getTelegramChatId());
            return ResponseEntity.ok(response);
        }

        response.put("status", "PENDING");
        return ResponseEntity.ok(response);
    }
}