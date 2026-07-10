export function mostrarToast(mensaje, tipo = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast toast--${tipo}`;
  toast.textContent = mensaje;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}

export function mostrarSpinner(contenedor) {
  contenedor.innerHTML = '<div class="spinner"></div>';
}

// Escapa texto para interpolarlo en innerHTML sin riesgo de inyección (B-F-11).
export function escaparHtml(valor) {
  if (valor == null) return '';
  const div = document.createElement('div');
  div.textContent = String(valor);
  return div.innerHTML;
}

export function mostrarVacio(contenedor, mensaje = 'No hay datos disponibles') {
  contenedor.innerHTML = `
    <div class="empty-state">
      <p>${mensaje}</p>
    </div>
  `;
}

export function crearModal({ titulo, contenido, onConfirm, labelConfirm = 'Confirmar', labelCancel = 'Cancelar' }) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `
    <div class="modal">
      <div class="modal-header">
        <h2 class="modal-title">${titulo}</h2>
        <button class="modal-close" aria-label="Cerrar">&times;</button>
      </div>
      <div class="modal-body">${contenido}</div>
      <div class="modal-footer">
        <button class="btn btn-secondary btn-cancel">${labelCancel}</button>
        <button class="btn btn-primary btn-confirm">${labelConfirm}</button>
      </div>
    </div>
  `;

  const cerrar = () => overlay.remove();
  overlay.querySelector('.modal-close').addEventListener('click', cerrar);
  overlay.querySelector('.btn-cancel').addEventListener('click', cerrar);
  overlay.querySelector('.btn-confirm').addEventListener('click', () => {
    onConfirm();
    cerrar();
  });

  document.body.appendChild(overlay);
  return overlay;
}
