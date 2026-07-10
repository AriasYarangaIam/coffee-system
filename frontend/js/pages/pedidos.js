import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, mostrarVacio } from '../utils/dom.js';
import { formatearMoneda } from '../utils/format.js';

requireRole('MESERO');

let carrito = [];
const productosGrid = document.getElementById('productos-grid');
const cartItemsEl = document.getElementById('cart-items');
const cartTotalEl = document.getElementById('cart-total');
const btnConfirmar = document.getElementById('btn-confirmar');
const btnDeshacer = document.getElementById('btn-deshacer');

let productosCache = [];
const inputBuscarProducto = document.getElementById('input-buscar-producto-mesero');

async function cargarProductos() {
  mostrarSpinner(productosGrid);
  try {
    productosCache = await apiFetch('/productos');
    renderizarProductos(productosCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar productos', 'error');
  }
}

inputBuscarProducto?.addEventListener('input', () => {
  const q = inputBuscarProducto.value.trim().toLowerCase();
  renderizarProductos(productosCache.filter(p => p.nombreProducto.toLowerCase().includes(q)));
});

function renderizarProductos(productos) {
  if (!productos.length) {
    mostrarVacio(productosGrid, 'No hay productos disponibles');
    return;
  }
  productosGrid.innerHTML = productos.map(p => `
    <div class="product-card ${!p.disponible ? 'out-of-stock' : ''}"
         data-id="${p.productoId}"
         data-nombre="${p.nombreProducto}"
         data-precio="${p.precio}">
      <div class="product-card__body">
        <p class="product-card__name">${p.nombreProducto}</p>
        <p class="product-card__category">${p.categoria ?? ''}</p>
        <div class="product-card__footer">
          <span class="product-card__price">${formatearMoneda(p.precio)}</span>
          ${!p.disponible ? '<span class="product-card__stock-badge">Sin stock</span>' : ''}
        </div>
      </div>
    </div>
  `).join('');

  productosGrid.querySelectorAll('.product-card:not(.out-of-stock)').forEach(card => {
    card.addEventListener('click', () => agregarAlCarrito({
      productoId: Number(card.dataset.id),
      nombre: card.dataset.nombre,
      precio: Number(card.dataset.precio),
    }));
  });
}

async function agregarAlCarrito(producto) {
  await pushSnapshot();
  const existente = carrito.find(i => i.productoId === producto.productoId);
  if (existente) {
    existente.cantidad++;
  } else {
    carrito.push({ ...producto, cantidad: 1 });
  }
  renderizarCarrito();
}

async function cambiarCantidad(productoId, delta) {
  const item = carrito.find(i => i.productoId === productoId);
  if (!item) return;
  await pushSnapshot();
  item.cantidad += delta;
  if (item.cantidad <= 0) carrito = carrito.filter(i => i.productoId !== productoId);
  renderizarCarrito();
}

async function actualizarEstadoDeshacer() {
  try {
    const estado = await apiFetch('/pedidos/undo');
    btnDeshacer.disabled = !estado?.canUndo;
  } catch {
    btnDeshacer.disabled = true;
  }
}

async function pushSnapshot() {
  try {
    await apiFetch('/pedidos/undo/push', {
      method: 'POST',
      body: JSON.stringify({ items: structuredClone(carrito), total: calcularTotal(carrito) })
    });
  } catch (error) {
    // No bloquear la mutación del carrito si el snapshot falla; el undo será best-effort.
    console.warn('No se pudo guardar snapshot de deshacer', error);
  }
}

function calcularTotal(items) {
  return items.reduce((acc, i) => acc + i.precio * i.cantidad, 0);
}

function renderizarCarrito() {
  actualizarEstadoDeshacer();
  if (!carrito.length) {
    cartItemsEl.innerHTML = '<p class="text-muted text-sm" style="text-align:center;padding:16px">El carrito está vacío</p>';
    cartTotalEl.textContent = formatearMoneda(0);
    btnConfirmar.disabled = true;
    return;
  }

  cartItemsEl.innerHTML = carrito.map(item => `
    <div class="cart-item">
      <span class="cart-item__name">${item.nombre}</span>
      <div class="cart-item__controls">
        <button class="qty-btn" onclick="cambiarCantidad(${item.productoId}, -1)">−</button>
        <span class="qty-value">${item.cantidad}</span>
        <button class="qty-btn" onclick="cambiarCantidad(${item.productoId}, 1)">+</button>
      </div>
      <span class="cart-item__subtotal">${formatearMoneda(item.precio * item.cantidad)}</span>
    </div>
  `).join('');

  cartTotalEl.textContent = formatearMoneda(calcularTotal(carrito));
  btnConfirmar.disabled = false;
}

btnConfirmar.addEventListener('click', async () => {
  btnConfirmar.disabled = true;
  try {
    const body = {
      detalles: carrito.map(i => ({ productoId: i.productoId, cantidadPedida: i.cantidad })),
    };
    const pedido = await apiFetch('/pedidos', { method: 'POST', body: JSON.stringify(body) });
    await apiFetch('/pedidos/undo', { method: 'DELETE' });
    localStorage.setItem('ultimo_pedido_id', pedido.pedidoId);
    window.location.href = '/pages/mesero/boleta.html';
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo registrar el pedido', 'error');
    btnConfirmar.disabled = false;
  }
});

btnDeshacer.addEventListener('click', async () => {
  try {
    const data = await apiFetch('/pedidos/undo', { method: 'POST' });
    if (!data?.snapshot?.items) return;
    carrito = data.snapshot.items;
    renderizarCarrito();
  } catch (error) {
    // 409 u otro error: mostrar feedback suave sin romper la UI.
    mostrarToast(error?.message || 'No hay acciones para deshacer', 'info');
  }
});

// Exponer para los botones inline del carrito
window.cambiarCantidad = cambiarCantidad;

cargarProductos();
