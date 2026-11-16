package com.student.taskmanager.dto;

import com.student.taskmanager.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Title is required") @Size(max = 120) String title,
        @Size(max = 2000) String description,
        @NotBlank(message = "Subject is required") @Size(max = 100) String subject,
        @NotNull(message = "Due date is required") @FutureOrPresent(message = "Due date cannot be in the past") LocalDate dueDate,
        @NotNull(message = "Priority is required") Priority priority,
        TaskStatus status) {}
