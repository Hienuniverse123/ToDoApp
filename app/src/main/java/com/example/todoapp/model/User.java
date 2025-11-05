package com.example.todoapp.model;

public class User {
    private String id; //UID từ Firebase Auth
    private String username;
    private String email;

    public User() { }

    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}