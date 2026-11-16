package com.student.taskmanager.repository;

import com.student.taskmanager.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select t from Task t where t.student.id = :studentId and " +
           "(:status is null or t.status = :status) and " +
           "(:search = '' or lower(t.title) like lower(concat('%', :search, '%')) or lower(t.subject) like lower(concat('%', :search, '%'))) and " +
           "(:subject = '' or lower(t.subject) = lower(:subject)) " +
           "order by " +
           "case when :sort = 'priority' and t.priority = com.student.taskmanager.entity.Priority.HIGH then 1 " +
           "     when :sort = 'priority' and t.priority = com.student.taskmanager.entity.Priority.MEDIUM then 2 " +
           "     when :sort = 'priority' and t.priority = com.student.taskmanager.entity.Priority.LOW then 3 else 0 end, " +
           "case when :sort = 'created' then t.createdAt end desc, " +
           "case when :sort = 'title' then t.title end asc, t.dueDate asc")
    List<Task> findForStudent(@Param("studentId") Long studentId, @Param("status") TaskStatus status,
                              @Param("search") String search, @Param("subject") String subject,
                              @Param("sort") String sort);

    List<Task> findByStudentIdAndStatusNotAndDueDateBeforeOrderByDueDateAsc(Long studentId, TaskStatus status, java.time.LocalDate date);
    List<Task> findByStudentIdAndStatusNotAndDueDateBetweenOrderByDueDateAsc(Long studentId, TaskStatus status,
                                                                               java.time.LocalDate start, java.time.LocalDate end);
    List<Task> findByStudentIdAndDueDateBetweenOrderByDueDateAsc(Long studentId, java.time.LocalDate start,
                                                                  java.time.LocalDate end);
    List<Task> findByStudentIdAndDueDate(Long studentId, java.time.LocalDate dueDate);
    @Query("select distinct t.subject from Task t where t.student.id = :studentId order by t.subject")
    List<String> findDistinctSubjectsByStudentId(@Param("studentId") Long studentId);
    long countByStudentIdAndStatusAndCompletedAtBetween(Long studentId, TaskStatus status,
                                                         java.time.LocalDateTime start, java.time.LocalDateTime end);
    @Query("select t.subject, count(t) from Task t where t.student.id = :studentId group by t.subject order by t.subject")
    List<Object[]> countBySubjectForStudent(@Param("studentId") Long studentId);
    Optional<Task> findByIdAndStudentId(Long id, Long studentId);
    long countByStudentId(Long studentId);
    long countByStudentIdAndStatus(Long studentId, TaskStatus status);
    long countByStudentIdAndDueDateBetween(Long studentId, java.time.LocalDate start, java.time.LocalDate end);
    List<Task> findTop5ByStudentIdOrderByUpdatedAtDesc(Long studentId);
}
