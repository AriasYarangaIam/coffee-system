// Control de stock (admin): registrar ingreso, ver Ultimos Ingresos y Deshacer (Pila en el backend).
import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner, escaparHtml } from '../utils/dom.js';
import { formatearFecha } from '../utils/format.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('stock-tbody');
const form = document.getElementById('form-ingreso');
const inputBuscar = document.getElementById('input-buscar-stock');
const historial = document.getElementById('stock-historial');
const btnDeshacer = document.getElementById('btn-deshacer');

let stocksCache = [];

async function cargarStock() {
  mostrarSpinner(tablaBody);
  try {
    stocksCache = await apiFetch('/admin/stocks');
    renderizarTabla(stocksCache);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar stock', 'error');
  }
}

// "Últimos Ingresos": traza de movimientos vigentes. Habilita "Deshacer" si hay alguno.
async function cargarMovimientos() {
  mostrarSpinner(historial);
  try {
    const movimientos = await apiFetch('/admin/stocks/movimientos');
    renderizarHistorial(movimientos);
    btnDeshacer.disabled = movimientos.length === 0;
  } catch (error) {
    historial.innerHTML = '<p class="text-muted text-sm">No se pudo cargar el historial</p>';
    mostrarToast(error?.message || 'Error al cargar el historial', 'error');
  }
}

function renderizarHistorial(movimientos) {
  if (!movimientos.length) {
    historial.innerHTML = '<div class="empty-state"><p>Aún no hay ingresos registrados</p></div>';
    return;
  }
  historial.innerHTML = movimientos.map(m => `
    <div class="stock-mov">
      <div class="stock-mov__main">
        <span class="stock-mov__name">${escaparHtml(m.nombreInsumo)}</span>
        <span class="stock-mov__qty">+${m.cantidad}${m.unidad ? ` ${escaparHtml(m.unidad)}` : ''}</span>
      </div>
      <div class="stock-mov__meta">
        <span>${escaparHtml(m.registradoPor)}</span>
        <span>${formatearFecha(m.fecha)}</span>
      </div>
    </div>
  `).join('');
}

// El select de ingreso lista TODOS los insumos (no solo los que ya tienen stock),
// para poder registrar el primer ingreso de un insumo recién creado.
async function cargarInsumosSelect() {
  const select = document.getElementById('select-insumo');
  if (!select) return;
  try {
    const insumos = await apiFetch('/admin/insumos');
    select.innerHTML = '<option value="">Seleccionar insumo...</option>' +
      insumos.map(i => `<option value="${i.idInsumo}">${i.nombreInsumo}${i.unidad ? ` (${i.unidad})` : ''}</option>`).join('');
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar insumos', 'error');
  }
}

function renderizarTabla(stocks) {
  if (!stocks.length) {
    tablaBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted" style="padding:24px">Sin registros de stock</td></tr>';
    return;
  }
  tablaBody.innerHTML = stocks.map(s => `
    <tr>
      <td>${s.nombreInsumo}</td>
      <td style="text-align:right">${s.cantidad}</td>
      <td>${s.unidad ?? '—'}</td>
      <td>${s.nombreAlmacen ?? '—'}</td>
    </tr>
  `).join('');
}

// Filtro de la tabla de stock por nombre de insumo.
inputBuscar?.addEventListener('input', () => {
  const q = inputBuscar.value.trim().toLowerCase();
  renderizarTabla(stocksCache.filter(s => s.nombreInsumo.toLowerCase().includes(q)));
});

async function cargarAlmacenes() {
  const select = document.getElementById('select-almacen');
  if (!select) return;
  try {
    const almacenes = await apiFetch('/admin/almacenes');
    select.innerHTML = '<option value="">Seleccionar almacén...</option>' +
      almacenes.map(a => `<option value="${a.almacenId}">${a.nombreAlmacen}</option>`).join('');
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar almacenes', 'error');
  }
}

form?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = form.querySelector('button[type="submit"]');
  btn.disabled = true;
  try {
    await apiFetch('/admin/stocks', {
      method: 'POST',
      body: JSON.stringify({
        insumoId: Number(document.getElementById('select-insumo').value),
        almacenId: Number(document.getElementById('select-almacen').value),
        cantidad: Number(document.getElementById('input-cantidad').value),
      }),
    });
    mostrarToast('Ingreso registrado correctamente', 'success');
    form.reset();
    cargarStock();
    cargarMovimientos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo registrar el ingreso', 'error');
  } finally {
    btn.disabled = false;
  }
});

// Deshacer el último ingreso (LIFO). El backend revierte el saldo y marca el movimiento.
btnDeshacer?.addEventListener('click', async () => {
  btnDeshacer.disabled = true;
  try {
    await apiFetch('/admin/stocks/deshacer', { method: 'POST' });
    mostrarToast('Último ingreso deshecho', 'success');
    cargarStock();
    cargarMovimientos();
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo deshacer', 'error');
    btnDeshacer.disabled = false; // reintentable si falló
  }
});

cargarAlmacenes();
cargarInsumosSelect();
cargarStock();
cargarMovimientos();