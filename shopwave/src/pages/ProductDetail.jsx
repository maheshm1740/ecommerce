import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { springApi } from '../api/client';
import { mlApi } from '../api/client';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import ProductCard from '../components/ProductCard';
import { Spinner, Btn } from '../components/Shared';
import { prodEmoji } from '../components/ProductCard';
import styles from './ProductDetail.module.css';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { token } = useAuth();
  const toast = useToast();

  const [product, setProduct]   = useState(null);
  const [similar, setSimilar]   = useState([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const p = await springApi.get(`/products/${id}`);
        setProduct(p);
      } catch { setProduct(null); }
      try {
        const s = await mlApi.get(`/similar/${id}`);
        setSimilar(s.filter(p => p.id !== Number(id)).slice(0, 4));
      } catch { setSimilar([]); }
      setLoading(false);
    }
    load();
  }, [id]);

  const handleAdd = async () => {
    if (!token) { toast('Sign in to add items to cart', 'error'); navigate('/auth'); return; }
    try {
      await addToCart(product.id);
      toast('Added to cart', 'success');
    } catch (err) { toast(err, 'error'); }
  };

  if (loading) return <Spinner />;
  if (!product) return <p style={{ color: 'var(--text3)' }}>Product not found.</p>;

  const stars = '★'.repeat(Math.round(product.rating || 0)) + '☆'.repeat(5 - Math.round(product.rating || 0));

  return (
    <div className="fade-up">
      <Btn variant="secondary" size="sm" onClick={() => navigate(-1)} style={{ marginBottom: 20 }}>← Back</Btn>

      <div className={styles.grid}>
        {/* Image */}
        <div className={styles.imgBox}>
          {product.imageUrl
            ? <img src={product.imageUrl} alt={product.name} onError={e => { e.target.parentNode.textContent = prodEmoji(product.category); }} />
            : prodEmoji(product.category)}
        </div>

        {/* Info */}
        <div className={styles.info}>
          <span className={styles.tag}>{product.category || 'Uncategorised'}</span>
          <h1 className={styles.name}>{product.name}</h1>
          <div className={styles.stars}>{stars} <span className={styles.reviewCount}>{product.reviewCount || 0} reviews</span></div>
          <div className={styles.price}>₹{Number(product.price).toLocaleString()}</div>
          <p className={styles.desc}>{product.description || 'No description available.'}</p>

          <div className={styles.metaGrid}>
            <div className={styles.metaItem}>
              <div className={styles.metaLabel}>Stock</div>
              <div className={styles.metaVal}>{product.stock} units</div>
            </div>
            <div className={styles.metaItem}>
              <div className={styles.metaLabel}>Rating</div>
              <div className={styles.metaVal}>{product.rating || 0} / 5</div>
            </div>
            <div className={styles.metaItem}>
              <div className={styles.metaLabel}>Category</div>
              <div className={styles.metaVal}>{product.category || '—'}</div>
            </div>
            <div className={styles.metaItem}>
              <div className={styles.metaLabel}>Reviews</div>
              <div className={styles.metaVal}>{product.reviewCount || 0}</div>
            </div>
          </div>

          <Btn onClick={handleAdd} disabled={product.stock === 0} style={{ marginTop: 8 }}>
            {product.stock === 0 ? 'Out of Stock' : 'Add to Cart'}
          </Btn>
        </div>
      </div>

      {similar.length > 0 && (
        <section className={styles.similarSection}>
          <h2 className={styles.similarTitle}>Similar Products</h2>
          <div className={styles.similarGrid}>
            {similar.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}
    </div>
  );
}
