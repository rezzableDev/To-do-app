package org.example.todo_app.service;

import org.example.todo_app.model.AuthSession;
import org.example.todo_app.model.User;
import org.example.todo_app.repository.AuthSessionRepository;
import org.example.todo_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.example.todo_app.controller.AppModeController;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    // Укажите ваш chat_id (число, которое можно узнать в боте @userinfobot)
    private static final Long ADMIN_CHAT_ID = 7305455575L;

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramBotService(
            @Value("${telegram.bot.token}") String token,
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository
    ) {
        super(token);
        this.botToken = token;
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    // Главный метод обработки всех входящих сообщений
    @Override
    public void onUpdateReceived(Update update) {
        // Обработка клика по инлайн-кнопке в боте
        if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (chatId.equals(ADMIN_CHAT_ID) && "toggle_special_mode".equals(callData)) {
                boolean newState = !AppModeController.isSpecialMode();
                AppModeController.setSpecialMode(newState);
                sendTextMessage(chatId, newState ? "✨ Special Mode АКТИВИРОВАН на сайте!" : "💤 Special Mode ВЫКЛЮЧЕН.");
                sendAdminPanel(chatId);
            }
            return;
        }

// Проверка команды /admin
        if (update.hasMessage() && update.getMessage().hasText()) {
            String incomingText = update.getMessage().getText();
            Long incomingChatId = update.getMessage().getChatId();

            if ("/admin".equals(incomingText)) {
                if (incomingChatId.equals(ADMIN_CHAT_ID)) {
                    sendAdminPanel(incomingChatId);
                } else {
                    sendTextMessage(incomingChatId, "⛔ Доступ запрещён.");
                }
                return;
            }
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        org.telegram.telegrambots.meta.api.objects.User tgUser = update.getMessage().getFrom();

        System.out.println(">>> [TG UPDATE] Получено сообщение: '" + text + "' от chatId: " + chatId);

        if (text.startsWith("/start")) {
            String[] parts = text.split("\\s+");
            System.out.println(">>> [TG START] Аргументов команды: " + parts.length);

            if (parts.length > 1) {
                String token = parts[1].trim();
                System.out.println(">>> [TG TOKEN] Поиск сессии с токеном: " + token);

                Optional<AuthSession> sessionOpt = authSessionRepository.findBySessionToken(token);

                if (sessionOpt.isPresent()) {
                    AuthSession session = sessionOpt.get();
                    System.out.println(">>> [TG SESSION НАЙДЕНА] Статус: " + session.getStatus() + ", Истекает: " + session.getExpiresAt());

                    // Проверяем статус и продлеваем проверку времени на случай расхождения таймзон
                    if ("PENDING".equals(session.getStatus())) {
                        User user = userRepository.findByTelegramChatId(chatId).orElseGet(() -> {
                            String fullName = tgUser.getFirstName() + (tgUser.getLastName() != null ? " " + tgUser.getLastName() : "");
                            return userRepository.save(new User(fullName, tgUser.getUserName(), null, chatId));
                        });

                        session.setUser(user);
                        session.setStatus("CONFIRMED");
                        authSessionRepository.save(session);

                        System.out.println(">>> [TG AUTH SUCCESS] Сессия подтверждена для user id: " + user.getId());
                        sendTextMessage(chatId, "✅ Авторизация успешна! Можете вернуться на сайт TaskFlow.");
                        return;
                    } else {
                        System.out.println(">>> [TG WARNING] Сессия уже не в статусе PENDING!");
                    }
                } else {
                    System.err.println(">>> [TG ERROR] Токен " + token + " не найден в таблице auth_sessions!");
                }
            }

            sendTextMessage(chatId, "👋 Привет! Чтобы войти в TaskFlow, нажмите кнопку «Вход через Telegram» на сайте.");
            return;
        }

        sendTextMessage(chatId, "Я принимаю команды авторизации и уведомления по вашим задачам.");

            // Обычный /start без токена
            sendTextMessage(chatId, "👋 Привет! Чтобы войти в TaskFlow, перейдите на сайт и нажмите «Войти через Telegram».");
            return;
        }


    // Метод отправки простого текста в чат
    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAdminPanel(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("⚙️ Панель администратора TaskFlow:\nУправление режимом сайта:");

        InlineKeyboardButton toggleBtn = new InlineKeyboardButton();
        boolean currentStatus = AppModeController.isSpecialMode();
        toggleBtn.setText(currentStatus ? "🔴 Выключить Special Mode" : "💖 Включить Special Mode (Iymana)");
        toggleBtn.setCallbackData("toggle_special_mode");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(toggleBtn)));
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}