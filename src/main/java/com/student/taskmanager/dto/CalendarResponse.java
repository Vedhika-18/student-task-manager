package com.student.taskmanager.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(LocalDate month, LocalDate selectedDate, List<TaskResponse> monthTasks,
                               List<TaskResponse> selectedDateTasks) {}
