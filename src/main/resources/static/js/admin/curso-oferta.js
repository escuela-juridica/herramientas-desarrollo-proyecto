/**
 * curso-oferta.js — Sección "Oferta" del formulario de cursos.
 *
 * - La oferta solo se puede activar si el curso tiene un precio mayor a S/ 0: mientras no lo tenga, el check queda
 *   deshabilitado y se muestra un aviso. Si se borra el precio con la oferta activa, la oferta se apaga.
 * - El check "Activar oferta" muestra u oculta el porcentaje, la temática y la fecha límite. Con el check apagado esos
 *   campos quedan deshabilitados, así que no se envían y el curso se guarda sin oferta.
 * - Un resumen muestra en vivo el precio que verá el cliente (precio original, precio con descuento y ahorro).
 * - Con el check encendido los tres campos son obligatorios: al guardar, los que falten se marcan con borde rojo y un
 *   mensaje debajo, y el formulario no se envía. El servidor repite las mismas reglas solo como respaldo.
 */

(function () {
  'use strict';

  var formulario = document.getElementById('formCurso');
  var activar = document.getElementById('ofertaActiva');
  var contenedor = document.getElementById('ofertaCampos');
  var precio = document.getElementById('precio');
  var aviso = document.getElementById('avisoPrecioOferta');
  if (!formulario || !activar || !contenedor || !precio) return;

  var CAMPOS = {
    descuentoPorcentaje: 'Selecciona el porcentaje de descuento.',
    tematicaOferta: 'Selecciona la temática de la oferta.',
    ofertaHasta: 'Indica hasta cuándo es válida la oferta.'
  };

  function campo(id) {
    return document.getElementById(id);
  }

  function soles(centimos) {
    return 'S/ ' + (centimos / 100).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  /** Precio del curso en céntimos, o 0 si está vacío o no es válido. */
  function precioEnCentimos() {
    var valor = parseFloat(precio.value);
    return isFinite(valor) && valor > 0 ? Math.round(valor * 100) : 0;
  }

  function marcar(id, mensaje) {
    var elemento = campo(id);
    var texto = document.getElementById('error-' + id);
    elemento.classList.toggle('invalido', mensaje !== '');
    elemento.setAttribute('aria-invalid', mensaje !== '' ? 'true' : 'false');
    texto.textContent = mensaje;
    texto.hidden = mensaje === '';
  }

  function limpiar() {
    Object.keys(CAMPOS).forEach(function (id) {
      marcar(id, '');
    });
  }

  /** Precio con descuento: mismo redondeo (mitad hacia arriba) que calcula el servidor. */
  function actualizarResumen() {
    var base = precioEnCentimos();
    var porcentaje = parseInt(campo('descuentoPorcentaje').value, 10);
    var anterior = campo('resumenAnterior');
    var final = campo('resumenFinal');
    var ahorro = campo('resumenAhorro');

    anterior.textContent = soles(base);
    if (!base) {
      final.textContent = soles(0);
      ahorro.textContent = 'Ingresa el precio del curso para calcular el precio con descuento.';
    } else if (!porcentaje) {
      final.textContent = soles(base);
      ahorro.textContent = 'Elige un porcentaje para ver el precio con descuento.';
    } else {
      var conDescuento = Math.floor((base * (100 - porcentaje) + 50) / 100);
      final.textContent = soles(conDescuento);
      ahorro.textContent = 'El cliente ahorra ' + soles(base - conDescuento) + ' (' + porcentaje + ' %).';
    }
  }

  function aplicarEstado() {
    var hayPrecio = precioEnCentimos() > 0;
    if (!hayPrecio) activar.checked = false;
    activar.disabled = !hayPrecio;
    if (aviso) aviso.hidden = hayPrecio;

    var activa = activar.checked;
    contenedor.hidden = !activa;
    Object.keys(CAMPOS).forEach(function (id) {
      campo(id).disabled = !activa;
    });
    if (!activa) limpiar();
    actualizarResumen();
  }

  activar.addEventListener('change', aplicarEstado);
  precio.addEventListener('input', aplicarEstado);
  aplicarEstado();

  // Al corregir un campo, su error desaparece y el resumen se actualiza
  Object.keys(CAMPOS).forEach(function (id) {
    function alCambiar() {
      if (campo(id).value) marcar(id, '');
      actualizarResumen();
    }
    campo(id).addEventListener('change', alCambiar);
    campo(id).addEventListener('input', alCambiar);
  });

  formulario.addEventListener('submit', function (evento) {
    if (!activar.checked) return;
    var primero = null;
    Object.keys(CAMPOS).forEach(function (id) {
      var falta = !campo(id).value;
      marcar(id, falta ? CAMPOS[id] : '');
      if (falta && !primero) primero = campo(id);
    });
    if (primero) {
      evento.preventDefault();
      primero.scrollIntoView({ behavior: 'smooth', block: 'center' });
      primero.focus({ preventScroll: true });
    }
  });
})();
