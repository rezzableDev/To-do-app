package org.example.todo_app.service;

import org.example.todo_app.model.Todo;
import org.example.todo_app.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private final TodoRepository todoRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.default-chat-id}")
    private String defaultChatId;

    public ReminderService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // Запуск каждую минуту (в 00 секунд)
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<Todo> activeTodos = todoRepository.findAll();

        for (Todo todo : activeTodos) {
            if (!todo.getIsCompleted() && todo.getDeadline() != null) {
                LocalDateTime taskTime = todo.getDeadline().truncatedTo(ChronoUnit.MINUTES);

                // Если время дедлайна совпадает с текущей минутой
                if (taskTime.isEqual(now)) {
                    sendTelegramMessage(todo);
                }
            }
        }
    }

    private void sendTelegramMessage(Todo todo) {
        String text = String.format(
                "⏰ *Напоминание о задаче!*\n\n📌 *%s*\n📝 %s\n🔥 Приоритет: %s",
                todo.getTitle(),
                (todo.getDescription() != null && !todo.getDescription().isBlank()) ? todo.getDescription() : "Без описания",
                todo.getPriority()
        );

        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                botToken, defaultChatId, text
        );

        try {
            restTemplate.getForObject(url, String.class);
            log.info("[ТЕЛЕГРАМ] Напоминание по задаче ID {} успешно отправлено в Telegram!", todo.getId());
        } catch (Exception e) {
            log.error("[ТЕЛЕГРАМ] Ошибка отправки: {}", e.getMessage());
        }
    }
}