package com.student.taskmanager.service;

import com.student.taskmanager.dto.RegisterRequest;
import com.student.taskmanager.entity.Student;
import com.student.taskmanager.exception.DuplicateEmailException;
import com.student.taskmanager.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Student register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("An account with that email already exists");
        }
        return studentRepository.save(new Student(request.name().trim(), email, passwordEncoder.encode(request.password())));
    }

    @Transactional(readOnly = true)
    public Student findByEmail(String email) {
        return studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    }
}
