package org.example.todo_app.controller;

import org.example.todo_app.model.User;
import org.example.todo_app.repository.UserRepository;
import org.example.todo_app.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(currentUserService.require(authorization));
    }

    @PutMapping("/current")
    @Transactional
    public ResponseEntity<User> updateProfile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                              @RequestBody User updatedData) {
        User user = currentUserService.require(authorization);
        if (updatedData.getName() != null && !updatedData.getName().isBlank()) user.setName(updatedData.getName().trim());
        if (updatedData.getUsername() != null && !updatedData.getUsername().isBlank()) user.setUsername(updatedData.getUsername().trim());
        if (updatedData.getAvatarUrl() != null) user.setAvatarUrl(updatedData.getAvatarUrl().trim());
        return ResponseEntity.ok(userRepository.save(user));
    }
}
