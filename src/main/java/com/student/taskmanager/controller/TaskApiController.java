package com.student.taskmanager.controller;

import com.student.taskmanager.dto.*;
import com.student.taskmanager.entity.*;
import com.student.taskmanager.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskApiController {
    private final StudentService studentService;
    private final TaskService taskService;

    public TaskApiController(StudentService studentService, TaskService taskService) {
        this.studentService = studentService;
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> list(@RequestParam(required = false) TaskStatus status,
                                   @RequestParam(defaultValue = "") String search,
                                   @RequestParam(defaultValue = "") String subject,
                                   @RequestParam(defaultValue = "dueDate") String sort,
                                   Authentication auth) {
        Student student = currentStudent(auth);
        return taskService.list(student.getId(), status, search, subject, sort);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse get(@PathVariable Long id, Authentication auth) {
        return taskService.get(currentStudent(auth).getId(), id);
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest request, Authentication auth) {
        Student student = currentStudent(auth);
        return taskService.create(student.getId(), request, student);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request, Authentication auth) {
        return taskService.update(currentStudent(auth).getId(), id, request);
    }

    @PatchMapping("/tasks/{id}/status")
    public TaskResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest request, Authentication auth) {
        return taskService.updateStatus(currentStudent(auth).getId(), id, request);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        taskService.delete(currentStudent(auth).getId(), id);
    }

    @GetMapping("/dashboard/stats")
    public StatsResponse stats(Authentication auth) {
        return taskService.stats(currentStudent(auth).getId());
    }

    @GetMapping("/dashboard/my-day")
    public MyDayResponse myDay(Authentication auth) {
        return taskService.myDay(currentStudent(auth).getId(), java.time.LocalDate.now());
    }

    @GetMapping("/dashboard/analytics")
    public AnalyticsResponse analytics(Authentication auth) {
        return taskService.analytics(currentStudent(auth).getId(), java.time.LocalDate.now());
    }

    private Student currentStudent(Authentication auth) {
        return studentService.findByEmail(auth.getName());
    }
}
