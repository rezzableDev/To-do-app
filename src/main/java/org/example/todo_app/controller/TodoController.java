package org.example.todo_app.controller;

import org.example.todo_app.dto.TodoCreateRequest;
import org.example.todo_app.dto.TodoResponse;
import org.example.todo_app.dto.TodoStatusUpdateRequest;
import org.example.todo_app.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*") // Разрешает запросы от фронтенда
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodoById(id));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@RequestBody TodoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(request));
    }

    // Сохранение статуса в БД при клике на чекбокс (bajarildi / bajarilmadi)
    @PatchMapping("/{id}/status")
    public ResponseEntity<TodoResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody TodoStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(todoService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id,
            @RequestBody TodoCreateRequest request
    ) {
        return ResponseEntity.ok(todoService.updateTodo(id, request));
    }
}