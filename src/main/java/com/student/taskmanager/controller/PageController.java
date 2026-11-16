package com.student.taskmanager.controller;

import com.student.taskmanager.dto.*;
import com.student.taskmanager.entity.*;
import com.student.taskmanager.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
public class PageController {
    private final StudentService studentService;
    private final TaskService taskService;

    public PageController(StudentService studentService, TaskService taskService) {
        this.studentService = studentService;
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String home() { return "redirect:/dashboard"; }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("student", student);
        model.addAttribute("stats", taskService.stats(student.getId()));
        model.addAttribute("myDay", taskService.myDay(student.getId(), LocalDate.now()));
        model.addAttribute("analytics", taskService.analytics(student.getId(), LocalDate.now()));
        model.addAttribute("recentTasks", taskService.recent(student.getId()));
        return "dashboard";
    }

    @GetMapping("/tasks")
    public String tasks(@RequestParam(required = false) TaskStatus status,
                        @RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "") String subject,
                        @RequestParam(defaultValue = "dueDate") String sort,
                            Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("student", student);
        model.addAttribute("tasks", taskService.list(student.getId(), status, search, subject, sort));
        model.addAttribute("subjects", taskService.subjects(student.getId()));
        model.addAttribute("selectedStatus", status == null ? "ALL" : status.name());
        model.addAttribute("search", search);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedSort", sort);
        return "tasks";
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) String month,
                           @RequestParam(required = false) String date,
                           Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        YearMonth selectedMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate selectedDate = date == null || date.isBlank() ? null : LocalDate.parse(date);
        model.addAttribute("calendar", taskService.calendar(student.getId(), selectedMonth, selectedDate));
        return "calendar";
    }

    @GetMapping("/tasks/new")
    public String newTask(Model model) {
        model.addAttribute("taskRequest", new TaskRequest("", "", "", LocalDate.now(), Priority.MEDIUM, TaskStatus.PENDING));
        model.addAttribute("formAction", "/tasks");
        model.addAttribute("pageTitle", "Add Task");
        return "task-form";
    }

    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute TaskRequest request, BindingResult result,
                             Authentication authentication, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/tasks");
            model.addAttribute("pageTitle", "Add Task");
            return "task-form";
        }
        Student student = currentStudent(authentication);
        taskService.create(student.getId(), request, student);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editTask(@PathVariable Long id, Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        TaskResponse task = taskService.get(student.getId(), id);
        model.addAttribute("taskRequest", new TaskRequest(task.title(), task.description(), task.subject(),
                task.dueDate(), task.priority(), task.status()));
        model.addAttribute("formAction", "/tasks/" + id);
        model.addAttribute("pageTitle", "Edit Task");
        return "task-form";
    }

    @GetMapping("/tasks/{id}")
    public String taskDetails(@PathVariable Long id, Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("task", taskService.get(student.getId(), id));
        return "task-details";
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id, @Valid @ModelAttribute TaskRequest request,
                             BindingResult result, Authentication authentication, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/tasks/" + id);
            model.addAttribute("pageTitle", "Edit Task");
            return "task-form";
        }
        Student student = currentStudent(authentication);
        taskService.update(student.getId(), id, request);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.delete(currentStudent(authentication).getId(), id);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/complete")
    public String completeTask(@PathVariable Long id, Authentication authentication,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        taskService.complete(currentStudent(authentication).getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Task marked as completed successfully.");
        return "redirect:/tasks";
    }

    private Student currentStudent(Authentication authentication) {
        return studentService.findByEmail(authentication.getName());
    }
}
