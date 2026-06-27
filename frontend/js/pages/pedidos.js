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

async function cargarProductos() {
  mostrarSpinner(productosGrid);
  try {
    const productos = await apiFetch('/productos');
    renderizarProductos(productos);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar productos', 'error');
  }
}

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

function agregarAlCarrito(producto) {
  const existente = carrito.find(i => i.productoId === producto.productoId);
  if (existente) {
    existente.cantidad++;
  } else {
    carrito.push({ ...producto, cantidad: 1 });
  }
  renderizarCarrito();
}

function cambiarCantidad(productoId, delta) {
  const item = carrito.find(i => i.productoId === productoId);
  if (!item) return;
  item.cantidad += delta;
  if (item.cantidad <= 0) carrito = carrito.filter(i => i.productoId !== productoId);
  renderizarCarrito();
}

function renderizarCarrito() {
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

// Exponer para los botones inline del carrito
window.cambiarCantidad = cambiarCantidad;

cargarProductos();
