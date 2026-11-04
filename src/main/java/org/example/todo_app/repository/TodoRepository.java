package org.example.todo_app.repository;

import org.example.todo_app.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    // Получение задач конкретного пользователя по его user_id
    List<Todo> findByUserIdOrderByIdDesc(Long userId);
}