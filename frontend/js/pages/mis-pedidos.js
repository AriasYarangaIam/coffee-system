// Pedidos del turno del mesero: tabla, busqueda, metricas del dia y modal de detalle.
import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, escaparHtml } from '../utils/dom.js';
import { formatearMoneda, formatearFecha } from '../utils/format.js';

requireRole('MESERO');

const tablaBody = document.getElementById('pedidos-tbody');
const inputBuscar = document.getElementById('input-buscar-pedido');
const btnRefrescar = document.getElementById('btn-refrescar');

// Modal detalle
const modalDetalle = document.getElementById('modal-detalle-pedido');

let pedidosCache = [];
// Cabeza actual de la cola de despacho (FIFO): solo ese ticket puede entregarse.
let headId = null;

async function cargarPedidos() {
  mostrarSpinner(tablaBody);
  try {
    // La lista de pedidos y la cabeza de la cola se piden en paralelo.
    const [pedidos, cabeza] = await Promise.all([
      apiFetch('/pedidos'),
      apiFetch('/pedidos/despacho/siguiente'),
    ]);
    pedidosCache = pedidos;
    headId = cabeza?.pedidoId ?? null;
    renderizarPedidos(pedidosCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar pedidos', 'error');
  }
}

// Entrega la cabeza de la cola (dequeue en el backend) y refresca la vista.
async function entregarPedido(pedidoId) {
  try {
    await apiFetch('/pedidos/despacho/entregar', { method: 'POST' });
    mostrarToast('Pedido entregado', 'success');
    await cargarPedidos();
    cargarMetricas();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo entregar el pedido', 'error');
  }
}

// Celda "Entregado": ✓ si ya salió, botón habilitado solo para la cabeza, resto en cola.
function celdaEntregado(p) {
  if (p.entregado) {
    return '<span style="color:var(--color-success,#16a34a);font-weight:600">✓ Entregado</span>';
  }
  if (p.pedidoId === headId) {
    return `<button class="btn btn-primary btn-sm" data-entregar="${p.pedidoId}">Entregar</button>`;
  }
  return '<button class="btn btn-secondary btn-sm" disabled>En cola</button>';
}

async function cargarMetricas() {
  try {
    const m = await apiFetch('/pedidos/mis-metricas');
    document.getElementById('metrica-pedidos').textContent = m.pedidosAtendidos ?? 0;
    document.getElementById('metrica-total').textContent = formatearMoneda(m.totalVendido ?? 0);
    document.getElementById('metrica-promedio').textContent = formatearMoneda(m.ticketPromedio ?? 0);
    document.getElementById('metrica-estrella').textContent = m.productoEstrella ?? '—';
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar métricas', 'error');
  }
}

function renderizarPedidos(pedidos) {
  if (!pedidos.length) {
    tablaBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">No hay pedidos en este turno</td></tr>';
    return;
  }
  // Sensación de cola FIFO: pendientes arriba (el más antiguo = cabeza primero) y ya
  // entregados al final. El pedidoId (IDENTITY) crece con la llegada ⇒ ordena por llegada.
  const ordenados = [...pedidos].sort((a, b) => {
    if (a.entregado !== b.entregado) return a.entregado ? 1 : -1;
    return a.pedidoId - b.pedidoId;
  });
  tablaBody.innerHTML = ordenados.map(p => `
    <tr data-id="${p.pedidoId}" style="cursor:pointer">
      <td><span class="font-semibold">${escaparHtml(p.aliasTicket)}</span></td>
      <td>${(p.detalle ?? []).map(d => `${escaparHtml(d.nombreProducto)} x${d.cantidadPedida}`).join(', ')}</td>
      <td style="text-align:right">${formatearMoneda(p.total)}</td>
      <td style="text-align:center">${celdaEntregado(p)}</td>
    </tr>
  `).join('');
}

// ── Modal de detalle ─────────────────────────────────────────────────────────
function abrirDetalle(pedidoId) {
  const pedido = pedidosCache.find(p => p.pedidoId === pedidoId);
  if (!pedido) return;

  document.getElementById('modal-ticket-numero').textContent = pedido.aliasTicket;
  document.getElementById('modal-detalle-fecha').textContent = formatearFecha(pedido.fechaPedido);
  document.getElementById('modal-detalle-total').textContent = formatearMoneda(pedido.total);
  document.getElementById('modal-detalle-lineas').innerHTML = (pedido.detalle ?? []).map(d => `
    <div style="display:grid;grid-template-columns:1fr auto auto;gap:8px;padding:8px 0;border-bottom:1px solid var(--color-border)">
      <span>${escaparHtml(d.nombreProducto)}</span>
      <span style="text-align:center">x${d.cantidadPedida}</span>
      <span style="text-align:right">${formatearMoneda(d.precioUnitario)}</span>
    </div>
  `).join('');

  // "Ver Boleta" reutiliza el flujo existente: guarda el id y navega a la boleta.
  document.getElementById('modal-detalle-ver-boleta').onclick = () => {
    localStorage.setItem('ultimo_pedido_id', pedidoId);
    window.location.href = '/pages/mesero/boleta.html';
  };

  modalDetalle.classList.remove('hidden');
}

function cerrarDetalle() {
  modalDetalle.classList.add('hidden');
}

// ── Eventos ──────────────────────────────────────────────────────────────────
tablaBody.addEventListener('click', (e) => {
  // El botón "Entregar" tiene prioridad y NO abre el modal de detalle.
  const btnEntregar = e.target.closest('[data-entregar]');
  if (btnEntregar) {
    e.stopPropagation();
    entregarPedido(Number(btnEntregar.dataset.entregar));
    return;
  }
  const fila = e.target.closest('tr[data-id]');
  if (fila) abrirDetalle(Number(fila.dataset.id));
});

btnRefrescar.addEventListener('click', () => {
  cargarPedidos();
  cargarMetricas();
});

document.getElementById('modal-detalle-cerrar').addEventListener('click', cerrarDetalle);
document.getElementById('modal-detalle-cerrar-btn').addEventListener('click', cerrarDetalle);

// Filtro por ticket o nombre de producto.
inputBuscar?.addEventListener('input', () => {
  const q = inputBuscar.value.trim().toLowerCase();
  renderizarPedidos(pedidosCache.filter(p =>
    p.aliasTicket.toLowerCase().includes(q) ||
    (p.detalle ?? []).some(d => d.nombreProducto.toLowerCase().includes(q))));
});

cargarPedidos();
cargarMetricas();