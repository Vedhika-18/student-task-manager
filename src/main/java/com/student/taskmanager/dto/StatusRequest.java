package com.student.taskmanager.dto;

import com.student.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Status is required") TaskStatus status) {}
