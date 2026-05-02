import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Add_livre.css'; // Assurez-vous que le nom du fichier CSS est correct

const AjouterLivre = () => {
    const navigate = useNavigate();
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
        const fetchCategories = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/categories");
                setCategories(response.data);
            } catch (error) {
                console.error("Erreur lors de la récupération des catégories:", error);
            }
        };
        fetchCategories();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (["nombrePages", "emplacementRayon", "auteur"].includes(name)) {
            setLivre({
                ...livre,
                details: {
                    ...livre.details,
                    [name]: value
                }
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
        if (!livre.categorie.id) {
            alert("Veuillez sélectionner une catégorie");
            return;
        }
        const livreData = {
            ...livre,
            categorie: {
                id: parseInt(livre.categorie.id, 10)
            },
            details: {
                ...livre.details,
                nombrePages: parseInt(livre.details.nombrePages, 10) || 0
            }
        };
        try {
            await axios.post("http://localhost:8080/api/livres", livreData);
            alert("Livre ajouté avec succès");
            navigate("/ListLivres");
        } catch (error) {
            console.error("Erreur lors de l'ajout du livre:", error);
            alert("Erreur lors de l'ajout du livre");
        }
    };

    return (
        <div className="edit-container">
            <h2 className="edit-title">Ajouter un nouveau livre</h2>
            
            <form onSubmit={handleSubmit} className="edit-form">
                <div className="form-section">
                    
                    {/* Section 1: Informations Générales */}
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
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="categorie">Catégorie</label>
                        <select id="categorie" name="categorie" value={livre.categorie.id} onChange={handleChange} required>
                            <option value="">Sélectionnez une catégorie</option>
                            {Array.isArray(categories) && categories.map((categorie) => (
                                <option key={categorie.id} value={categorie.id}>
                                    {categorie.libelle}
                                </option>
                            ))}
                        </select>
                    </div>

                    <hr className="full-width" />

                    {/* Section 2: Détails du Livre */}
                    <h3 className="section-subtitle">Détails techniques</h3>

                    <div className="input-group">
                        <label htmlFor="nombrePages">Nombre de pages</label>
                        <input 
                            id="nombrePages" 
                            type="number" 
                            name='nombrePages' 
                            value={livre.details.nombrePages} 
                            onChange={handleChange} 
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
                        />
                    </div>
                </div>

                <div className="button-group">
                    <button type="button" className="btn-cancel" onClick={() => navigate("/ListLivres")}>
                        Annuler
                    </button>
                    <button type="submit" className="btn-save">
                        Enregistrer le livre
                    </button>
                </div>
            </form>
        </div>
    );
}

export default AjouterLivre;