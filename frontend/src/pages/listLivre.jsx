import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./listLivre.css";

const ListLivre = () => {
    const [livres, setLivres] = useState([]);
    const [categories, setCategories] = useState([]); // État ajouté pour les catégories
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [filterCategorie, setFilterCategorie] = useState("");
    
    const navigate = useNavigate();
    const API_URL = "http://localhost:8080/api/livres";

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            // On récupère les livres ET les catégories pour le filtre
            const [resLivres, resCats] = await Promise.all([
                axios.get(API_URL),
                axios.get("http://localhost:8080/api/categories")
            ]);
            setLivres(resLivres.data);
            setCategories(resCats.data);
        } catch (error) {
            console.error("Erreur:", error);
        } finally {
            setLoading(false);
        }
    };

    // Logique de filtrage (Calculée à chaque rendu)
    const livresFiltres = livres.filter(livre => {
        const matchesSearch = 
            livre.titre.toLowerCase().includes(searchTerm.toLowerCase()) || 
            livre.isbn.toLowerCase().includes(searchTerm.toLowerCase()) || 
            (livre.details?.auteur && livre.details.auteur.toLowerCase().includes(searchTerm.toLowerCase()));
        
        const matchesCategorie = filterCategorie === "" || livre.categorie?.libelle === filterCategorie;
        
        return matchesSearch && matchesCategorie;
    });

    const handleDelete = async (id) => {
        if (window.confirm("Supprimer ce livre ?")) {
            try {
                await axios.delete(`${API_URL}/${id}`);
                setLivres(livres.filter(l => l.id !== id));
            } catch (error) {
                alert("Erreur lors de la suppression");
            }
        }
    };

    if (loading) return <div className="loading">Chargement...</div>;

    return (
        <div className="list-container">
            <div className="list-header">
                <div>
                    <h2>Liste des livres</h2>
                    <p><strong>{livresFiltres.length}</strong> livres trouvés</p>
                </div>
                <button className="btn-add" onClick={() => navigate('/ajouter-livre')}>
                    + Ajouter un livre
                </button>
            </div>

            {/* Barre de recherche et filtres */}
            <div className="filter-bar">
                <input
                    type="text"
                    placeholder="Rechercher par titre, ISBN ou auteur..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="filter-input"
                />
                <select 
                    value={filterCategorie} 
                    onChange={(e) => setFilterCategorie(e.target.value)} 
                    className="filter-select"
                >
                    <option value="">Toutes les catégories</option>
                    {categories.map((cat) => (
                        <option key={cat.id} value={cat.libelle}>
                            {cat.libelle}
                        </option>
                    ))}
                </select>
            </div>

            <div className="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Titre</th>
                            <th>ISBN</th>
                            <th>Catégorie</th>
                            <th>Pages</th>
                            <th>Rayon</th>
                            <th>Auteur</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {/* CRITIQUE : Utiliser livresFiltres ici au lieu de livres */}
                        {livresFiltres.map((livre) => (
                            <tr key={livre.id}>
                                <td style={{fontWeight: '600'}}>{livre.titre}</td>
                                <td>{livre.isbn}</td>
                                <td>
                                    <span className="badge-category">
                                        {livre.categorie?.libelle || "N/A"}
                                    </span>
                                </td>
                                <td>{livre.details?.nombrePages || "-"}</td>
                                <td>{livre.details?.emplacementRayon || "-"}</td>
                                <td>{livre.details?.auteur || "-"}</td>
                                <td>
                                    <div className="actions-cell">
                                        <button className="btn-edit" onClick={() => navigate(`/modifier-livre/${livre.id}`)}>
                                            Modifier
                                        </button>
                                        <button className="btn-delete" onClick={() => handleDelete(livre.id)}>
                                            Supprimer
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                
                {/* Message si aucun résultat */}
                {livresFiltres.length === 0 && (
                    <div style={{textAlign: 'center', padding: '20px', color: '#666'}}>
                        Aucun livre ne correspond à votre recherche.
                    </div>
                )}
            </div>
        </div>
    );
};

export default ListLivre;