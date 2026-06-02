import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Add_livre.css'; 

const AjouterLivre = () => {
    const navigate = useNavigate();
    const [categories, setCategories] = useState([]);
    const [error, setError] = useState(""); 
    const [isSubmitting, setIsSubmitting] = useState(false); // Bloque le bouton pendant l'envoi
    const [livre, setLivre] = useState({
        titre: "",
        isbn: "",
        categorie: { id: "" },
        details: {
            nombrePages: "",
            emplacementRayon: "",
            auteur: ""
        }
    });

    // Récupération des catégories au chargement du composant
    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/categories");
                setCategories(response.data);
            } catch (err) {
                console.error("Erreur lors de la récupération des catégories:", err);
                setError("Impossible de charger les catégories nécessaires. Vérifiez que le backend est démarré.");
            }
        };
        fetchCategories();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (error) setError(""); // Efface le message d'erreur dès que l'utilisateur corrige sa saisie
        
        if (["nombrePages", "emplacementRayon", "auteur"].includes(name)) {
            setLivre({
                ...livre,
                details: { ...livre.details, [name]: value }
            });
        } else if (name === "categorie") {
            setLivre({
                ...livre,
                categorie: { id: value }
            });
        } else {
            setLivre({
                ...livre,
                [name]: value
            });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        
        // Validation rapide côté client
        if (!livre.categorie.id) {
            setError("Veuillez sélectionner une catégorie valide pour ce livre.");
            return;
        }

        setIsSubmitting(true); // Désactive le formulaire

        // Structuration propre des données avant envoi (Cast en entier)
        const livreData = {
            ...livre,
            categorie: { id: parseInt(livre.categorie.id, 10) },
            details: {
                ...livre.details,
                nombrePages: parseInt(livre.details.nombrePages, 10) || 0
            }
        };

        try {
            await axios.post("http://localhost:8080/api/livres", livreData);
            navigate("/listLivres");
        } catch (err) {
            console.error("Erreur complète reçue du serveur:", err);
            
            // Extraction chirurgicale alignée sur ton GlobalExceptionHandler
            if (err.response) {
                const status = err.response.status;
                const data = err.response.data;

                if (status === 403) {
                    setError("Accès refusé : Vos privilèges actuels ne vous permettent pas d'ajouter des livres.");
                } else if (status === 401) {
                    setError("Votre session a expiré. Veuillez vous reconnecter.");
                } else if (data && data.message) {
                    // Si l'ISBN existe déjà, le handler renverra le message d'unicité personnalisé
                    setError(data.message);
                } else {
                    setError(`Le serveur a retourné une erreur (Code: ${status}).`);
                }
            } else if (err.request) {
                // Pas de réponse du serveur
                setError("Le serveur Spring Boot est injoignable. Vérifiez votre connexion réseau.");
            } else {
                setError("Une erreur interne est survenue lors de la configuration de la requête.");
            }
        } finally {
            setIsSubmitting(false); // Réactive le formulaire si échec
        }
    };

    return (
        <div className="edit-container">
            <h2 className="edit-title">Ajouter un nouveau livre</h2>
            
            {/* Bannière d'affichage de l'erreur stylisée */}
            {error && (
                <div className="error-banner" style={{ 
                    color: '#721c24', 
                    backgroundColor: '#f8d7da', 
                    border: '1px solid #f5c6cb',
                    padding: '12px', 
                    borderRadius: '6px', 
                    marginBottom: '15px', 
                    fontWeight: '500',
                    fontSize: '0.9rem'
                }}>
                    <strong>Erreur : </strong> {error}
                </div>
            )}
            
            <form onSubmit={handleSubmit} className="edit-form">
                <div className="form-section">
                    <h3 className="section-subtitle">Informations Générales</h3>
                    
                    <div className="input-group full-width">
                        <label htmlFor="titre">Titre du livre</label>
                        <input 
                            type="text" 
                            id="titre" 
                            name="titre" 
                            placeholder="Ex: Le Petit Prince" 
                            value={livre.titre} 
                            onChange={handleChange} 
                            required 
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="isbn">Code ISBN</label>
                        <input 
                            type="text" 
                            id="isbn" 
                            name="isbn" 
                            placeholder="Ex: 978-2070612758" 
                            value={livre.isbn} 
                            onChange={handleChange} 
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="categorie">Catégorie</label>
                        <select 
                            id="categorie" 
                            name="categorie" 
                            value={livre.categorie.id} 
                            onChange={handleChange} 
                            required
                            disabled={isSubmitting}
                        >
                            <option value="">Sélectionnez une catégorie</option>
                            {Array.isArray(categories) && categories.map((categorie) => (
                                <option key={categorie.id} value={categorie.id}>{categorie.libelle}</option>
                            ))}
                        </select>
                    </div>

                    <hr className="full-width" />

                    <h3 className="section-subtitle">Détails techniques</h3>

                    <div className="input-group">
                        <label htmlFor="nombrePages">Nombre de pages</label>
                        <input 
                            id="nombrePages" 
                            type="number" 
                            name='nombrePages' 
                            value={livre.details.nombrePages} 
                            onChange={handleChange} 
                            disabled={isSubmitting}
                            min="0"
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="emplacementRayon">Emplacement Rayon</label>
                        <input 
                            type="text" 
                            id="emplacementRayon" 
                            name="emplacementRayon" 
                            placeholder="Ex: Rayon A1" 
                            value={livre.details.emplacementRayon} 
                            onChange={handleChange} 
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="input-group full-width">
                        <label htmlFor="auteur">Nom de l'auteur</label>
                        <input 
                            type="text" 
                            id="auteur" 
                            name="auteur" 
                            placeholder="Ex: Antoine de Saint-Exupéry" 
                            value={livre.details.auteur} 
                            onChange={handleChange} 
                            disabled={isSubmitting}
                        />
                    </div>
                </div>

                <div className="button-group">
                    <button 
                        type="button" 
                        className="btn-cancel" 
                        onClick={() => navigate("/listLivres")}
                        disabled={isSubmitting}
                    >
                        Annuler
                    </button>
                    <button 
                        type="submit" 
                        className="btn-save"
                        disabled={isSubmitting}
                        style={{ opacity: isSubmitting ? 0.7 : 1, cursor: isSubmitting ? 'not-allowed' : 'pointer' }}
                    >
                        {isSubmitting ? "Enregistrement..." : "Enregistrer le livre"}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default AjouterLivre;