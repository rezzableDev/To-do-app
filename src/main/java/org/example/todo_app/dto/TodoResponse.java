package org.example.todo_app.dto;

import org.example.todo_app.model.Priority;
import org.example.todo_app.model.Todo;
import java.time.LocalDateTime;

public record TodoResponse(
        Long id,
        String title,
        String description,
        LocalDateTime deadline,
        Priority priority,
        String priorityLabelRu,
        String priorityLabelUz,
        Boolean isCompleted,
        Long userId
) {
    public static TodoResponse fromEntity(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getDeadline(),
                todo.getPriority(),
                todo.getPriority().getLabelRu(),
                todo.getPriority().getLabelUz(),
                todo.getIsCompleted(),
                todo.getUser() != null ? todo.getUser().getId() : null
        );
    }
}