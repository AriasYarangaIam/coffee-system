export function guardarSesion(token, usuario) {
  localStorage.setItem('jwt_token', token);
  localStorage.setItem('usuario', JSON.stringify(usuario));
}

export function obtenerUsuario() {
  return JSON.parse(localStorage.getItem('usuario') || '{}');
}

export function obtenerToken() {
  return localStorage.getItem('jwt_token');
}

export function logout() {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('usuario');
  window.location.href = '/index.html';
}

export function requireRole(rolRequerido) {
  const usuario = obtenerUsuario();
  const token = obtenerToken();

  if (!token || !usuario.rol) {
    window.location.href = '/index.html';
    return;
  }

  if (usuario.rol !== rolRequerido) {
    if (usuario.rol === 'ADMIN') {
      window.location.href = '/pages/admin/dashboard.html';
    } else {
      window.location.href = '/pages/mesero/pedidos.html';
    }
  }
}
