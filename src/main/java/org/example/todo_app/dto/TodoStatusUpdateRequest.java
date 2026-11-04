package org.example.todo_app.dto;

public record TodoStatusUpdateRequest(
        Boolean isCompleted
) {}