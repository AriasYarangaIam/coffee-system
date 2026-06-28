import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner } from '../utils/dom.js';
import { formatearMoneda } from '../utils/format.js';

requireRole('MESERO');

const tablaBody = document.getElementById('pedidos-tbody');
const inputBuscar = document.getElementById('input-buscar-pedido');

let pedidosCache = [];

async function cargarPedidos() {
  mostrarSpinner(tablaBody);
  try {
    pedidosCache = await apiFetch('/pedidos');
    renderizarPedidos(pedidosCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar pedidos', 'error');
  }
}

function renderizarPedidos(pedidos) {
  if (!pedidos.length) {
    tablaBody.innerHTML = '<tr><td colspan="3" class="text-center text-muted" style="padding:24px">No hay pedidos en este turno</td></tr>';
    return;
  }
  tablaBody.innerHTML = pedidos.map(p => `
    <tr>
      <td><span class="font-semibold">${p.aliasTicket}</span></td>
      <td>${(p.detalle ?? []).map(d => `${d.nombreProducto} x${d.cantidadPedida}`).join(', ')}</td>
      <td>${formatearMoneda(p.total)}</td>
    </tr>
  `).join('');
}

// Filtro por ticket o nombre de producto.
inputBuscar?.addEventListener('input', () => {
  const q = inputBuscar.value.trim().toLowerCase();
  renderizarPedidos(pedidosCache.filter(p =>
    p.aliasTicket.toLowerCase().includes(q) ||
    (p.detalle ?? []).some(d => d.nombreProducto.toLowerCase().includes(q))));
});

cargarPedidos();
