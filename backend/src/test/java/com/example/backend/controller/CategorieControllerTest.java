package com.example.backend.controller;

import com.example.backend.entity.Categorie;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.CategorieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategorieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategorieService categorieService;

    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = new Categorie();
        categorie.setId(1L);
        categorie.setLibelle("Roman");
    }

    // ==================== GET /api/categories ====================

    @Test
    @WithMockUser(authorities = "USER")
    void testObtenirToutesLesCategories_Returns200() throws Exception {
        Categorie categorie2 = new Categorie();
        categorie2.setId(2L);
        categorie2.setLibelle("Science-Fiction");

        Mockito.when(categorieService.getAllCategorie()).thenReturn(List.of(categorie, categorie2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].libelle").value("Roman"))
                .andExpect(jsonPath("$[1].libelle").value("Science-Fiction"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testObtenirToutesLesCategories_ReturnsEmptyList() throws Exception {
        Mockito.when(categorieService.getAllCategorie()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== POST /api/categories ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testAjouterCategorie_Returns200() throws Exception {
        Mockito.when(categorieService.enregistrerCategorie(Mockito.any(Categorie.class))).thenReturn(categorie);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.libelle").value("Roman"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testAjouterCategorie_Returns400WhenLibelleBlank() throws Exception {
        // libellé vide → violation @NotBlank
        Categorie invalid = new Categorie();
        invalid.setLibelle("");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/categories/{id} ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateCategorie_Returns200() throws Exception {
        Categorie updated = new Categorie();
        updated.setId(1L);
        updated.setLibelle("Policier");

        Mockito.when(categorieService.modifierCategorie(Mockito.eq(1L), Mockito.any(Categorie.class)))
               .thenReturn(updated);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Policier"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateCategorie_Returns404WhenNotFound() throws Exception {
        Mockito.when(categorieService.modifierCategorie(Mockito.eq(99L), Mockito.any(Categorie.class)))
               .thenThrow(new ResourceNotFoundException("Catégorie non reconnue avec l'id: 99"));

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorie)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/categories/{id} ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteCategorie_Returns200() throws Exception {
        Mockito.doNothing().when(categorieService).supprimerCategorie(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk());

        Mockito.verify(categorieService, Mockito.times(1)).supprimerCategorie(1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteCategorie_Returns404WhenNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Catégorie non trouvée avec l'id: 99"))
               .when(categorieService).supprimerCategorie(99L);

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound());
    }
}