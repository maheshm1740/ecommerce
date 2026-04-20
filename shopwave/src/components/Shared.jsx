import styles from './Shared.module.css';

export function Spinner() {
  return <div className={styles.spinnerWrap}><div className={styles.spinner} /></div>;
}

export function PageHeader({ title, sub, children }) {
  return (
    <div className={styles.pageHeader}>
      <div>
        <h1 className={styles.pageTitle}>{title}</h1>
        {sub && <p className={styles.pageSub}>{sub}</p>}
      </div>
      {children}
    </div>
  );
}

export function EmptyState({ icon, message, children }) {
  return (
    <div className={styles.empty}>
      <div className={styles.emptyIcon}>{icon}</div>
      <p>{message}</p>
      {children}
    </div>
  );
}

export function Btn({ children, variant = 'primary', size, onClick, disabled, type = 'button', style }) {
  return (
    <button
      type={type}
      className={`${styles.btn} ${styles[variant]} ${size ? styles[size] : ''}`}
      onClick={onClick}
      disabled={disabled}
      style={style}
    >
      {children}
    </button>
  );
}
