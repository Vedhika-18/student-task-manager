package com.student.taskmanager.dto;

import java.util.Map;

public record AnalyticsResponse(long completedThisWeek, long totalThisWeek, int completionPercentage,
                                long pending, long completed, Map<String, Long> subjectDistribution) {}
