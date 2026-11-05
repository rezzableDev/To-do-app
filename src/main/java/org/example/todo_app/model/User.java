package org.example.todo_app.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    @JsonIgnore
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    @JdbcTypeCode(Types.LONGVARBINARY)
    @Column(name = "avatar_data")
    private byte[] avatarData;

    @Column(name = "avatar_content_type")
    @JsonIgnore
    private String avatarContentType;

    @Column(name = "telegram_chat_id", unique = true)
    private Long telegramChatId;

    public User() {}

    // Конструктор на 3 параметра (требуется для TodoService и UserController)
    public User(String name, String username, String avatarUrl) {
        this.name = name;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    // Конструктор на 4 параметра (для Telegram-бота с chatId)
    public User(String name, String username, String avatarUrl, Long telegramChatId) {
        this.name = name;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.telegramChatId = telegramChatId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public byte[] getAvatarData() { return avatarData; }
    public void setAvatarData(byte[] avatarData) { this.avatarData = avatarData; }

    public String getAvatarContentType() { return avatarContentType; }
    public void setAvatarContentType(String avatarContentType) { this.avatarContentType = avatarContentType; }

    public Long getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(Long telegramChatId) { this.telegramChatId = telegramChatId; }
}
