import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import "./ListCategorie.css"; 
import { getUser } from '../services/auth';

const ListCategorie = () => {
    const navigate = useNavigate();
    const [categories, setCategories] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [searchTerm, setSearchTerm] = React.useState("");
    const [globalError, setGlobalError] = React.useState(""); // Gère toutes les erreurs remontées du backend

    // Fonction pour extraire le message d'erreur du backend
    const extractBackendMessage = (error, defaultMessage) => {
        if (error.response) {
            const data = error.response.data;
            const status = error.response.status;

            // 1. Erreurs de droits d'accès direct Spring Security (si interceptées avant ton handler)
            if (status === 403) {
                return "Accès refusé : Vous n'avez pas les privilèges nécessaires pour cette action.";
            }
            if (status === 401) {
                return "Votre session a expiré. Veuillez vous reconnecter.";
            }

            // 2. Alignement parfait avec ton buildResponse du backend (body.put("message", message))
            if (data && data.message) {
                return data.message;
            }

            // Fallback si la structure est différente
            if (data && typeof data === 'string') {
                return data;
            }
        } else if (error.request) {
            return "Impossible de joindre le serveur backend. Assurez-vous que l'application Spring Boot est démarrée.";
        }
        
        return defaultMessage;
    };
    const fetchCategories = async () => {
        try {
            setGlobalError("");
            const response = await axios.get("http://localhost:8080/api/categories");   
            if (Array.isArray(response.data)) {
                setCategories(response.data);
            } else {
                setCategories([]);
            }
        } catch (error) {
            console.error("Error fetching categories:", error);
            const msg = extractBackendMessage(error, "Impossible de charger les catégories depuis le serveur.");
            setGlobalError(msg);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);

    const categorieesFiltrees = categories.filter(cat => 
        cat.libelle?.toLowerCase().includes(searchTerm.toLowerCase()) || 
        cat.id?.toString().includes(searchTerm)
    );

    const handleDelete = async (id) => {
        if (window.confirm("Supprimer cette catégorie ?")) {
            try {
                setGlobalError("");
                await axios.delete(`http://localhost:8080/api/categories/${id}`);
                fetchCategories(); // Rafraîchir la liste après suppression réussie
            } catch (error) {
                console.error("Error deleting category:", error);
                
                // Extraction chirurgicale de l'erreur d'intégrité ou d'absence de ressource du Backend
                const msg = extractBackendMessage(
                    error, 
                    "Erreur lors de la suppression. Cette catégorie est probablement liée à un ou plusieurs livres (Contrainte d'intégrité de la base de données)."
                );
                setGlobalError(msg);
            }
        }
    };

    if (loading) return <div className="loading-text">Chargement des catégories...</div>;

    return (
        <div className="cat-container">
            <div className="cat-header">
                <h1>Catégories</h1>
                <button className="btn-add-cat" onClick={() => navigate('/ajouter-categorie')}>
                    + Ajouter
                </button>
            </div>

            {/* Bannière d'erreur globale dynamique */}
            {globalError && (
                <div className="error-banner" style={{ 
                    color: '#721c24', 
                    backgroundColor: '#f8d7da', 
                    border: '1px solid #f5c6cb', 
                    padding: '12px', 
                    borderRadius: '8px', 
                    marginBottom: '20px',
                    fontWeight: '500',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <div>
                        <strong>Erreur Backend : </strong> {globalError}
                    </div>
                    {/* Petit bouton pour fermer la bannière d'erreur */}
                    <button 
                        onClick={() => setGlobalError("")} 
                        style={{
                            background: 'none',
                            border: 'none',
                            color: '#721c24',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            fontSize: '1.2rem'
                        }}
                    >
                        &times;
                    </button>
                </div>
            )}

            <div className="filter-bar" style={{ marginBottom: '20px' }}>
                <input
                    type="text"
                    placeholder="Rechercher une catégorie (nom ou ID)..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{
                        width: '100%',
                        padding: '12px',
                        borderRadius: '8px',
                        border: '1px solid #ddd',
                        fontSize: '1rem'
                    }}
                />
            </div>
            
            <table className="cat-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Libellé</th>
                        <th>Actions</th> 
                    </tr>
                </thead>
                <tbody>
                    {Array.isArray(categorieesFiltrees) && categorieesFiltrees.map((category) => (
                        <tr key={category.id}>
                            <td className="cat-id">#{category.id}</td>
                            <td>{category.libelle}</td>
                            <td>
                                <div className="actions-cell">
                                    <button className="btn-delete-cat" onClick={() => handleDelete(category.id)}>
                                        Supprimer
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {categorieesFiltrees.length === 0 && (
                <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
                    Aucune catégorie ne correspond à votre recherche.
                </div>
            )}
        </div>
    );
}

export default ListCategorie;