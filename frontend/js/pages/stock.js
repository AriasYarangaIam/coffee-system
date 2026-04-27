import { requireRole } from '../core/auth.js';
import { apiFetch } from '../core/api.js';
import { mostrarToast, mostrarSpinner } from '../utils/dom.js';

requireRole('ADMIN');

const tablaBody = document.getElementById('stock-tbody');
const form = document.getElementById('form-ingreso');

async function cargarStock() {
  mostrarSpinner(tablaBody);
  try {
    const stocks = await apiFetch('/admin/stocks');
    renderizarTabla(stocks);
    poblarSelectInsumos(stocks);
  } catch (error) {
    mostrarToast(error?.message || 'Error al cargar stock', 'error');
  }
}

function renderizarTabla(stocks) {
  if (!stocks.length) {
    tablaBody.innerHTML = '<tr><td colspan="3" class="text-center text-muted" style="padding:24px">Sin registros de stock</td></tr>';
    return;
  }
  tablaBody.innerHTML = stocks.map(s => `
    <tr>
      <td>${s.nombreInsumo}</td>
      <td>${s.cantidad}</td>
      <td>${s.nombreAlmacen ?? '—'}</td>
    </tr>
  `).join('');
}

function poblarSelectInsumos(stocks) {
  const select = document.getElementById('select-insumo');
  if (!select) return;
  select.innerHTML = '<option value="">Seleccionar insumo...</option>' +
    stocks.map(s => `<option value="${s.insumoId}">${s.nombreInsumo}</option>`).join('');
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
        cantidad: Number(document.getElementById('input-cantidad').value),
        codigoAlmacen: Number(document.getElementById('select-almacen').value),
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

cargarStock();
