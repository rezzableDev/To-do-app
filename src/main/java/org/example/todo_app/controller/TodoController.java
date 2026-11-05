package org.example.todo_app.controller;

import org.example.todo_app.dto.TodoCreateRequest;
import org.example.todo_app.dto.TodoResponse;
import org.example.todo_app.dto.TodoStatusUpdateRequest;
import org.example.todo_app.service.TodoService;
import org.example.todo_app.service.CurrentUserService;
import org.example.todo_app.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*") // Разрешает запросы от фронтенда
public class TodoController {

    private final TodoService todoService;
    private final CurrentUserService currentUserService;

    public TodoController(TodoService todoService, CurrentUserService currentUserService) {
        this.todoService = todoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(todoService.getAllTodos(currentUserService.require(authorization)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(todoService.getTodoById(id, currentUserService.require(authorization)));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@RequestBody TodoCreateRequest request, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(request, currentUserService.require(authorization)));
    }

    // Сохранение статуса в БД при клике на чекбокс (bajarildi / bajarilmadi)
    @PatchMapping("/{id}/status")
    public ResponseEntity<TodoResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody TodoStatusUpdateRequest request, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ResponseEntity.ok(todoService.updateStatus(id, request, currentUserService.require(authorization)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
        todoService.deleteTodo(id, currentUserService.require(authorization));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id,
            @RequestBody TodoCreateRequest request, @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ResponseEntity.ok(todoService.updateTodo(id, request, currentUserService.require(authorization)));
    }
}
