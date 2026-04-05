import React, { useState, useEffect } from 'react';
import { collection, onSnapshot, addDoc, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../firebase';
import { Plus, Trash2, Search, Tag, Sparkles } from 'lucide-react';
import { toast } from 'sonner';

// Corrected interface to match your Firestore screenshot
interface Category {
  id: string;          // Firestore Document ID
  categoryId: string;   // e.g., "cate1"
  categoryName: string; // e.g., "Exterior"
}

export default function Categories() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // States for the form
  const [newCategoryId, setNewCategoryId] = useState('');
  const [newCategoryName, setNewCategoryName] = useState('');

  useEffect(() => {
    if (!db) {
      setLoading(false);
      return;
    }

    // Using onSnapshot for real-time updates
    const unsubscribe = onSnapshot(collection(db, 'categories'), (snapshot) => {
      const data = snapshot.docs.map(doc => ({ 
        id: doc.id, 
        ...doc.data() 
      } as Category));
      setCategories(data);
      setLoading(false);
    }, (error) => {
      console.error("Error fetching categories:", error);
      toast.error("Failed to load categories");
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!db) return toast.error("Firebase not configured");
    if (!newCategoryName.trim() || !newCategoryId.trim()) {
        return toast.error("Both ID and Name are required");
    }

    try {
      await addDoc(collection(db, 'categories'), {
        categoryId: newCategoryId,
        categoryName: newCategoryName
      });
      toast.success("Category added successfully");
      setNewCategoryName('');
      setNewCategoryId('');
    } catch (error) {
      console.error("Error adding category:", error);
      toast.error("Failed to add category");
    }
  };

  const handleDelete = async (id: string) => {
    if (!db) return;
    if (window.confirm("Are you sure you want to delete this category?")) {
      try {
        await deleteDoc(doc(db, 'categories', id));
        toast.success("Category deleted successfully");
      } catch (error) {
        console.error("Error deleting category:", error);
        toast.error("Failed to delete category");
      }
    }
  };

  const filteredCategories = categories.filter(c => 
    c.categoryName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.categoryId?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">Category Management</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Add Category Form */}
        <div className="md:col-span-1">
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sticky top-6">
            <div className="flex items-center mb-4 text-blue-600">
                <Sparkles className="w-5 h-5 mr-2" />
                <h2 className="text-lg font-bold text-slate-900">New Category</h2>
            </div>
            <form onSubmit={handleAddCategory} className="space-y-4">
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">ID (e.g. cate5)</label>
                <input 
                  required 
                  type="text" 
                  value={newCategoryId} 
                  onChange={(e) => setNewCategoryId(e.target.value)} 
                  placeholder="cate..."
                  className="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all" 
                />
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Name (e.g. Interior)</label>
                <input 
                  required 
                  type="text" 
                  value={newCategoryName} 
                  onChange={(e) => setNewCategoryName(e.target.value)} 
                  placeholder="Service name"
                  className="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all" 
                />
              </div>
              <button 
                type="submit" 
                className="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-3 rounded-xl font-bold flex items-center justify-center transition-all shadow-md shadow-blue-100"
              >
                <Plus className="w-5 h-5 mr-2" />
                Create Category
              </button>
            </form>
          </div>
        </div>

        {/* Category List */}
        <div className="md:col-span-2">
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden min-h-[400px] flex flex-col">
            <div className="p-4 border-b border-slate-200 bg-slate-50/50">
              <div className="relative w-full">
                <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input 
                  type="text" 
                  placeholder="Search by name or ID..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 outline-none transition-all bg-white"
                />
              </div>
            </div>
            
            <div className="flex-1 p-6">
              {loading ? (
                <div className="flex flex-col items-center justify-center py-12 space-y-3">
                    <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-slate-500 font-medium">Loading categories...</p>
                </div>
              ) : filteredCategories.length === 0 ? (
                <div className="text-center py-12">
                    <Tag className="w-12 h-12 text-slate-200 mx-auto mb-3" />
                    <p className="text-slate-400 font-medium">No categories matching your search.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 gap-4">
                  {filteredCategories.map((category) => (
                    <div key={category.id} className="flex items-center justify-between p-4 rounded-xl border border-slate-100 bg-slate-50/30 hover:bg-blue-50/50 hover:border-blue-200 transition-all group">
                      <div className="flex items-center">
                        <div className="w-12 h-12 rounded-xl bg-blue-600 text-white flex items-center justify-center mr-4 shadow-lg shadow-blue-100">
                          <Tag className="w-6 h-6" />
                        </div>
                        <div>
                          <p className="font-bold text-slate-900">{category.categoryName}</p>
                          <p className="text-xs font-mono text-slate-500 uppercase tracking-wider">{category.categoryId}</p>
                        </div>
                      </div>
                      <button 
                        onClick={() => handleDelete(category.id)} 
                        className="text-slate-300 hover:text-red-600 p-2 rounded-lg hover:bg-white transition-all shadow-none hover:shadow-sm"
                        title="Delete Category"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}