package org.example.todo_app.controller;

import org.example.todo_app.model.User;
import org.example.todo_app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    @Transactional
    public ResponseEntity<User> getCurrentUser() {
        List<User> users = userRepository.findAll();
        User user;
        if (users.isEmpty()) {
            user = new User("Jasur", "jasur", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150");
            user = userRepository.save(user);
        } else {
            user = users.get(0);
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/current")
    @Transactional
    public ResponseEntity<User> updateProfile(@RequestBody User updatedData) {
        List<User> users = userRepository.findAll();
        User user = users.isEmpty() ? new User() : users.get(0);

        user.setName(updatedData.getName());
        user.setUsername(updatedData.getUsername());
        user.setAvatarUrl(updatedData.getAvatarUrl());
        User saved = userRepository.save(user);

        log.info("[ПРОФИЛЬ ОБНОВЛЕН В БД] ID: {} | Имя: '{}' | Ник: '@{}'", saved.getId(), saved.getName(), saved.getUsername());
        return ResponseEntity.ok(saved);
    }
}