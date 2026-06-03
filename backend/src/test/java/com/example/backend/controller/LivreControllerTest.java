package com.example.backend.controller;

import com.example.backend.dto.LivreDTO;
import com.example.backend.entity.Categorie;
import com.example.backend.entity.DetailsLivre;
import com.example.backend.entity.Livre;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.LivreMapper;
import com.example.backend.service.LivreService;
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
public class LivreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LivreService livreService;

    @MockBean
    private LivreMapper livreMapper;

    private Livre livre;
    private LivreDTO livreDTO;

    @BeforeEach
    void setUp() {
        Categorie categorie = new Categorie();
        categorie.setId(1L);
        categorie.setLibelle("Roman");

        DetailsLivre details = new DetailsLivre();
        details.setAuteur("Victor Hugo");
        details.setNombrePages(500);
        details.setEmplacementRayon("A1");

        livre = new Livre();
        livre.setId(1L);
        livre.setTitre("Les Misérables");
        livre.setIsbn("978-2-07-040850-4");
        livre.setCategorie(categorie);
        livre.setDetails(details);

        LivreDTO.CategoriesDTO catDTO = new LivreDTO.CategoriesDTO();
        catDTO.setId(1L);
        catDTO.setLibelle("Roman");

        livreDTO = new LivreDTO();
        livreDTO.setId(1L);
        livreDTO.setTitre("Les Misérables");
        livreDTO.setIsbn("978-2-07-040850-4");
        livreDTO.setCategorie(catDTO);
        livreDTO.setDetails(details);
    }

    // ==================== GET /api/livres ====================

    @Test
    @WithMockUser(authorities = "USER")
    void testObtenirTousLesLivres_Returns200() throws Exception {
        Mockito.when(livreService.listerTousLesLivres()).thenReturn(List.of(livre));
        Mockito.when(livreMapper.toDTOs(List.of(livre))).thenReturn(List.of(livreDTO));

        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].titre").value("Les Misérables"))
                .andExpect(jsonPath("$[0].isbn").value("978-2-07-040850-4"));
    }

    // ==================== GET /api/livres/{id} ====================

    @Test
    @WithMockUser(authorities = "USER")
    void testObtenirLivreParId_Returns200() throws Exception {
        Mockito.when(livreService.trouverParId(1L)).thenReturn(livre);
        Mockito.when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        mockMvc.perform(get("/api/livres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("Les Misérables"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testObtenirLivreParId_Returns404WhenNotFound() throws Exception {
        Mockito.when(livreService.trouverParId(99L))
               .thenThrow(new ResourceNotFoundException("Livre non trouvé avec l'id: 99"));

        mockMvc.perform(get("/api/livres/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/livres ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreerLivre_Returns200() throws Exception {
        Mockito.when(livreMapper.toEntity(Mockito.any(LivreDTO.class))).thenReturn(livre);
        Mockito.when(livreService.enregistrerLivre(Mockito.any(Livre.class))).thenReturn(livre);
        Mockito.when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        mockMvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(livreDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables"))
                .andExpect(jsonPath("$.isbn").value("978-2-07-040850-4"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreerLivre_Returns400WhenInvalidBody() throws Exception {
        // DTO vide → violations @NotBlank / @NotNull
        LivreDTO invalid = new LivreDTO();

        mockMvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/livres/{id} ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testModifierLivre_Returns200() throws Exception {
        Mockito.when(livreMapper.toEntity(Mockito.any(LivreDTO.class))).thenReturn(livre);
        Mockito.when(livreService.modifierLivre(Mockito.eq(1L), Mockito.any(Livre.class))).thenReturn(livre);
        Mockito.when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        mockMvc.perform(put("/api/livres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(livreDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testModifierLivre_Returns404WhenNotFound() throws Exception {
        Mockito.when(livreMapper.toEntity(Mockito.any(LivreDTO.class))).thenReturn(livre);
        Mockito.when(livreService.modifierLivre(Mockito.eq(99L), Mockito.any(Livre.class)))
               .thenThrow(new ResourceNotFoundException("Livre non trouvé avec l'id: 99"));

        mockMvc.perform(put("/api/livres/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(livreDTO)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/livres/{id} ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testSupprimerLivre_Returns200() throws Exception {
        Mockito.doNothing().when(livreService).supprimerLivre(1L);

        mockMvc.perform(delete("/api/livres/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Livre supprimé avec succès"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testSupprimerLivre_Returns404WhenNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Livre non trouvé avec l'id: 99"))
               .when(livreService).supprimerLivre(99L);

        mockMvc.perform(delete("/api/livres/99"))
                .andExpect(status().isNotFound());
    }
}