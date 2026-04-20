import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { springApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Spinner, PageHeader, EmptyState, Btn } from '../components/Shared';
import { prodEmoji } from '../components/ProductCard';
import styles from './Orders.module.css';

const statusClass = { DELIVERED: 'delivered', CANCELLED: 'cancelled', PENDING: 'pending', PROCESSING: 'pending' };

export default function Orders() {
  const { token } = useAuth();
  const navigate  = useNavigate();
  const [orders, setOrders]   = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) { setLoading(false); return; }
    springApi.get('/orders')
      .then(setOrders)
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, [token]);

  if (!token) return (
    <div className="fade-up">
      <PageHeader title="My Orders" />
      <EmptyState icon="≡" message="Sign in to view your orders">
        <Btn onClick={() => navigate('/auth')} style={{ marginTop: 16 }}>Sign In</Btn>
      </EmptyState>
    </div>
  );

  return (
    <div className="fade-up">
      <PageHeader title="My Orders" sub={`${orders.length} orders`} />

      {loading ? <Spinner /> : orders.length === 0 ? (
        <EmptyState icon="≡" message="No orders yet">
          <Btn onClick={() => navigate('/products')} style={{ marginTop: 16 }}>Start Shopping</Btn>
        </EmptyState>
      ) : (
        <div className={styles.list}>
          {orders.map(o => {
            const sc = statusClass[o.status] || 'pending';
            return (
              <div key={o.id} className={styles.card}>
                <div className={styles.header}>
                  <span className={styles.orderId}>Order #{o.id}</span>
                  <span className={`${styles.badge} ${styles[sc]}`}>{o.status || 'PENDING'}</span>
                </div>

                <div className={styles.products}>
                  {(o.products || []).map((p, i) => (
                    <div key={i} className={styles.prodThumb} title={p.name}>
                      {prodEmoji(p.category)}
                    </div>
                  ))}
                </div>

                <div className={styles.prodNames}>
                  {(o.products || []).map(p => p.name).join(', ')}
                </div>

                <div className={styles.footer}>
                  <span className={styles.date}>
                    {o.createdAt
                      ? new Date(o.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
                      : '—'}
                  </span>
                  <span className={styles.total}>₹{Number(o.totalAmount || 0).toLocaleString()}</span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
