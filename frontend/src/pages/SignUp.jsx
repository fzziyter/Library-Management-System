import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import './Login.css'; // Réutilise le même style

const SignUp = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm]   = useState('');
  const [error, setError]       = useState('');
  const [success, setSuccess]   = useState('');
  const [loading, setLoading]   = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    e.stopPropagation(); // Évite la propagation et les rechargements HTML parasites
    setError('');
    setSuccess('');

    // Validation locale côté client
    if (password !== confirm) {
      setError('Les mots de passe ne correspondent pas.');
      return;
    }

    setLoading(true);

    try {
      // Utilisation d'une instance Axios isolée pour contourner d'éventuels intercepteurs globaux perturbateurs
      await axios.create().post('http://localhost:8080/api/auth/signup', { 
        username, 
        password 
      });

      setSuccess('Compte créé avec succès ! Redirection vers la connexion…');
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      console.error("Erreur lors de l'inscription :", err);

      if (err.response) {
        // Le serveur a répondu avec un statut d'erreur (400, 409, 500, etc.)
        const status = err.response.status;
        const data = err.response.data;

        if (status === 409) {
          setError(data?.message || "Ce nom d'utilisateur est déjà pris.");
        } else if (status === 400) {
          // Capte les erreurs de validation de Spring (ex: @Valid, mot de passe trop court, etc.)
          setError(data?.message || "Données d'inscription invalides. Veuillez vérifier les critères requis.");
        } else {
          setError(data?.message || `Une erreur système est survenue lors de la création du compte (Code: ${status}).`);
        }
      } else if (err.request) {
        // Le backend Spring Boot est éteint ou inaccessible
        setError("Le serveur est injoignable. Impossible de traiter votre inscription pour le moment.");
      } else {
        setError("Une erreur inattendue a empêché la soumission du formulaire.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-root">
      {/* Left decorative panel */}
      <div className="login-panel-left">
        <div className="login-brand">
          Bibliothèque<br />Centrale
        </div>
        <p className="login-tagline">
          Créez votre compte pour accéder au catalogue de la bibliothèque.
        </p>
        <div className="login-deco">
          <div className="deco-card">
            <strong>Accès USER</strong>
            Consultation du catalogue — livres et catégories disponibles
          </div>
        </div>
      </div>

      {/* Right form panel */}
      <div className="login-panel-right">
        <div className="login-form-box">
          <h2>Créer un compte</h2>
          <p>Inscription — rôle utilisateur</p>

          {/* Affichage des bannières dynamiques d'erreur et de succès */}
          {error   && <div className="login-error">{error}</div>}
          {success && <div className="login-error" style={{ background: 'rgba(34,197,94,0.1)', borderColor: 'rgba(34,197,94,0.3)', color: '#4ade80' }}>{success}</div>}

          <form onSubmit={handleSubmit}>
            <div className="login-field">
              <label>Identifiant</label>
              <input
                type="text"
                placeholder="nom_utilisateur"
                value={username}
                onChange={(e) => {
                  setError(''); // Réinitialise l'erreur pendant la saisie
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
                  setError('');
                  setPassword(e.target.value);
                }}
                required
                disabled={loading}
              />
            </div>
            <div className="login-field">
              <label>Confirmer le mot de passe</label>
              <input
                type="password"
                placeholder="••••••••"
                value={confirm}
                onChange={(e) => {
                  setError('');
                  setConfirm(e.target.value);
                }}
                required
                disabled={loading}
              />
            </div>
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Création en cours…' : "S'inscrire"}
            </button>
          </form>

          <p style={{ marginTop: '24px', fontSize: '0.88rem', color: '#7a6e8a', textAlign: 'center' }}>
            Déjà un compte ?{' '}
            <Link to="/login" style={{ color: '#a78bfa', textDecoration: 'none', fontWeight: '500' }}>
              Se connecter
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignUp;