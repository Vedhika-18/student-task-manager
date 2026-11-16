package com.student.taskmanager.dto;

public record StatsResponse(long total, long pending, long inProgress, long completed) {}
