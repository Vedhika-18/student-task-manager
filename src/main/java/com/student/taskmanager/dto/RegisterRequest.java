package com.student.taskmanager.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password) {}
