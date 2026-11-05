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

    public List<TodoResponse> getAllTodos(User currentUser) {
        List<Todo> list = todoRepository.findByUserIdOrderByIdDesc(currentUser.getId());
        log.info("[DB READ] Задачи пользователя ID {}: найдено {}", currentUser.getId(), list.size());
        return list.stream().map(TodoResponse::fromEntity).toList();
    }

    public TodoResponse getTodoById(Long id, User currentUser) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        assertOwner(todo, currentUser);
        return TodoResponse.fromEntity(todo);
    }

    @Transactional
    public TodoResponse createTodo(TodoCreateRequest request, User currentUser) {
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
    public TodoResponse updateStatus(Long id, TodoStatusUpdateRequest request, User currentUser) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        assertOwner(todo, currentUser);
        todo.setIsCompleted(request.isCompleted());
        Todo saved = todoRepository.save(todo);
        log.info("[СТАТУС ИЗМЕНЕН] ID: {} -> {}", id, saved.getIsCompleted());
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public TodoResponse updateTodo(Long id, TodoCreateRequest request, User currentUser) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        assertOwner(todo, currentUser);

        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setDeadline(request.deadline());
        todo.setPriority(request.priority());
        Todo saved = todoRepository.save(todo);
        log.info("[ОТРЕДАКТИРОВАНО] ID: {} | Название: '{}'", id, saved.getTitle());
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteTodo(Long id, User currentUser) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        assertOwner(todo, currentUser);
        todoRepository.delete(todo);
        log.info("[УДАЛЕНО] Задача ID: {}", id);
    }

    private void assertOwner(Todo todo, User user) {
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Task belongs to another user");
        }
    }
}
