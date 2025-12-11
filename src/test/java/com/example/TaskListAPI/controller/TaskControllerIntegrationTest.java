package com.example.TaskListAPI.controller;

import com.example.TaskListAPI.dto.TaskRequest;
import com.example.TaskListAPI.entity.Task;
import com.example.TaskListAPI.enums.Status;
import com.example.TaskListAPI.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Task task;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create test data
        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setStatus(Status.PENDING);
        task.setPriority(Task.Priority.MEDIUM);
        task = taskRepository.save(task);

        taskRequest = new TaskRequest(
            "New Task",
            "New Description",
            LocalDate.now().plusDays(14),
            Status.IN_PROGRESS,
            Task.Priority.HIGH
        );
    }

    @Test
    void createTask_ShouldReturnCreatedTask_WhenValidRequest() throws Exception {
        // Given
        String taskRequestJson = objectMapper.writeValueAsString(taskRequest);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(taskRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(taskRequest.getDescription()))
                .andExpect(jsonPath("$.status").value(taskRequest.getStatus().toString()))
                .andExpect(jsonPath("$.priority").value(taskRequest.getPriority().toString()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void createTask_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        TaskRequest invalidRequest = new TaskRequest();
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.id == '" + task.getId() + "')].title").value(task.getTitle()));
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId().toString()))
                .andExpect(jsonPath("$.title").value(task.getTitle()))
                .andExpect(jsonPath("$.description").value(task.getDescription()))
                .andExpect(jsonPath("$.status").value(task.getStatus().toString()))
                .andExpect(jsonPath("$.priority").value(task.getPriority().toString()));
    }

    @Test
    void getTaskById_ShouldReturnNotFound_WhenTaskDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask_WhenTaskExists() throws Exception {
        // Given
        String taskRequestJson = objectMapper.writeValueAsString(taskRequest);

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId().toString()))
                .andExpect(jsonPath("$.title").value(taskRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(taskRequest.getDescription()))
                .andExpect(jsonPath("$.status").value(taskRequest.getStatus().toString()))
                .andExpect(jsonPath("$.priority").value(taskRequest.getPriority().toString()));
    }

    @Test
    void updateTask_ShouldReturnNotFound_WhenTaskDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        String taskRequestJson = objectMapper.writeValueAsString(taskRequest);

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    void deleteTask_ShouldReturnNoContent_WhenTaskExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_ShouldReturnNotFound_WhenTaskDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    void getTasksByStatus_ShouldReturnTasksWithGivenStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/filter")
                .param("status", Status.PENDING.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.status == '" + Status.PENDING + "')]").exists());
    }

    @Test
    void updateTaskStatus_ShouldReturnUpdatedTask_WhenTaskExists() throws Exception {
        // Given
        Status newStatus = Status.COMPLETED;

        // When & Then
        mockMvc.perform(patch("/api/tasks/{id}/status", task.getId())
                .param("status", newStatus.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId().toString()))
                .andExpect(jsonPath("$.status").value(newStatus.toString()));
    }

    @Test
    void updateTaskStatus_ShouldReturnNotFound_WhenTaskDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Status newStatus = Status.COMPLETED;

        // When & Then
        mockMvc.perform(patch("/api/tasks/{id}/status", nonExistentId)
                .param("status", newStatus.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    void searchTasks_ShouldReturnTasksContainingKeyword() throws Exception {
        // Given
        String keyword = "Test";

        // When & Then
        mockMvc.perform(get("/api/tasks/search")
                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.title contains '" + keyword + "')]").exists());
    }

    @Test
    void getOverdueTasks_ShouldReturnOverdueTasks() throws Exception {
        // Given - Create an overdue task
        Task overdueTask = new Task();
        overdueTask.setTitle("Overdue Task");
        overdueTask.setDescription("This task is overdue");
        overdueTask.setDueDate(LocalDate.now().minusDays(1));
        overdueTask.setStatus(Status.PENDING);
        overdueTask.setPriority(Task.Priority.HIGH);
        taskRepository.save(overdueTask);

        // When & Then
        mockMvc.perform(get("/api/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
