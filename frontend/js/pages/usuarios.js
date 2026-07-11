// Gestion de usuarios (admin): crear/editar/eliminar, con guardas (no auto-eliminarse).
import { requireRole, obtenerUsuario } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, escaparHtml } from '../utils/dom.js';

requireRole('ADMIN');

const correoActual = obtenerUsuario().correo;

const tablaBody = document.getElementById('usuarios-tbody');
const btnNuevo = document.getElementById('btn-nuevo-usuario');

// Modal crear/editar
const modalUsuario = document.getElementById('modal-usuario');
const modalTitulo = document.getElementById('modal-usuario-titulo');
const formUsuario = document.getElementById('form-usuario');
const inputId = document.getElementById('usuario-id');
const inputNombre = document.getElementById('usuario-nombre');
const inputApellido = document.getElementById('usuario-apellido');
const inputCorreo = document.getElementById('usuario-correo');
const inputRol = document.getElementById('usuario-rol');
const inputClave = document.getElementById('usuario-clave');
const hintClave = document.getElementById('hint-clave');

// Modal eliminar
const modalEliminar = document.getElementById('modal-eliminar-usuario');
const spanNombreEliminar = document.getElementById('nombre-usuario-eliminar');
const btnConfirmarEliminar = document.getElementById('btn-confirmar-eliminar-usuario');

let usuariosCache = [];
let idAEliminar = null;

// ── Cargar ────────────────────────────────────────────────────────────────
async function cargarUsuarios() {
  mostrarSpinner(tablaBody);
  try {
    usuariosCache = await apiFetch('/admin/usuarios');
    renderizarTabla(usuariosCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar usuarios', 'error');
  }
}

function renderizarTabla(usuarios) {
  if (!usuarios.length) {
    tablaBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">Sin usuarios registrados</td></tr>';
    return;
  }
  tablaBody.innerHTML = usuarios.map(u => {
    const esYo = u.correoUsuario === correoActual;
    // En la propia fila no se ofrece eliminar (el backend igual lo rechaza con 409).
    const accionEliminar = esYo
      ? '<span class="badge badge-pagado" style="margin-left:4px">Tú</span>'
      : `<button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarUsuario(${u.usuarioId})">Eliminar</button>`;
    return `
    <tr>
      <td>${escaparHtml(u.nombreUsuario)} ${escaparHtml(u.apellidoUsuario)}</td>
      <td>${escaparHtml(u.correoUsuario)}</td>
      <td><span class="badge ${u.rol === 'ADMIN' ? 'badge-pagado' : 'badge-pendiente'}">${escaparHtml(u.rol)}</span></td>
      <td style="text-align:right">
        <button class="btn btn-outline btn-sm" onclick="editarUsuario(${u.usuarioId})">Editar</button>
        ${accionEliminar}
      </td>
    </tr>
  `;
  }).join('');
}

// ── Modal crear/editar ─────────────────────────────────────────────────────
function abrirModalCrear() {
  modalTitulo.textContent = 'Nuevo Usuario';
  inputId.value = '';
  formUsuario.reset();
  inputClave.required = true;
  hintClave.style.display = 'none';
  modalUsuario.classList.remove('hidden');
}

function abrirModalEditar(id) {
  const usuario = usuariosCache.find(u => u.usuarioId === id);
  if (!usuario) return;
  modalTitulo.textContent = 'Editar Usuario';
  inputId.value = usuario.usuarioId;
  inputNombre.value = usuario.nombreUsuario;
  inputApellido.value = usuario.apellidoUsuario;
  inputCorreo.value = usuario.correoUsuario;
  inputRol.value = usuario.rol;
  inputClave.value = '';
  inputClave.required = false;
  hintClave.style.display = 'block';
  modalUsuario.classList.remove('hidden');
}

function cerrarModalUsuario() {
  modalUsuario.classList.add('hidden');
}

// ── Guardar (crear o editar) ───────────────────────────────────────────────
formUsuario.addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = formUsuario.querySelector('button[type="submit"]');
  btn.disabled = true;

  const id = inputId.value;
  const body = {
    nombreUsuario: inputNombre.value.trim(),
    apellidoUsuario: inputApellido.value.trim(),
    correoUsuario: inputCorreo.value.trim(),
    rol: inputRol.value,
    ...(inputClave.value && { claveUsuario: inputClave.value }),
  };

  try {
    if (id) {
      await apiFetch(`/admin/usuarios/${id}`, { method: 'PUT', body: JSON.stringify(body) });
      mostrarToast('Usuario actualizado', 'success');
    } else {
      await apiFetch('/admin/usuarios', { method: 'POST', body: JSON.stringify(body) });
      mostrarToast('Usuario creado', 'success');
    }
    cerrarModalUsuario();
    cargarUsuarios();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo guardar el usuario', 'error');
  } finally {
    btn.disabled = false;
  }
});

// ── Eliminar ───────────────────────────────────────────────────────────────
function abrirModalEliminar(id, nombre) {
  idAEliminar = id;
  spanNombreEliminar.textContent = nombre;
  modalEliminar.classList.remove('hidden');
}

function cerrarModalEliminar() {
  modalEliminar.classList.add('hidden');
  idAEliminar = null;
}

btnConfirmarEliminar.addEventListener('click', async () => {
  if (!idAEliminar) return;
  try {
    await apiFetch(`/admin/usuarios/${idAEliminar}`, { method: 'DELETE' });
    mostrarToast('Usuario eliminado', 'success');
    cerrarModalEliminar();
    cargarUsuarios();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo eliminar', 'error');
  }
});

// ── Eventos ────────────────────────────────────────────────────────────────
btnNuevo.addEventListener('click', abrirModalCrear);
document.getElementById('modal-usuario-cerrar').addEventListener('click', cerrarModalUsuario);
document.getElementById('btn-cancelar-usuario').addEventListener('click', cerrarModalUsuario);
document.getElementById('modal-eliminar-usuario-cerrar').addEventListener('click', cerrarModalEliminar);
document.getElementById('btn-cancelar-eliminar-usuario').addEventListener('click', cerrarModalEliminar);

// Filtro por nombre, apellido o correo.
const inputBuscar = document.getElementById('input-buscar-usuario');
inputBuscar?.addEventListener('input', () => {
  const q = inputBuscar.value.trim().toLowerCase();
  renderizarTabla(usuariosCache.filter(u =>
    `${u.nombreUsuario} ${u.apellidoUsuario} ${u.correoUsuario}`.toLowerCase().includes(q)));
});

window.editarUsuario = (id) => abrirModalEditar(id);
// El nombre se busca en la caché (no viaja en el onclick) para no inyectar datos del
// servidor en un contexto HTML/JS: un nombre con comilla rompería el string del onclick.
window.eliminarUsuario = (id) => {
  const u = usuariosCache.find(x => x.usuarioId === id);
  if (u) abrirModalEliminar(id, u.nombreUsuario);
};

cargarUsuarios();