import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner } from '../utils/dom.js';
import { formatearMoneda } from '../utils/format.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('productos-tbody');
const btnNuevo = document.getElementById('btn-nuevo-producto');

// Modal crear/editar
const modalProducto = document.getElementById('modal-producto');
const modalTitulo = document.getElementById('modal-producto-titulo');
const formProducto = document.getElementById('form-producto');
const inputId = document.getElementById('producto-id');
const inputNombre = document.getElementById('producto-nombre');
const inputCategoria = document.getElementById('producto-categoria');
const inputPrecio = document.getElementById('producto-precio');

// Modal eliminar
const modalEliminar = document.getElementById('modal-eliminar-producto');
const spanNombreEliminar = document.getElementById('nombre-producto-eliminar');
const btnConfirmarEliminar = document.getElementById('btn-confirmar-eliminar-producto');

let productosCache = [];
let idAEliminar = null;

// ── Cargar ────────────────────────────────────────────────────────────────
async function cargarProductos() {
  mostrarSpinner(tablaBody);
  try {
    productosCache = await apiFetch('/admin/productos');
    renderizarTabla(productosCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar productos', 'error');
  }
}

async function cargarCategorias() {
  try {
    const categorias = await apiFetch('/admin/categorias');
    inputCategoria.innerHTML = '<option value="">Seleccionar categoría...</option>' +
      categorias.map(c => `<option value="${c.categoriaId}">${c.nombreCategoria}</option>`).join('');
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar categorías', 'error');
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
      <td>${p.nombreCategoria ?? '—'}</td>
      <td style="text-align:right">${formatearMoneda(p.precioActual)}</td>
      <td style="text-align:right">
        <button class="btn btn-outline btn-sm" onclick="editarProducto(${p.productoId})">Editar</button>
        <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarProducto(${p.productoId}, '${p.nombreProducto}')">Eliminar</button>
      </td>
    </tr>
  `).join('');
}

// ── Modal crear/editar ─────────────────────────────────────────────────────
function abrirModalCrear() {
  modalTitulo.textContent = 'Nuevo Producto';
  inputId.value = '';
  formProducto.reset();
  modalProducto.classList.remove('hidden');
}

function abrirModalEditar(id) {
  const producto = productosCache.find(p => p.productoId === id);
  if (!producto) return;
  modalTitulo.textContent = 'Editar Producto';
  inputId.value = producto.productoId;
  inputNombre.value = producto.nombreProducto;
  inputCategoria.value = producto.categoriaId ?? '';
  inputPrecio.value = producto.precioActual;
  modalProducto.classList.remove('hidden');
}

function cerrarModalProducto() {
  modalProducto.classList.add('hidden');
}

// ── Guardar (crear o editar) ───────────────────────────────────────────────
formProducto.addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = formProducto.querySelector('button[type="submit"]');
  btn.disabled = true;

  const id = inputId.value;
  const body = {
    nombreProducto: inputNombre.value.trim(),
    precioActual: parseFloat(inputPrecio.value),
    categoriaId: Number(inputCategoria.value),
  };

  try {
    if (id) {
      await apiFetch(`/admin/productos/${id}`, { method: 'PUT', body: JSON.stringify(body) });
      mostrarToast('Producto actualizado', 'success');
    } else {
      await apiFetch('/admin/productos', { method: 'POST', body: JSON.stringify(body) });
      mostrarToast('Producto creado', 'success');
    }
    cerrarModalProducto();
    cargarProductos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo guardar el producto', 'error');
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
    await apiFetch(`/admin/productos/${idAEliminar}`, { method: 'DELETE' });
    mostrarToast('Producto eliminado', 'success');
    cerrarModalEliminar();
    cargarProductos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo eliminar', 'error');
  }
});

// ── Eventos ────────────────────────────────────────────────────────────────
btnNuevo.addEventListener('click', abrirModalCrear);
document.getElementById('modal-producto-cerrar').addEventListener('click', cerrarModalProducto);
document.getElementById('btn-cancelar-producto').addEventListener('click', cerrarModalProducto);
document.getElementById('modal-eliminar-producto-cerrar').addEventListener('click', cerrarModalEliminar);
document.getElementById('btn-cancelar-eliminar-producto').addEventListener('click', cerrarModalEliminar);

window.editarProducto = (id) => abrirModalEditar(id);
window.eliminarProducto = (id, nombre) => abrirModalEliminar(id, nombre);

cargarCategorias();
cargarProductos();
