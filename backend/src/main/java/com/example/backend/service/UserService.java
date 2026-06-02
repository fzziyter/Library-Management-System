package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.Role;
import com.example.backend.dto.UserDTO;
import com.example.backend.entity.ERole;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Méthode utilitaire pour convertir String vers ERole en toute sécurité
    private ERole convertToERole(String roleName) {
        try {
            return ERole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Erreur: Le rôle '" + roleName + "' n'est pas reconnu par le système.");
        }
    }

    public User saveUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Utilisation de la conversion sécurisée
        ERole roleEnum = convertToERole(roleName);

        Role userRole = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Erreur: Le rôle '" + roleName + "' n'existe pas en base de données."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer. Utilisateur non trouvé avec l'id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        existingUser.setUsername(userDTO.getUsername());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRoles() != null) {
            Set<Role> roles = userDTO.getRoles().stream()
                    .map(r -> {
                        // Utilisation de la conversion sécurisée ici aussi
                        ERole roleEnum = convertToERole(r);
                        return roleRepository.findByName(roleEnum)
                                .orElseThrow(() -> new ResourceNotFoundException("Le rôle spécifié n'existe pas : " + r));
                    })
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        return userRepository.save(existingUser);
    }
}