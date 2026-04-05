import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'sonner';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Categories from './pages/Categories';
import Bookings from './pages/Bookings';
import { Users } from 'lucide-react';
import UserManagement from './pages/Users';
// import DetailerManagement from './pages/Detailers';
// import ServiceManagement from './pages/Services';

export default function App() {
  return (
    <Router>
      <Toaster position="top-right" richColors />
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="products" element={<Products />} />
          <Route path="categories" element={<Categories />} />
          <Route path="bookings" element={<Bookings />} />
          <Route path="users" element={<UserManagement />} />
          {/* <Route path="detailers" element={<DetailerManagement />} /> */}
        </Route>
      </Routes>
    </Router>
  );
}
