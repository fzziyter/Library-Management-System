package com.example.backend.integration;

import com.example.backend.entity.Categorie;
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
 * TRUE Integration Tests — CategorieController
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
public class CategorieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private LivreRepository livreRepository;

    private Categorie savedCategorie;

    @BeforeEach
    void setUp() {
        livreRepository.deleteAll();
        categorieRepository.deleteAll();

        Categorie cat = new Categorie();
        cat.setLibelle("Roman");
        savedCategorie = categorieRepository.save(cat);
    }

    // ─── GET /api/categories ────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "USER")
    void testGetAllCategories_ReturnsRealDataFromH2() throws Exception {
        // Add a second category
        Categorie cat2 = new Categorie();
        cat2.setLibelle("Science-Fiction");
        categorieRepository.save(cat2);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].libelle", containsInAnyOrder("Roman", "Science-Fiction")));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testGetAllCategories_ReturnsEmptyList_WhenH2IsEmpty() throws Exception {
        categorieRepository.deleteAll();

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── POST /api/categories ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateCategorie_PersistsReallyInH2() throws Exception {
        String body = """
            { "libelle": "Policier" }
            """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Policier"))
                .andExpect(jsonPath("$.id").isNumber());

        // Verify it was actually saved in H2
        assertEquals(2, categorieRepository.count());
        assertTrue(categorieRepository.findAll()
                .stream().anyMatch(c -> c.getLibelle().equals("Policier")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateCategorie_Returns400_WhenLibelleBlank() throws Exception {
        String body = """
            { "libelle": "" }
            """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        // Verify nothing extra was saved
        assertEquals(1, categorieRepository.count());
    }

    // ─── PUT /api/categories/{id} ───────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateCategorie_UpdatesReallyInH2() throws Exception {
        String body = """
            { "libelle": "Policier" }
            """;

        mockMvc.perform(put("/api/categories/" + savedCategorie.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Policier"))
                .andExpect(jsonPath("$.id").value(savedCategorie.getId()));

        // Verify the update was persisted in H2
        Categorie updated = categorieRepository.findById(savedCategorie.getId()).orElseThrow();
        assertEquals("Policier", updated.getLibelle());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateCategorie_Returns404_WhenNotInH2() throws Exception {
        String body = """
            { "libelle": "Fantôme" }
            """;

        mockMvc.perform(put("/api/categories/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateCategorie_PreservesBookAssociations_InH2() throws Exception {
        // Attach a real Livre to the category in H2
        Livre livre = new Livre();
        livre.setTitre("Les Misérables");
        livre.setIsbn("978-2-07-040850-4");
        livre.setCategorie(savedCategorie);
        livreRepository.save(livre);

        // Update the category name
        String body = """
            { "libelle": "Littérature Classique" }
            """;

        mockMvc.perform(put("/api/categories/" + savedCategorie.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Littérature Classique"));

        // Verify the book still belongs to this category in H2
        Livre livreInDb = livreRepository.findById(livre.getId()).orElseThrow();
        assertEquals(savedCategorie.getId(), livreInDb.getCategorie().getId());
        assertEquals("Littérature Classique", livreInDb.getCategorie().getLibelle());
    }

    // ─── DELETE /api/categories/{id} ────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteCategorie_RemovesReallyFromH2() throws Exception {
        mockMvc.perform(delete("/api/categories/" + savedCategorie.getId()))
                .andExpect(status().isOk());

        // Verify it was actually removed from H2
        assertEquals(0, categorieRepository.count());
        assertFalse(categorieRepository.existsById(savedCategorie.getId()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteCategorie_Returns404_WhenNotInH2() throws Exception {
        mockMvc.perform(delete("/api/categories/9999"))
                .andExpect(status().isNotFound());

        // Original record still exists
        assertEquals(1, categorieRepository.count());
    }
}