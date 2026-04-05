import { Bell, Search, UserCircle } from 'lucide-react';

export default function Header() {
  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6">
      <div className="flex items-center flex-1">
        <div className="relative w-64">
          <span className="absolute inset-y-0 left-0 flex items-center pl-3">
            <Search className="w-4 h-4 text-slate-400" />
          </span>
          <input
            type="text"
            className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Search..."
          />
        </div>
      </div>
      <div className="flex items-center space-x-4">
        <button className="text-slate-500 hover:text-slate-700 relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white" />
        </button>
        <div className="flex items-center space-x-2 border-l pl-4 border-slate-200">
          <UserCircle className="w-8 h-8 text-slate-400" />
          <div className="text-sm">
            <p className="font-medium text-slate-700">Admin User</p>
            <p className="text-xs text-slate-500">nahji@carcarego.com</p>
          </div>
        </div>
      </div>
    </header>
  );
}
