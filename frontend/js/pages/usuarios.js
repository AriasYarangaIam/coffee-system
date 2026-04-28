import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, crearModal } from '../utils/dom.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('usuarios-tbody');
const btnNuevo = document.getElementById('btn-nuevo-usuario');

async function cargarUsuarios() {
  mostrarSpinner(tablaBody);
  try {
    const usuarios = await apiFetch('/admin/usuarios');
    renderizarTabla(usuarios);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar usuarios', 'error');
  }
}

function renderizarTabla(usuarios) {
  if (!usuarios.length) {
    tablaBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">Sin usuarios registrados</td></tr>';
    return;
  }
  tablaBody.innerHTML = usuarios.map(u => `
    <tr>
      <td>${u.nombreUsuario} ${u.apellidoUsuario}</td>
      <td>${u.correoUsuario}</td>
      <td><span class="badge ${u.rol === 'ADMIN' ? 'badge-pagado' : 'badge-pendiente'}">${u.rol}</span></td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="editarUsuario(${u.usuarioId})">Editar</button>
        <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarUsuario(${u.usuarioId}, '${u.nombreUsuario}')">Eliminar</button>
      </td>
    </tr>
  `).join('');
}

async function eliminarUsuario(id, nombre) {
  crearModal({
    titulo: 'Eliminar usuario',
    contenido: `<p>¿Seguro que deseas eliminar a <strong>${nombre}</strong>?</p>`,
    labelConfirm: 'Eliminar',
    onConfirm: async () => {
      try {
        await apiFetch(`/admin/usuarios/${id}`, { method: 'DELETE' });
        mostrarToast('Usuario eliminado', 'success');
        cargarUsuarios();
      } catch (error) {
        mostrarToast(error?.message || 'No se pudo eliminar', 'error');
      }
    },
  });
}

window.editarUsuario = (id) => { /* TODO: abrir modal de edición */ };
window.eliminarUsuario = eliminarUsuario;

btnNuevo.addEventListener('click', () => { /* TODO: abrir modal de creación */ });

cargarUsuarios();
