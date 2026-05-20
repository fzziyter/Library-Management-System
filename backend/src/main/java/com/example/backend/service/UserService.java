package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.Role;
import com.example.backend.dto.UserDTO;
import com.example.backend.entity.ERole;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(ERole.valueOf(roleName))
                .orElseThrow(() -> new RuntimeException("Erreur: Rôle non trouvé."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        existingUser.setUsername(userDTO.getUsername());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRoles() != null) {
            Set<Role> roles = userDTO.getRoles().stream()
                .map(r -> roleRepository.findByName(ERole.valueOf(r))
                .orElseThrow(() -> new RuntimeException("Role non trouvé: " + r)))
                .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        return userRepository.save(existingUser);
    }
}