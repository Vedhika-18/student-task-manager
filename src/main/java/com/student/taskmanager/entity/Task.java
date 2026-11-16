package com.student.taskmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = @Index(name = "idx_task_student_due_date", columnList = "student_id,due_date"))
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    protected Task() {}

    public Task(String title, String description, String subject, LocalDate dueDate,
                Priority priority, TaskStatus status, Student student) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        setStatus(status);
        this.student = student;
    }

    public void update(String title, String description, String subject, LocalDate dueDate,
                       Priority priority, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        setStatus(status);
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public LocalDate getDueDate() { return dueDate; }
    public Priority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Student getStudent() { return student; }
    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == TaskStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = this.updatedAt;
        } else if (status != TaskStatus.COMPLETED) {
            this.completedAt = null;
        }
    }
}
