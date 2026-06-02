package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Ressource Introuvable (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    // 2. Erreurs de login / Bad credentials (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Identifiants incorrects (nom d'utilisateur ou mot de passe invalide).");
    }

    // 3. User introuvable lors de l'authentification (404)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    // 🆕 3.bis Compte utilisateur désactivé (403)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabledException(DisabledException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Votre compte est désactivé. Veuillez contacter l'administrateur.");
    }

    // 🆕 3.ter Compte utilisateur bloqué (403)
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> handleLockedException(LockedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Votre compte est bloqué suite à de trop nombreuses tentatives.");
    }

    // 🆕 3.quater Accès refusé / Rôle insuffisant (403) - Déclenché par @PreAuthorize("hasRole('ADMIN')")
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Accès refusé : Vous n'avez pas les permissions nécessaires (Rôle requis non possédé).");
    }

    // 4. Erreur de validation des champs DTO (400) - Déclenché par @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Erreurs de validation -> " + errors);
    }

    // 5. Mauvais type de données ou paramètre incorrect (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    // 🆕 5.bis Erreur de type de paramètre dans l'URL (ex: passer une String au lieu d'un Long pour l'ID) (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Le paramètre '%s' attend une valeur de type '%s'.", ex.getName(), ex.getRequiredType().getSimpleName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    // 🆕 5.ter Paramètre requis manquant dans la requête (ex: @RequestParam String role manquant) (400)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Le paramètre requis '" + ex.getParameterName() + "' est manquant.");
    }

    // 6. Capturer les erreurs de format JSON cassé ou types incohérents (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Le corps de la requête JSON est mal formé ou contient des types invalides.");
    }

    // 6.bis Capturer les erreurs de contraintes SQL (ex: clé étrangère ou doublon de contrainte unique) (409)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        String message = "Impossible d'effectuer cette action : contrainte d'intégrité violée.";

        // Extraction intelligente pour rendre le message plus précis si c'est lié aux catégories ou aux utilisateurs
        if (ex.getMessage() != null && ex.getMessage().contains("FOREIGN KEY")) {
            message = "Impossible de supprimer ou modifier cet élément car il est actuellement lié à d'autres données du système.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("UK_")) {
            message = "Une donnée unique (comme le nom d'utilisateur ou l'ISBN) existe déjà dans la base de données.";
        }

        return buildResponse(HttpStatus.CONFLICT, "Conflict", message);
    }

    // 7. Intercepteur ultime pour toutes les autres exceptions système non prévues (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalExceptions(Exception ex) {
        // Optionnel : Garder une trace dans la console du serveur pour le débogage de l'erreur imprévue
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Une erreur interne imprévue est survenue au niveau du serveur.");
    }

    // Méthode utilitaire standardisée pour l'ensemble de ton API
    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}