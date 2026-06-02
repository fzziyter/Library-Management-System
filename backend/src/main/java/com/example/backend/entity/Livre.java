package com.example.backend.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Livre {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String isbn;
    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @OneToOne(mappedBy = "livre", cascade = CascadeType.ALL)
    private DetailsLivre details;
}

