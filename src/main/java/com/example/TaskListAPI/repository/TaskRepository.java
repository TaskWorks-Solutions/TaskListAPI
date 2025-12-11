package com.example.TaskListAPI.repository;

import com.example.TaskListAPI.entity.Task;
import com.example.TaskListAPI.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    List<Task> findByStatus(Status status);
    
    @Query("SELECT t FROM Task t WHERE t.status = :status ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByStatusOrderByPriorityDesc(@Param("status") Status status);
    
    @Query("SELECT t FROM Task t WHERE t.title LIKE %:keyword% OR t.description LIKE %:keyword%")
    List<Task> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate <= :date AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("date") java.time.LocalDate date);
    
    boolean existsByTitle(String title);
}