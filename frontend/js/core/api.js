// Cliente HTTP central: toda llamada al backend pasa por apiFetch (agrega el JWT, maneja 401/403/errores).
import { logout } from './auth.js';

const BASE_URL = 'http://localhost:8080/api';

export async function apiFetch(endpoint, options = {}) {
  const token = localStorage.getItem('jwt_token');

  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    logout();
    return;
  }

  if (response.status === 403) {
    // Autenticado pero sin permiso para el recurso: mensaje claro (B-F-13).
    throw { message: 'No tienes permiso para esta acción' };
  }

  if (response.status === 204) return null;

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Error del servidor' }));
    throw error;
  }

  return response.json();
}