package com.example.backend.security;

import com.example.backend.security.jwt.AuthEntryPointJwt;
import com.example.backend.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Récupère automatiquement la configuration CORS depuis ta classe CorsConfig
                .cors(cors -> cors.configure(http))

                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Autorise les requêtes de vérification preflight des navigateurs (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Routes d'authentification et documentation publiques
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger/**", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs").permitAll()

                        // Accès restreints selon les rôles
                        .requestMatchers("/api/export/**").hasAnyAuthority("USER", "MANAGER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // Gestion des catégories
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").hasAnyAuthority("USER", "MANAGER", "ADMIN")
                        .requestMatchers("/api/categories/**").hasAnyAuthority("ADMIN", "MANAGER")

                        // Gestion des livres
                        .requestMatchers(HttpMethod.GET, "/api/livres/**").hasAnyAuthority("USER", "MANAGER", "ADMIN")
                        .requestMatchers("/api/livres/**").hasAnyAuthority("ADMIN", "MANAGER")

                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}