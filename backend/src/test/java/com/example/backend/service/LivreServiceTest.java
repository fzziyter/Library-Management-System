package com.example.backend.service;

import com.example.backend.entity.Categorie;
import com.example.backend.entity.DetailsLivre;
import com.example.backend.entity.Livre;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CategorieRepository;
import com.example.backend.repository.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @InjectMocks
    private LivreService livreService;

    private Categorie categorie;
    private Livre livre;

    @BeforeEach
    void setUp() {
        categorie = new Categorie();
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
    }

    // ==================== enregistrerLivre ====================

    @Test
    void testEnregistrerLivre_Success() {
        // ARRANGE
        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        Mockito.when(livreRepository.save(Mockito.any(Livre.class))).thenReturn(livre);

        // ACT
        Livre result = livreService.enregistrerLivre(livre);

        // ASSERT
        assertNotNull(result);
        assertEquals("Les Misérables", result.getTitre());
        assertEquals("978-2-07-040850-4", result.getIsbn());
        assertNotNull(result.getDetails());
        assertEquals(livre, result.getDetails().getLivre()); // lien bidirectionnel vérifié
        Mockito.verify(livreRepository, Mockito.times(1)).save(Mockito.any(Livre.class));
    }

    @Test
    void testEnregistrerLivre_ThrowsWhenCategorieNotFound() {
        // ARRANGE
        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> livreService.enregistrerLivre(livre));
        Mockito.verify(livreRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void testEnregistrerLivre_SansCategorie_Success() {
        // ARRANGE : un livre sans catégorie ne doit pas déclencher la vérification
        Livre livreSansCategorie = new Livre();
        livreSansCategorie.setTitre("Livre Libre");
        livreSansCategorie.setIsbn("000-0-00-000000-0");

        Mockito.when(livreRepository.save(Mockito.any(Livre.class))).thenReturn(livreSansCategorie);

        // ACT
        Livre result = livreService.enregistrerLivre(livreSansCategorie);

        // ASSERT
        assertNotNull(result);
        Mockito.verify(categorieRepository, Mockito.never()).findById(Mockito.any());
    }

    // ==================== listerTousLesLivres ====================

    @Test
    void testListerTousLesLivres_ReturnsList() {
        // ARRANGE
        Mockito.when(livreRepository.findAll()).thenReturn(List.of(livre));

        // ACT
        List<Livre> result = livreService.listerTousLesLivres();

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Les Misérables", result.get(0).getTitre());
    }

    @Test
    void testListerTousLesLivres_ReturnsEmptyList() {
        // ARRANGE
        Mockito.when(livreRepository.findAll()).thenReturn(List.of());

        // ACT
        List<Livre> result = livreService.listerTousLesLivres();

        // ASSERT
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== trouverParId ====================

    @Test
    void testTrouverParId_Success() {
        // ARRANGE
        Mockito.when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));

        // ACT
        Livre result = livreService.trouverParId(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Les Misérables", result.getTitre());
    }

    @Test
    void testTrouverParId_ThrowsWhenNotFound() {
        // ARRANGE
        Mockito.when(livreRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> livreService.trouverParId(99L));
    }

    // ==================== supprimerLivre ====================

    @Test
    void testSupprimerLivre_Success() {
        // ARRANGE
        Mockito.when(livreRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(livreRepository).deleteById(1L);

        // ACT
        assertDoesNotThrow(() -> livreService.supprimerLivre(1L));

        // ASSERT
        Mockito.verify(livreRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    void testSupprimerLivre_ThrowsWhenNotFound() {
        // ARRANGE
        Mockito.when(livreRepository.existsById(99L)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> livreService.supprimerLivre(99L));
        Mockito.verify(livreRepository, Mockito.never()).deleteById(Mockito.any());
    }

    // ==================== modifierLivre ====================

    @Test
    void testModifierLivre_Success() {
        // ARRANGE
        Livre livreModifie = new Livre();
        livreModifie.setTitre("Notre-Dame de Paris");
        livreModifie.setIsbn("978-2-07-040123-9");
        livreModifie.setCategorie(categorie);

        DetailsLivre nouveauxDetails = new DetailsLivre();
        nouveauxDetails.setAuteur("Victor Hugo");
        nouveauxDetails.setNombrePages(600);
        nouveauxDetails.setEmplacementRayon("B2");
        livreModifie.setDetails(nouveauxDetails);

        Mockito.when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        Mockito.when(livreRepository.save(Mockito.any(Livre.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        Livre result = livreService.modifierLivre(1L, livreModifie);

        // ASSERT
        assertNotNull(result);
        assertEquals("Notre-Dame de Paris", result.getTitre());
        assertEquals("978-2-07-040123-9", result.getIsbn());
        assertEquals(600, result.getDetails().getNombrePages());
    }

    @Test
    void testModifierLivre_ThrowsWhenLivreNotFound() {
        // ARRANGE
        Mockito.when(livreRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> livreService.modifierLivre(99L, livre));
    }

    @Test
    void testModifierLivre_ThrowsWhenCategorieNotFound() {
        // ARRANGE
        Mockito.when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> livreService.modifierLivre(1L, livre));
    }
}