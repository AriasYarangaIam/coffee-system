import { obtenerUsuario, obtenerToken } from './auth.js';

export function redirigirPorRol() {
  const token = obtenerToken();
  const usuario = obtenerUsuario();

  if (!token || !usuario.rol) {
    window.location.href = '/index.html';
    return;
  }

if (usuario.rol === 'ADMINISTRADOR') {        // 👈
    window.location.href = '/pages/admin/dashboard.html';
} else if (usuario.rol === 'MESERO') {        // verifica este también con el backend
    window.location.href = '/pages/mesero/pedidos.html';
} else {
    window.location.href = '/index.html';
}
}
