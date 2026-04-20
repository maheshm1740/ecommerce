import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { springApi } from '../api/client';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { token } = useAuth();
  const [cart, setCart] = useState([]);

  const fetchCart = useCallback(async () => {
    if (!token) { setCart([]); return; }
    try { setCart(await springApi.get('/cart')); } catch { setCart([]); }
  }, [token]);

  useEffect(() => { fetchCart(); }, [fetchCart]);

  const addToCart = async (productId, quantity = 1) => {
    await springApi.post('/cart/add', { productId, quantity });
    await fetchCart();
  };

  const updateQty = async (cartItemId, quantity) => {
    if (quantity < 1) { await removeItem(cartItemId); return; }
    await springApi.put(`/cart/${cartItemId}`, { quantity });
    await fetchCart();
  };

  const removeItem = async (cartItemId) => {
    await springApi.delete(`/cart/${cartItemId}`);
    await fetchCart();
  };

  const clearCart = async () => {
    await springApi.delete('/cart/clear');
    await fetchCart();
  };

  const totalCount = cart.reduce((s, i) => s + (i.quantity || 0), 0);
  const totalAmount = cart.reduce((s, i) => s + (i.subtotal || 0), 0);

  return (
    <CartContext.Provider value={{ cart, addToCart, updateQty, removeItem, clearCart, fetchCart, totalCount, totalAmount }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
