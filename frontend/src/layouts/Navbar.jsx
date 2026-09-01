import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { User, LogOut, Menu } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';

export const Navbar = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };
  
  // Basic breadcrumb title logic based on path
  const getPageTitle = () => {
    const path = location.pathname;
    if (path === '/') return 'Dashboard';
    if (path.startsWith('/interviews')) return 'Interviews';
    if (path.startsWith('/coding')) return 'Coding Practice';
    if (path.startsWith('/gate')) return 'GATE Practice';
    if (path.startsWith('/deep-dive')) return 'Deep Dive';
    if (path.startsWith('/english')) return 'English Practice';
    if (path.startsWith('/study-plan')) return 'Study Plan';
    if (path.startsWith('/projects')) return 'Projects';
    if (path.startsWith('/analytics')) return 'Analytics';
    if (path.startsWith('/profile')) return 'Profile';
    return '';
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-gray-200 bg-white px-4 sm:px-6">
      <div className="flex items-center">
        <button 
          onClick={onMenuClick}
          className="mr-4 text-gray-500 hover:text-gray-700 focus:outline-none md:hidden"
        >
          <Menu className="h-6 w-6" />
        </button>
        <h1 className="text-xl font-semibold text-gray-800 hidden sm:block">{getPageTitle()}</h1>
      </div>
      <div className="flex items-center space-x-4">
        <span className="text-sm font-medium text-gray-700 hidden sm:block">
          {user?.name || user?.email || 'User'}
        </span>
        <button 
          onClick={() => navigate('/profile')}
          className="flex items-center text-sm font-medium text-gray-700 hover:text-blue-600 focus:outline-none"
        >
          <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 mr-2 sm:mr-0">
            <User className="h-4 w-4" />
          </div>
          <span className="sm:hidden">Profile</span>
        </button>
        <button
          onClick={handleLogout}
          className="flex items-center text-sm font-medium text-red-600 hover:text-red-800 focus:outline-none"
        >
          <LogOut className="h-5 w-5 sm:mr-2" />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </div>
    </header>
  );
};

