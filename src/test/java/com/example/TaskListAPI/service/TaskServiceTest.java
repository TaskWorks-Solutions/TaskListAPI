package com.example.TaskListAPI.service;

import com.example.TaskListAPI.dto.TaskRequest;
import com.example.TaskListAPI.dto.TaskResponse;
import com.example.TaskListAPI.entity.Task;
import com.example.TaskListAPI.enums.Status;
import com.example.TaskListAPI.exception.TaskNotFoundException;
import com.example.TaskListAPI.mapper.TaskMapper;
import com.example.TaskListAPI.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequest taskRequest;
    private TaskResponse taskResponse;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        taskRequest = new TaskRequest(
            "Test Task",
            "Test Description",
            LocalDate.now().plusDays(7),
            Status.PENDING,
            Task.Priority.MEDIUM
        );

        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setStatus(Status.PENDING);
        task.setPriority(Task.Priority.MEDIUM);

        taskResponse = new TaskResponse(
            taskId,
            "Test Task",
            "Test Description",
            LocalDate.now().plusDays(7),
            Status.PENDING,
            Task.Priority.MEDIUM,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );
    }

    @Test
    void createTask_ShouldReturnTaskResponse_WhenValidRequest() {
        // Given
        when(taskMapper.toEntity(taskRequest)).thenReturn(task);
        when(taskRepository.saveAndFlush(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.createTask(taskRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(taskRequest.getTitle());
        assertThat(result.getDescription()).isEqualTo(taskRequest.getDescription());
        assertThat(result.getStatus()).isEqualTo(taskRequest.getStatus());
        assertThat(result.getPriority()).isEqualTo(taskRequest.getPriority());

        verify(taskMapper).toEntity(taskRequest);
        verify(taskRepository).saveAndFlush(task);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void getTaskById_ShouldReturnTaskResponse_WhenTaskExists() {
        // Given
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.getTaskById(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void getTaskById_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Given
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskMapper, never()).toResponse(any());
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Given
        List<Task> tasks = List.of(task);

        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        List<TaskResponse> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(taskId);

        verify(taskRepository).findAll();
        verify(taskMapper).toResponse(task);
    }

    @Test
    void getTasksByStatus_ShouldReturnTasksWithGivenStatus() {
        // Given
        Status status = Status.PENDING;
        List<Task> tasks = List.of(task);
        when(taskRepository.findByStatusOrderByPriorityDesc(status)).thenReturn(tasks);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        List<TaskResponse> result = taskService.getTasksByStatus(status);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);

        verify(taskRepository).findByStatusOrderByPriorityDesc(status);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void updateTask_ShouldReturnUpdatedTaskResponse_WhenTaskExists() {
        // Given
        TaskRequest updatedRequest = new TaskRequest(
            "Updated Task",
            "Updated Description",
            LocalDate.now().plusDays(14),
            Status.IN_PROGRESS,
            Task.Priority.HIGH
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.updateTask(taskId, updatedRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskMapper).updateEntity(task, updatedRequest);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void updateTask_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Given
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(taskId, taskRequest));

        verify(taskRepository).findById(taskId);
        verify(taskMapper, never()).updateEntity(any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenTaskExists() {
        // Given
        when(taskRepository.existsById(taskId)).thenReturn(true);

        // When
        taskService.deleteTask(taskId);

        // Then
        verify(taskRepository).existsById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Given
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(taskId));

        verify(taskRepository).existsById(taskId);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void searchTasks_ShouldReturnTasksContainingKeyword() {
        // Given
        String keyword = "test";
        when(taskRepository.findByKeyword(keyword)).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        List<TaskResponse> result = taskService.searchTasks(keyword);

        // Then
        assertThat(result).hasSize(1);

        verify(taskRepository).findByKeyword(keyword);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void getOverdueTasks_ShouldReturnOverdueTasks() {
        // Given
        List<Task> tasks = List.of(task);

        when(taskRepository.findOverdueTasks(any(LocalDate.class))).thenReturn(tasks);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        List<TaskResponse> result = taskService.getOverdueTasks();

        // Then
        assertThat(result).hasSize(1);

        verify(taskRepository).findOverdueTasks(any(LocalDate.class));
        verify(taskMapper).toResponse(task);
    }

    @Test
    void updateTaskStatus_ShouldReturnUpdatedTaskResponse_WhenTaskExists() {
        // Given
        Status newStatus = Status.COMPLETED;

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.updateTaskStatus(taskId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void updateTaskStatus_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Given
        Status newStatus = Status.COMPLETED;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTaskStatus(taskId, newStatus));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }
}
