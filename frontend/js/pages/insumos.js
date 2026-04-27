import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, crearModal } from '../utils/dom.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('insumos-tbody');
const btnNuevo = document.getElementById('btn-nuevo-insumo');

async function cargarInsumos() {
  mostrarSpinner(tablaBody);
  try {
    const insumos = await apiFetch('/admin/insumos');
    renderizarTabla(insumos);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar insumos', 'error');
  }
}

function renderizarTabla(insumos) {
  if (!insumos.length) {
    tablaBody.innerHTML = '<tr><td colspan="2" class="text-center text-muted" style="padding:24px">Sin insumos registrados</td></tr>';
    return;
  }
  tablaBody.innerHTML = insumos.map(i => `
    <tr>
      <td>${i.nombreInsumo}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="editarInsumo(${i.idInsumo})">Editar</button>
        <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarInsumo(${i.idInsumo}, '${i.nombreInsumo}')">Eliminar</button>
      </td>
    </tr>
  `).join('');
}

async function eliminarInsumo(id, nombre) {
  crearModal({
    titulo: 'Eliminar insumo',
    contenido: `<p>¿Seguro que deseas eliminar <strong>${nombre}</strong>?</p>`,
    labelConfirm: 'Eliminar',
    onConfirm: async () => {
      try {
        await apiFetch(`/admin/insumos/${id}`, { method: 'DELETE' });
        mostrarToast('Insumo eliminado', 'success');
        cargarInsumos();
      } catch (error) {
        mostrarToast(error?.message || 'No se pudo eliminar', 'error');
      }
    },
  });
}

window.editarInsumo = (id) => { /* TODO: abrir modal de edición */ };
window.eliminarInsumo = eliminarInsumo;

btnNuevo.addEventListener('click', () => { /* TODO: abrir modal de creación */ });

cargarInsumos();
