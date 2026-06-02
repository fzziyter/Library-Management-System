package com.example.backend.controller;

import com.example.backend.dto.UserDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import com.example.backend.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
@ActiveProfiles("test")
@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité Spring Security globale pour tester purement le comportement logique du contrôleur
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testGetAllUsers() throws Exception {
        User user1 = new User(); user1.setId(1L); user1.setUsername("user1");
        User user2 = new User(); user2.setId(2L); user2.setUsername("user2");

        Mockito.when(userService.findAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        User inputUser = new User();
        inputUser.setUsername("khalid");
        inputUser.setPassword("pass123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("khalid");

        Mockito.when(userRepository.findByUsername("khalid")).thenReturn(Optional.empty());
        Mockito.when(userService.saveUser(Mockito.any(User.class), Mockito.eq("ADMIN"))).thenReturn(savedUser);

        mockMvc.perform(post("/api/admin/users")
                        .param("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("khalid")));
    }

    @Test
    void testCreateUserThrowsExceptionWhenUsernameExists() throws Exception {
        User inputUser = new User();
        inputUser.setUsername("deja_pris");

        // Simule que l'utilisateur existe déjà
        Mockito.when(userRepository.findByUsername("deja_pris")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/admin/users")
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isBadRequest()); // Propage une IllegalArgumentException (HTTP 400)
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(delete("/api/admin/users/{id}", userId))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        Long userId = 999L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/admin/users/{id}", userId))
                .andExpect(status().isNotFound()); // Capture ta ResourceNotFoundException (HTTP 404)
    }
}