package com.example.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permet au frontend React d'envoyer les headers d'authentification (Bearer JWT)
        config.setAllowCredentials(true);

        // 🟢 CORRECTION : On autorise explicitement le port 5174 (et le 5173 par sécurité)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));

        // Autorise les en-têtes standards requis pour les requêtes HTTP et JWT
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));

        // Autorise toutes les méthodes d'accès, y compris OPTIONS pour le preflight check
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}