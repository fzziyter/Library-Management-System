package com.example.backend.service;

import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.entity.DetailsLivre;
import com.example.backend.entity.Livre;
import com.example.backend.repository.LivreRepository;
import com.example.backend.repository.CategorieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LivreService {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private CategorieRepository categorieRepository; // Injecté pour sécuriser la vérification de la catégorie

    public Livre enregistrerLivre(Livre livre){
        // Sécurité supplémentaire : Vérifie si la catégorie rattachée existe en BDR
        if (livre.getCategorie() != null && livre.getCategorie().getId() != null) {
            categorieRepository.findById(livre.getCategorie().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("La catégorie spécifiée avec l'id " + livre.getCategorie().getId() + " n'existe pas."));
        }

        if (livre.getDetails() != null) {
            livre.getDetails().setLivre(livre);
        }
        return livreRepository.save(livre);
    }

    public List<Livre> listerTousLesLivres(){
        return livreRepository.findAll();
    }

    @Transactional
    public void supprimerLivre(Long id){
        if (!livreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer. Livre non trouvé avec l'id: " + id);
        }
        livreRepository.deleteById(id);
    }

    @Transactional
    public Livre modifierLivre(Long id, Livre livreDetails){
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé avec l'id: " + id));

        // Sécurité : Vérification de la catégorie lors du PUT
        if (livreDetails.getCategorie() != null && livreDetails.getCategorie().getId() != null) {
            categorieRepository.findById(livreDetails.getCategorie().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("La catégorie spécifiée avec l'id " + livreDetails.getCategorie().getId() + " n'existe pas."));
            livre.setCategorie(livreDetails.getCategorie());
        }

        livre.setTitre(livreDetails.getTitre());
        livre.setIsbn(livreDetails.getIsbn());
        if (livreDetails.getDetails() != null) {
            DetailsLivre detailsLivre = livre.getDetails();
            if (detailsLivre == null) {
                detailsLivre = new DetailsLivre();
                detailsLivre.setLivre(livre);
            }
            detailsLivre.setAuteur(livreDetails.getDetails().getAuteur());
            detailsLivre.setNombrePages(livreDetails.getDetails().getNombrePages());
            detailsLivre.setEmplacementRayon(livreDetails.getDetails().getEmplacementRayon());
            livre.setDetails(detailsLivre);
        }
        return livreRepository.save(livre);
    }
    public Livre trouverParId(Long id) {
        return livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé avec l'id: " + id));
    }
}