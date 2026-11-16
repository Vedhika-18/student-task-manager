package com.student.taskmanager.service;

import com.student.taskmanager.dto.*;
import com.student.taskmanager.entity.*;
import com.student.taskmanager.exception.ResourceNotFoundException;
import com.student.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) { this.taskRepository = taskRepository; }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(Long studentId, TaskStatus status, String search, String subject, String sort) {
        String normalizedSort = switch (sort == null ? "" : sort) {
            case "priority", "created", "title" -> sort;
            default -> "dueDate";
        };
        return taskRepository.findForStudent(studentId, status, search == null ? "" : search.trim(),
                        subject == null ? "" : subject.trim(), normalizedSort)
                .stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> recent(Long studentId) {
        return map(taskRepository.findTop5ByStudentIdOrderByUpdatedAtDesc(studentId));
    }

    public List<TaskResponse> list(Long studentId, TaskStatus status, String search) {
        return list(studentId, status, search, "", "dueDate");
    }

    @Transactional(readOnly = true)
    public MyDayResponse myDay(Long studentId, LocalDate today) {
        return new MyDayResponse(
                map(taskRepository.findByStudentIdAndStatusNotAndDueDateBeforeOrderByDueDateAsc(studentId, TaskStatus.COMPLETED, today)),
                map(taskRepository.findByStudentIdAndStatusNotAndDueDateBetweenOrderByDueDateAsc(studentId, TaskStatus.COMPLETED, today, today)),
                map(taskRepository.findByStudentIdAndStatusNotAndDueDateBetweenOrderByDueDateAsc(studentId, TaskStatus.COMPLETED, today.plusDays(1), today.plusDays(7))));
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse analytics(Long studentId, LocalDate today) {
        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDateTime start = weekStartDate.atStartOfDay();
        LocalDateTime end = weekStartDate.plusDays(7).atStartOfDay();
        long completedThisWeek = taskRepository.countByStudentIdAndStatusAndCompletedAtBetween(
                studentId, TaskStatus.COMPLETED, start, end);
        long totalThisWeek = taskRepository.countByStudentIdAndDueDateBetween(studentId, weekStartDate, weekStartDate.plusDays(6));
        long total = taskRepository.countByStudentId(studentId);
        long completed = taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.COMPLETED);
        Map<String, Long> subjects = new LinkedHashMap<>();
        taskRepository.countBySubjectForStudent(studentId).forEach(row -> subjects.put((String) row[0], (Long) row[1]));
        return new AnalyticsResponse(completedThisWeek, totalThisWeek,
                totalThisWeek == 0 ? 0 : (int) Math.round(completedThisWeek * 100.0 / totalThisWeek),
                total - completed, completed, subjects);
    }

    @Transactional(readOnly = true)
    public CalendarResponse calendar(Long studentId, YearMonth month, LocalDate selectedDate) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        LocalDate selected = selectedDate != null && !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
                ? selectedDate : null;
        List<TaskResponse> selectedTasks = selected == null ? List.of()
                : map(taskRepository.findByStudentIdAndDueDate(studentId, selected));
        return new CalendarResponse(month.atDay(1), selected,
                map(taskRepository.findByStudentIdAndDueDateBetweenOrderByDueDateAsc(studentId, start, end)), selectedTasks);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long studentId, Long taskId) {
        return TaskResponse.from(ownedTask(studentId, taskId));
    }

    @Transactional
    public TaskResponse create(Long studentId, TaskRequest request, Student student) {
        Task task = new Task(request.title().trim(), request.description(), request.subject().trim(),
                request.dueDate(), request.priority(),
                request.status() == null ? TaskStatus.PENDING : request.status(), student);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long studentId, Long taskId, TaskRequest request) {
        Task task = ownedTask(studentId, taskId);
        task.update(request.title().trim(), request.description(), request.subject().trim(), request.dueDate(),
                request.priority(), request.status() == null ? task.getStatus() : request.status());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long studentId, Long taskId, StatusRequest request) {
        Task task = ownedTask(studentId, taskId);
        task.setStatus(request.status());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void complete(Long studentId, Long taskId) {
        Task task = ownedTask(studentId, taskId);
        if (task.getStatus() != TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.COMPLETED);
            taskRepository.save(task);
        }
    }

    @Transactional
    public void delete(Long studentId, Long taskId) {
        taskRepository.delete(ownedTask(studentId, taskId));
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(Long studentId) {
        return new StatsResponse(taskRepository.countByStudentId(studentId),
                taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.PENDING),
                taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.IN_PROGRESS),
                taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.COMPLETED));
    }

    @Transactional(readOnly = true)
    public List<String> subjects(Long studentId) {
        return taskRepository.findDistinctSubjectsByStudentId(studentId);
    }

    private List<TaskResponse> map(List<Task> tasks) {
        return tasks.stream().map(TaskResponse::from).toList();
    }

    private Task ownedTask(Long studentId, Long taskId) {
        return taskRepository.findByIdAndStudentId(taskId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }
}
