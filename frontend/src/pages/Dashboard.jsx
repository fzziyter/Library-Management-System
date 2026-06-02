import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getUser } from '../services/auth';
import axios from 'axios';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell
} from 'recharts';
import './Dashboard.css';

// ── Carte de navigation (inchangée) ─────────────────────────────────
const Card = ({ to, emoji, title, desc }) => {
  const [hovered, setHovered] = React.useState(false);
  return (
    <Link to={to} style={{
      display: 'block',
      background: '#1e1c28',
      border: `1px solid ${hovered ? 'rgba(138,43,226,0.5)' : 'rgba(138,43,226,0.2)'}`,
      borderRadius: '12px',
      padding: '24px',
      textDecoration: 'none',
      transform: hovered ? 'translateY(-2px)' : 'none',
      transition: 'border-color 0.2s, transform 0.15s',
    }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <div style={{ fontSize: '1.8rem', marginBottom: '10px' }}>{emoji}</div>
      <div style={{ color: '#e8e0f0', fontWeight: 500, marginBottom: '6px', fontFamily: "'DM Sans', sans-serif" }}>{title}</div>
      <div style={{ color: '#7a6e8a', fontSize: '0.88rem', fontFamily: "'DM Sans', sans-serif" }}>{desc}</div>
    </Link>
  );
};

// ── Couleurs pour le bar chart ───────────────────────────────────────
const COLORS = ['#c084fc', '#818cf8', '#38bdf8', '#34d399', '#fb923c', '#f472b6'];

// ── Composant principal ──────────────────────────────────────────────
const Dashboard = () => {
  const user = getUser();
  const roles = user?.roles ?? [];
  const isAdmin   = roles.includes('ADMIN');
  const isManager = roles.includes('MANAGER') || isAdmin;

  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(""); // État pour capturer les anomalies et retours du GlobalExceptionHandler

  useEffect(() => {
    setError("");
    axios.get('http://localhost:8080/api/export/stats')
      .then(res => {
        setStats(res.data);
      })
      .catch((err) => {
        console.error("Erreur lors de la récupération des statistiques :", err);
        setStats(null);
        if (err.response?.status === 403) {
          setError("Accès refusé : Privilèges insuffisants pour charger les statistiques analytiques.");
        } else {
          setError("Impossible de charger les indicateurs système depuis le serveur.");
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const handleExport = async (type) => {
    setError("");
    const token = sessionStorage.getItem('token');

    if (!token) {
      setError("Token d'authentification introuvable. Veuillez vous reconnecter.");
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/export/${type}`, {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!response.ok) {
        // Intercepte les erreurs structurées du GlobalExceptionHandler renvoyées en format texte/json
        const text = await response.text();
        try {
          const jsonError = JSON.parse(text);
          setError(jsonError.message || `Erreur de traitement sur l'export (${response.status})`);
        } catch {
          setError(`Une anomalie est survenue lors de l'export ${type.toUpperCase()} (Code: ${response.status}).`);
        }
        return;
      }

      const blob = await response.blob();
      const url  = window.URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = type === 'pdf' ? 'catalogue_livres.pdf' : 'catalogue_livres.xlsx';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error(`Erreur réseau lors de l'export ${type} :`, err);
      setError("Le serveur est injoignable. Impossible de générer le fichier d'export.");
    }
  };

  return (
    <div className="dashboard-wrapper">
      {/* Greeting */}
      <div className="dashboard-greeting">
        <h1>Bonjour, {user?.username} 👋</h1>
        <p>Tableau de bord de la bibliothèque</p>
      </div>

      {/* Affichage globalisé des bannières d'erreur au premier plan */}
      {error && (
        <div className="error-banner" style={{
          color: '#f87171',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          border: '1px solid rgba(239, 68, 68, 0.2)',
          padding: '14px 20px',
          borderRadius: '10px',
          marginBottom: '25px',
          fontFamily: "'DM Sans', sans-serif",
          fontSize: '0.9rem',
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}>
          <span>⚠️</span> <span><strong>Alerte Système :</strong> {error}</span>
        </div>
      )}

      {/* KPI Cards */}
      {loading ? (
        <div style={{ color: '#7a6e8a', marginBottom: '20px', fontStyle: 'italic' }}>Chargement des données...</div>
      ) : (
        stats && (
          <div className="kpi-grid">
            <div className="kpi-card">
              <div className="kpi-emoji">📚</div>
              <div className="kpi-value">{stats.totalLivres}</div>
              <div className="kpi-label">Livres</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-emoji">🏷️</div>
              <div className="kpi-value">{stats.totalCategories}</div>
              <div className="kpi-label">Catégories</div>
            </div>
            {isAdmin && (
              <div className="kpi-card">
                <div className="kpi-emoji">👥</div>
                <div className="kpi-value">{stats.totalUsers}</div>
                <div className="kpi-label">Utilisateurs</div>
              </div>
            )}
          </div>
        )
      )}

      {/* Graphique livres par catégorie */}
      {!loading && stats?.livresParCategorie?.length > 0 && (
        <div className="chart-section">
          <h3>📊 Livres par catégorie</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={stats.livresParCategorie} margin={{ top: 0, right: 16, left: 0, bottom: 0 }}>
              <XAxis dataKey="categorie" tick={{ fill: '#7a6e8a', fontSize: 12 }} />
              <YAxis tick={{ fill: '#7a6e8a', fontSize: 12 }} allowDecimals={false} />
              <Tooltip
                contentStyle={{ background: '#1e1c28', border: '1px solid #3b0764', color: '#e8e0f0' }}
                cursor={{ fill: 'rgba(138,43,226,0.1)' }}
              />
              <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                {stats.livresParCategorie.map((_, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Boutons export — réservés MANAGER/ADMIN */}
      {isManager && (
        <div className="export-bar">
          <button className="btn-export pdf" onClick={() => handleExport('pdf')}>
            📄 Exporter PDF
          </button>
          <button className="btn-export excel" onClick={() => handleExport('excel')}>
            📊 Exporter Excel
          </button>
        </div>
      )}

      {/* Cartes de navigation (identiques à l'original) */}
      <div className="nav-cards-grid">
        <Card to="/listLivres"         emoji="📚" title="Catalogue des livres"    desc="Consulter, rechercher et filtrer les livres" />
        {isManager && <Card to="/ajouter-livre"     emoji="➕" title="Ajouter un livre"       desc="Enregistrer un nouveau titre" />}
        {isManager && <Card to="/listCategorie"     emoji="🏷️" title="Catégories"             desc="Gérer les catégories du catalogue" />}
        {isManager && <Card to="/ajouter-categorie" emoji="🆕" title="Ajouter une catégorie"  desc="Créer une nouvelle catégorie" />}
        {isAdmin   && <Card to="/admin/users"       emoji="👥" title="Utilisateurs"           desc="Gérer le personnel et les accès" />}
      </div>
    </div>
  );
};

export default Dashboard;