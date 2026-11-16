package com.student.taskmanager.dto;

import java.util.List;

public record MyDayResponse(List<TaskResponse> overdue, List<TaskResponse> dueToday, List<TaskResponse> upcoming) {}
