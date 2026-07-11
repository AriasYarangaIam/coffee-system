import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, escaparHtml } from '../utils/dom.js';
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
const recetaRows = document.getElementById('receta-rows');
const btnAgregarInsumo = document.getElementById('btn-agregar-insumo');
const inputBuscar = document.getElementById('input-buscar-producto');
const selectFiltroCategoria = document.getElementById('select-filtro-categoria');

// Modal eliminar
const modalEliminar = document.getElementById('modal-eliminar-producto');
const spanNombreEliminar = document.getElementById('nombre-producto-eliminar');
const btnConfirmarEliminar = document.getElementById('btn-confirmar-eliminar-producto');

let productosCache = [];
let insumosCache = [];
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
    const opciones = categorias.map(c => `<option value="${c.categoriaId}">${escaparHtml(c.nombreCategoria)}</option>`).join('');
    inputCategoria.innerHTML = '<option value="">Seleccionar categoría...</option>' + opciones;
    if (selectFiltroCategoria) {
      selectFiltroCategoria.innerHTML = '<option value="">Todas las categorías</option>' + opciones;
    }
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar categorías', 'error');
  }
}

// Filtra la tabla por texto (nombre) y categoría seleccionada.
function aplicarFiltros() {
  const q = (inputBuscar?.value ?? '').trim().toLowerCase();
  const cat = selectFiltroCategoria?.value ?? '';
  renderizarTabla(productosCache.filter(p =>
    p.nombreProducto.toLowerCase().includes(q) &&
    (!cat || String(p.categoriaId) === cat)));
}

async function cargarInsumos() {
  try {
    insumosCache = await apiFetch('/admin/insumos');
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar insumos', 'error');
  }
}

// ── Receta (filas dinámicas insumo + cantidad) ──────────────────────────────
function opcionesInsumos() {
  return '<option value="">Insumo...</option>' +
    insumosCache.map(i => `<option value="${i.idInsumo}">${escaparHtml(i.nombreInsumo)}</option>`).join('');
}

function unidadDeInsumo(insumoId) {
  const insumo = insumosCache.find(i => String(i.idInsumo) === String(insumoId));
  return insumo?.unidad || '—';
}

function agregarFilaReceta(insumoId = '', cantidad = '') {
  const fila = document.createElement('div');
  fila.className = 'receta-row';
  fila.style.cssText = 'display:flex;gap:8px;align-items:center;margin-bottom:8px';
  fila.innerHTML = `
    <select class="form-select receta-insumo" style="flex:1">${opcionesInsumos()}</select>
    <input class="form-input receta-cantidad" type="number" min="1" step="1" placeholder="Cant." style="width:80px">
    <span class="receta-unidad text-muted text-sm" style="min-width:42px" title="Se descuenta del stock en esta medida">—</span>
    <button class="btn btn-danger btn-sm receta-quitar" type="button" title="Quitar">×</button>`;

  const select = fila.querySelector('.receta-insumo');
  const labelUnidad = fila.querySelector('.receta-unidad');
  const refrescarUnidad = () => { labelUnidad.textContent = unidadDeInsumo(select.value); };

  select.value = insumoId;
  fila.querySelector('.receta-cantidad').value = cantidad;
  refrescarUnidad();
  select.addEventListener('change', refrescarUnidad);
  fila.querySelector('.receta-quitar').addEventListener('click', () => fila.remove());
  recetaRows.appendChild(fila);
}

// Lee las filas; valida sin duplicados y cantidad > 0. Devuelve [{insumoId, cantidadUsada}].
function recolectarReceta() {
  const items = [];
  const vistos = new Set();
  for (const fila of recetaRows.querySelectorAll('.receta-row')) {
    const insumoId = fila.querySelector('.receta-insumo').value;
    const cantidad = fila.querySelector('.receta-cantidad').value;
    if (!insumoId && !cantidad) continue; // fila vacía → se ignora
    if (!insumoId) throw new Error('Hay una fila de receta sin insumo seleccionado');
    if (!(Number(cantidad) > 0)) throw new Error('La cantidad de cada insumo debe ser mayor a 0');
    if (vistos.has(insumoId)) throw new Error('No repitas el mismo insumo en la receta');
    vistos.add(insumoId);
    items.push({ insumoId: Number(insumoId), cantidadUsada: Number(cantidad) });
  }
  return items;
}

function renderizarTabla(productos) {
  if (!productos.length) {
    tablaBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">Sin productos registrados</td></tr>';
    return;
  }
  tablaBody.innerHTML = productos.map(p => `
    <tr>
      <td>${escaparHtml(p.nombreProducto)}</td>
      <td>${escaparHtml(p.nombreCategoria ?? '—')}</td>
      <td style="text-align:right">${formatearMoneda(p.precioActual)}</td>
      <td style="text-align:right">
        <button class="btn btn-outline btn-sm" onclick="editarProducto(${p.productoId})">Editar</button>
        <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="eliminarProducto(${p.productoId}, '${escaparHtml(p.nombreProducto)}')">Eliminar</button>
      </td>
    </tr>
  `).join('');
}

// ── Modal crear/editar ─────────────────────────────────────────────────────
function abrirModalCrear() {
  modalTitulo.textContent = 'Nuevo Producto';
  inputId.value = '';
  formProducto.reset();
  recetaRows.innerHTML = '';
  agregarFilaReceta(); // arranca con una fila vacía
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
  recetaRows.innerHTML = '';
  (producto.receta ?? []).forEach(r => agregarFilaReceta(String(r.idInsumo), r.cantidadUsada));
  if (!recetaRows.children.length) agregarFilaReceta();
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
  let receta;
  try {
    receta = recolectarReceta();
  } catch (validacion) {
    mostrarToast(validacion.message, 'error');
    btn.disabled = false;
    return;
  }
  const body = {
    nombreProducto: inputNombre.value.trim(),
    precioActual: parseFloat(inputPrecio.value),
    categoriaId: Number(inputCategoria.value),
    receta,
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
btnAgregarInsumo.addEventListener('click', () => agregarFilaReceta());
inputBuscar?.addEventListener('input', aplicarFiltros);
selectFiltroCategoria?.addEventListener('change', aplicarFiltros);
document.getElementById('modal-producto-cerrar').addEventListener('click', cerrarModalProducto);
document.getElementById('btn-cancelar-producto').addEventListener('click', cerrarModalProducto);
document.getElementById('modal-eliminar-producto-cerrar').addEventListener('click', cerrarModalEliminar);
document.getElementById('btn-cancelar-eliminar-producto').addEventListener('click', cerrarModalEliminar);

window.editarProducto = (id) => abrirModalEditar(id);
window.eliminarProducto = (id, nombre) => abrirModalEliminar(id, nombre);

cargarCategorias();
cargarInsumos();
cargarProductos();
