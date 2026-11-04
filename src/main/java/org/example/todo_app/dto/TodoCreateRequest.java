package org.example.todo_app.dto;

import org.example.todo_app.model.Priority;
import java.time.LocalDateTime;

public record TodoCreateRequest(
        String title,
        String description,
        LocalDateTime deadline,
        Priority priority
) {}