// Boleta/recibo de un pedido: la carga por id y la muestra (con opcion de imprimir).
import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast } from '../utils/dom.js';
import { formatearMoneda, formatearFecha } from '../utils/format.js';

requireRole('MESERO');

const pedidoId = localStorage.getItem('ultimo_pedido_id');
const spinnerEl = document.getElementById('boleta-spinner');
const detalleEl = document.getElementById('boleta-detalle');

function ocultarSpinner() {
  spinnerEl?.remove();
}

async function cargarBoleta() {
  if (!pedidoId) {
    ocultarSpinner();
    mostrarToast('No hay pedido activo', 'error');
    return;
  }
  try {
    const boleta = await apiFetch(`/pedidos/${pedidoId}/boleta`);
    renderizarBoleta(boleta);
  } catch (error) {
    ocultarSpinner();
    mostrarToast(error?.message || 'Error al cargar la boleta', 'error');
  }
}

function renderizarBoleta(b) {
  document.getElementById('boleta-ticket').textContent = b.aliasTicket;
  document.getElementById('boleta-fecha').textContent = formatearFecha(b.fechaPedido);

  document.getElementById('boleta-lineas').innerHTML = b.detalle.map(d => `
    <div style="display:grid;grid-template-columns:auto 1fr auto;gap:8px;padding:4px 0">
      <span class="text-sm">${d.cantidadPedida}x</span>
      <span class="text-sm">${d.nombreProducto}</span>
      <span class="text-sm" style="text-align:right">${formatearMoneda(d.precioUnitario * d.cantidadPedida)}</span>
    </div>
  `).join('');

  document.getElementById('boleta-subtotal').textContent = formatearMoneda(b.total);
  document.getElementById('boleta-total').textContent = formatearMoneda(b.total);

  ocultarSpinner();
  detalleEl.classList.remove('hidden');
}

document.getElementById('btn-nuevo-pedido').addEventListener('click', () => {
  localStorage.removeItem('ultimo_pedido_id');
  window.location.href = '/pages/mesero/pedidos.html';
});

cargarBoleta();