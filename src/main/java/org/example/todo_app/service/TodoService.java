package org.example.todo_app.service;

import org.example.todo_app.dto.TodoCreateRequest;
import org.example.todo_app.dto.TodoResponse;
import org.example.todo_app.dto.TodoStatusUpdateRequest;
import org.example.todo_app.model.Todo;
import org.example.todo_app.model.User;
import org.example.todo_app.repository.TodoRepository;
import org.example.todo_app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoService.class);
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    private User getOrCreateCurrentUser() {
        return userRepository.findAll().stream().findFirst().orElseGet(() -> {
            log.info("[ИНИЦИАЛИЗАЦИЯ] Создание первичного пользователя в БД...");
            User defaultUser = new User("Jasur", "jasur", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150");
            return userRepository.save(defaultUser);
        });
    }

    public List<TodoResponse> getAllTodos() {
        User currentUser = getOrCreateCurrentUser();
        List<Todo> list = todoRepository.findByUserIdOrderByIdDesc(currentUser.getId());
        log.info("[DB READ] Задачи пользователя ID {}: найдено {}", currentUser.getId(), list.size());
        return list.stream().map(TodoResponse::fromEntity).toList();
    }

    public TodoResponse getTodoById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        return TodoResponse.fromEntity(todo);
    }

    @Transactional
    public TodoResponse createTodo(TodoCreateRequest request) {
        User currentUser = getOrCreateCurrentUser();
        Todo todo = new Todo(
                request.title(),
                request.description(),
                request.deadline(),
                request.priority(),
                false,
                currentUser // Привязка к пользователю
        );
        Todo saved = todoRepository.save(todo);
        log.info("[ЗАДАЧА СОЗДАНА] ID: {} | user_id: {} | Название: '{}'", saved.getId(), currentUser.getId(), saved.getTitle());
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public TodoResponse updateStatus(Long id, TodoStatusUpdateRequest request) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        todo.setIsCompleted(request.isCompleted());
        Todo saved = todoRepository.save(todo);
        log.info("[СТАТУС ИЗМЕНЕН] ID: {} -> {}", id, saved.getIsCompleted());
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public TodoResponse updateTodo(Long id, TodoCreateRequest request) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));

        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setDeadline(request.deadline());
        todo.setPriority(request.priority());
        Todo saved = todoRepository.save(todo);
        log.info("[ОТРЕДАКТИРОВАНО] ID: {} | Название: '{}'", id, saved.getTitle());
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
        log.info("[УДАЛЕНО] Задача ID: {}", id);
    }
}