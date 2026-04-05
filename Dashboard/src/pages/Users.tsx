import { useState, useEffect } from 'react';
import { collection, onSnapshot, doc, updateDoc } from 'firebase/firestore';
import { db } from '../firebase';
import { Search, User, Mail, Phone, ShieldCheck, ShieldAlert, Edit2 } from 'lucide-react';
import { toast } from 'sonner';

interface UserProfile {
  id: string;
  name: string;
  email: string;
  mobileNumber?: string; // Mapped exactly from your Firestore screenshot
  status?: 'active' | 'suspended';
}

export default function UserManagement() {
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (!db) return;
    const unsubscribe = onSnapshot(collection(db, 'users'), (snapshot) => {
      const userData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      } as UserProfile));
      setUsers(userData);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleUpdatePhone = async (userId: string) => {
    const newPhone = window.prompt("Enter new mobile number:");
    if (newPhone !== null && newPhone.trim() !== "") {
      try {
        await updateDoc(doc(db, 'users', userId), { mobileNumber: newPhone });
        toast.success("Mobile number updated!");
      } catch (error) {
        toast.error("Failed to update mobile number");
      }
    }
  };

  const toggleUserStatus = async (userId: string, currentStatus: string) => {
    const newStatus = currentStatus === 'suspended' ? 'active' : 'suspended';
    try {
      await updateDoc(doc(db, 'users', userId), { status: newStatus });
      toast.success(`User is now ${newStatus}`);
    } catch (error) {
      toast.error("Status update failed");
    }
  };

  const filteredUsers = users.filter(u => 
    u.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (u.mobileNumber || '').includes(searchTerm)
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">User Management</h1>
        <div className="bg-blue-600 text-white px-4 py-2 rounded-xl text-sm font-bold shadow-md shadow-blue-100">
          {users.length} Registered Users
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50">
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input 
              type="text" 
              placeholder="Search users..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 border border-slate-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-blue-600 bg-white transition-all"
            />
          </div>
        </div>

        <div className="p-6">
          {loading ? (
            <div className="text-center py-12 text-slate-400 animate-pulse">Fetching CarCareGo Users...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredUsers.map((user) => (
                <div key={user.id} className="group bg-white border border-slate-100 rounded-2xl p-6 hover:border-blue-400 transition-all shadow-sm hover:shadow-md">
                  <div className="flex items-start justify-between mb-4">
                    <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center group-hover:bg-blue-600 group-hover:text-white transition-all">
                      <User className="w-6 h-6" />
                    </div>
                    <span className={`px-2.5 py-1 rounded-lg text-[10px] font-black uppercase ${
                      user.status === 'suspended' ? 'bg-red-100 text-red-600' : 'bg-emerald-100 text-emerald-600'
                    }`}>
                      {user.status || 'Active'}
                    </span>
                  </div>

                  <h3 className="font-bold text-slate-900 text-lg mb-1 truncate">{user.name || 'Anonymous User'}</h3>
                  
                  <div className="space-y-3 mb-6">
                    <div className="flex items-center text-sm text-slate-500">
                      <Mail className="w-4 h-4 mr-3 text-slate-300" /> {user.email}
                    </div>
                    <div className="flex items-center justify-between group/phone">
                      <div className="flex items-center text-sm font-medium text-slate-600">
                        <Phone className="w-4 h-4 mr-3 text-blue-500" /> 
                        {/* Logic: If mobileNumber exists show it, otherwise say No number */}
                        {user.mobileNumber ? user.mobileNumber : <span className="text-slate-400 italic">No number</span>}
                      </div>
                      <button 
                        onClick={() => handleUpdatePhone(user.id)}
                        className="opacity-0 group-hover/phone:opacity-100 text-blue-500 hover:text-blue-700 transition-all p-1"
                        title="Edit Number"
                      >
                        <Edit2 className="w-3 h-3" />
                      </button>
                    </div>
                  </div>

                  <button 
                    onClick={() => toggleUserStatus(user.id, user.status || 'active')}
                    className={`w-full flex items-center justify-center py-2.5 rounded-xl text-xs font-bold transition-all ${
                      user.status === 'suspended' 
                      ? 'bg-emerald-600 text-white hover:bg-emerald-700' 
                      : 'bg-slate-50 text-slate-500 hover:bg-red-50 hover:text-red-600'
                    }`}
                  >
                    {user.status === 'suspended' ? (
                      <><ShieldCheck className="w-3.5 h-3.5 mr-2" /> Re-activate User</>
                    ) : (
                      <><ShieldAlert className="w-3.5 h-3.5 mr-2" /> Suspend Account</>
                    )}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}