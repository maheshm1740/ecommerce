import { useEffect, useState } from 'react';
import { mlApi } from '../api/client';
import { springApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import ProductCard from '../components/ProductCard';
import { Spinner, PageHeader } from '../components/Shared';
import styles from './Home.module.css';

export default function Home() {
  const { user } = useAuth();
  const [trending, setTrending]   = useState([]);
  const [recs, setRecs]           = useState(null);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const data = await mlApi.get('/trending');
        setTrending(data);
      } catch {
        try {
          const data = await springApi.get('/products');
          setTrending(data.slice(0, 6));
        } catch { setTrending([]); }
      }
      if (user?.id) {
        try {
          const data = await mlApi.get(`/recommendations/${user.id}`);
          setRecs(data);
        } catch { setRecs(null); }
      }
      setLoading(false);
    }
    load();
  }, [user]);

  return (
    <div className="fade-up">
      <PageHeader title="Discover" sub="Curated picks and trending items" />

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>🔥 Trending Now</h2>
        {loading ? <Spinner /> : (
          trending.length
            ? <div className={styles.grid}>{trending.map(p => <ProductCard key={p.id} product={p} />)}</div>
            : <p className={styles.empty}>Could not reach backend. Make sure it's running on port 8080.</p>
        )}
      </section>

      {recs && (
        <section className={`${styles.section} fade-up-2`} style={{ marginTop: 48 }}>
          <h2 className={styles.sectionTitle}>✦ For You</h2>
          <p className={styles.recReason}>{recs.reason}</p>
          <div className={styles.grid}>
            {recs.recommendations.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}
    </div>
  );
}
