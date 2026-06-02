import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { getUser } from "../services/auth";
import "./listLivre.css";

const ListLivre = () => {
    const [livres, setLivres] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [globalError, setGlobalError] = useState(""); 
    const [searchTerm, setSearchTerm] = useState("");
    const [filterCategorie, setFilterCategorie] = useState("");

    const navigate = useNavigate();
    const API_URL = "http://localhost:8080/api/livres";

    const user = getUser();
    const roles = user?.roles ?? [];
    const isAdmin = roles.includes('ADMIN');
    const canEdit = roles.includes('MANAGER') || isAdmin;

    useEffect(() => { 
        fetchData(); 
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            setGlobalError("");
            
            // Lancement des requêtes parallèles vers Spring Boot
            const [resLivres, resCats] = await Promise.all([
                axios.get(API_URL),
                axios.get("http://localhost:8080/api/categories")
            ]);
            
            setLivres(resLivres.data);
            setCategories(resCats.data);
        } catch (error) {
            console.error("Erreur au chargement des données:", error);
            
            // Interception dynamique sur le chargement initial
            if (error.response) {
                const status = error.response.status;
                if (status === 401) {
                    setGlobalError("Votre session a expiré. Veuillez vous reconnecter pour accéder au catalogue.");
                } else if (status === 403) {
                    setGlobalError("Accès refusé : Vous n'avez pas les privilèges nécessaires pour consulter ces listes.");
                } else if (error.response.data?.message) {
                    setGlobalError(error.response.data.message);
                } else {
                    setGlobalError(`Erreur serveur lors de la récupération des données (Code: ${status}).`);
                }
            } else if (error.request) {
                setGlobalError("Le serveur API Spring Boot est injoignable. Vérifiez qu'il tourne correctement sur le port 8080.");
            } else {
                setGlobalError("Une erreur inattendue est survenue lors de l'initialisation du catalogue.");
            }
        } finally {
            setLoading(false);
        }
    };

    const livresFiltres = livres.filter(livre => {
        const matchesSearch =
            livre.titre.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (livre.isbn && livre.isbn.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (livre.details?.auteur && livre.details.auteur.toLowerCase().includes(searchTerm.toLowerCase()));
        const matchesCategorie = filterCategorie === "" || livre.categorie?.libelle === filterCategorie;
        return matchesSearch && matchesCategorie;
    });

    const handleDelete = async (id) => {
        if (window.confirm("Êtes-vous sûr de vouloir supprimer définitivement ce livre ?")) {
            try {
                setGlobalError("");
                await axios.delete(`${API_URL}/${id}`);
                
                // Mise à jour de l'état local uniquement en cas de succès de la promesse
                setLivres(livres.filter(l => l.id !== id));
            } catch (error) {
                console.error("Erreur lors de la suppression:", error);
                
                // Extraction chirurgicale des messages d'erreur du GlobalExceptionHandler (ex: ResourceNotFoundException)
                if (error.response) {
                    const status = error.response.status;
                    const data = error.response.data;

                    if (status === 403) {
                        setGlobalError("Accès refusé : Seul un administrateur possède les droits pour supprimer un livre.");
                    } else if (status === 401) {
                        setGlobalError("Votre session a expiré. Action de suppression interrompue.");
                    } else if (data && data.message) {
                        // Récupère par exemple : "Impossible de supprimer : Aucun utilisateur/livre trouvé avec l'ID..."
                        setGlobalError(data.message);
                    } else {
                        setGlobalError(`La suppression a échoué du côté du serveur (Code: ${status}).`);
                    }
                } else if (error.request) {
                    setGlobalError("Impossible de joindre le serveur pour valider la suppression.");
                } else {
                    setGlobalError("Une erreur est survenue lors de l'exécution de la suppression.");
                }
            }
        }
    };

    if (loading) return <div className="loading" style={{ textAlign: 'center', padding: '40px', fontSize: '1.2rem', fontWeight: '500', color: '#555' }}>Chargement du catalogue en cours...</div>;

    return (
        <div className="list-container">
            <div className="list-header">
                <div>
                    <h2>Liste des livres</h2>
                    <p><strong>{livresFiltres.length}</strong> livres trouvés</p>
                </div>
                {canEdit && (
                    <button className="btn-add" onClick={() => navigate('/ajouter-livre')}>
                        + Ajouter un livre
                    </button>
                )}
            </div>

            {/* Bannière d'erreur globale stylisée */}
            {globalError && (
                <div className="error-banner" style={{ 
                    color: '#721c24', 
                    backgroundColor: '#f8d7da', 
                    border: '1px solid #f5c6cb', 
                    padding: '14px', 
                    borderRadius: '6px', 
                    marginBottom: '20px',
                    fontWeight: '500',
                    fontSize: '0.95rem'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'between', alignItems: 'center' }}>
                        <span><strong>Erreur système :</strong> {globalError}</span>
                        <button 
                            onClick={() => setGlobalError("")} 
                            style={{ background: 'none', border: 'none', color: '#721c24', float: 'right', fontWeight: 'bold', cursor: 'pointer', marginLeft: '15px' }}
                        >
                            ✕
                        </button>
                    </div>
                </div>
            )}

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
                        <option key={cat.id} value={cat.libelle}>{cat.libelle}</option>
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
                            {canEdit && <th>Actions</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {livresFiltres.map((livre) => (
                            <tr key={livre.id}>
                                <td style={{ fontWeight: '600' }}>{livre.titre}</td>
                                <td>{livre.isbn || "-"}</td>
                                <td><span className="badge-category">{livre.categorie?.libelle || "N/A"}</span></td>
                                <td>{livre.details?.nombrePages || "-"}</td>
                                <td>{livre.details?.emplacementRayon || "-"}</td>
                                <td>{livre.details?.auteur || "-"}</td>
                                {canEdit && (
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
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
                
                {livresFiltres.length === 0 && (
                    <div style={{ textAlign: 'center', padding: '30px', color: '#666', fontSize: '0.95rem', fontStyle: 'italic' }}>
                        Aucun livre ne correspond à vos critères de recherche.
                    </div>
                )}
            </div>
        </div>
    );
};

export default ListLivre;