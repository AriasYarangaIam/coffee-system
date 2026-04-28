import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, crearModal } from '../utils/dom.js';
import { formatearMoneda } from '../utils/format.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('productos-tbody');
const btnNuevo = document.getElementById('btn-nuevo-producto');

async function cargarProductos() {
  mostrarSpinner(tablaBody);
  try {
    const productos = await apiFetch('/admin/productos');
    renderizarTabla(productos);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar productos', 'error');
  }
}

function renderizarTabla(productos) {
  if (!productos.length) {
    tablaBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">Sin productos registrados</td></tr>';
    return;
  }
  tablaBody.innerHTML = productos.map(p => `
    <tr>
      <td>${p.nombreProducto}</td>
      <td>${p.categoria ?? '—'}</td>
      <td>${formatearMoneda(p.precio)}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="editarProducto(${p.productoId})">Editar</button>
        <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarProducto(${p.productoId}, '${p.nombreProducto}')">Eliminar</button>
      </td>
    </tr>
  `).join('');
}

async function eliminarProducto(id, nombre) {
  crearModal({
    titulo: 'Eliminar producto',
    contenido: `<p>¿Seguro que deseas eliminar <strong>${nombre}</strong>?</p>`,
    labelConfirm: 'Eliminar',
    onConfirm: async () => {
      try {
        await apiFetch(`/admin/productos/${id}`, { method: 'DELETE' });
        mostrarToast('Producto eliminado', 'success');
        cargarProductos();
      } catch (error) {
        mostrarToast(error?.message || 'No se pudo eliminar', 'error');
      }
    },
  });
}

window.editarProducto = (id) => { /* TODO: abrir modal de edición */ };
window.eliminarProducto = eliminarProducto;

btnNuevo.addEventListener('click', () => { /* TODO: abrir modal de creación */ });

cargarProductos();
