import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner } from '../utils/dom.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('stock-tbody');
const form = document.getElementById('form-ingreso');
const inputBuscar = document.getElementById('input-buscar-stock');

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
  } catch (error) {
    mostrarToast(error?.message || 'No se pudo registrar el ingreso', 'error');
  } finally {
    btn.disabled = false;
  }
});

cargarAlmacenes();
cargarInsumosSelect();
cargarStock();
