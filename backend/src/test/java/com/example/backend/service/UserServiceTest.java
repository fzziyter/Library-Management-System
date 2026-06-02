package com.example.backend.service;


import com.example.backend.dto.UserDTO;
import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class) // Initialise Mockito pour JUnit 5
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService; // Injecte les mocks définis ci-dessus dans ton UserService

    @Test
    void testSaveUserSuccess() {
        // 1. ARRANGE : Données fictives et simulation des comportements (Mocks)
        User inputUser = new User();
        inputUser.setUsername("khalid");
        inputUser.setPassword("plain_password");

        Role mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setName(ERole.USER); // On utilise ton énumération ERole

        Mockito.when(passwordEncoder.encode("plain_password")).thenReturn("encoded_password_123");
        Mockito.when(roleRepository.findByName(ERole.USER)).thenReturn(Optional.of(mockRole));

        // Simule le retour après la sauvegarde en BDD
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("khalid");
        savedUser.setPassword("encoded_password_123");
        savedUser.setRoles(Set.of(mockRole));

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        // 2. ACT : Exécution de la méthode réelle de ton service
        User result = userService.saveUser(inputUser, "USER");

        // 3. ASSERT : Vérifications
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("encoded_password_123", result.getPassword());
        assertTrue(result.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.USER)));

        // Vérifie que la BDD a bien reçu l'ordre d'enregistrer
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    void testSaveUserThrowsExceptionWhenRoleNotFound() {
        // ARRANGE
        User inputUser = new User();
        inputUser.setUsername("user_error");
        inputUser.setPassword("password");

        Mockito.when(passwordEncoder.encode("password")).thenReturn("encoded_pass");
        // On simule le fait que le rôle n'existe pas en base de données
        Mockito.when(roleRepository.findByName(ERole.ADMIN)).thenReturn(Optional.empty());

        // ACT & ASSERT
        // Vérifie que le service lève bien ton exception personnalisée ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.saveUser(inputUser, "ADMIN");
        });

        // Sécurité : Vérifie que le code s'arrête et ne tente jamais de sauvegarder en BDD
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void testUpdateUserSuccess() {
        // ARRANGE
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("old_username");
        existingUser.setPassword("old_encoded_password");

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("new_username");
        userDTO.setPassword("new_plain_password");
        userDTO.setRoles(Collections.singleton("USER"));

        Role mockRole = new Role();
        mockRole.setName(ERole.USER);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.encode("new_plain_password")).thenReturn("new_encoded_password");
        Mockito.when(roleRepository.findByName(ERole.USER)).thenReturn(Optional.of(mockRole));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User updatedUser = userService.updateUser(userId, userDTO);

        // ASSERT
        assertNotNull(updatedUser);
        assertEquals("new_username", updatedUser.getUsername());
        assertEquals("new_encoded_password", updatedUser.getPassword());
    }

    @Test
    void testDeleteUserThrowsExceptionWhenUserDoesNotExist() {
        // ARRANGE
        Long targetId = 999L;
        // On simule le fait que l'utilisateur n'existe pas
        Mockito.when(userRepository.existsById(targetId)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(targetId);
        });

        // Vérifie qu'aucune suppression n'a été déclenchée
        Mockito.verify(userRepository, Mockito.never()).deleteById(targetId);
    }
}