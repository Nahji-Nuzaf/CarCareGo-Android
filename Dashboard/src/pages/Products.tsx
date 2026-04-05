import React, { useState, useEffect } from 'react';
import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, onSnapshot } from 'firebase/firestore';
import { db } from '../firebase';
import { Plus, Edit2, Trash2, Search, Image as ImageIcon, ChevronDown } from 'lucide-react';
import { toast } from 'sonner';

interface Product {
  id: string;
  title: string;
  price: number;
  images: string[];
  categoryId: string;
  description: string;
  status: boolean;
  stockCount: number;
}

interface Category {
  id: string;
  categoryId: string;
  categoryName: string;
}

export default function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]); // New state for dropdown
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  
  const [formData, setFormData] = useState({
    title: '',
    price: '',
    imageUrl: '',
    categoryId: '',
    description: ''
  });

  // Fetch Categories for the dropdown
  useEffect(() => {
    if (!db) return;
    const unsubscribe = onSnapshot(collection(db, 'categories'), (snapshot) => {
      const cats = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Category));
      setCategories(cats);
    });
    return () => unsubscribe();
  }, []);

  const fetchProducts = async () => {
    if (!db) {
      setLoading(false);
      return;
    }
    try {
      const querySnapshot = await getDocs(collection(db, 'products'));
      const data = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Product));
      setProducts(data);
    } catch (error) {
      console.error("Error fetching products:", error);
      toast.error("Failed to load products");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!db) return toast.error("Firebase not configured");

    // --- VALIDATION: Prevent Negative Price ---
    const priceNum = Number(formData.price);
    if (priceNum < 0) {
      return toast.error("Price cannot be a negative value");
    }

    if (!formData.categoryId) {
      return toast.error("Please select a category");
    }
    
    try {
      const productData = {
        title: formData.title,
        price: priceNum,
        images: [formData.imageUrl],
        categoryId: formData.categoryId,
        description: formData.description,
        status: true,
        stockCount: 5 
      };

      if (editingProduct) {
        await updateDoc(doc(db, 'products', editingProduct.id), productData);
        toast.success("Product updated successfully");
      } else {
        await addDoc(collection(db, 'products'), productData);
        toast.success("Product added successfully");
      }
      
      setIsModalOpen(false);
      setEditingProduct(null);
      setFormData({ title: '', price: '', imageUrl: '', categoryId: '', description: '' });
      fetchProducts();
    } catch (error) {
      console.error("Error saving product:", error);
      toast.error("Failed to save product");
    }
  };

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    setFormData({
      title: product.title,
      price: product.price.toString(),
      imageUrl: product.images && product.images.length > 0 ? product.images[0] : '',
      categoryId: product.categoryId,
      description: product.description
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!db) return;
    if (window.confirm("Are you sure you want to delete this product?")) {
      try {
        await deleteDoc(doc(db, 'products', id));
        toast.success("Product deleted successfully");
        fetchProducts();
      } catch (error) {
        toast.error("Failed to delete product");
      }
    }
  };

  const filteredProducts = products.filter(p => 
    p.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.categoryId?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">Product Management</h1>
        <button 
          onClick={() => {
            setEditingProduct(null);
            setFormData({ title: '', price: '', imageUrl: '', categoryId: '', description: '' });
            setIsModalOpen(true);
          }}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium flex items-center transition-colors shadow-sm"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add New Product
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50">
          <div className="relative w-72">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input 
              type="text" 
              placeholder="Search products..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-slate-300 rounded-md text-sm focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600 font-medium border-b border-slate-200 uppercase tracking-wider text-xs">
              <tr>
                <th className="px-6 py-4">Image</th>
                <th className="px-6 py-4">Title</th>
                <th className="px-6 py-4">Category</th>
                <th className="px-6 py-4">Price</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {loading ? (
                <tr><td colSpan={5} className="px-6 py-12 text-center text-slate-500">Loading your inventory...</td></tr>
              ) : filteredProducts.length === 0 ? (
                <tr><td colSpan={5} className="px-6 py-12 text-center text-slate-500">No products found.</td></tr>
              ) : (
                filteredProducts.map((product) => (
                  <tr key={product.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      {product.images && product.images.length > 0 ? (
                        <img src={product.images[0]} alt={product.title} className="w-12 h-12 rounded-lg object-cover border border-slate-200 shadow-sm" />
                      ) : (
                        <div className="w-12 h-12 rounded-lg bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-400">
                          <ImageIcon className="w-5 h-5" />
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 font-semibold text-slate-900">{product.title}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100">
                        {product.categoryId || 'Uncategorized'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-900 font-bold">LKR {Number(product.price).toLocaleString()}</td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex justify-end space-x-1">
                        <button onClick={() => handleEdit(product)} className="text-slate-500 hover:text-blue-600 p-2 rounded-full hover:bg-blue-50 transition-colors">
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button onClick={() => handleDelete(product.id)} className="text-slate-500 hover:text-red-600 p-2 rounded-full hover:bg-red-50 transition-colors">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-200 flex justify-between items-center bg-slate-50">
              <h2 className="text-lg font-bold text-slate-900">
                {editingProduct ? 'Edit Product' : 'Add New Product'}
              </h2>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 text-2xl leading-none">
                &times;
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Product Title</label>
                <input required type="text" name="title" value={formData.title} onChange={handleInputChange} className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" placeholder="e.g. Tire Shine" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-slate-700 mb-1">Price (LKR)</label>
                  <input 
                    required 
                    type="number" 
                    min="0" // HTML level validation
                    name="price" 
                    value={formData.price} 
                    onChange={handleInputChange} 
                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" 
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-slate-700 mb-1">Category</label>
                  <div className="relative">
                    {/* --- CATEGORY DROPDOWN --- */}
                    <select 
                      required 
                      name="categoryId" 
                      value={formData.categoryId} 
                      onChange={handleInputChange} 
                      className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none appearance-none bg-white pr-10"
                    >
                      <option value="" disabled>Select Category</option>
                      {categories.map((cat) => (
                        <option key={cat.id} value={cat.categoryName}>
                          {cat.categoryName}
                        </option>
                      ))}
                    </select>
                    <ChevronDown className="w-4 h-4 absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                  </div>
                </div>
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Image URL</label>
                <input required type="url" name="imageUrl" value={formData.imageUrl} onChange={handleInputChange} className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" placeholder="https://..." />
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-1">Description</label>
                <textarea rows={3} name="description" value={formData.description} onChange={handleInputChange} className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Product details..."></textarea>
              </div>
              <div className="pt-4 flex justify-end space-x-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-6 py-2 text-sm font-bold text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200 transition-colors">
                  Cancel
                </button>
                <button type="submit" className="px-6 py-2 text-sm font-bold text-white bg-blue-600 rounded-lg hover:bg-blue-700 shadow-md shadow-blue-200 transition-colors">
                  {editingProduct ? 'Update Product' : 'Create Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}