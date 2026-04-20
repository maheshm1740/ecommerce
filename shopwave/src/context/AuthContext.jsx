import { createContext, useContext, useState, useCallback } from 'react';
import { springApi } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user,  setUser]  = useState(() => JSON.parse(localStorage.getItem('user') || 'null'));
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  const persist = (res) => {
    // Backend returns { token, email, name } — use those directly
    const u = {
      email: res.email || '',
      name:  res.name  || '',
      role:  res.role  || 'USER',
    };
    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify(u));
    setToken(res.token);
    setUser(u);
    return u;
  };

  const login = useCallback(async (email, password) => {
    const res = await springApi.post('/auth/login', { email, password });
    return persist(res);
  }, []);

  const register = useCallback(async (name, email, password) => {
    const res = await springApi.post('/auth/register', { name, email, password });
    return persist(res);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{
      user,
      token,
      login,
      register,
      logout,
      isAdmin: user?.role === 'ADMIN',
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);