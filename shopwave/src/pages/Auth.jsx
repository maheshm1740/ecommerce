import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Btn } from '../components/Shared';
import styles from './Auth.module.css';

export default function Auth() {
  const [isLogin, setIsLogin] = useState(true);
  const [form, setForm]       = useState({ name: '', email: '', password: '' });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register }   = useAuth();
  const navigate              = useNavigate();
  const toast                 = useToast();

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      if (isLogin) {
        await login(form.email, form.password);
        toast('Signed in successfully', 'success');
      } else {
        await register(form.name, form.email, form.password);
        toast('Account created!', 'success');
      }
      navigate('/');
    } catch (err) {
      setError(typeof err === 'string' ? err : 'Something went wrong');
    }
    setLoading(false);
  };

  return (
    <div className={styles.wrap}>
      <div className={styles.card}>
        <div className={styles.logoMark}>✦</div>
        <h2 className={styles.title}>{isLogin ? 'Sign In' : 'Create Account'}</h2>
        <p className={styles.sub}>{isLogin ? 'Welcome back to ShopWave' : 'Join ShopWave today'}</p>

        <form onSubmit={handleSubmit} className={styles.form}>
          {!isLogin && (
            <div className={styles.field}>
              <label>Name</label>
              <input type="text" value={form.name} onChange={e => set('name', e.target.value)} placeholder="Your name" required />
            </div>
          )}
          <div className={styles.field}>
            <label>Email</label>
            <input type="email" value={form.email} onChange={e => set('email', e.target.value)} placeholder="you@example.com" required />
          </div>
          <div className={styles.field}>
            <label>Password</label>
            <input type="password" value={form.password} onChange={e => set('password', e.target.value)} placeholder="••••••••" required />
          </div>
          {error && <p className={styles.error}>{error}</p>}
          <Btn type="submit" disabled={loading} style={{ width: '100%', justifyContent: 'center', marginTop: 4 }}>
            {loading ? 'Please wait…' : isLogin ? 'Sign In' : 'Create Account'}
          </Btn>
        </form>

        <p className={styles.switch}>
          {isLogin ? "No account? " : "Have an account? "}
          <span onClick={() => { setIsLogin(!isLogin); setError(''); }}>{isLogin ? 'Create one' : 'Sign in'}</span>
        </p>
      </div>
    </div>
  );
}
