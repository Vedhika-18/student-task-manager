package com.student.taskmanager.service;

import com.student.taskmanager.dto.RegisterRequest;
import com.student.taskmanager.entity.Student;
import com.student.taskmanager.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class StudentServiceTest {
    @Mock StudentRepository repository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void registrationHashesPasswordAndSavesStudent() {
        when(repository.existsByEmailIgnoreCase("student@example.com")).thenReturn(false);
        when(repository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        StudentService service = new StudentService(repository, encoder);

        Student saved = service.register(new RegisterRequest(" Student ", "STUDENT@example.com", "password123"));

        assertThat(saved.getEmail()).isEqualTo("student@example.com");
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(encoder.matches("password123", saved.getPassword())).isTrue();
    }

    @Test
    void lookupUsesEmail() {
        Student student = new Student("Student", "student@example.com", "hash");
        when(repository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(student));
        assertThat(new StudentService(repository, encoder).findByEmail("student@example.com")).isSameAs(student);
    }
}
