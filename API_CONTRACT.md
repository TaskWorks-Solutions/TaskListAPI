# Task List API Contract

## Base URL
```
http://localhost:8080/api/tasks
```

## Authentication
Currently no authentication required (Phase 1).

## Task Model

### TaskRequest (Create/Update)
```json
{
  "title": "string (required, max 200 chars)",
  "description": "string (optional, max 1000 chars)",
  "dueDate": "2024-01-01 (ISO date, optional)",
  "status": "PENDING|IN_PROGRESS|COMPLETED|CANCELLED (required)",
  "priority": "LOW|MEDIUM|HIGH (required)"
}
```

### TaskResponse (Read)
```json
{
  "id": "uuid",
  "title": "string",
  "description": "string",
  "dueDate": "2024-01-01",
  "status": "PENDING|IN_PROGRESS|COMPLETED|CANCELLED",
  "priority": "LOW|MEDIUM|HIGH",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

## Endpoints

### 1. Create Task
- **POST** `/api/tasks`
- **Request Body**: TaskRequest
- **Response**: TaskResponse (201 Created)
- **Error**: 400 Bad Request (validation errors)

### 2. Get All Tasks
- **GET** `/api/tasks`
- **Response**: Array of TaskResponse (200 OK)

### 3. Get Task by ID
- **GET** `/api/tasks/{id}`
- **Response**: TaskResponse (200 OK)
- **Error**: 404 Not Found

### 4. Update Task
- **PUT** `/api/tasks/{id}`
- **Request Body**: TaskRequest
- **Response**: TaskResponse (200 OK)
- **Error**: 404 Not Found, 400 Bad Request

### 5. Delete Task
- **DELETE** `/api/tasks/{id}`
- **Response**: 204 No Content
- **Error**: 404 Not Found

### 6. Filter Tasks by Status
- **GET** `/api/tasks/filter?status={status}`
- **Query Param**: status (PENDING|IN_PROGRESS|COMPLETED|CANCELLED)
- **Response**: Array of TaskResponse (200 OK)

### 7. Update Task Status
- **PATCH** `/api/tasks/{id}/status?status={status}`
- **Query Param**: status (new status)
- **Response**: TaskResponse (200 OK)
- **Error**: 404 Not Found

### 8. Search Tasks
- **GET** `/api/tasks/search?keyword={keyword}`
- **Query Param**: keyword (searches title and description)
- **Response**: Array of TaskResponse (200 OK)

### 9. Get Overdue Tasks
- **GET** `/api/tasks/overdue`
- **Response**: Array of TaskResponse (200 OK)

## Error Response Format
```json
{
  "status": 404,
  "message": "Task not found with id: xxx",
  "timestamp": "2024-01-01T10:00:00",
  "errors": null
}
```

## Validation Errors
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-01T10:00:00",
  "errors": {
    "title": "Title is required",
    "description": "Description must not exceed 1000 characters"
  }
}
```

## Environment Variables
- `DB_URL`: PostgreSQL connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

## Swagger UI
Available at: `http://localhost:8080/swagger-ui.html`

## OpenAPI Docs
Available at: `http://localhost:8080/api-docs`

## Docker Setup
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Database Schema
- **Table**: tasks
- **Columns**: id (UUID), title, description, due_date, status, priority, created_at, updated_at
- **Indexes**: status, priority, due_date, created_at
