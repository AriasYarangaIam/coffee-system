// Formateadores es-PE: moneda (S/) y fechas.
export function formatearMoneda(monto) {
  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: 'PEN',
  }).format(monto);
  // Resultado: "S/ 12.50"
}

export function formatearFecha(isoString) {
  return new Date(isoString).toLocaleDateString('es-PE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
  // Resultado: "15/07/2025, 10:30"
}

export function formatearFechaSolo(isoString) {
  return new Date(isoString).toLocaleDateString('es-PE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}