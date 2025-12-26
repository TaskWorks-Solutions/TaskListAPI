package com.example.TaskListAPI.controller;

import com.example.TaskListAPI.dto.TaskRequest;
import com.example.TaskListAPI.dto.TaskResponse;
import com.example.TaskListAPI.enums.Status;
import com.example.TaskListAPI.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        TaskResponse createdTask = taskService.createTask(taskRequest);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves a list of all tasks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class)))
    })
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates an existing task with new details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Valid @RequestBody TaskRequest taskRequest) {
        TaskResponse updatedTask = taskService.updateTask(id, taskRequest);
        return ResponseEntity.ok(updatedTask);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID") @PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/filter")
    @Operation(summary = "Filter tasks by status", description = "Retrieves tasks filtered by their status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks filtered successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class)))
    })
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(
            @Parameter(description = "Task status") @RequestParam Status status) {
        List<TaskResponse> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Updates only the status of a specific task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status updated successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @Parameter(description = "Task ID") @PathVariable UUID id,
            @Parameter(description = "New status") @RequestParam Status status) {
        TaskResponse updatedTask = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Searches tasks by keyword in title or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class)))
    })
    public ResponseEntity<List<TaskResponse>> searchTasks(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<TaskResponse> tasks = taskService.searchTasks(keyword);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks", description = "Retrieves all overdue tasks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue tasks retrieved",
            content = @Content(schema = @Schema(implementation = TaskResponse.class)))
    })
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        List<TaskResponse> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }
}