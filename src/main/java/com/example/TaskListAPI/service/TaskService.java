package com.example.TaskListAPI.service;

import com.example.TaskListAPI.dto.TaskRequest;
import com.example.TaskListAPI.dto.TaskResponse;
import com.example.TaskListAPI.entity.Task;
import com.example.TaskListAPI.enums.Status;
import com.example.TaskListAPI.exception.TaskNotFoundException;
import com.example.TaskListAPI.mapper.TaskMapper;
import com.example.TaskListAPI.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }
    
    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = taskMapper.toEntity(taskRequest);
        Task savedTask = taskRepository.saveAndFlush(task);
        return taskMapper.toResponse(savedTask);
    }
    
    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        return taskMapper.toResponse(task);
    }
    
    public List<TaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
            .map(taskMapper::toResponse)
            .toList();
    }
    
    public List<TaskResponse> getTasksByStatus(Status status) {
        List<Task> tasks = taskRepository.findByStatusOrderByPriorityDesc(status);
        return tasks.stream()
            .map(taskMapper::toResponse)
            .toList();
    }
    
    public TaskResponse updateTask(UUID id, TaskRequest taskRequest) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        taskMapper.updateEntity(existingTask, taskRequest);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponse(updatedTask);
    }
    
    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    public List<TaskResponse> searchTasks(String keyword) {
        List<Task> tasks = taskRepository.findByKeyword(keyword);
        return tasks.stream()
            .map(taskMapper::toResponse)
            .toList();
    }
    
    public List<TaskResponse> getOverdueTasks() {
        List<Task> tasks = taskRepository.findOverdueTasks(LocalDate.now());
        return tasks.stream()
            .map(taskMapper::toResponse)
            .toList();
    }
    
    public TaskResponse updateTaskStatus(UUID id, Status status) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }
}