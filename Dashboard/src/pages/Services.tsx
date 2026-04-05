import React, { useState, useEffect } from 'react';
import { collection, onSnapshot, addDoc, updateDoc, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../firebase';
import { Plus, Edit2, Trash2, Clock, Shield, Tag, Search } from 'lucide-react';
import { toast } from 'sonner';

interface WashService {
  id: string;
  name: string;
  price: number;
  duration: string;
  category: string;
  description: string;
  detailerId?: string;
}

export default function ServiceManagement() {
  const [services, setServices] = useState<WashService[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingService, setEditingService] = useState<WashService | null>(null);
  
  const [formData, setFormData] = useState({
    name: '',
    price: '',
    duration: '',
    category: 'Basic',
    description: ''
  });

  useEffect(() => {
    if (!db) return;
    const unsubscribe = onSnapshot(collection(db, 'BookingServices'), (snapshot) => {
      const data = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as WashService));
      setServices(data);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const serviceData = {
      ...formData,
      price: Number(formData.price),
      detailerId: "prPNJkoJJYBKS9L4AVHN" // Using ID from your screenshot
    };

    try {
      if (editingService) {
        await updateDoc(doc(db, 'BookingServices', editingService.id), serviceData);
        toast.success("Service updated!");
      } else {
        await addDoc(collection(db, 'BookingServices'), serviceData);
        toast.success("New service added!");
      }
      setIsModalOpen(false);
      setFormData({ name: '', price: '', duration: '', category: 'Basic', description: '' });
    } catch (error) {
      toast.error("Operation failed");
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm("Delete this service permanently?")) {
      await deleteDoc(doc(db, 'BookingServices', id));
      toast.success("Service removed");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">Wash & Detail Services</h1>
        <button 
          onClick={() => { setEditingService(null); setIsModalOpen(true); }}
          className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-bold flex items-center shadow-lg shadow-blue-100 transition-all"
        >
          <Plus className="w-5 h-5 mr-2" /> Add Service
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {loading ? (
          <div className="col-span-full text-center py-12 text-slate-400">Loading services...</div>
        ) : (
          services.map((service) => (
            <div key={service.id} className="bg-white border border-slate-200 rounded-2xl p-6 hover:shadow-xl transition-all group relative overflow-hidden">
              <div className="absolute top-0 right-0 p-4 flex space-x-2">
                <button onClick={() => { setEditingService(service); setFormData({ ...service, price: service.price.toString() }); setIsModalOpen(true); }} className="p-2 bg-slate-50 text-slate-400 hover:text-blue-600 rounded-lg transition-colors">
                  <Edit2 className="w-4 h-4" />
                </button>
                <button onClick={() => handleDelete(service.id)} className="p-2 bg-slate-50 text-slate-400 hover:text-red-600 rounded-lg transition-colors">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <div className={`w-14 h-14 rounded-2xl flex items-center justify-center mb-4 ${service.category === 'Premium' ? 'bg-amber-50 text-amber-600' : 'bg-blue-50 text-blue-600'}`}>
                <Shield className="w-7 h-7" />
              </div>

              <h3 className="text-xl font-bold text-slate-900 mb-1">{service.name}</h3>
              <p className="text-sm text-slate-500 mb-4 line-clamp-2">{service.description}</p>

              <div className="flex flex-wrap gap-3 mb-6">
                <div className="flex items-center text-xs font-bold text-slate-600 bg-slate-100 px-3 py-1.5 rounded-lg">
                  <Clock className="w-3.5 h-3.5 mr-1.5" /> {service.duration}
                </div>
                <div className="flex items-center text-xs font-bold text-blue-600 bg-blue-50 px-3 py-1.5 rounded-lg">
                  <Tag className="w-3.5 h-3.5 mr-1.5" /> {service.category}
                </div>
              </div>

              <div className="flex items-center justify-between pt-4 border-t border-slate-100">
                <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Pricing</span>
                <span className="text-2xl font-black text-slate-900">LKR {service.price.toLocaleString()}</span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal for Add/Edit */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-3xl shadow-2xl w-full max-w-lg overflow-hidden animate-in zoom-in duration-200">
            <div className="px-8 py-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h2 className="text-xl font-bold text-slate-900">{editingService ? 'Edit Service' : 'New Wash Package'}</h2>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 text-3xl">&times;</button>
            </div>
            <form onSubmit={handleSubmit} className="p-8 space-y-5">
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm font-bold text-slate-700 mb-1.5">Service Name</label>
                  <input required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border-transparent rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" placeholder="e.g. Full Ceramic Shine" />
                </div>
                <div>
                  <label className="block text-sm font-bold text-slate-700 mb-1.5">Price (LKR)</label>
                  <input required type="number" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border-transparent rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div>
                  <label className="block text-sm font-bold text-slate-700 mb-1.5">Duration</label>
                  <input required value={formData.duration} onChange={e => setFormData({...formData, duration: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border-transparent rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" placeholder="e.g. 2 Hours" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1.5">Category</label>
                <select value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border-transparent rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none transition-all">
                  <option value="Basic">Basic</option>
                  <option value="Premium">Premium</option>
                  <option value="Luxury">Luxury</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1.5">Description</label>
                <textarea rows={3} value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border-transparent rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" placeholder="Details about the service..."></textarea>
              </div>
              <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white py-4 rounded-2xl font-bold shadow-lg shadow-blue-100 transition-all">
                {editingService ? 'Update Service' : 'Create Service'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}