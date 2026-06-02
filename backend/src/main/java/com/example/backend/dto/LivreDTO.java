package com.example.backend.dto;

import com.example.backend.entity.DetailsLivre;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LivreDTO {
    private Long id;

    @NotBlank(message = "Le titre du livre est obligatoire.")
    @Size(min = 2, max = 150, message = "Le titre doit contenir entre 2 et 150 caractères.")
    private String titre;

    @NotBlank(message = "Le code ISBN est obligatoire.")
    private String isbn;

    @NotNull(message = "La catégorie du livre doit être spécifiée.")
    @Valid
    private CategoriesDTO categorie;

    @NotNull(message = "Les détails du livre (auteur, pages...) sont obligatoires.")
    @Valid
    private DetailsLivre details;

    @Data
    public static class CategoriesDTO {
        @NotNull(message = "L'ID de la catégorie est obligatoire.")
        private Long id;
        private String libelle;
    }
}