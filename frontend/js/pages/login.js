import { apiFetch } from '../core/api.js';
import { guardarSesion } from '../core/auth.js';
import { redirigirPorRol } from '../core/router.js';
import { mostrarToast } from '../utils/dom.js';

const form = document.getElementById('login-form');
const btnSubmit = document.getElementById('btn-login');
const inputCorreo = document.getElementById('correo');
const inputClave = document.getElementById('clave');
const errorMsg = document.getElementById('error-msg');

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  errorMsg.textContent = '';
  btnSubmit.disabled = true;
  btnSubmit.textContent = 'Ingresando...';

  try {
    const data = await apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        correo: inputCorreo.value.trim(),
        contraseña: inputClave.value,         // 👈 fix 1
      }),
    });

    guardarSesion(data.token, {             // 👈 fix 2
      correo: data.correo,
      rol: data.rol,
      nombreCompleto: data.nombreCompleto
    });
    redirigirPorRol();
  } catch (error) {
    errorMsg.textContent = error?.message || 'Correo o contraseña incorrectos';
    mostrarToast(error?.message || 'Error al iniciar sesión', 'error');
  } finally {
    btnSubmit.disabled = false;
    btnSubmit.textContent = 'Ingresar';
  }
});