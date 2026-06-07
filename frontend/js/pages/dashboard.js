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

function renderizarGrafico(reporte) {
  const ctx = document.getElementById('chart-ventas').getContext('2d');
  if (chartVentas) chartVentas.destroy();

  chartVentas = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: reporte.map(r => `Día ${r.dia}`),
      datasets: [{
        label: 'Ventas (S/.)',
        data: reporte.map(r => r.total),
        backgroundColor: '#5C3317',
        borderRadius: 6,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        y: { beginAtZero: true, ticks: { callback: v => `S/ ${v}` } },
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
