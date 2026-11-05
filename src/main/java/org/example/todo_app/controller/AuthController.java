package org.example.todo_app.controller;

import org.example.todo_app.dto.AuthResponse;
import org.example.todo_app.dto.LoginRequest;
import org.example.todo_app.dto.RegisterRequest;
import org.example.todo_app.model.AuthSession;
import org.example.todo_app.model.User;
import org.example.todo_app.repository.AuthSessionRepository;
import org.example.todo_app.repository.UserRepository;
import org.example.todo_app.service.CurrentUserService;
import org.example.todo_app.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final String botUsername;

    public AuthController(AuthSessionRepository authSessionRepository, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtService jwtService,
                          CurrentUserService currentUserService,
                          @Value("${telegram.bot.username}") String botUsername) {
        this.authSessionRepository = authSessionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
        this.botUsername = botUsername;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String name = required(request.name(), "name");
        String username = required(request.username(), "username").toLowerCase(Locale.ROOT);
        String email = required(request.email(), "email").toLowerCase(Locale.ROOT);
        String password = request.password();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is invalid or password is shorter than 8 characters"));
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent() || userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email or username is already in use"));
        }
        User user = new User(name, username, null);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(user, jwtService.create(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        User user = userRepository.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null || user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
        }
        return ResponseEntity.ok(AuthResponse.from(user, jwtService.create(user)));
    }

    @PostMapping("/init")
    @Transactional
    public ResponseEntity<?> initAuthSession(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User user = currentUserService.require(authorization);
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthSession session = new AuthSession(token, "PENDING", LocalDateTime.now().plusMinutes(5));
        session.setUser(user);
        authSessionRepository.save(session);
        return ResponseEntity.ok(Map.of("token", token, "botUrl", "https://t.me/" + botUsername + "?start=" + token));
    }

    @GetMapping("/status")
    @Transactional
    public ResponseEntity<?> checkAuthStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestParam("token") String token) {
        User requester = currentUserService.require(authorization);
        AuthSession session = authSessionRepository.findBySessionToken(token).orElse(null);
        if (session == null || session.getUser() == null || !session.getUser().getId().equals(requester.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "INVALID"));
        }
        if (LocalDateTime.now().isAfter(session.getExpiresAt()) && "PENDING".equals(session.getStatus())) {
            session.setStatus("EXPIRED");
            authSessionRepository.save(session);
        }
        if ("CONFIRMED".equals(session.getStatus())) {
            User user = session.getUser();
            return ResponseEntity.ok(Map.of("status", "CONFIRMED", "userId", user.getId(), "name", user.getName(),
                    "username", user.getUsername(), "chatId", user.getTelegramChatId()));
        }
        return ResponseEntity.ok(Map.of("status", session.getStatus()));
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank() || value.trim().length() > 120) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }
}
