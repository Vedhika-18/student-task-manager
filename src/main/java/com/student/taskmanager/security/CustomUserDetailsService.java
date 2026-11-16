package com.student.taskmanager.security;

import com.student.taskmanager.entity.Student;
import com.student.taskmanager.repository.StudentRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final StudentRepository studentRepository;
    public CustomUserDetailsService(StudentRepository studentRepository) { this.studentRepository = studentRepository; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Student student = studentRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Student account not found"));
        return User.withUsername(student.getEmail()).password(student.getPassword()).roles("STUDENT").build();
    }
}
