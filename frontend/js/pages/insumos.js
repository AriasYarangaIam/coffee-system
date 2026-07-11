import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, escaparHtml } from '../utils/dom.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('insumos-tbody');
const btnNuevo = document.getElementById('btn-nuevo-insumo');

// Modal crear/editar
const modalInsumo = document.getElementById('modal-insumo');
const modalTitulo = document.getElementById('modal-insumo-titulo');
const formInsumo = document.getElementById('form-insumo');
const inputId = document.getElementById('insumo-id');
const inputNombre = document.getElementById('insumo-nombre');
const inputUnidad = document.getElementById('insumo-unidad');
const hintUnidad = document.getElementById('hint-unidad');

// Modal eliminar
const modalEliminar = document.getElementById('modal-eliminar-insumo');
const spanNombreEliminar = document.getElementById('nombre-insumo-eliminar');
const btnConfirmarEliminar = document.getElementById('btn-confirmar-eliminar-insumo');

let insumosCache = [];
let idAEliminar = null;

// ── Cargar ────────────────────────────────────────────────────────────────
async function cargarInsumos() {
  mostrarSpinner(tablaBody);
  try {
    insumosCache = await apiFetch('/admin/insumos');
    renderizarTabla(insumosCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar insumos', 'error');
  }
}

function renderizarTabla(insumos) {
  if (!insumos.length) {
    tablaBody.innerHTML = '<tr><td colspan="3" class="text-center text-muted" style="padding:24px">Sin insumos registrados</td></tr>';
    return;
  }
  tablaBody.innerHTML = insumos.map(i => `
    <tr>
      <td>${escaparHtml(i.nombreInsumo)}</td>
      <td>${escaparHtml(i.unidad ?? '—')}</td>
      <td style="text-align:right">
        <button class="btn btn-outline btn-sm" onclick="editarInsumo(${i.idInsumo})">Editar</button>
        <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarInsumo(${i.idInsumo}, '${escaparHtml(i.nombreInsumo)}')">Eliminar</button>
      </td>
    </tr>
  `).join('');
}

// ── Modal crear/editar ─────────────────────────────────────────────────────
function abrirModalCrear() {
  modalTitulo.textContent = 'Nuevo Insumo';
  inputId.value = '';
  formInsumo.reset();
  inputUnidad.disabled = false; // al crear, la unidad siempre se puede elegir
  hintUnidad.style.display = 'none';
  modalInsumo.classList.remove('hidden');
}

function abrirModalEditar(id) {
  const insumo = insumosCache.find(i => i.idInsumo === id);
  if (!insumo) return;
  modalTitulo.textContent = 'Editar Insumo';
  inputId.value = insumo.idInsumo;
  inputNombre.value = insumo.nombreInsumo;
  inputUnidad.value = insumo.unidad ?? '';
  // Si el insumo ya tiene stock o está en una receta, la unidad queda bloqueada.
  inputUnidad.disabled = insumo.unidadEditable === false;
  hintUnidad.style.display = insumo.unidadEditable === false ? 'block' : 'none';
  modalInsumo.classList.remove('hidden');
}

function cerrarModalInsumo() {
  modalInsumo.classList.add('hidden');
}

// ── Guardar (crear o editar) ───────────────────────────────────────────────
formInsumo.addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = formInsumo.querySelector('button[type="submit"]');
  btn.disabled = true;

  const id = inputId.value;
  const body = { nombreInsumo: inputNombre.value.trim(), unidad: inputUnidad.value };

  try {
    if (id) {
      await apiFetch(`/admin/insumos/${id}`, { method: 'PUT', body: JSON.stringify(body) });
      mostrarToast('Insumo actualizado', 'success');
    } else {
      await apiFetch('/admin/insumos', { method: 'POST', body: JSON.stringify(body) });
      mostrarToast('Insumo creado', 'success');
    }
    cerrarModalInsumo();
    cargarInsumos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo guardar el insumo', 'error');
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
    await apiFetch(`/admin/insumos/${idAEliminar}`, { method: 'DELETE' });
    mostrarToast('Insumo eliminado', 'success');
    cerrarModalEliminar();
    cargarInsumos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo eliminar', 'error');
  }
});

// ── Eventos ────────────────────────────────────────────────────────────────
btnNuevo.addEventListener('click', abrirModalCrear);
document.getElementById('modal-insumo-cerrar').addEventListener('click', cerrarModalInsumo);
document.getElementById('btn-cancelar-insumo').addEventListener('click', cerrarModalInsumo);
document.getElementById('modal-eliminar-insumo-cerrar').addEventListener('click', cerrarModalEliminar);
document.getElementById('btn-cancelar-eliminar-insumo').addEventListener('click', cerrarModalEliminar);

window.editarInsumo = (id) => abrirModalEditar(id);
window.eliminarInsumo = (id, nombre) => abrirModalEliminar(id, nombre);

// Filtro por nombre.
const inputBuscar = document.getElementById('input-buscar-insumo');
inputBuscar?.addEventListener('input', () => {
  const q = inputBuscar.value.trim().toLowerCase();
  renderizarTabla(insumosCache.filter(i => i.nombreInsumo.toLowerCase().includes(q)));
});

cargarInsumos();
