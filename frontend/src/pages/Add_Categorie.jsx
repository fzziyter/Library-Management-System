import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import React from "react";
import "./Add_Categorie.css";

const Add_Categorie = () => {
    const [libelle, setLibelle] = useState("");
    const [error, setError] = useState(""); 
    const [isSubmitting, setIsSubmitting] = useState(false); // Évite les doubles soumissions
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(""); 
        setIsSubmitting(true);

        // 1. Validation de premier niveau côté Client
        if (!libelle.trim()) {
            setError("Le libellé ne peut pas être vide ou composé uniquement d'espaces.");
            setIsSubmitting(false);
            return;
        }

        try {
            // Envoi de la requête vers l'API Spring Boot
            await axios.post("http://localhost:8080/api/categories", { 
                libelle: libelle.trim() 
            });
            
            // Redirection vers la liste en cas de succès
            navigate("/listCategorie");
        } catch (err) {
            console.error("Error adding category:", err);
            
            // 2. Interception dynamique des erreurs structurées du GlobalExceptionHandler
            if (err.response) {
                const status = err.response.status;
                const data = err.response.data;

                if (status === 403) {
                    setError("Accès refusé : Vous devez être connecté avec un compte Administrateur ou Manager pour ajouter une catégorie.");
                } else if (status === 401) {
                    setError("Votre session a expiré. Veuillez vous reconnecter.");
                } else if (data && data.message) {
                    // Capture soit le message d'unicité (UK_), soit la chaîne d'erreurs DTO cumulée
                    setError(data.message);
                } else {
                    setError("Le serveur a refusé l'ajout de cette catégorie (Code d'erreur : " + status + ").");
                }
            } else if (err.request) {
                // Le serveur n'a pas répondu (ex: coupure réseau ou serveur Java arrêté)
                setError("Impossible de joindre le serveur. Assurez-vous que l'application Spring Boot est bien démarrée sur le port 8080.");
            } else {
                setError("Une erreur inattendue est survenue lors de l'envoi du formulaire.");
            }
        } finally {
            setIsSubmitting(false);
        }
    };   

    return (
        <div className="form-container">
            <h2 className="form-title">Nouvelle Catégorie</h2>
            
            {/* Bannière d'erreur adaptative */}
            {error && (
                <div className="error-banner" style={{
                    color: '#721c24',
                    backgroundColor: '#f8d7da',
                    border: '1px solid #f5c6cb',
                    padding: '12px',
                    borderRadius: '8px',
                    marginBottom: '15px',
                    fontWeight: '500',
                    fontSize: '0.9rem',
                    position: 'relative'
                }}>
                    <strong>Erreur de l'API :</strong> {error}
                </div>
            )}
            
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="libelle">Libellé de la catégorie :</label>
                    <input 
                        type="text" 
                        id="libelle" 
                        name="libelle" 
                        value={libelle} 
                        onChange={(e) => {
                            setLibelle(e.target.value);
                            if(error) setError(""); // Efface l'alerte dès que l'utilisateur modifie sa saisie
                        }} 
                        placeholder="Ex: Informatique, Gestion, Roman..."
                        required 
                        disabled={isSubmitting}
                    />
                </div>
                
                <button 
                    type="submit" 
                    className="btn-submit" 
                    disabled={isSubmitting}
                    style={{ opacity: isSubmitting ? 0.7 : 1, cursor: isSubmitting ? 'not-allowed' : 'pointer' }}
                >
                    {isSubmitting ? "Enregistrement..." : "Ajouter la catégorie"}
                </button>
            </form>
        </div>
    );
}

export default Add_Categorie;