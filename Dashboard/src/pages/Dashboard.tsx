import { useEffect, useState } from 'react';
import { collection, onSnapshot } from 'firebase/firestore';
import { db } from '../firebase';
import { Users, DollarSign, Calendar, Clock, ArrowUpRight, Phone } from 'lucide-react';
import { format } from 'date-fns';

interface Booking {
  id: string;
  orderDate?: any;       // Mapped from your screenshot
  totalAmount?: number | string;
  status?: string;
  shippingAddress?: {    // Mapped from your screenshot
    name: string;
    phone: string;
    city: string;
  };
  [key: string]: any;
}

export default function Dashboard() {
  const [stats, setStats] = useState({
    totalBookings: 0,
    totalRevenue: 0,
    activeUsers: 0,
    pendingWashes: 0,
  });
  const [recentBookings, setRecentBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  // Updated Helper to handle the "26 March 2026..." string format from your DB
  const formatBookingDate = (dateVal: any) => {
    if (!dateVal) return 'No Date';
    
    // If it's a Firestore Timestamp
    if (dateVal.toDate) return format(dateVal.toDate(), 'MMM dd, yyyy');
    
    // If it's the string shown in your screenshot "26 March 2026 at..."
    if (typeof dateVal === 'string') {
      // We take the first part of the string before " at"
      return dateVal.split(' at')[0]; 
    }
    
    return 'Date Format Error';
  };

  useEffect(() => {
    if (!db) {
      setLoading(false);
      return;
    }

    const unsubscribeBookings = onSnapshot(collection(db, 'bookings'), (snapshot) => {
      let revenue = 0;
      let pending = 0;
      
      const allData = snapshot.docs.map(doc => {
        const data = doc.data() as Booking;
        // Using totalAmount if exists, otherwise subtotal + shippingFee
        const amount = Number(data.totalAmount) || (Number(data.subtotal) + Number(data.shippingFee)) || 0;
        revenue += amount;
        
        if (data.status === 'Pending') pending++;
        
        return { id: doc.id, ...data, calculatedAmount: amount };
      });

      // Sort by newest first based on the ID or date string if necessary
      const sorted = [...allData].reverse(); 

      setStats(prev => ({
        ...prev,
        totalBookings: snapshot.size,
        totalRevenue: revenue,
        pendingWashes: pending,
      }));
      
      setRecentBookings(sorted.slice(0, 5));
      setLoading(false);
    });

    const unsubscribeUsers = onSnapshot(collection(db, 'users'), (snapshot) => {
      setStats(prev => ({ ...prev, activeUsers: snapshot.size }));
    });

    return () => {
      unsubscribeBookings();
      unsubscribeUsers();
    };
  }, []);

  const statCards = [
    { title: 'Total Orders', value: stats.totalBookings, icon: Calendar, color: 'text-blue-600', bg: 'bg-blue-100' },
    { title: 'Total Revenue', value: `LKR ${stats.totalRevenue.toLocaleString()}`, icon: DollarSign, color: 'text-emerald-600', bg: 'bg-emerald-100' },
    { title: 'Active Users', value: stats.activeUsers, icon: Users, color: 'text-indigo-600', bg: 'bg-indigo-100' },
  ];

  if (loading) return <div className="p-8 text-center">Syncing with CarCareGo Database...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Dashboard Overview</h1>
      
      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, index) => (
          <div key={index} className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex items-center">
            <div className={`p-3 rounded-lg ${stat.bg} ${stat.color} mr-4`}>
              <stat.icon className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">{stat.title}</p>
              <p className="text-2xl font-bold text-slate-900">{stat.value}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Bookings Table */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-200 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-slate-900">Recent Orders</h2>
          <button className="text-sm text-blue-600 hover:text-blue-800 font-medium flex items-center">
            View All <ArrowUpRight className="w-4 h-4 ml-1" />
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200">
              <tr>
                <th className="px-6 py-3">Order ID</th>
                <th className="px-6 py-3">Customer Details</th>
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Amount</th>
                <th className="px-6 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {recentBookings.map((booking) => (
                <tr key={booking.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4 font-mono text-xs text-slate-400">
                    #{booking.id.split('-').pop()}
                  </td>
                  
                  {/* FIX: Using shippingAddress.name and shippingAddress.phone */}
                  <td className="px-6 py-4">
                    <div className="flex flex-col">
                      <span className="font-semibold text-slate-900">
                        {booking.shippingAddress?.name || 'Walk-in Customer'}
                      </span>
                      <span className="text-xs text-slate-500 flex items-center">
                        <Phone className="w-3 h-3 mr-1" /> {booking.shippingAddress?.phone || 'No Phone'}
                      </span>
                    </div>
                  </td>
                  
                  {/* FIX: Using orderDate field */}
                  <td className="px-6 py-4 text-slate-600">
                    {formatBookingDate(booking.orderDate)}
                  </td>
                  
                  <td className="px-6 py-4 text-slate-900 font-bold">
                    LKR {(booking.calculatedAmount || 0).toLocaleString()}
                  </td>
                  
                  <td className="px-6 py-4">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-bold
                      ${booking.status === 'PAID' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'}`}>
                      {booking.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}