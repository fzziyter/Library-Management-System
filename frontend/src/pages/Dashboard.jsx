import { Link } from 'react-router-dom';
const Dashboard = () => {
  return (
    <div>
    <div>Dashboard</div>
      <ul>
        <li><Link to="/listLivres">Liste des livres</Link></li>
        <li><Link to="/admin/users">Gestion des utilisateurs</Link></li>
        <li><Link to="/listCategorie">Liste des catégories</Link></li>
        <li><Link to="/ajouter-livre">Ajouter un livre</Link></li>
        <li><Link to="/ajouter-categorie">Ajouter une catégorie</Link></li>
      </ul>
       
    </div>
  );
}
export default Dashboard;