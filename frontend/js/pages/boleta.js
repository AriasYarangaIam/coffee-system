import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast } from '../utils/dom.js';
import { formatearMoneda, formatearFecha } from '../utils/format.js';

requireRole('MESERO');

const pedidoId = localStorage.getItem('ultimo_pedido_id');
const boletaEl = document.getElementById('boleta-contenido');

async function cargarBoleta() {
  if (!pedidoId) {
    boletaEl.innerHTML = '<p class="text-muted text-center">No hay pedido activo.</p>';
    return;
  }
  try {
    const boleta = await apiFetch(`/pedidos/${pedidoId}/boleta`);
    renderizarBoleta(boleta);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar la boleta', 'error');
  }
}

function renderizarBoleta(b) {
  boletaEl.innerHTML = `
    <div class="boleta-header">
      <h2>Cafetería MYPE</h2>
      <p class="text-muted text-sm">${formatearFecha(b.fechaPedido)}</p>
      <p class="font-semibold" style="margin-top:8px">Ticket: ${b.aliasTicket}</p>
    </div>
    <hr style="margin:16px 0;border-color:var(--color-border)">
    <table class="table">
      <thead>
        <tr>
          <th>Producto</th>
          <th>Cant.</th>
          <th>P. Unit.</th>
          <th>Subtotal</th>
        </tr>
      </thead>
      <tbody>
        ${b.detalle.map(d => `
          <tr>
            <td>${d.nombreProducto}</td>
            <td>${d.cantidadPedida}</td>
            <td>${formatearMoneda(d.precioUnitario)}</td>
            <td>${formatearMoneda(d.precioUnitario * d.cantidadPedida)}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
    <hr style="margin:16px 0;border-color:var(--color-border)">
    <p class="cart-total">
      <span>TOTAL</span>
      <span>${formatearMoneda(b.total)}</span>
    </p>
  `;
}

document.getElementById('btn-nuevo-pedido').addEventListener('click', () => {
  localStorage.removeItem('ultimo_pedido_id');
  window.location.href = '/pages/mesero/pedidos.html';
});

cargarBoleta();
