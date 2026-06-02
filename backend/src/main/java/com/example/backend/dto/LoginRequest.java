package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    // 1. Constructeur par défaut (Obligatoire pour Spring/Jackson)
    public LoginRequest() {
    }

    // 2. Constructeur avec arguments (Facilite l'instanciation dans vos tests)
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}