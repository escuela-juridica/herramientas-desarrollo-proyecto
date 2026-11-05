/**
 * contacto.js — Formulario de Contacto.
 *
 * - Validación de campos en el navegador: al enviar, los campos incorrectos se marcan con borde rojo y un mensaje
 *   debajo, y el formulario no se envía hasta que todo esté bien. El servidor repite las mismas reglas solo como
 *   respaldo (por si alguien salta el JavaScript).
 * - El texto de ayuda del mensaje cambia según el motivo elegido (consulta, pedido u opinión).
 * - Contador de caracteres del mensaje (máximo 1000, el mismo límite que valida el servidor).
 */

(function () {
  'use strict';

  var mensaje = document.getElementById('mensaje');
  var contador = document.getElementById('contadorMensaje');
  var motivos = document.querySelectorAll('.motivos input[type="radio"]');
  if (!mensaje) return;

  var AYUDAS = {
    CONSULTA: 'Cuéntanos tu consulta: fechas, modalidades, costos, certificados…',
    PEDIDO: 'Cuéntanos qué curso o capacitación necesitas y para cuántas personas…',
    OPINION: 'Cuéntanos tu experiencia con Escuela Jurídica y cómo podemos mejorar…'
  };

  function actualizarAyuda() {
    var elegido = document.querySelector('.motivos input[type="radio"]:checked');
    mensaje.placeholder = AYUDAS[elegido ? elegido.value : 'CONSULTA'];
  }

  function actualizarContador() {
    if (contador) contador.textContent = mensaje.value.length + ' / ' + mensaje.maxLength;
  }

  motivos.forEach(function (radio) {
    radio.addEventListener('change', actualizarAyuda);
  });
  mensaje.addEventListener('input', actualizarContador);

  actualizarAyuda();
  actualizarContador();

  /* ── Validación de campos ───────────────────────────────────────────── */
  var formulario = document.getElementById('formContacto');

  // Cada regla devuelve el mensaje de error, o '' si el campo está bien.
  var REGLAS = {
    nombres: function (valor) {
      return valor.trim() ? '' : 'Ingresa tu nombre.';
    },
    telefono: function (valor) {
      var v = valor.trim();
      return !v || /^[0-9+ ]{6,20}$/.test(v) ? '' : 'El teléfono solo puede tener números, espacios o +.';
    },
    correo: function (valor) {
      return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim()) ? '' : 'Ingresa un correo válido.';
    },
    mensaje: function (valor) {
      var v = valor.trim();
      if (!v) return 'Escribe tu mensaje.';
      return v.length > 1000 ? 'El mensaje no puede superar los 1000 caracteres.' : '';
    }
  };

  function mostrarError(id, texto) {
    var campo = document.getElementById(id);
    var aviso = document.getElementById('error-' + id);
    campo.classList.toggle('invalido', texto !== '');
    campo.setAttribute('aria-invalid', texto !== '' ? 'true' : 'false');
    aviso.textContent = texto;
    aviso.hidden = texto === '';
  }

  function validarCampo(id) {
    var texto = REGLAS[id](document.getElementById(id).value);
    mostrarError(id, texto);
    return texto === '';
  }

  if (formulario) {
    var intentado = false;

    formulario.addEventListener('submit', function (evento) {
      intentado = true;
      var primeroConError = null;
      Object.keys(REGLAS).forEach(function (id) {
        if (!validarCampo(id) && !primeroConError) primeroConError = document.getElementById(id);
      });
      if (primeroConError) {
        evento.preventDefault();
        primeroConError.focus();
      }
    });

    // Tras el primer intento, cada campo se vuelve a revisar mientras se corrige y al salir de él
    Object.keys(REGLAS).forEach(function (id) {
      var campo = document.getElementById(id);
      campo.addEventListener('input', function () {
        if (intentado) validarCampo(id);
      });
      campo.addEventListener('blur', function () {
        if (intentado || campo.value.trim() !== '') validarCampo(id);
      });
    });
  }
})();
