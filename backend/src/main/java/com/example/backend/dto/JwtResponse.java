package com.example.backend.dto;

import java.util.List;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    // Getters & Setters
    public String getAccessToken() { return token; }
    public void setAccessToken(String accessToken) { this.token = accessToken; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
}