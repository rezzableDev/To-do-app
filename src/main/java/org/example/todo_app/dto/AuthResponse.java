package org.example.todo_app.dto;

import org.example.todo_app.model.User;

public record AuthResponse(String token, Long userId, String name, String username, String email,
                           String avatarUrl, Long telegramChatId) {
    public static AuthResponse from(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getName(), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), user.getTelegramChatId());
    }
}
