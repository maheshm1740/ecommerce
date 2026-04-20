import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { springApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { PageHeader, Btn, EmptyState } from '../components/Shared';
import styles from './AddProduct.module.css';

export default function AddProduct() {
  const { isAdmin } = useAuth();
  const navigate    = useNavigate();
  const toast       = useToast();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ name: '', description: '', category: '', price: '', stock: '', imageUrl: '' });

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  if (!isAdmin) return (
    <EmptyState icon="✕" message="Admin access required">
      <Btn onClick={() => navigate('/')} style={{ marginTop: 16 }}>Go Home</Btn>
    </EmptyState>
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name || !form.price) { toast('Name and price are required', 'error'); return; }
    setLoading(true);
    try {
      await springApi.post('/products', {
        name: form.name,
        description: form.description,
        category: form.category,
        price: parseFloat(form.price),
        stock: parseInt(form.stock) || 0,
        imageUrl: form.imageUrl,
      });
      toast('Product created!', 'success');
      setForm({ name: '', description: '', category: '', price: '', stock: '', imageUrl: '' });
    } catch (err) {
      toast(err, 'error');
    }
    setLoading(false);
  };

  return (
    <div className="fade-up">
      <PageHeader title="Add Product" sub="Create a new product listing" />
      <div className={styles.formWrap}>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.row}>
            <div className={styles.field}>
              <label>Product Name *</label>
              <input value={form.name} onChange={e => set('name', e.target.value)} placeholder="e.g. Wireless Earbuds Pro" required />
            </div>
            <div className={styles.field}>
              <label>Category</label>
              <input value={form.category} onChange={e => set('category', e.target.value)} placeholder="e.g. Electronics" />
            </div>
          </div>
          <div className={styles.field}>
            <label>Description</label>
            <input value={form.description} onChange={e => set('description', e.target.value)} placeholder="Short product description" />
          </div>
          <div className={styles.row}>
            <div className={styles.field}>
              <label>Price (₹) *</label>
              <input type="number" value={form.price} onChange={e => set('price', e.target.value)} placeholder="1999" required min="0" />
            </div>
            <div className={styles.field}>
              <label>Stock</label>
              <input type="number" value={form.stock} onChange={e => set('stock', e.target.value)} placeholder="100" min="0" />
            </div>
          </div>
          <div className={styles.field}>
            <label>Image URL</label>
            <input value={form.imageUrl} onChange={e => set('imageUrl', e.target.value)} placeholder="https://…" />
          </div>
          <div className={styles.actions}>
            <Btn type="submit" disabled={loading}>{loading ? 'Creating…' : 'Create Product'}</Btn>
            <Btn variant="secondary" type="button" onClick={() => navigate('/products')}>View Products</Btn>
          </div>
        </form>

        {/* Preview */}
        {form.name && (
          <div className={styles.preview}>
            <p className={styles.previewLabel}>Preview</p>
            <div className={styles.previewCard}>
              <div className={styles.previewImg}>
                {form.imageUrl ? <img src={form.imageUrl} alt="" onError={e => { e.target.style.display='none'; }} /> : '📦'}
              </div>
              <div className={styles.previewInfo}>
                <div className={styles.previewCat}>{form.category || 'Uncategorised'}</div>
                <div className={styles.previewName}>{form.name}</div>
                {form.price && <div className={styles.previewPrice}>₹{Number(form.price).toLocaleString()}</div>}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
