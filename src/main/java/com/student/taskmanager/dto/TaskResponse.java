package com.student.taskmanager.dto;

import com.student.taskmanager.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(Long id, String title, String description, String subject, LocalDate dueDate,
                           Priority priority, TaskStatus status, boolean overdue,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getSubject(),
                task.getDueDate(), task.getPriority(), task.getStatus(),
                task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED,
                task.getCreatedAt(), task.getUpdatedAt());
    }
}
