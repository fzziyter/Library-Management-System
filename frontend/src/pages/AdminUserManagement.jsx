import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './AdminUserManagement.css';

const AdminUserManagement = () => {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(""); // État pour capturer les messages du GlobalExceptionHandler
    const [isSubmitting, setIsSubmitting] = useState(false); // Évite les requêtes concurrentes
    
    // État pour gérer le popup (Modal)
    const [modalConfig, setModalConfig] = useState({
        isOpen: false,
        type: 'add', // 'add' ou 'update'
        userId: null,
        username: '',
        password: '',
        role: 'USER'
    });

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setError("");
            const response = await axios.get("http://localhost:8080/api/admin/users");
            setUsers(response.data);
        } catch (error) {
            console.error("Erreur lors de la récupération :", error);
            if (error.response?.status === 403) {
                setError("Accès refusé : Vous devez avoir le rôle ADMIN pour gérer les utilisateurs.");
            } else {
                setError("Impossible de charger la liste des membres depuis le serveur.");
            }
        }
    };

    // Ouvre le modal en mode "Ajout"
    const openAddModal = () => {
        setError("");
        setModalConfig({
            isOpen: true,
            type: 'add',
            userId: null,
            username: '',
            password: '',
            role: 'USER'
        });
    };

    // Ouvre le modal en mode "Modification"
    const openUpdateModal = (user) => {
        setError("");
        const currentRole = user.roles && user.roles.length > 0 ? user.roles[0].name : 'USER';
        
        setModalConfig({
            isOpen: true,
            type: 'update',
            userId: user.id,
            username: user.username,
            password: '', 
            role: currentRole
        });
    };

    const closeModal = () => {
        setModalConfig({ ...modalConfig, isOpen: false });
        setError("");
    };

    // Soumission du formulaire du Modal
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setError("");
        const { type, username, password, role, userId } = modalConfig;

        if (!username.trim()) return setError("Le nom d'utilisateur est requis.");

        try {
            setIsSubmitting(true);
            if (type === 'add') {
                if (!password) {
                    setError("Le mot de passe est requis pour une nouvelle inscription.");
                    setIsSubmitting(false);
                    return;
                }
                await axios.post(`http://localhost:8080/api/admin/users?role=${role}`, {
                    username: username.trim(),
                    password: password
                });
            } else if (type === 'update') {
                await axios.put(`http://localhost:8080/api/admin/users/${userId}`, {
                    username: username.trim(),
                    roles: [role]
                });
            }
            
            fetchUsers();
            closeModal(); // Ferme uniquement si l'enregistrement réussit
        } catch (error) {
            console.error(`Erreur lors de l'opération (${type}) :`, error);
            
            if (error.response) {
                const status = error.response.status;
                const data = error.response.data;

                if (status === 403) {
                    setError("Action refusée : Privilèges insuffisants.");
                } else if (data && data.message) {
                    // Intercepte le message d'unicité renvoyé au premier plan
                    setError(data.message);
                } else {
                    setError(`Le serveur a retourné une erreur (Code: ${status}).`);
                }
            } else {
                setError("Le serveur est injoignable. L'opération a échoué.");
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Supprimer cet utilisateur ?")) {
            try {
                setError("");
                await axios.delete(`http://localhost:8080/api/admin/users/${id}`);
                fetchUsers();
            } catch (error) {
                console.error("Erreur lors de la suppression :", error);
                if (error.response?.data?.message) {
                    setError(error.response.data.message);
                } else {
                    setError("Erreur lors de la révocation du membre.");
                }
            }
        }
    };

    return (
        <div className="library-admin-container">
            <div className="library-header">
                <div>
                    <h2>Gestion du Personnel // Registre Central</h2>
                    <p><strong>{users.length}</strong> comptes enregistrés</p>
                </div>
                <button onClick={openAddModal} className="btn-add-user" disabled={isSubmitting}>
                    + Enregistrer un membre
                </button>
            </div>
            
            {/* L'erreur générale ne s'affiche ici QUE si le modal est fermé (ex: erreur au fetch initial ou à la suppression) */}
            {error && !modalConfig.isOpen && (
                <div className="error-banner" style={{ 
                    color: '#721c24', 
                    backgroundColor: '#f8d7da', 
                    border: '1px solid #f5c6cb', 
                    padding: '12px', 
                    borderRadius: '6px', 
                    marginBottom: '20px',
                    fontWeight: '500',
                    fontSize: '0.9rem',
                    fontFamily: 'Georgia, serif'
                }}>
                    <strong>Alerte Système :</strong> {error}
                </div>
            )}
            
            <table className="library-table">
                <thead>
                    <tr>
                        <th>Cote [id]</th>
                        <th>Identifiant</th>
                        <th>Rang d'accès</th>
                        <th>Actions administratives</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>#{user.id}</td>
                            <td><strong>{user.username}</strong></td>
                            <td>
                                {user.roles.map((r, index) => (
                                    <span key={index} className="role-badge">
                                        {r.name}
                                    </span>
                                ))}
                            </td>
                            <td>
                                <button onClick={() => openUpdateModal(user)} className="btn-action-modify" disabled={isSubmitting}>
                                    Rectifier
                                </button>
                                <button onClick={() => handleDelete(user.id)} className="btn-action-delete" disabled={isSubmitting}>
                                    Révoquer
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* --- LE MODAL CHIC (PREMIER PLAN) --- */}
            {modalConfig.isOpen && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-box" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-title">
                            {modalConfig.type === 'add' ? 'Nouvelle Fiche Membre' : 'Rectification de la Fiche'}
                        </h3>
                        
                        {/* AFFICHAGE DE L'ERREUR AU PREMIER PLAN : Juste sous le titre du Modal */}
                        {error && (
                            <div className="error-banner" style={{ 
                                color: '#a44a4a', 
                                backgroundColor: '#fdf6f6', 
                                border: '1px solid #f5c6cb', 
                                padding: '10px', 
                                borderRadius: '4px', 
                                marginBottom: '15px',
                                fontSize: '0.85rem',
                                fontStyle: 'italic',
                                fontFamily: 'Georgia, serif'
                            }}>
                                <strong>Erreur :</strong> {error}
                            </div>
                        )}
                        
                        <form onSubmit={handleFormSubmit} className="modal-form">
                            {/* Identifiant */}
                            <div className="form-group">
                                <label>Identifiant (Username)</label>
                                <input 
                                    type="text" 
                                    value={modalConfig.username}
                                    onChange={(e) => {
                                        setError(""); // Efface l'erreur dès que l'utilisateur corrige sa saisie
                                        setModalConfig({...modalConfig, username: e.target.value});
                                    }}
                                    placeholder="Ex: a_camus"
                                    required
                                    disabled={isSubmitting}
                                />
                            </div>

                            {/* Mot de passe (Ajout uniquement) */}
                            {modalConfig.type === 'add' && (
                                <div className="form-group">
                                    <label>Mot de passe</label>
                                    <input 
                                        type="password" 
                                        value={modalConfig.password}
                                        onChange={(e) => {
                                            setError("");
                                            setModalConfig({...modalConfig, password: e.target.value});
                                        }}
                                        placeholder="••••••••"
                                        required
                                        disabled={isSubmitting}
                                    />
                                </div>
                            )}

                            {/* Sélection du rôle */}
                            <div className="form-group">
                                <label>Rang d'accès (Rôle)</label>
                                <select 
                                    value={modalConfig.role}
                                    onChange={(e) => setModalConfig({...modalConfig, role: e.target.value})}
                                    disabled={isSubmitting}
                                >
                                    <option value="USER">USER</option>
                                    <option value="MANAGER">MANAGER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                            </div>

                            <div className="modal-actions">
                                <button type="button" onClick={closeModal} className="btn-cancel" disabled={isSubmitting}>
                                    Annuler
                                </button>
                                <button type="submit" className="btn-submit" disabled={isSubmitting}>
                                    {isSubmitting ? "Traitement..." : (modalConfig.type === 'add' ? 'Inscrire' : 'Appliquer')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminUserManagement;