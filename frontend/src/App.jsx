import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { isLoggedIn, getUser } from './services/auth';
import './services/auth'; // activates axios interceptors
import SignUp from './pages/SignUp.jsx';

import Login from './pages/Login.jsx';
import Navbar from './components/Navbar.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ListLivre from './pages/listLivre.jsx';
import AjouterLivre from './pages/Add_livre.jsx';
import Add_Categorie from './pages/Add_Categorie.jsx';
import ListCategorie from './pages/ListCategorie.jsx';
import Modifier_livre from './pages/Modifier_livre.jsx';
import UserManagement from './pages/AdminUserManagement.jsx';

// ─── Guard: redirects to /login if not authenticated ───────────────
const Protected = ({ children, requiredRole }) => {
  if (!isLoggedIn()) return <Navigate to="/login" replace />;
  if (requiredRole) {
    const roles = getUser()?.roles ?? [];
    if (!roles.includes(requiredRole)) return <Navigate to="/" replace />;
  }
  return children;
};

// ─── Layout with Navbar ─────────────────────────────────────────────
const Layout = ({ children }) => (
  <>
    <Navbar />
    {children}
  </>
);

function App() {
  return (
    <Router>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />

        {/* Protected — all logged-in users */}
        <Route path="/" element={
          <Protected>
            <Layout><Dashboard /></Layout>
          </Protected>
        } />
        <Route path="/listLivres" element={
          <Protected>
            <Layout><ListLivre /></Layout>
          </Protected>
        } />
        <Route path="/modifier-livre/:id" element={
          <Protected>
            <Layout><Modifier_livre /></Layout>
          </Protected>
        } />

        {/* Protected — MANAGER or ADMIN (backend enforces, frontend just hides nav) */}
        <Route path="/ajouter-livre" element={
          <Protected>
            <Layout><AjouterLivre /></Layout>
          </Protected>
        } />
        <Route path="/ajouter-categorie" element={
          <Protected>
            <Layout><Add_Categorie /></Layout>
          </Protected>
        } />
        <Route path="/listCategorie" element={
          <Protected>
            <Layout><ListCategorie /></Layout>
          </Protected>
        } />

        {/* Protected — ADMIN only */}
        <Route path="/admin/users" element={
          <Protected requiredRole="ADMIN">
            <Layout><UserManagement /></Layout>
          </Protected>
        } />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
