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
    setError('');
    if (password !== confirm) {
      setError('Les mots de passe ne correspondent pas.');
      return;
    }
    setLoading(true);
    try {
      await axios.post('http://localhost:8080/api/auth/signup', { username, password });
      setSuccess('Compte créé ! Redirection vers la connexion…');
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      if (err.response?.status === 409) {
        setError("Ce nom d'utilisateur est déjà pris.");
      } else {
        setError("Une erreur est survenue. Veuillez réessayer.");
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

          {error   && <div className="login-error">{error}</div>}
          {success && <div className="login-error" style={{background:'rgba(34,197,94,0.1)', borderColor:'rgba(34,197,94,0.3)', color:'#4ade80'}}>{success}</div>}

          <form onSubmit={handleSubmit}>
            <div className="login-field">
              <label>Identifiant</label>
              <input
                type="text"
                placeholder="nom_utilisateur"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required autoFocus
              />
            </div>
            <div className="login-field">
              <label>Mot de passe</label>
              <input
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="login-field">
              <label>Confirmer le mot de passe</label>
              <input
                type="password"
                placeholder="••••••••"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Création en cours…' : "S'inscrire"}
            </button>
          </form>

          <p style={{marginTop:'24px', fontSize:'0.88rem', color:'#7a6e8a', textAlign:'center'}}>
            Déjà un compte ?{' '}
            <Link to="/login" style={{color:'#a78bfa', textDecoration:'none', fontWeight:'500'}}>
              Se connecter
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignUp;