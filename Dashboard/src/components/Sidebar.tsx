import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Package, Tags, CalendarCheck, Car, Users } from 'lucide-react';
import { cn } from '../lib/utils';

const navItems = [
  { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  { name: 'Products', path: '/products', icon: Package },
  { name: 'Categories', path: '/categories', icon: Tags },
  { name: 'Bookings', path: '/bookings', icon: CalendarCheck },
  { name: 'Users', path: '/users', icon: Users },
  // { name: 'Detailers', path: '/detailers', icon: Users },
];

export default function Sidebar() {
  return (
    <div className="w-64 bg-slate-900 text-slate-300 flex flex-col">
      <div className="h-16 flex items-center px-6 border-b border-slate-800">
        {/* <Car className="w-8 h-8 text-blue-500 mr-3" /> */}
        <span className="text-xl font-bold text-white tracking-tight">Car Care Go</span>
      </div>
      <nav className="flex-1 py-6 px-3 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.name}
            to={item.path}
            className={({ isActive }) =>
              cn(
                'flex items-center px-3 py-2.5 text-sm font-medium rounded-md transition-colors',
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'hover:bg-slate-800 hover:text-white'
              )
            }
          >
            <item.icon className="w-5 h-5 mr-3 flex-shrink-0" />
            {item.name}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t border-slate-800 text-xs text-slate-500">
        &copy; 2026 CarCareGo Admin
      </div>
    </div>
  );
}
