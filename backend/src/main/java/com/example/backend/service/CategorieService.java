package com.example.backend.service;

import com.example.backend.entity.Categorie;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CategorieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategorieService {

    @Autowired
    private CategorieRepository categorieRepository;

    public Categorie enregistrerCategorie(Categorie categorie){
        return categorieRepository.save(categorie);
    }

    @Transactional
    public void supprimerCategorie(Long id){
        if (!categorieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer. Catégorie non trouvée avec l'id: " + id);
        }
        categorieRepository.deleteById(id);
    }

    public List<Categorie> getAllCategorie(){
        return categorieRepository.findAll();
    }

    @Transactional
    public Categorie modifierCategorie(Long id, Categorie categorie){
        Categorie categorie1 = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non reconnue avec l'id: " + id));

        categorie1.setLibelle(categorie.getLibelle());
        // 🟢 CORRECTION : Suppression de la ligne setLivres() qui écrasait les associations existantes !
        return categorieRepository.save(categorie1);
    }
}