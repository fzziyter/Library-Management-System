package com.example.backend.integration;

import com.example.backend.entity.Categorie;
import com.example.backend.entity.DetailsLivre;
import com.example.backend.entity.Livre;
import com.example.backend.repository.CategorieRepository;
import com.example.backend.repository.LivreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TRUE Integration Tests — LivreController
 *
 * Full flow: HTTP Request → Security → Controller → Service → Repository → H2 Database
 * No @MockBean — everything is real, using H2 in-memory database.
 *
 * Place in: src/test/java/com/example/backend/controller/
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LivreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Real repositories — no mocks
    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    private Categorie savedCategorie;
    private Livre savedLivre;

    @BeforeEach
    void setUp() {
        // Clean state before each test (H2 in-memory)
        livreRepository.deleteAll();
        categorieRepository.deleteAll();

        // Seed a real Categorie in H2
        Categorie cat = new Categorie();
        cat.setLibelle("Roman");
        savedCategorie = categorieRepository.save(cat);

        // Seed a real Livre with its DetailsLivre in H2
        DetailsLivre details = new DetailsLivre();
        details.setAuteur("Victor Hugo");
        details.setNombrePages(500);
        details.setEmplacementRayon("A1");

        Livre livre = new Livre();
        livre.setTitre("Les Misérables");
        livre.setIsbn("978-2-07-040850-4");
        livre.setCategorie(savedCategorie);
        details.setLivre(livre);
        livre.setDetails(details);

        savedLivre = livreRepository.save(livre);
    }

    // ─── GET /api/livres ────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "USER")
    void testGetAllLivres_ReturnsRealDataFromH2() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre").value("Les Misérables"))
                .andExpect(jsonPath("$[0].isbn").value("978-2-07-040850-4"))
                .andExpect(jsonPath("$[0].categorie.libelle").value("Roman"));
    }

    // ─── GET /api/livres/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "USER")
    void testGetLivreById_ReturnsRealBookFromH2() throws Exception {
        mockMvc.perform(get("/api/livres/" + savedLivre.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables"))
                .andExpect(jsonPath("$.isbn").value("978-2-07-040850-4"))
                .andExpect(jsonPath("$.details.auteur").value("Victor Hugo"))
                .andExpect(jsonPath("$.details.nombrePages").value(500));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testGetLivreById_Returns404_WhenNotInH2() throws Exception {
        mockMvc.perform(get("/api/livres/9999"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/livres ───────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateLivre_PersistsReallyInH2() throws Exception {
        String body = String.format("""
            {
              "titre": "Notre-Dame de Paris",
              "isbn": "978-2-07-040123-9",
              "categorie": { "id": %d },
              "details": {
                "auteur": "Victor Hugo",
                "nombrePages": 600,
                "emplacementRayon": "B2"
              }
            }
            """, savedCategorie.getId());

        mockMvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Notre-Dame de Paris"))
                .andExpect(jsonPath("$.isbn").value("978-2-07-040123-9"))
                .andExpect(jsonPath("$.id").isNumber());

        // Verify it was actually saved in H2
        assertEquals(2, livreRepository.count());
        assertTrue(livreRepository.findAll()
                .stream().anyMatch(l -> l.getTitre().equals("Notre-Dame de Paris")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateLivre_Returns404_WhenCategorieDoesNotExistInH2() throws Exception {
        String body = """
            {
              "titre": "Livre Orphelin",
              "isbn": "000-0-00-000000-0",
              "categorie": { "id": 9999 },
              "details": {
                "auteur": "Inconnu",
                "nombrePages": 100,
                "emplacementRayon": "Z9"
              }
            }
            """;

        // Category 9999 does not exist in H2 → service throws ResourceNotFoundException
        mockMvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        // Verify nothing was persisted
        assertEquals(1, livreRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateLivre_Returns400_WhenBodyInvalid() throws Exception {
        // Missing required fields (titre, isbn, categorie, details)
        String body = "{}";

        mockMvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /api/livres/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateLivre_UpdatesReallyInH2() throws Exception {
        String body = String.format("""
            {
              "titre": "Les Misérables — Édition Intégrale",
              "isbn": "978-2-07-040850-4",
              "categorie": { "id": %d },
              "details": {
                "auteur": "Victor Hugo",
                "nombrePages": 1500,
                "emplacementRayon": "A2"
              }
            }
            """, savedCategorie.getId());

        mockMvc.perform(put("/api/livres/" + savedLivre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables — Édition Intégrale"))
                .andExpect(jsonPath("$.details.nombrePages").value(1500));

        // Verify the update was actually persisted in H2
        Livre updated = livreRepository.findById(savedLivre.getId()).orElseThrow();
        assertEquals("Les Misérables — Édition Intégrale", updated.getTitre());
        assertEquals(1500, updated.getDetails().getNombrePages());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateLivre_Returns404_WhenNotInH2() throws Exception {
        String body = String.format("""
            {
              "titre": "Fantôme",
              "isbn": "000-0",
              "categorie": { "id": %d },
              "details": { "auteur": "X", "nombrePages": 1, "emplacementRayon": "Z" }
            }
            """, savedCategorie.getId());

        mockMvc.perform(put("/api/livres/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/livres/{id} ────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteLivre_RemovesReallyFromH2() throws Exception {
        mockMvc.perform(delete("/api/livres/" + savedLivre.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Livre supprimé avec succès"));

        // Verify it was actually removed from H2
        assertEquals(0, livreRepository.count());
        assertFalse(livreRepository.existsById(savedLivre.getId()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteLivre_Returns404_WhenNotInH2() throws Exception {
        mockMvc.perform(delete("/api/livres/9999"))
                .andExpect(status().isNotFound());

        // Original record still exists
        assertEquals(1, livreRepository.count());
    }
}