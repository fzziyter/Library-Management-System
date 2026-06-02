package com.example.backend.controller;

import com.example.backend.dto.LivreDTO;
import com.example.backend.entity.Livre;
import com.example.backend.mapper.LivreMapper;
import com.example.backend.service.LivreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/livres")
@CrossOrigin("*")
public class LivreController {

    @Autowired
    private LivreService livreService;
    @Autowired
    private LivreMapper livreMapper;

    @PostMapping
    public LivreDTO creerLivre(@Valid @RequestBody LivreDTO livreDTO){
        Livre entity = livreMapper.toEntity(livreDTO);
        Livre sauve = livreService.enregistrerLivre(entity);
        return livreMapper.toDTO(sauve);
    }

    @GetMapping
    public List<LivreDTO> obtenirTousLesLivres(){
        List<Livre> livres = livreService.listerTousLesLivres();
        return livreMapper.toDTOs(livres);
    }

    @DeleteMapping("/{id}")
    public String supprimerLivre(@PathVariable Long id){
        livreService.supprimerLivre(id);
        return "Livre supprimé avec succès";
    }

    @PutMapping("/{id}")
    public LivreDTO modifierLivre(@PathVariable Long id, @Valid @RequestBody LivreDTO livredto){
        Livre entity = livreMapper.toEntity(livredto);
        Livre modifie = livreService.modifierLivre(id, entity);
        return livreMapper.toDTO(modifie);
    }

    @GetMapping("/{id}")
    public LivreDTO obtenirLivreParId(@PathVariable Long id) {
        Livre livre = livreService.trouverParId(id);
        return livreMapper.toDTO(livre);
    }
}