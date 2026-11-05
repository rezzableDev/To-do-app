package org.example.todo_app.service;

import org.example.todo_app.model.AuthSession;
import org.example.todo_app.model.User;
import org.example.todo_app.repository.AuthSessionRepository;
import org.example.todo_app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramBotService extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);
    private final String username;
    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;

    public TelegramBotService(@Value("${telegram.bot.token}") String token,
                              @Value("${telegram.bot.username}") String username,
                              AuthSessionRepository authSessionRepository, UserRepository userRepository) {
        super(token);
        this.username = username;
        this.authSessionRepository = authSessionRepository;
        this.userRepository = userRepository;
    }

    @Override public String getBotUsername() { return username; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        if (!text.startsWith("/start")) {
            sendTextMessage(chatId, "Откройте TaskFlow и выберите «Подключить Telegram».");
            return;
        }
        String[] parts = text.split("\\s+", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            sendTextMessage(chatId, "Откройте ссылку подключения из TaskFlow.");
            return;
        }
        String result = confirmSession(parts[1].trim(), chatId);
        sendTextMessage(chatId, result);
    }

    @Transactional
    public String confirmSession(String token, Long chatId) {
        AuthSession session = authSessionRepository.findWithLockBySessionToken(token).orElse(null);
        if (session == null) return "Ссылка недействительна. Создайте новую в TaskFlow.";
        if (!"PENDING".equals(session.getStatus())) return "Эта ссылка уже использована или истекла.";
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            session.setStatus("EXPIRED");
            return "Срок действия ссылки истёк. Создайте новую в TaskFlow.";
        }
        User user = session.getUser();
        if (user == null) {
            log.warn("Pending auth session {} has no user", session.getId());
            return "Не удалось определить аккаунт. Создайте новую ссылку.";
        }
        User existingChatOwner = userRepository.findByTelegramChatId(chatId).orElse(null);
        if (existingChatOwner != null && !existingChatOwner.getId().equals(user.getId())) {
            return "Этот Telegram уже подключён к другому аккаунту TaskFlow.";
        }
        user.setTelegramChatId(chatId);
        userRepository.save(user);
        session.setStatus("CONFIRMED");
        authSessionRepository.save(session);
        log.info("Telegram linked to TaskFlow user {}", user.getId());
        return "✅ Telegram подключён. Вернитесь в TaskFlow.";
    }

    public void sendTextMessage(Long chatId, String text) {
        if (chatId == null) return;
        try {
            execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException ex) {
            log.warn("Telegram delivery failed for chat {}: {}", chatId, ex.getMessage());
        }
    }
}
