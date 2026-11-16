package com.student.taskmanager.service;

import com.student.taskmanager.dto.*;
import com.student.taskmanager.entity.*;
import com.student.taskmanager.exception.ResourceNotFoundException;
import com.student.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository repository;

    @Test
    void taskCreationDefaultsStatusToPending() {
        Student student = new Student("Student", "student@example.com", "hash");
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TaskRequest request = new TaskRequest("Read chapter", "", "Math", LocalDate.now(), Priority.HIGH, null);

        TaskResponse response = new TaskService(repository).create(1L, request, student);

        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
        verify(repository).save(any(Task.class));
    }

    @Test
    void anotherStudentCannotAccessTask() {
        when(repository.findByIdAndStudentId(7L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new TaskService(repository).get(99L, 7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found");
        verify(repository, never()).delete(any());
    }

    @Test
    void myDayExcludesCompletedTasksThroughRepositoryScopes() {
        when(repository.findByStudentIdAndStatusNotAndDueDateBeforeOrderByDueDateAsc(eq(1L), eq(TaskStatus.COMPLETED), any()))
                .thenReturn(List.of());
        when(repository.findByStudentIdAndStatusNotAndDueDateBetweenOrderByDueDateAsc(eq(1L), eq(TaskStatus.COMPLETED), any(), any()))
                .thenReturn(List.of());

        MyDayResponse response = new TaskService(repository).myDay(1L, LocalDate.of(2026, 11, 16));

        assertThat(response.overdue()).isEmpty();
        verify(repository, times(2)).findByStudentIdAndStatusNotAndDueDateBetweenOrderByDueDateAsc(
                eq(1L), eq(TaskStatus.COMPLETED), any(), any());
    }

    @Test
    void analyticsCalculatesWeeklyCompletionPercentageAndSubjects() {
        when(repository.countByStudentIdAndStatusAndCompletedAtBetween(eq(1L), eq(TaskStatus.COMPLETED), any(), any())).thenReturn(2L);
        when(repository.countByStudentIdAndDueDateBetween(1L, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22))).thenReturn(4L);
        when(repository.countByStudentId(1L)).thenReturn(5L);
        when(repository.countByStudentIdAndStatus(1L, TaskStatus.COMPLETED)).thenReturn(2L);
        when(repository.countBySubjectForStudent(1L)).thenReturn(List.<Object[]>of(new Object[]{"Math", 3L}));

        AnalyticsResponse response = new TaskService(repository).analytics(1L, LocalDate.of(2026, 11, 18));

        assertThat(response.completedThisWeek()).isEqualTo(2);
        assertThat(response.completionPercentage()).isEqualTo(50);
        assertThat(response.pending()).isEqualTo(3);
        assertThat(response.subjectDistribution()).isEqualTo(Map.of("Math", 3L));
    }

    @Test
    void listCombinesSubjectSearchAndSafeSort() {
        when(repository.findForStudent(1L, TaskStatus.PENDING, "math", "Algebra", "priority")).thenReturn(List.of());

        assertThat(new TaskService(repository).list(1L, TaskStatus.PENDING, "math", "Algebra", "priority")).isEmpty();

        verify(repository).findForStudent(1L, TaskStatus.PENDING, "math", "Algebra", "priority");
    }

    @Test
    void completeMarksOwnedOpenTaskCompleted() {
        Student student = new Student("Student", "student@example.com", "hash");
        Task task = new Task("Task", "", "Math", LocalDate.now(), Priority.MEDIUM, TaskStatus.IN_PROGRESS, student);
        when(repository.findByIdAndStudentId(7L, 1L)).thenReturn(Optional.of(task));

        new TaskService(repository).complete(1L, 7L);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
        verify(repository).save(task);
    }

    @Test
    void completeCannotAccessAnotherStudentsTask() {
        when(repository.findByIdAndStudentId(7L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new TaskService(repository).complete(2L, 7L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void calendarScopesTasksToMonthAndSelectedDate() {
        Student student = new Student("Student", "student@example.com", "hash");
        Task november16 = new Task("Monday task", "", "Math", LocalDate.of(2026, 11, 16),
                Priority.HIGH, TaskStatus.PENDING, student);
        Task november20 = new Task("Friday task", "", "Science", LocalDate.of(2026, 11, 20),
                Priority.LOW, TaskStatus.PENDING, student);
        when(repository.findByStudentIdAndDueDateBetweenOrderByDueDateAsc(eq(1L), eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 30))))
                .thenReturn(List.of(november16, november20));
        when(repository.findByStudentIdAndDueDate(1L, LocalDate.of(2026, 11, 16))).thenReturn(List.of());

        CalendarResponse response = new TaskService(repository).calendar(1L, java.time.YearMonth.of(2026, 11), LocalDate.of(2026, 11, 16));

        assertThat(response.month()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(response.monthTasks()).extracting(TaskResponse::dueDate)
                .containsExactly(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 20));
        assertThat(response.monthTasks()).extracting(TaskResponse::title)
                .containsExactly("Monday task", "Friday task");
        assertThat(LocalDate.of(2026, 11, 16).getDayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);
        assertThat(LocalDate.of(2026, 11, 20).getDayOfWeek()).isEqualTo(java.time.DayOfWeek.FRIDAY);
        assertThat(java.time.YearMonth.of(2026, 11).atEndOfMonth()).isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @Test
    void calendarUsesLeapYearFebruaryBoundaries() {
        when(repository.findByStudentIdAndDueDateBetweenOrderByDueDateAsc(eq(1L), eq(LocalDate.of(2028, 2, 1)), eq(LocalDate.of(2028, 2, 29))))
                .thenReturn(List.of());

        CalendarResponse response = new TaskService(repository).calendar(1L, java.time.YearMonth.of(2028, 2), null);

        assertThat(response.month()).isEqualTo(LocalDate.of(2028, 2, 1));
        verify(repository).findByStudentIdAndDueDateBetweenOrderByDueDateAsc(
                1L, LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));
    }
}
