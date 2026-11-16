package com.student.taskmanager.controller;

import com.student.taskmanager.dto.RegisterRequest;
import com.student.taskmanager.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final StudentService studentService;
    public AuthController(StudentService studentService) { this.studentService = studentService; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) return "register";
        try {
            studentService.register(request);
            return "redirect:/login?registered";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }
}
