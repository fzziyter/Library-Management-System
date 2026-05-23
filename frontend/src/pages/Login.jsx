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
    setError('');
    setLoading(true);
    try {
      const res = await axios.post('http://localhost:8080/api/auth/signin', {
        username,
        password
      });
      // res.data = { accessToken, id, username, roles: ["ADMIN"] }
      saveAuth(res.data);
      navigate('/');
    } catch (err) {
      setError("Identifiants invalides. Vérifiez votre nom d'utilisateur et mot de passe.");
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

      {/* Right form panel */}
      <div className="login-panel-right">
        <div className="login-form-box">
          <h2>Connexion</h2>
          <p>Accès au système de gestion</p>

          {error && <div className="login-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="login-field">
              <label>Identifiant</label>
              <input
                type="text"
                placeholder="nom_utilisateur"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                autoFocus
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
