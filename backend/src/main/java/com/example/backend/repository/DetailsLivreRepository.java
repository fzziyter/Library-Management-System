package com.example.backend.repository;

import com.example.backend.entity.DetailsLivre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailsLivreRepository extends JpaRepository<DetailsLivre, Long> { // 🟢 Clé primaire passée en Long
}