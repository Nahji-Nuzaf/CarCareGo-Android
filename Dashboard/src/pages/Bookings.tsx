import { useState, useEffect } from 'react';
import { collection, query, orderBy, onSnapshot, updateDoc, doc } from 'firebase/firestore';
import { db } from '../firebase';
import { Search, Filter, Clock, Calendar } from 'lucide-react';
import { toast } from 'sonner';

interface Booking {
  id: string;
  userId: string;
  serviceName: string;
  servicePrice: number;
  status: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'PAID';
  bookingDate: string; // From your screenshot
  bookingTime: string; // From your screenshot
  vehicleModel: string;
}

export default function Bookings() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('All');

  useEffect(() => {
    if (!db) {
      setLoading(false);
      return;
    }

    // UPDATED: Points to 'ServiceBookings' collection
    const q = query(collection(db, 'ServiceBookings'));
    
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map(doc => ({ 
        id: doc.id, 
        ...doc.data() 
      } as Booking));
      
      // Sort manually by date string if timestamp isn't available
      const sorted = data.sort((a, b) => b.bookingDate.localeCompare(a.bookingDate));
      
      setBookings(sorted);
      setLoading(false);
    }, (error) => {
      console.error("Error fetching bookings:", error);
      toast.error("Failed to load real-time bookings");
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (!db) return;
    try {
      // UPDATED: Points to 'ServiceBookings' document
      await updateDoc(doc(db, 'ServiceBookings', id), { status: newStatus });
      toast.success(`Status updated to ${newStatus}`);
    } catch (error) {
      console.error("Error updating status:", error);
      toast.error("Failed to update status");
    }
  };

  const filteredBookings = bookings.filter(b => {
    const matchesSearch = 
      (b.serviceName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) || 
      (b.id?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
      (b.vehicleModel?.toLowerCase() || '').includes(searchTerm.toLowerCase());
    
    const matchesStatus = statusFilter === 'All' || b.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">Order & Booking Management</h1>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 flex flex-col sm:flex-row sm:justify-between sm:items-center bg-slate-50 gap-4">
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input 
              type="text" 
              placeholder="Search service, ID, or vehicle..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-slate-300 rounded-md text-sm focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-slate-500" />
            <select 
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 border border-slate-300 rounded-md text-sm bg-white outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="All">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="PAID">Paid</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200 uppercase tracking-wider text-xs">
              <tr>
                <th className="px-6 py-4">Booking ID</th>
                <th className="px-6 py-4">Vehicle & User</th>
                <th className="px-6 py-4">Service</th>
                <th className="px-6 py-4">Schedule</th>
                <th className="px-6 py-4">Price</th>
                <th className="px-6 py-4 text-center">Update Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {loading ? (
                <tr><td colSpan={6} className="px-6 py-12 text-center text-slate-500">Loading bookings...</td></tr>
              ) : filteredBookings.length === 0 ? (
                <tr><td colSpan={6} className="px-6 py-12 text-center text-slate-500">No bookings found.</td></tr>
              ) : (
                filteredBookings.map((booking) => (
                  <tr key={booking.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 font-mono text-xs text-slate-400">#{booking.id.slice(-6).toUpperCase()}</td>
                    <td className="px-6 py-4">
                      <p className="font-semibold text-slate-900">{booking.vehicleModel}</p>
                      <p className="text-xs text-slate-500">{booking.vehiclePlate}</p>
                    </td>
                    <td className="px-6 py-4 font-medium text-blue-700">{booking.serviceName}</td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col space-y-1">
                        <span className="flex items-center text-slate-600"><Calendar className="w-3 h-3 mr-1"/> {booking.bookingDate}</span>
                        <span className="flex items-center text-slate-400 text-xs"><Clock className="w-3 h-3 mr-1"/> {booking.bookingTime}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-slate-900 font-bold">LKR {Number(booking.servicePrice).toLocaleString()}</td>
                    <td className="px-6 py-4 text-center">
                      <select
                        value={booking.status}
                        onChange={(e) => handleStatusChange(booking.id, e.target.value)}
                        className={`text-xs font-bold rounded-full px-3 py-1.5 border-0 outline-none cursor-pointer transition-all
                          ${booking.status === 'COMPLETED' || booking.status === 'PAID' ? 'bg-emerald-100 text-emerald-800' : 
                            booking.status === 'CONFIRMED' ? 'bg-blue-100 text-blue-800' : 
                            'bg-amber-100 text-amber-800'}`}
                      >
                        <option value="PENDING">PENDING</option>
                        <option value="PAID">PAID</option>
                        <option value="CONFIRMED">CONFIRMED</option>
                        <option value="COMPLETED">COMPLETED</option>
                      </select>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}