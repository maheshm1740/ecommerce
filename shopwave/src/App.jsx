import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { ToastProvider } from './context/ToastContext';
import Layout from './components/Layout';
import Home         from './pages/Home';
import Products     from './pages/Products';
import ProductDetail from './pages/ProductDetail';
import Cart         from './pages/Cart';
import Orders       from './pages/Orders';
import Auth         from './pages/Auth';
import AddProduct   from './pages/AddProduct';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <ToastProvider>
            <Layout>
              <Routes>
                <Route path="/"              element={<Home />} />
                <Route path="/products"      element={<Products />} />
                <Route path="/products/:id"  element={<ProductDetail />} />
                <Route path="/cart"          element={<Cart />} />
                <Route path="/orders"        element={<Orders />} />
                <Route path="/auth"          element={<Auth />} />
                <Route path="/add-product"   element={<AddProduct />} />
              </Routes>
            </Layout>
          </ToastProvider>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
