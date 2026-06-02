import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { saveAuth } from '../services/auth';
import './Login.css';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    e.stopPropagation(); // Bloque toute remontée d'événement HTML indésirable
    setError('');
    setLoading(true);
    
    try {
      // On utilise une instance vierge d'Axios pour contourner les intercepteurs globaux 
      // qui forcent parfois un window.location.reload() sur les erreurs 401/403.
      const res = await axios.create().post('http://localhost:8080/api/auth/signin', {
        username,
        password
      });

      // res.data = { accessToken, id, username, roles: ["ADMIN"] }
      saveAuth(res.data);
      navigate('/');
    } catch (err) {
      console.error("Erreur d'authentification :", err);
      
      if (err.response) {
        // Le serveur a répondu avec un code d'erreur (400, 401, 403, etc.)
        const status = err.response.status;
        const data = err.response.data;

        if (status === 401 || status === 400) {
          // Affiche le message précis du GlobalExceptionHandler s'il existe
          if (data && data.message) {
            setError(data.message);
          } else {
            setError("Identifiants invalides. Veuillez vérifier votre nom d'utilisateur et mot de passe.");
          }
        } else if (status === 403) {
          setError(data?.message || "Accès refusé : Votre compte est désactivé ou restreint.");
        } else {
          setError(data?.message || `Une erreur système est survenue (Code: ${status}).`);
        }
      } else if (err.request) {
        // Serveur Spring Boot éteint ou inaccessible
        setError("Le serveur est injoignable. Veuillez vérifier votre connexion ou réessayer plus tard.");
      } else {
        setError("Une erreur inattendue est survenue lors de la tentative de connexion.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-root">
      {/* Panneau décoratif gauche */}
      <div className="login-panel-left">
        <div className="login-brand">
          Bibliothèque<br />Centrale
        </div>
        <p className="login-tagline">
          Système de gestion intégré pour le catalogue, les collections et le personnel.
        </p>
        <div className="login-deco">
          <div className="deco-card">
            <strong>ADMIN</strong>
            Gestion complète — utilisateurs, livres, catégories
          </div>
          <div className="deco-card">
            <strong>MANAGER</strong>
            Gestion du catalogue — livres et catégories
          </div>
          <div className="deco-card">
            <strong>USER</strong>
            Consultation du catalogue
          </div>
        </div>
      </div>

      {/* Panneau formulaire droit */}
      <div className="login-panel-right">
        <div className="login-form-box">
          <h2>Connexion</h2>
          <p>Accès au système de gestion</p>

          {/* Affichage de la bannière d'erreur dynamique sans rechargement */}
          {error && <div className="login-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="login-field">
              <label>Identifiant</label>
              <input
                type="text"
                placeholder="nom_utilisateur"
                value={username}
                onChange={(e) => {
                  setError(''); // Efface le message dès que l'utilisateur re-saisit
                  setUsername(e.target.value);
                }}
                required
                autoFocus
                disabled={loading}
              />
            </div>
            <div className="login-field">
              <label>Mot de passe</label>
              <input
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => {
                  setError(''); // Efface le message dès que l'utilisateur re-saisit
                  setPassword(e.target.value);
                }}
                required
                disabled={loading}
              />
            </div>
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Connexion en cours…' : 'Se connecter'}
            </button>
          </form>
          <p style={{ marginTop: '24px', fontSize: '0.88rem', color: '#7a6e8a', textAlign: 'center' }}>
            Pas encore de compte ?{' '}
            <Link to="/signup" style={{ color: '#a78bfa', textDecoration: 'none', fontWeight: '500' }}>
              Créer un compte
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;