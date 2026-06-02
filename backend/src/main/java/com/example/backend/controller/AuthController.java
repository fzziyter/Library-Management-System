package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.JwtResponse;
import com.example.backend.dto.SignupRequest;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.jwt.JwtUtils;
import com.example.backend.service.UserDetailsImpl;
import com.example.backend.service.UserService;
import com.example.backend.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

        @Autowired
        AuthenticationManager authenticationManager;

        @Autowired
        JwtUtils jwtUtils;

        @Autowired
        UserService userService;

        @Autowired
        UserRepository userRepository;

        @PostMapping("/signin")
        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
                // Déclenche automatiquement BadCredentialsException (gérée par le GlobalExceptionHandler) si invalide
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                String jwt = jwtUtils.generateJwtToken(userDetails);

                List<String> roles = userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        roles));
        }

        @PostMapping("/signup")
        public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
                // Utilisation d'une exception de conflit au lieu d'une String brute pour forcer le passage dans le handler
                if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
                        throw new IllegalArgumentException("Erreur : Ce nom d'utilisateur est déjà pris.");
                }

                User user = new User();
                user.setUsername(signupRequest.getUsername());
                user.setPassword(signupRequest.getPassword());

                userService.saveUser(user, "USER");

                return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
                        put("message", "Compte créé avec succès.");
                }});
        }
}