import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast } from '../utils/dom.js';
import { formatearMoneda } from '../utils/format.js';

requireRole('ADMIN');

let chartVentas = null;

async function cargarDashboard() {
  try {
    const [kpis, reporte] = await Promise.all([
      apiFetch('/admin/dashboard'),
      apiFetch('/admin/reportes/mensual'),
    ]);
    renderizarKpis(kpis);
    renderizarGrafico(reporte);
    renderizarStockBajo(kpis.stockBajo ?? []);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar el dashboard', 'error');
  }
}

function renderizarKpis(kpis) {
  document.getElementById('kpi-ventas').textContent = formatearMoneda(kpis.totalVentasDia ?? 0);
  document.getElementById('kpi-pedidos').textContent = kpis.totalPedidosDia ?? 0;
  document.getElementById('kpi-estrella').textContent = kpis.productoEstrella ?? '—';
  document.getElementById('kpi-stock-alerta').textContent = (kpis.stockBajo ?? []).length;
}

// Paleta para las series de producto (se cicla si hay más productos que colores).
const PALETA = ['#5C3317', '#A0522D', '#C8964B', '#7B9E5E', '#4E6E81', '#8D6E63', '#B5651D'];

function renderizarGrafico(reporte) {
  const ctx = document.getElementById('chart-ventas').getContext('2d');
  if (chartVentas) chartVentas.destroy();

  // Matriz producto × día (RF-DS-02): una serie apilada por producto, eje X = días.
  const datasets = reporte.productos.map((producto, i) => ({
    label: producto,
    data: reporte.dias.map((_, j) => reporte.celdas[i][j]),
    backgroundColor: PALETA[i % PALETA.length],
    borderRadius: 4,
    stack: 'ventas',
  }));

  chartVentas = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: reporte.dias.map(d => `Día ${d}`),
      datasets,
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: datasets.length > 1 } },
      scales: {
        x: { stacked: true },
        y: { stacked: true, beginAtZero: true, ticks: { callback: v => `S/ ${v}` } },
      },
    },
  });
}

function renderizarStockBajo(items) {
  const lista = document.getElementById('stock-bajo-lista');
  if (!items.length) {
    lista.innerHTML = '<p class="text-muted text-sm">Sin alertas de stock</p>';
    return;
  }
  lista.innerHTML = items.map(i => `
    <div class="stock-alert-item">
      <span class="stock-alert-item__name">${i.nombreInsumo}</span>
      <span class="stock-alert-item__qty">${i.cantidad} ${i.unidad ?? ''}</span>
    </div>
  `).join('');
}

cargarDashboard();
