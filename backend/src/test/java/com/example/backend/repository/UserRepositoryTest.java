package com.example.backend.repository;


import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFindByName() {
        // Arrange
        Role role = new Role();
        role.setName(ERole.USER);
        roleRepository.save(role);

        // Act
        Optional<Role> foundRole = roleRepository.findByName(ERole.USER);

        // Assert
        assertTrue(foundRole.isPresent());
        assertEquals(ERole.USER, foundRole.get().getName());
    }

    @Test
    void testFindByName_NotFound() {
        // Act
        Optional<Role> foundRole = roleRepository.findByName(ERole.ADMIN);

        // Assert
        assertFalse(foundRole.isPresent());
    }
}
