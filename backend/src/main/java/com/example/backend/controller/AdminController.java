package com.example.backend.controller;

import com.example.backend.dto.UserDTO;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.repository.UserRepository;
import com.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody User user, @RequestParam String role) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide.");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        }
        return userService.saveUser(user, role);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        // Intercepte l'erreur avant la suppression si l'ID n'existe pas
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de supprimer : Aucun utilisateur trouvé avec l'ID #" + id));
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de modifier : Aucun utilisateur trouvé avec l'ID #" + id));
        return userService.updateUser(id, userDTO);
    }
}