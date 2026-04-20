import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { springApi } from '../api/client';
import ProductCard from '../components/ProductCard';
import { Spinner, PageHeader, EmptyState } from '../components/Shared';
import styles from './Products.module.css';

export default function Products() {
  const [searchParams] = useSearchParams();
  const q   = searchParams.get('q') || '';
  const [products, setProducts] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [activecat, setActiveCat] = useState('');
  const [cats, setCats] = useState([]);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        let data;
        if (q) {
          data = await springApi.get(`/products/search?q=${encodeURIComponent(q)}`);
        } else if (activecat) {
          data = await springApi.get(`/products/category/${encodeURIComponent(activecat)}`);
        } else {
          data = await springApi.get('/products');
        }
        setProducts(data);
        if (!q && !activecat) {
          const unique = [...new Set(data.map(p => p.category).filter(Boolean))];
          setCats(unique);
        }
      } catch { setProducts([]); }
      setLoading(false);
    }
    load();
  }, [q, activecat]);

  const handleCat = (cat) => { setActiveCat(cat); };

  return (
    <div className="fade-up">
      <PageHeader title={q ? `Results for "${q}"` : 'All Products'} sub={`${products.length} products`} />

      {!q && cats.length > 0 && (
        <div className={styles.filterBar}>
          <button className={`${styles.pill} ${!activecat ? styles.active : ''}`} onClick={() => handleCat('')}>All</button>
          {cats.map(c => (
            <button key={c} className={`${styles.pill} ${activecat === c ? styles.active : ''}`} onClick={() => handleCat(c)}>{c}</button>
          ))}
        </div>
      )}

      {loading ? <Spinner /> : products.length
        ? <div className={styles.grid}>{products.map(p => <ProductCard key={p.id} product={p} />)}</div>
        : <EmptyState icon="◻" message="No products found" />
      }
    </div>
  );
}
