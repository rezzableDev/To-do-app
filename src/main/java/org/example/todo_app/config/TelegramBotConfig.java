package org.example.todo_app.config;

import org.example.todo_app.service.TelegramBotService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    private final TelegramBotService telegramBotService;

    public TelegramBotConfig(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            System.out.println(">>> [TELEGRAM BOT] Успешно зарегистрирован и запущен! <<<");
        } catch (TelegramApiException e) {
            System.err.println(">>> [TELEGRAM BOT ERROR] Ошибка запуска бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}