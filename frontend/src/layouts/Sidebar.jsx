import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, Users, Code, BookOpen, 
  FileText, MessageSquare, Briefcase, Settings,
  BarChart3, User, X
} from 'lucide-react';
import { cn } from '../components/Button';

const navigation = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Interviews', href: '/interviews', icon: Users },
  { name: 'Coding Practice', href: '/coding', icon: Code },
  { name: 'GATE Practice', href: '/gate', icon: BookOpen },
  { name: 'Deep Dive', href: '/deep-dive', icon: FileText },
  { name: 'English Practice', href: '/english', icon: MessageSquare },
  { name: 'Study Plan', href: '/study-plan', icon: BookOpen },
  { name: 'Projects', href: '/projects', icon: Briefcase },
  { name: 'Analytics', href: '/analytics', icon: BarChart3 },
];

const secondaryNavigation = [
  { name: 'Profile', href: '/profile', icon: User },
  { name: 'Settings', href: '/settings', icon: Settings },
];

export const Sidebar = ({ isOpen, setIsOpen }) => {
  return (
    <div className={cn(
      "fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out md:relative md:translate-x-0 flex flex-col",
      isOpen ? "translate-x-0" : "-translate-x-full"
    )}>
      <div className="flex items-center justify-between h-16 px-4 border-b border-gray-200">
        <span className="text-xl font-bold text-blue-600 flex items-center gap-2">
          <div className="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center">
            <span className="text-white text-sm">AI</span>
          </div>
          Coach
        </span>
        <button 
          className="md:hidden text-gray-500 hover:text-gray-700 focus:outline-none"
          onClick={() => setIsOpen(false)}
        >
          <X className="h-6 w-6" />
        </button>
      </div>
      
      <div className="flex-1 overflow-y-auto flex flex-col justify-between py-4">
        <nav className="px-3 space-y-1">
          {navigation.map((item) => (
            <NavLink
              key={item.name}
              to={item.href}
              onClick={() => setIsOpen && setIsOpen(false)}
              className={({ isActive }) =>
                cn(
                  "group flex items-center px-3 py-2 text-sm font-medium rounded-md mb-1",
                  isActive
                    ? "bg-blue-50 text-blue-700"
                    : "text-gray-700 hover:bg-gray-100 hover:text-gray-900"
                )
              }
            >
              {({ isActive }) => (
                <>
                  <item.icon 
                    className={cn(
                      "mr-3 h-5 w-5 flex-shrink-0",
                      isActive ? "text-blue-600" : "text-gray-400 group-hover:text-gray-500"
                    )} 
                    aria-hidden="true" 
                  />
                  {item.name}
                </>
              )}
            </NavLink>
          ))}
        </nav>
        
        <div className="px-3 mt-8">
          <h3 className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
            Account
          </h3>
          <nav className="space-y-1">
            {secondaryNavigation.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                onClick={() => setIsOpen && setIsOpen(false)}
                className={({ isActive }) =>
                  cn(
                    "group flex items-center px-3 py-2 text-sm font-medium rounded-md",
                    isActive
                      ? "bg-blue-50 text-blue-700"
                      : "text-gray-700 hover:bg-gray-100 hover:text-gray-900"
                  )
                }
              >
                <item.icon className="mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500 flex-shrink-0" aria-hidden="true" />
                {item.name}
              </NavLink>
            ))}
          </nav>
        </div>
      </div>
    </div>
  );
};

