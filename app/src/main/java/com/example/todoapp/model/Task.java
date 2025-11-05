package com.example.todoapp.model;

public class Task {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String createdAt;
    private String dueDate;
    private int status;

    public Task() { } // Bắt buộc cho Firebase

    public Task(String id, String userId, String title, String description, String createdAt, String dueDate, int status) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
