import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner } from '../utils/dom.js';
import { formatearMoneda, formatearFecha, renderBadgeEstado } from '../utils/format.js';

requireRole('MESERO');

const tablaBody = document.getElementById('pedidos-tbody');

async function cargarPedidos() {
  mostrarSpinner(tablaBody);
  try {
    const pedidos = await apiFetch('/pedidos');
    renderizarPedidos(pedidos);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar pedidos', 'error');
  }
}

function renderizarPedidos(pedidos) {
  if (!pedidos.length) {
    tablaBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding:24px">No hay pedidos en este turno</td></tr>';
    return;
  }
  tablaBody.innerHTML = pedidos.map(p => `
    <tr>
      <td><span class="font-semibold">${p.aliasTicket}</span></td>
      <td>${p.detalle.map(d => `${d.nombreProducto} x${d.cantidadPedida}`).join(', ')}</td>
      <td>${formatearMoneda(p.total)}</td>
      <td>${renderBadgeEstado(p.estado)}</td>
      <td>${renderAcciones(p)}</td>
    </tr>
  `).join('');
}

function renderAcciones(pedido) {
  if (pedido.estado !== 'PENDIENTE') {
    return '<span class="text-muted text-sm">—</span>';
  }
  return `
    <button class="btn btn-primary btn-sm" onclick="cambiarEstado(${pedido.pedidoId}, 'PAGADO')">Cobrado</button>
    <button class="btn btn-danger btn-sm" style="margin-left:4px" onclick="cambiarEstado(${pedido.pedidoId}, 'CANCELADO')">Cancelar</button>
  `;
}

async function cambiarEstado(pedidoId, nuevoEstado) {
  try {
    await apiFetch(`/pedidos/${pedidoId}/estado`, {
      method: 'PATCH',
      body: JSON.stringify({ estado: nuevoEstado }),
    });
    await cargarPedidos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo cambiar el estado', 'error');
  }
}

window.cambiarEstado = cambiarEstado;

cargarPedidos();
