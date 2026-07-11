// Ingresos (BI admin): comparativa mensual, grafico semanal y detalle de boletas por rango.
import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, escaparHtml } from '../utils/dom.js';
import { formatearMoneda, formatearFecha, formatearFechaSolo } from '../utils/format.js';

requireRole('ADMIN');

const comparativaBox = document.getElementById('comparativa');
const inputMes = document.getElementById('input-mes');
const boletasBody = document.getElementById('boletas-tbody');
const inputDesde = document.getElementById('input-desde');
const inputHasta = document.getElementById('input-hasta');
const btnFiltrar = document.getElementById('btn-filtrar');

let chartSemanal = null;

// ── Comparativa mensual ──────────────────────────────────────────────────────
async function cargarComparativa(anio, mes) {
  comparativaBox.innerHTML = '<div class="spinner"></div>';
  try {
    const qs = (anio && mes) ? `?anio=${anio}&mes=${mes}` : '';
    const c = await apiFetch(`/admin/reportes/ingresos/comparativa${qs}`);
    renderizarComparativa(c);
  } catch (error) {
    comparativaBox.innerHTML = '<p class="text-muted text-sm">No se pudo cargar la comparativa</p>';
    mostrarToast(error?.message || 'Error al cargar la comparativa', 'error');
  }
}

function renderizarComparativa(c) {
  const variacion = c.variacionPorcentual;
  let badge = '<span class="text-muted text-sm">Sin base de comparación</span>';
  if (variacion != null) {
    const sube = Number(variacion) >= 0;
    const signo = sube ? '+' : '';
    badge = `<span class="ingresos-var ${sube ? 'ingresos-var--up' : 'ingresos-var--down'}">
      <span class="material-symbols-outlined" style="font-size:16px;vertical-align:middle">${sube ? 'trending_up' : 'trending_down'}</span>
      ${signo}${Number(variacion).toFixed(1)}%
    </span>`;
  }
  comparativaBox.innerHTML = `
    <div class="ingresos-comp-cell">
      <p class="kpi-card__label">Mes seleccionado</p>
      <p class="kpi-card__value">${formatearMoneda(c.totalMesActual)}</p>
      ${badge}
    </div>
    <div class="ingresos-comp-cell">
      <p class="kpi-card__label">Mes anterior</p>
      <p class="kpi-card__value" style="color:var(--color-text-muted)">${formatearMoneda(c.totalMesAnterior)}</p>
    </div>
  `;
}

// ── Ingresos por semana ──────────────────────────────────────────────────────
async function cargarSemanal(desde, hasta) {
  try {
    const data = await apiFetch(`/admin/reportes/ingresos/semanal?desde=${desde}&hasta=${hasta}`);
    renderizarGraficoSemanal(data.semanas ?? []);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar el gráfico semanal', 'error');
  }
}

function renderizarGraficoSemanal(semanas) {
  const ctx = document.getElementById('chart-semanal').getContext('2d');
  if (chartSemanal) chartSemanal.destroy();

  chartSemanal = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: semanas.map(s => formatearFechaSolo(s.inicioSemana)),
      datasets: [{
        label: 'Ingresos',
        data: semanas.map(s => Number(s.total)),
        backgroundColor: '#5C3317',
        borderRadius: 6,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true, ticks: { callback: v => `S/ ${v}` } } },
    },
  });
}

// ── Detalle de boletas ───────────────────────────────────────────────────────
async function cargarBoletas(desde, hasta) {
  boletasBody.innerHTML = '<tr><td colspan="4"><div class="spinner"></div></td></tr>';
  try {
    const boletas = await apiFetch(`/admin/reportes/boletas?desde=${desde}&hasta=${hasta}`);
    renderizarBoletas(boletas);
  } catch (error) {
    boletasBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">No se pudo cargar</td></tr>';
    mostrarToast(error?.message || 'Error al cargar las boletas', 'error');
  }
}

function renderizarBoletas(boletas) {
  if (!boletas.length) {
    boletasBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">Sin boletas en el rango</td></tr>';
    return;
  }
  boletasBody.innerHTML = boletas.map(b => `
    <tr>
      <td class="font-semibold">${escaparHtml(b.aliasTicket)}</td>
      <td>${formatearFecha(b.fecha)}</td>
      <td>${escaparHtml(b.mesero)}</td>
      <td style="text-align:right">${formatearMoneda(b.total)}</td>
    </tr>
  `).join('');
}

// ── Inicialización ───────────────────────────────────────────────────────────
function primerDiaDelMes(d) {
  return new Date(d.getFullYear(), d.getMonth(), 1);
}
function iso(d) {
  return d.toISOString().slice(0, 10); // YYYY-MM-DD
}

const hoy = new Date();
inputMes.value = iso(hoy).slice(0, 7);          // YYYY-MM
inputDesde.value = iso(primerDiaDelMes(hoy));
inputHasta.value = iso(hoy);

inputMes.addEventListener('change', () => {
  const [a, m] = inputMes.value.split('-').map(Number);
  if (a && m) cargarComparativa(a, m);
});

btnFiltrar.addEventListener('click', () => {
  if (!inputDesde.value || !inputHasta.value) {
    mostrarToast('Selecciona ambas fechas', 'warning');
    return;
  }
  cargarBoletas(inputDesde.value, inputHasta.value);
  cargarSemanal(inputDesde.value, inputHasta.value);
});

const [a0, m0] = inputMes.value.split('-').map(Number);
cargarComparativa(a0, m0);
cargarSemanal(inputDesde.value, inputHasta.value);
cargarBoletas(inputDesde.value, inputHasta.value);