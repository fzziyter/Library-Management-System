import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';
import './Modifier_livre.css';

const Modifier_livre = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const API_URL = `http://localhost:8080/api/livres/${id}`;
    const API_CATEGORIES_URL = "http://localhost:8080/api/categories";

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

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

    useEffect(() => {
        const fetchInitialData = async () => {
            setError("");
            setLoading(true);
            const token = sessionStorage.getItem('token');

            try {
                // Configuration de la requête avec le token d'authentification
                const config = token ? { headers: { 'Authorization': `Bearer ${token}` } } : {};

                const [resLivre, resCats] = await Promise.all([
                    axios.get(API_URL, config),
                    axios.get(API_CATEGORIES_URL, config)
                ]);
                
                setLivre({
                    ...resLivre.data,
                    categorie: resLivre.data.categorie || { id: "" },
                    details: resLivre.data.details || { nombrePages: "", emplacementRayon: "", auteur: "" }
                });
                setCategories(resCats.data);
            } catch (err) {
                console.error("Erreur de chargement initial:", err);
                if (err.response) {
                    const status = err.response.status;
                    if (status === 404) {
                        setError("Le livre demandé est introuvable au sein du catalogue.");
                    } else if (status === 403) {
                        setError("Accès refusé : Vos droits actuels ne permettent pas de modifier ce livre.");
                    } else {
                        setError(err.response.data?.message || "Impossible de récupérer les informations nécessaires.");
                    }
                } else if (err.request) {
                    setError("Le serveur est injoignable. Veuillez vérifier la connexion au backend.");
                } else {
                    setError("Une anomalie est survenue lors de la préparation des données.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
    }, [id, API_URL]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        
        // Efface les notifications d'erreur dès que l'utilisateur ajuste sa saisie
        if (error) setError("");

        if (["nombrePages", "emplacementRayon", "auteur"].includes(name)) {
            setLivre(prev => ({
                ...prev,
                details: { ...prev.details, [name]: value }
            }));
        } else if (name === "categorie") {
            setLivre(prev => ({
                ...prev,
                categorie: { id: value }
            }));
        } else {
            setLivre(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");
        setSubmitting(true);

        const token = sessionStorage.getItem('token');
        if (!token) {
            setError("Session expirée ou invalide. Veuillez vous reconnecter.");
            setSubmitting(false);
            return;
        }

        try {
            await axios.put(API_URL, livre, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            setSuccess("Livre mis à jour avec succès ! Redirection en cours...");
            // Petite temporisation pour laisser le temps de lire le message de succès avant la redirection
            setTimeout(() => {
                navigate("/listLivres");
            }, 1500);
        } catch (err) {
            console.error("Erreur lors de la modification:", err);
            if (err.response) {
                // Intercepte les validations ou business exceptions du GlobalExceptionHandler
                setError(err.response.data?.message || `Échec du traitement de la modification (Code: ${err.response.status}).`);
            } else if (err.request) {
                setError("Le serveur n'a pas répondu. Échec de la mise à jour.");
            } else {
                setError("Une erreur inattendue a bloqué la soumission.");
            }
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="edit-container" style={{ textAlign: 'center', color: '#7a6e8a', padding: '40px' }}>
                <p>Chargement des spécifications du livre et des catégories...</p>
            </div>
        );
    }

    return (
        <div className="edit-container">
            <h2 className="edit-title">Modifier le Livre</h2>
            
            {/* Bannière d'erreur dynamique */}
            {error && (
                <div className="error-banner" style={{
                    color: '#f87171',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    border: '1px solid rgba(239, 68, 68, 0.2)',
                    padding: '12px 16px',
                    borderRadius: '8px',
                    marginBottom: '20px',
                    fontSize: '0.9rem'
                }}>
                    ⚠️ <strong>Erreur :</strong> {error}
                </div>
            )}

            {/* Bannière de succès dynamique */}
            {success && (
                <div className="success-banner" style={{
                    color: '#34d399',
                    backgroundColor: 'rgba(52, 211, 153, 0.1)',
                    border: '1px solid rgba(52, 211, 153, 0.2)',
                    padding: '12px 16px',
                    borderRadius: '8px',
                    marginBottom: '20px',
                    fontSize: '0.9rem'
                }}>
                    ✅ {success}
                </div>
            )}

            {/* Le formulaire n'est soumis que s'il n'y a pas d'erreur critique de chargement initial */}
            {(!livre.titre && error) ? (
                <div className="button-group">
                    <button type="button" className="btn-cancel" onClick={() => navigate("/listLivres")}>
                        Retourner au catalogue
                    </button>
                </div>
            ) : (
                <form onSubmit={handleSubmit} className="edit-form">
                    <div className="form-section">
                        <div className="input-group full-width">
                            <label>Titre du livre</label>
                            <input 
                                type="text" 
                                name="titre" 
                                value={livre.titre} 
                                onChange={handleChange} 
                                required 
                                disabled={submitting} 
                            />
                        </div>

                        <div className="input-group">
                            <label>ISBN</label>
                            <input 
                                type="text" 
                                name="isbn" 
                                value={livre.isbn} 
                                onChange={handleChange} 
                                required 
                                disabled={submitting} 
                            />
                        </div>

                        <div className="input-group">
                            <label>Catégorie</label>
                            <select 
                                name="categorie" 
                                value={livre.categorie?.id || ""} 
                                onChange={handleChange} 
                                required
                                disabled={submitting}
                            >
                                <option value="">Choisir...</option>
                                {categories.map((cat) => (
                                    <option key={cat.id} value={cat.id}>{cat.libelle}</option>
                                ))}
                            </select>
                        </div>

                        <hr className="full-width" />
                        <h3 className="section-subtitle">Détails techniques</h3>

                        <div className="input-group">
                            <label>Nombre de pages</label>
                            <input 
                                type="number" 
                                name="nombrePages" 
                                value={livre.details?.nombrePages || ""} 
                                onChange={handleChange} 
                                disabled={submitting}
                            />
                        </div>

                        <div className="input-group">
                            <label>Rayon (Emplacement)</label>
                            <input 
                                type="text" 
                                name="emplacementRayon" 
                                value={livre.details?.emplacementRayon || ""} 
                                onChange={handleChange} 
                                disabled={submitting}
                            />
                        </div>

                        <div className="input-group full-width">
                            <label>Auteur</label>
                            <input 
                                type="text" 
                                name="auteur" 
                                value={livre.details?.auteur || ""} 
                                onChange={handleChange} 
                                disabled={submitting}
                            />
                        </div>
                    </div>

                    <div className="button-group">
                        <button type="submit" className="btn-save" disabled={submitting}>
                            {submitting ? 'Enregistrement…' : 'Enregistrer les modifications'}
                        </button>
                        <button type="button" className="btn-cancel" onClick={() => navigate("/listLivres")} disabled={submitting}>
                            Annuler
                        </button>
                    </div>
                </form>
            )}
        </div>
    );
};

export default Modifier_livre;