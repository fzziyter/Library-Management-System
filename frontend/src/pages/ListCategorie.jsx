import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import "./ListCategorie.css"; // Importation du fichier CSS

const ListCategorie = () => {
    const navigate = useNavigate();
    const [categories, setCategories] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const[searchTerm, setSearchTerm] = React.useState("");
    const fetchCategories = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/categories");   
            if (Array.isArray(response.data)) {
                setCategories(response.data);
            } else {
                setCategories([]);
            }
        } catch (error) {
            console.error("Error fetching categories:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);
    const categorieesFiltrees = categories.filter(cat => cat.libelle.toLowerCase().includes(searchTerm.toLowerCase()) || cat.id.toString().includes(searchTerm));
    const handleDelete = async (id) => {
        if (window.confirm("Supprimer cette catégorie ?")) {
            try {
                await axios.delete(`http://localhost:8080/api/categories/${id}`);
                fetchCategories();
            } catch (error) {
                console.error("Error deleting category:", error);
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
                        <th>Actions</th> {/* Maintenant centré via le CSS */}
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
        </div>
    );
}

export default ListCategorie;