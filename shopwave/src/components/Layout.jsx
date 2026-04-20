import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import styles from './Layout.module.css';

export default function Layout({ children }) {
  const { user, logout, isAdmin } = useAuth();
  const { totalCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [search, setSearch] = useState('');

  const navItems = [
    { path: '/',        label: 'Home',     icon: '⌂' },
    { path: '/products',label: 'Products', icon: '◫' },
    { path: '/cart',    label: 'Cart',     icon: '○' },
    { path: '/orders',  label: 'Orders',   icon: '≡' },
  ];

  const handleSearch = (e) => {
    e.preventDefault();
    if (search.trim()) navigate(`/products?q=${encodeURIComponent(search.trim())}`);
  };

  return (
    <div className={styles.shell}>
      {/* Topbar */}
      <header className={styles.topbar}>
        <Link to="/" className={styles.logo}>ShopWave</Link>
        <form className={styles.searchWrap} onSubmit={handleSearch}>
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search products…"
            className={styles.searchInput}
          />
          <button type="submit" className={styles.searchBtn}>⌕</button>
        </form>
        <button className={styles.cartBtn} onClick={() => navigate('/cart')}>
          🛒 Cart <span className={styles.badge}>{totalCount}</span>
        </button>
        {user ? (
          <div className={styles.userRow}>
            <span className={styles.userName}>{user.name || user.email}</span>
            <button className={styles.signOutBtn} onClick={() => { logout(); navigate('/'); }}>Sign out</button>
          </div>
        ) : (
          <button className={styles.signInBtn} onClick={() => navigate('/auth')}>Sign In</button>
        )}
      </header>

      {/* Sidebar */}
      <nav className={styles.sidebar}>
        <div className={styles.navSection}>Shop</div>
        {navItems.map(item => (
          <Link
            key={item.path}
            to={item.path}
            className={`${styles.navItem} ${location.pathname === item.path ? styles.active : ''}`}
          >
            <span className={styles.navIcon}>{item.icon}</span>
            {item.label}
          </Link>
        ))}
        {isAdmin && (
          <>
            <div className={styles.navSection}>Admin</div>
            <Link
              to="/add-product"
              className={`${styles.navItem} ${location.pathname === '/add-product' ? styles.active : ''}`}
            >
              <span className={styles.navIcon}>+</span> Add Product
            </Link>
          </>
        )}
        <div className={styles.navSection}>Account</div>
        {!user && (
          <Link to="/auth" className={`${styles.navItem} ${location.pathname === '/auth' ? styles.active : ''}`}>
            <span className={styles.navIcon}>→</span> Sign In
          </Link>
        )}
      </nav>

      {/* Main */}
      <main className={styles.main}>
        {children}
      </main>
    </div>
  );
}
