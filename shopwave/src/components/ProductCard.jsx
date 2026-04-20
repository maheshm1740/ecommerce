import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import styles from './ProductCard.module.css';

const EMOJI = { Electronics:'💻', Fashion:'👗', Books:'📚', Beauty:'💄', Sports:'⚽', Home:'🏠', Food:'🍎', Toys:'🎮' };
export const prodEmoji = (cat) => EMOJI[cat] || '📦';

export default function ProductCard({ product: p }) {
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { token } = useAuth();
  const toast = useToast();

  const stars = '★'.repeat(Math.round(p.rating || 0)) + '☆'.repeat(5 - Math.round(p.rating || 0));

  const handleAdd = async (e) => {
    e.stopPropagation();
    if (!token) { toast('Sign in to add items to cart', 'error'); navigate('/auth'); return; }
    try {
      await addToCart(p.id);
      toast('Added to cart', 'success');
    } catch (err) {
      toast(err, 'error');
    }
  };

  return (
    <div className={styles.card} onClick={() => navigate(`/products/${p.id}`)}>
      <div className={styles.img}>
        {(p.imageUrl || p.image_url)
          ? <img src={p.imageUrl || p.image_url} alt={p.name} onError={e => { e.target.parentNode.textContent = prodEmoji(p.category); }} />
          : prodEmoji(p.category)}
      </div>
      <div className={styles.info}>
        <div className={styles.cat}>{p.category || ''}</div>
        <div className={styles.name}>{p.name}</div>
        <div className={styles.stars}>
          {stars} <span className={styles.reviewCount}>({p.reviewCount || p.review_count || 0})</span>
        </div>
        <div className={styles.row}>
          <div className={styles.price}>₹{Number(p.price).toLocaleString()}</div>
          <button className={styles.addBtn} onClick={handleAdd} disabled={p.stock === 0}>
            {p.stock === 0 ? 'Out' : '+ Add'}
          </button>
        </div>
      </div>
    </div>
  );
}
