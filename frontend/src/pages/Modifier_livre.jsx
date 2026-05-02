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
            try {
                const [resLivre, resCats] = await Promise.all([
                    axios.get(API_URL),
                    axios.get(API_CATEGORIES_URL)
                ]);
                
                // On fusionne les données reçues avec notre structure par défaut
                // au cas où 'details' ou 'categorie' seraient null en base
                setLivre({
                    ...resLivre.data,
                    categorie: resLivre.data.categorie || { id: "" },
                    details: resLivre.data.details || { nombrePages: "", emplacementRayon: "", auteur: "" }
                });
                setCategories(resCats.data);
            } catch (error) {
                console.error("Erreur de chargement:", error);
                alert("Erreur lors de la récupération des données");
            }
        };
        fetchInitialData();
    }, [id]);

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (["nombrePages", "emplacementRayon", "auteur"].includes(name)) {
            setLivre(prev => ({
                ...prev,
                details: { ...prev.details, [name]: value }
            }));
        } else if (name === "categorie") {
            setLivre(prev => ({
                ...prev,
                categorie: { id: value } // On envoie juste l'ID au backend
            }));
        } else {
            setLivre(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.put(API_URL, livre);
            alert("Livre modifié avec succès !");
            navigate("/listLivres");
        } catch (error) {
            console.error("Erreur modification:", error);
            alert("Échec de la modification");
        }
    };

    return (
        <div className="edit-container">
            <h2 className="edit-title">Modifier le Livre</h2>
            
            <form onSubmit={handleSubmit} className="edit-form">
                
                <div className="form-section">
                    <div className="input-group full-width">
                        <label>Titre du livre</label>
                        <input type="text" name="titre" value={livre.titre} onChange={handleChange} required />
                    </div>

                    <div className="input-group">
                        <label>ISBN</label>
                        <input type="text" name="isbn" value={livre.isbn} onChange={handleChange} required />
                    </div>

                    <div className="input-group">
                        <label>Catégorie</label>
                        <select name="categorie" value={livre.categorie?.id || ""} onChange={handleChange} required>
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
                        <input type="number" name="nombrePages" value={livre.details?.nombrePages || ""} onChange={handleChange} />
                    </div>

                    <div className="input-group">
                        <label>Rayon (Emplacement)</label>
                        <input type="text" name="emplacementRayon" value={livre.details?.emplacementRayon || ""} onChange={handleChange} />
                    </div>

                    <div className="input-group full-width">
                        <label>Auteur</label>
                        <input type="text" name="auteur" value={livre.details?.auteur || ""} onChange={handleChange} />
                    </div>
                </div>

                <div className="button-group">
                    <button type="submit" className="btn-save">Enregistrer les modifications</button>
                    <button type="button" className="btn-cancel" onClick={() => navigate("/listLivres")}>
                        Annuler
                    </button>
                </div>
            </form>
        </div>
    );

};

export default Modifier_livre;