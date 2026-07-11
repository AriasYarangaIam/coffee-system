import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, mostrarVacio, escaparHtml } from '../utils/dom.js';
import { formatearMoneda } from '../utils/format.js';

requireRole('MESERO');

let carrito = [];
// RF-DS-03: la Pila de "Deshacer" vive en el backend (Java). Aquí solo llevamos cuántos
// snapshots hay apilados para habilitar/deshabilitar el botón. Cada acción del carrito es
// una ida al servidor (decisión de diseño: la estructura corre en el back).
let profundidadUndo = 0;
const productosGrid = document.getElementById('productos-grid');
const cartItemsEl = document.getElementById('cart-items');
const cartTotalEl = document.getElementById('cart-total');
const btnConfirmar = document.getElementById('btn-confirmar');
const btnDeshacer = document.getElementById('btn-deshacer');
const filtroCategorias = document.getElementById('filtro-categorias');

let productosCache = [];
let categoriaActiva = 'todas';
const inputBuscarProducto = document.getElementById('input-buscar-producto-mesero');

async function cargarProductos() {
  mostrarSpinner(productosGrid);
  try {
    productosCache = await apiFetch('/productos');
    renderizarChips();
    aplicarFiltros();
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar productos', 'error');
  }
}

// Chips de categoría derivados del catálogo (más "Todas").
function renderizarChips() {
  const categorias = [...new Set(productosCache.map(p => p.categoria).filter(Boolean))];
  filtroCategorias.innerHTML = [
    `<button class="chip ${categoriaActiva === 'todas' ? 'active' : ''}" data-cat="todas">Todas</button>`,
    ...categorias.map(c => `<button class="chip ${categoriaActiva === c ? 'active' : ''}" data-cat="${escaparHtml(c)}">${escaparHtml(c)}</button>`),
  ].join('');
  filtroCategorias.querySelectorAll('.chip').forEach(chip => {
    chip.addEventListener('click', () => {
      categoriaActiva = chip.dataset.cat;
      renderizarChips();
      aplicarFiltros();
    });
  });
}

// Búsqueda y categoría se combinan.
function aplicarFiltros() {
  const q = inputBuscarProducto.value.trim().toLowerCase();
  renderizarProductos(productosCache.filter(p =>
    (categoriaActiva === 'todas' || p.categoria === categoriaActiva) &&
    p.nombreProducto.toLowerCase().includes(q)));
}

inputBuscarProducto?.addEventListener('input', aplicarFiltros);

function renderizarProductos(productos) {
  if (!productos.length) {
    mostrarVacio(productosGrid, 'No hay productos disponibles');
    return;
  }
  productosGrid.innerHTML = productos.map(p => `
    <div class="product-card ${!p.disponible ? 'out-of-stock' : ''}"
         data-id="${p.productoId}"
         data-nombre="${escaparHtml(p.nombreProducto)}"
         data-precio="${p.precio}">
      <div class="product-card__body">
        <p class="product-card__name">${escaparHtml(p.nombreProducto)}</p>
        <p class="product-card__category">${escaparHtml(p.categoria ?? '')}</p>
        <div class="product-card__footer">
          <span class="product-card__price">${formatearMoneda(p.precio)}</span>
          ${!p.disponible ? '<span class="product-card__stock-badge">Sin stock</span>' : ''}
        </div>
      </div>
    </div>
  `).join('');

  productosGrid.querySelectorAll('.product-card:not(.out-of-stock)').forEach(card => {
    card.addEventListener('click', () => {
      agregarAlCarrito({
        productoId: Number(card.dataset.id),
        nombre: card.dataset.nombre,
        precio: Number(card.dataset.precio),
      });
      // Feedback: pulso breve en la card agregada.
      card.classList.remove('product-card--added');
      void card.offsetWidth; // reinicia la animación si se hace clic seguido
      card.classList.add('product-card--added');
    });
  });
}

// Apila el estado ACTUAL del carrito en la Pila del backend, antes de mutarlo (RF-DS-03).
async function apilarSnapshot() {
  try {
    const r = await apiFetch('/pedidos/carrito/push', {
      method: 'POST',
      body: JSON.stringify({ items: carrito }),
    });
    profundidadUndo = r.profundidad;
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo guardar el estado para deshacer', 'error');
  }
}

async function agregarAlCarrito(producto) {
  await apilarSnapshot();
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
  await apilarSnapshot();
  item.cantidad += delta;
  if (item.cantidad <= 0) carrito = carrito.filter(i => i.productoId !== productoId);
  renderizarCarrito();
}

function renderizarCarrito() {
  btnDeshacer.disabled = profundidadUndo === 0;
  if (!carrito.length) {
    cartItemsEl.innerHTML = '<p class="text-muted text-sm" style="text-align:center;padding:16px">El carrito está vacío</p>';
    cartTotalEl.textContent = formatearMoneda(0);
    btnConfirmar.disabled = true;
    return;
  }

  cartItemsEl.innerHTML = carrito.map(item => `
    <div class="cart-item">
      <span class="cart-item__name">${escaparHtml(item.nombre)}</span>
      <div class="cart-item__controls">
        <button class="qty-btn" onclick="cambiarCantidad(${item.productoId}, -1)">−</button>
        <span class="qty-value">${item.cantidad}</span>
        <button class="qty-btn" onclick="cambiarCantidad(${item.productoId}, 1)">+</button>
      </div>
      <span class="cart-item__subtotal">${formatearMoneda(item.precio * item.cantidad)}</span>
    </div>
  `).join('');

  const total = carrito.reduce((acc, i) => acc + i.precio * i.cantidad, 0);
  cartTotalEl.textContent = formatearMoneda(total);
  btnConfirmar.disabled = false;
}

btnConfirmar.addEventListener('click', async () => {
  btnConfirmar.disabled = true;
  try {
    const body = {
      detalles: carrito.map(i => ({ productoId: i.productoId, cantidadPedida: i.cantidad })),
    };
    const pedido = await apiFetch('/pedidos', { method: 'POST', body: JSON.stringify(body) });
    localStorage.setItem('ultimo_pedido_id', pedido.pedidoId);
    window.location.href = '/pages/mesero/boleta.html';
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo registrar el pedido', 'error');
    btnConfirmar.disabled = false;
  }
});

btnDeshacer.addEventListener('click', async () => {
  btnDeshacer.disabled = true;
  try {
    const r = await apiFetch('/pedidos/carrito/undo', { method: 'POST' });
    carrito = r.items ?? [];
    profundidadUndo = r.profundidad;
    renderizarCarrito();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo deshacer', 'error');
    btnDeshacer.disabled = profundidadUndo === 0;
  }
});

// Exponer para los botones inline del carrito
window.cambiarCantidad = cambiarCantidad;

cargarProductos();
