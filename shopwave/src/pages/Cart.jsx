import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { springApi } from '../api/client';
import { PageHeader, EmptyState, Btn } from '../components/Shared';
import { prodEmoji } from '../components/ProductCard';
import styles from './Cart.module.css';

export default function Cart() {
  const { cart, updateQty, removeItem, clearCart, totalAmount, totalCount } = useCart();
  const { token } = useAuth();
  const navigate  = useNavigate();
  const toast     = useToast();

  if (!token) return (
    <div className="fade-up">
      <PageHeader title="My Cart" />
      <EmptyState icon="○" message="Sign in to view your cart">
        <Btn onClick={() => navigate('/auth')} style={{ marginTop: 16 }}>Sign In</Btn>
      </EmptyState>
    </div>
  );

  const handlePlaceOrder = async () => {
    try {
      await springApi.post('/orders/place');
      await clearCart();
      toast('Order placed!', 'success');
      navigate('/orders');
    } catch (err) {
      toast(err, 'error');
    }
  };

  return (
    <div className="fade-up">
      <PageHeader title="My Cart">
        {cart.length > 0 && (
          <Btn variant="secondary" size="sm" onClick={async () => { await clearCart(); toast('Cart cleared', 'info'); }}>
            Clear all
          </Btn>
        )}
      </PageHeader>

      {cart.length === 0 ? (
        <EmptyState icon="○" message="Your cart is empty">
          <Btn onClick={() => navigate('/products')} style={{ marginTop: 16 }}>Browse Products</Btn>
        </EmptyState>
      ) : (
        <div className={styles.layout}>
          {/* Items */}
          <div className={styles.items}>
            {cart.map(item => (
              <div key={item.id} className={styles.item}>
                <div className={styles.thumb}>{prodEmoji(item.product?.category)}</div>
                <div className={styles.itemInfo}>
                  <div className={styles.itemName}>{item.product?.name}</div>
                  <div className={styles.itemPrice}>
                    ₹{Number(item.product?.price || 0).toLocaleString()} each
                    &nbsp;·&nbsp;
                    <span style={{ color: 'var(--accent)' }}>₹{Number(item.subtotal || 0).toLocaleString()}</span>
                  </div>
                </div>
                <div className={styles.qtyCtrl}>
                  <button className={styles.qtyBtn} onClick={() => updateQty(item.id, item.quantity - 1)}>−</button>
                  <span className={styles.qtyNum}>{item.quantity}</span>
                  <button className={styles.qtyBtn} onClick={() => updateQty(item.id, item.quantity + 1)}>+</button>
                  <button className={`${styles.qtyBtn} ${styles.removeBtn}`} onClick={() => removeItem(item.id)}>✕</button>
                </div>
              </div>
            ))}
          </div>

          {/* Summary */}
          <div className={styles.summary}>
            <h3 className={styles.summaryTitle}>Order Summary</h3>
            <div className={styles.summaryRow}>
              <span>{totalCount} items</span>
              <span>₹{Number(totalAmount).toLocaleString()}</span>
            </div>
            <div className={styles.summaryRow}>
              <span>Delivery</span>
              <span style={{ color: 'var(--green)' }}>Free</span>
            </div>
            <div className={styles.summaryTotal}>
              <span>Total</span>
              <span>₹{Number(totalAmount).toLocaleString()}</span>
            </div>
            <Btn onClick={handlePlaceOrder} style={{ width: '100%', justifyContent: 'center', marginTop: 16 }}>
              Place Order
            </Btn>
          </div>
        </div>
      )}
    </div>
  );
}
