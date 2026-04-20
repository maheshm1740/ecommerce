import axios from 'axios';

export const springApi = axios.create({ baseURL: 'http://localhost:8080/api' });
export const mlApi     = axios.create({ baseURL: 'http://localhost:8000' });

springApi.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

springApi.interceptors.response.use(
  r => r.data,
  e => Promise.reject(e.response?.data?.message || e.message || 'Request failed')
);

mlApi.interceptors.response.use(
  r => r.data,
  e => Promise.reject(e.response?.data?.detail || e.message || 'ML service error')
);
