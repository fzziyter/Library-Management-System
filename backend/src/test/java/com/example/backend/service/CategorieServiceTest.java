package com.example.backend.service;

import com.example.backend.entity.Categorie;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CategorieRepository;
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
public class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    @InjectMocks
    private CategorieService categorieService;

    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = new Categorie();
        categorie.setId(1L);
        categorie.setLibelle("Roman");
    }

    // ==================== enregistrerCategorie ====================

    @Test
    void testEnregistrerCategorie_Success() {
        // ARRANGE
        Mockito.when(categorieRepository.save(Mockito.any(Categorie.class))).thenReturn(categorie);

        // ACT
        Categorie result = categorieService.enregistrerCategorie(categorie);

        // ASSERT
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Roman", result.getLibelle());
        Mockito.verify(categorieRepository, Mockito.times(1)).save(categorie);
    }

    // ==================== getAllCategorie ====================

    @Test
    void testGetAllCategorie_ReturnsList() {
        // ARRANGE
        Categorie categorie2 = new Categorie();
        categorie2.setId(2L);
        categorie2.setLibelle("Science-Fiction");

        Mockito.when(categorieRepository.findAll()).thenReturn(List.of(categorie, categorie2));

        // ACT
        List<Categorie> result = categorieService.getAllCategorie();

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Roman", result.get(0).getLibelle());
        assertEquals("Science-Fiction", result.get(1).getLibelle());
    }

    @Test
    void testGetAllCategorie_ReturnsEmptyList() {
        // ARRANGE
        Mockito.when(categorieRepository.findAll()).thenReturn(List.of());

        // ACT
        List<Categorie> result = categorieService.getAllCategorie();

        // ASSERT
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== supprimerCategorie ====================

    @Test
    void testSupprimerCategorie_Success() {
        // ARRANGE
        Mockito.when(categorieRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(categorieRepository).deleteById(1L);

        // ACT
        assertDoesNotThrow(() -> categorieService.supprimerCategorie(1L));

        // ASSERT
        Mockito.verify(categorieRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    void testSupprimerCategorie_ThrowsWhenNotFound() {
        // ARRANGE
        Mockito.when(categorieRepository.existsById(99L)).thenReturn(false);

        // ACT & ASSERT
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> categorieService.supprimerCategorie(99L)
        );
        assertTrue(ex.getMessage().contains("99"));
        Mockito.verify(categorieRepository, Mockito.never()).deleteById(Mockito.any());
    }

    // ==================== modifierCategorie ====================

    @Test
    void testModifierCategorie_Success() {
        // ARRANGE
        Categorie categorieUpdate = new Categorie();
        categorieUpdate.setLibelle("Policier");

        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        Mockito.when(categorieRepository.save(Mockito.any(Categorie.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        Categorie result = categorieService.modifierCategorie(1L, categorieUpdate);

        // ASSERT
        assertNotNull(result);
        assertEquals("Policier", result.getLibelle()); // Le libellé a été mis à jour
        assertEquals(1L, result.getId());              // L'ID reste inchangé
        Mockito.verify(categorieRepository, Mockito.times(1)).save(Mockito.any(Categorie.class));
    }

    @Test
    void testModifierCategorie_ThrowsWhenNotFound() {
        // ARRANGE
        Mockito.when(categorieRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
            ResourceNotFoundException.class,
            () -> categorieService.modifierCategorie(99L, categorie)
        );
        Mockito.verify(categorieRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void testModifierCategorie_PreservesAssociations() {
        // ARRANGE : vérifie que les livres associés ne sont pas écrasés
        Categorie categorieUpdate = new Categorie();
        categorieUpdate.setLibelle("Nouveau Libellé");

        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        Mockito.when(categorieRepository.save(Mockito.any(Categorie.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        Categorie result = categorieService.modifierCategorie(1L, categorieUpdate);

        // ASSERT : la liste de livres de la catégorie d'origine doit rester inchangée
        assertEquals(categorie.getLivres(), result.getLivres());
    }
}