package com.example.TaskListAPI.mapper;

import com.example.TaskListAPI.dto.TaskRequest;
import com.example.TaskListAPI.dto.TaskResponse;
import com.example.TaskListAPI.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    
    public Task toEntity(TaskRequest taskRequest) {
        if (taskRequest == null) {
            return null;
        }
        
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setDueDate(taskRequest.getDueDate());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());
        
        return task;
    }
    
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        
        return TaskResponse.fromEntity(task);
    }
    
    public void updateEntity(Task task, TaskRequest taskRequest) {
        if (task == null || taskRequest == null) {
            return;
        }
        
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setDueDate(taskRequest.getDueDate());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());
    }
}