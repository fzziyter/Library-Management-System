import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import React from "react";
import "./Add_Categorie.css";
const Add_Categorie = () => {
    const [libelle, setLibelle] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async(event) => {
        event.preventDefault();
        try {
            await axios.post("http://localhost:8080/api/categories", {libelle});
            alert("Category added successfully!");
            navigate("/listCategorie");
        } catch (error) {
            console.error("Error adding category:", error);
            alert("Failed to add category. Please try again.");
        }
    };  
   return (
    <div className="form-container">
        <h2 className="form-title">Nouvelle Catégorie</h2>
        
        <form onSubmit={handleSubmit}>
            {/* On utilise form-group pour envelopper le label et l'input ensemble */}
            <div className="form-group">
                <label htmlFor="libelle">Libellé:</label>
                <input 
                    type="text" 
                    id="libelle" 
                    name="libelle" 
                    value={libelle} 
                    onChange={(e) => setLibelle(e.target.value)} 
                    placeholder="Ex: Informatique"
                    required 
                />
            </div>
            
            {/* Le bouton est à l'extérieur du groupe pour un meilleur espacement */}
            <button type="submit" className="btn-submit">
                Add Category
            </button>
        </form>
    </div>
);
}
export default Add_Categorie;