import { apiFetch } from '../core/api.js';
import { guardarSesion } from '../core/auth.js';
import { redirigirPorRol } from '../core/router.js';
import { mostrarToast } from '../utils/dom.js';

const form       = document.getElementById('login-form');
const btnSubmit  = document.getElementById('btn-login');
const inputCorreo = document.getElementById('correo');
const inputClave  = document.getElementById('clave');
const errorMsg   = document.getElementById('error-msg');
const tabPersonal = document.getElementById('tab-personal');
const tabAdmin    = document.getElementById('tab-admin');
const toggleClave = document.getElementById('toggle-clave');

const PLACEHOLDERS = {
  personal: 'mesero@cafeteria.com',
  admin:    'admin@cafeteria.com',
};

// El perfil elegido en las pestañas debe coincidir con el rol real que devuelve el back.
const ROL_ESPERADO = { personal: 'MESERO', admin: 'ADMIN' };

let rolSeleccionado = 'personal';

function activarTab(rol) {
  rolSeleccionado = rol;
  tabPersonal.classList.toggle('active', rol === 'personal');
  tabAdmin.classList.toggle('active', rol === 'admin');
  inputCorreo.placeholder = PLACEHOLDERS[rol];
  errorMsg.textContent = '';
}

tabPersonal.addEventListener('click', () => activarTab('personal'));
tabAdmin.addEventListener('click',    () => activarTab('admin'));

// Ojito: mostrar/ocultar la contraseña.
const iconEye    = document.getElementById('icon-eye');
const iconEyeOff = document.getElementById('icon-eye-off');

toggleClave?.addEventListener('click', () => {
  const ocultar = inputClave.type === 'password';

  inputClave.type = ocultar ? 'text' : 'password';

  // Ojo abierto = contraseña visible | Ojo tachado = contraseña oculta
  iconEye.style.display    = ocultar ? 'block' : 'none';
  iconEyeOff.style.display = ocultar ? 'none'  : 'block';

  toggleClave.setAttribute('aria-label', ocultar ? 'Ocultar contraseña' : 'Mostrar contraseña');
});

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

    // Validar que el perfil elegido coincida con el rol real de la cuenta.
    if (data.rol !== ROL_ESPERADO[rolSeleccionado]) {
      errorMsg.textContent = 'Esta cuenta no corresponde al perfil seleccionado';
      mostrarToast('Esta cuenta no corresponde al perfil seleccionado', 'error');
      return;
    }

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