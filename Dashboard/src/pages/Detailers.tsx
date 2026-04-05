import React, { useState, useEffect } from 'react';
import {
    collection,
    onSnapshot,
    doc,
    addDoc,
    updateDoc,
    deleteDoc
} from 'firebase/firestore';
import { db } from '../firebase';
import { Search, MapPin, Phone, Star, Plus, Edit2, Trash2, Truck } from 'lucide-react';
import { toast } from 'sonner';

interface Detailer {
    id: string;
    name: string;
    providerName: string;
    phone: string;
    city: string;
    address: string;
    description: string;
    image: string;
    rating: number;
    reviewCount: number;
    serviceType: string;
}

export default function DetailerManagement() {
    const [detailers, setDetailers] = useState<Detailer[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Key fix: Track the ID separately for updates
    const [editingDetailerId, setEditingDetailerId] = useState<string | null>(null);

    const [formData, setFormData] = useState({
        name: '',
        providerName: '',
        phone: '',
        city: 'Mount Lavinia',
        address: 'Your Doorstep',
        description: '',
        image: '',
        serviceType: 'MOBILE',
        rating: 5,
        reviewCount: 0
    });

    useEffect(() => {
        if (!db) return;

        setLoading(true);

        // Create the real-time listener
        const unsubscribe = onSnapshot(collection(db, 'detailers'), (snapshot) => {
            // We map the docs into a fresh array every time the DB changes
            const freshData = snapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data()
            } as Detailer));

            // Replacing the state entirely with freshData prevents duplication
            setDetailers(freshData);
            setLoading(false);
        }, (error) => {
            console.error("Snapshot error:", error);
            setLoading(false);
        });

        // CRITICAL: This cleans up the listener when the component unmounts
        return () => unsubscribe();
    }, []);

    const resetForm = () => {
        setFormData({
            name: '', providerName: '', phone: '', city: 'Mount Lavinia',
            address: 'Your Doorstep', description: '', image: '',
            serviceType: 'MOBILE', rating: 5, reviewCount: 0
        });
        setEditingDetailerId(null);
    };

    const handleEdit = (detailer: Detailer) => {
        setEditingDetailerId(detailer.id);
        setFormData({
            name: detailer.name || '',
            providerName: detailer.providerName || '',
            phone: detailer.phone || '',
            city: detailer.city || 'Mount Lavinia',
            address: detailer.address || 'Your Doorstep',
            description: detailer.description || '',
            image: detailer.image || '',
            serviceType: detailer.serviceType || 'MOBILE',
            rating: detailer.rating || 5,
            reviewCount: detailer.reviewCount || 0
        });
        setIsModalOpen(true);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!db) return;

        try {
            if (editingDetailerId) {
                // UPDATE: Points to 'detailers/[VALID_ID]'
                const detailerRef = doc(db, 'detailers', editingDetailerId);
                await updateDoc(detailerRef, formData);
                toast.success("Detailer updated successfully!");
            } else {
                // CREATE: Add new document
                await addDoc(collection(db, 'detailers'), formData);
                toast.success("New detailer registered!");
            }

            setIsModalOpen(false);
            resetForm();
        } catch (error: any) {
            console.error("Firestore Error:", error);
            toast.error(`Operation failed: ${error.message}`);
        }
    };

    const handleDelete = async (id: string) => {
        if (!db) return;
        if (window.confirm("Are you sure you want to delete this detailer?")) {
            try {
                await deleteDoc(doc(db, 'detailers', id));
                toast.success("Detailer removed!");
            } catch (error) {
                toast.error("Delete failed");
            }
        }
    };

    const filteredDetailers = detailers.filter(d =>
        d.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        d.providerName?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-900">Detailer Partners</h1>
                <button
                    onClick={() => { resetForm(); setIsModalOpen(true); }}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-bold flex items-center shadow-md transition-all"
                >
                    <Plus className="w-5 h-5 mr-2" /> Register Detailer
                </button>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <div className="p-4 border-b border-slate-200 bg-slate-50">
                    <div className="relative w-80">
                        <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                        <input
                            type="text"
                            placeholder="Search detailers..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-600 bg-white"
                        />
                    </div>
                </div>

                <div className="p-6">
                    {loading ? (
                        <div className="text-center py-10 text-slate-500 font-medium animate-pulse">Syncing with database...</div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {filteredDetailers.map((detailer) => (
                                <div key={detailer.id} className="border border-slate-100 rounded-3xl overflow-hidden hover:shadow-lg transition-all bg-white">
                                    <div className="h-40 bg-slate-100">
                                        <img src={detailer.image || 'https://via.placeholder.com/400x200'} alt={detailer.name} className="w-full h-full object-cover" />
                                    </div>
                                    <div className="p-5 space-y-3">
                                        <div className="flex justify-between items-start">
                                            <h3 className="font-bold text-slate-900">{detailer.name}</h3>
                                            <div className="flex items-center text-amber-500 font-bold text-sm">
                                                <Star className="w-4 h-4 mr-1 fill-current" /> {detailer.rating}
                                            </div>
                                        </div>
                                        <p className="text-xs text-slate-500 italic font-medium">By {detailer.providerName}</p>
                                        <div className="flex items-center text-xs text-slate-600 font-medium"><MapPin className="w-3.5 h-3.5 mr-2 text-blue-500" /> {detailer.city}</div>
                                        <div className="flex items-center text-xs text-slate-600 font-medium"><Truck className="w-3.5 h-3.5 mr-2 text-blue-500" /> {detailer.serviceType}</div>

                                        <div className="flex gap-2 pt-4">
                                            <button
                                                onClick={() => handleEdit(detailer)}
                                                className="flex-1 py-2 bg-blue-50 text-blue-600 rounded-xl text-xs font-bold hover:bg-blue-600 hover:text-white transition-all"
                                            >
                                                Edit
                                            </button>
                                            <button
                                                onClick={() => handleDelete(detailer.id)}
                                                className="p-2 bg-red-50 text-red-500 rounded-xl hover:bg-red-500 hover:text-white transition-all"
                                            >
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* MODAL */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl animate-in zoom-in duration-200">
                        <div className="px-8 py-5 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
                            <h2 className="font-bold text-slate-800 text-lg">{editingDetailerId ? 'Edit Detailer Profile' : 'Register New Partner'}</h2>
                            <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 text-2xl">&times;</button>
                        </div>
                        <form onSubmit={handleSubmit} className="p-8 space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div className="col-span-2">
                                    <label className="text-xs font-black text-slate-500 uppercase tracking-wider mb-1 block">Business Name</label>
                                    <input
                                        required
                                        value={formData.name}
                                        onChange={e => setFormData({ ...formData, name: e.target.value })}
                                        className="w-full p-3 bg-white border border-slate-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                        placeholder="e.g. Quick Shine Mobile"
                                    />
                                </div>
                                <div>
                                    <label className="text-xs font-black text-slate-500 uppercase tracking-wider mb-1 block">Provider Name</label>
                                    <input
                                        required
                                        value={formData.providerName}
                                        onChange={e => setFormData({ ...formData, providerName: e.target.value })}
                                        className="w-full p-3 bg-white border border-slate-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                        placeholder="Owner name"
                                    />
                                </div>
                                <div>
                                    <label className="text-xs font-black text-slate-500 uppercase tracking-wider mb-1 block">Phone Number</label>
                                    <input
                                        required
                                        value={formData.phone}
                                        onChange={e => setFormData({ ...formData, phone: e.target.value })}
                                        className="w-full p-3 bg-white border border-slate-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                        placeholder="077xxxxxxx"
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="text-xs font-black text-slate-500 uppercase tracking-wider mb-1 block">Image URL</label>
                                <input
                                    required
                                    value={formData.image}
                                    onChange={e => setFormData({ ...formData, image: e.target.value })}
                                    className="w-full p-3 bg-white border border-slate-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                    placeholder="https://images.unsplash.com/..."
                                />
                            </div>
                            <button type="submit" className="w-full py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl font-black text-sm uppercase tracking-widest shadow-lg shadow-blue-200 transition-all mt-4">
                                {editingDetailerId ? 'Update Partner Profile' : 'Confirm Registration'}
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}