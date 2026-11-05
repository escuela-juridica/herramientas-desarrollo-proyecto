/**
 * pago-modal.js — Modal de pago del carrito (estilo checkout de pasarela, sin pasarela real).
 *
 * "Confirmar compra" abre el modal con los datos del comprador y el medio de pago (tarjeta, Yape o transferencia).
 * Al pagar se validan los datos, se muestra la pantalla "Procesando tu pago…" y recién entonces se envía el formulario
 * a /carrito/comprar. Los datos de tarjeta, Yape y operación son de demostración: esos campos no tienen atributo
 * name, así que nunca viajan al servidor ni se guardan; el servidor solo recibe los datos de la boleta y el medio.
 *
 * Datos de prueba: 4111 1111 1111 1111 (aprobada) y 4000 0000 0000 0002 (rechazada).
 * Atajo: Alt + R con el modal abierto llena todos los campos con datos de prueba.
 */

(function () {
  'use strict';

  var modal = document.getElementById('modalPago');
  var formulario = document.getElementById('formCompra');
  if (!modal || !formulario) return;

  var cajaErrores = document.getElementById('cqErrores');
  var listaErrores = document.getElementById('cqErroresLista');
  var botonPagar = document.getElementById('btnPagar');
  var procesando = document.getElementById('cqProcesando');
  var spinner = document.getElementById('cqSpinner');
  var iconoOk = document.getElementById('cqOk');
  var textoProcesando = document.getElementById('cqProcesandoTexto');
  var marca = document.getElementById('tarjMarca');

  function campo(id) {
    return document.getElementById(id);
  }

  function medioSeleccionado() {
    var marcado = formulario.querySelector('input[name="medioPago"]:checked');
    return marcado ? marcado.value : 'TARJETA';
  }

  /* ── Medios de pago: se muestra solo el panel del medio elegido ───────── */
  function mostrarPanel() {
    var medio = medioSeleccionado();
    modal.querySelectorAll('.cq-panel').forEach(function (panel) {
      panel.hidden = panel.getAttribute('data-panel') !== medio;
    });
  }

  formulario.querySelectorAll('input[name="medioPago"]').forEach(function (radio) {
    radio.addEventListener('change', function () {
      mostrarPanel();
      // Para la demostración: cada medio de pago aparece con datos de prueba, sin pisar lo que ya se escribió
      rellenarMedio(medioSeleccionado(), false);
    });
  });
  mostrarPanel();

  /* ── Formato de los campos mientras se escribe ────────────────────────── */
  function soloDigitos(valor) {
    return valor.replace(/\D/g, '');
  }

  function marcaTarjeta(digitos) {
    if (/^4/.test(digitos)) return 'fa-brands fa-cc-visa';
    if (/^(5[1-5]|2[2-7])/.test(digitos)) return 'fa-brands fa-cc-mastercard';
    return 'fa-regular fa-credit-card';
  }

  campo('tarjNumero').addEventListener('input', function (e) {
    var digitos = soloDigitos(e.target.value).slice(0, 16);
    e.target.value = digitos.replace(/(.{4})/g, '$1 ').trim();
    marca.className = marcaTarjeta(digitos);
  });

  campo('tarjVence').addEventListener('input', function (e) {
    var digitos = soloDigitos(e.target.value).slice(0, 4);
    e.target.value = digitos.length > 2 ? digitos.slice(0, 2) + '/' + digitos.slice(2) : digitos;
  });

  ['tarjCvv', 'yapeCelular', 'yapeCodigo', 'transOperacion', 'documento'].forEach(function (id) {
    campo(id).addEventListener('input', function (e) {
      e.target.value = soloDigitos(e.target.value);
    });
  });

  /* ── Atajo: rellenar con datos de prueba ──────────────────────────────── */
  // Personas ficticias; los DNI no coinciden con los clientes de demostración del script.
  var PERSONAS = [
    { nombres: 'Ana María', apellidos: 'Torres Vega', documento: '72345618', telefono: '987654321', correo: 'ana.torres@example.com' },
    { nombres: 'Luis Fernando', apellidos: 'Rojas Campos', documento: '71234509', telefono: '986123450', correo: 'luis.rojas@example.com' },
    { nombres: 'Valeria', apellidos: 'Salas Quiroz', documento: '70987654', telefono: '985432167', correo: 'valeria.salas@example.com' },
    { nombres: 'Diego Alonso', apellidos: 'Paredes Luna', documento: '76543210', telefono: '984321098', correo: 'diego.paredes@example.com' }
  ];

  function poner(id, valor) {
    var el = campo(id);
    el.value = valor;
    // el evento "input" reaplica el formato (espacios de la tarjeta, "/" del vencimiento, marca de la tarjeta)
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }

  function rellenarPrueba() {
    var persona = PERSONAS[Math.floor(Math.random() * PERSONAS.length)];
    poner('nombres', persona.nombres);
    poner('apellidos', persona.apellidos);
    poner('documento', persona.documento);
    poner('telefono', persona.telefono);
    poner('correo', persona.correo);

    rellenarMedio(medioSeleccionado(), true);
    mostrarErrores([]);
  }

  /**
   * Rellena los campos del medio de pago indicado con datos de prueba.
   * Con sobrescribir en false solo se llenan los campos que están vacíos.
   */
  function rellenarMedio(medio, sobrescribir) {
    function ponerSiVacio(id, valor) {
      if (sobrescribir || campo(id).value.trim() === '') poner(id, valor);
    }

    if (medio === 'TARJETA') {
      var anio = String((new Date().getFullYear() + 3) % 100);
      ponerSiVacio('tarjNumero', '4111111111111111');
      ponerSiVacio('tarjVence', '12' + (anio.length < 2 ? '0' + anio : anio));
      ponerSiVacio('tarjCvv', '123');
    } else if (medio === 'YAPE') {
      // el celular de Yape es el teléfono ya escrito, si es un celular válido; si no, uno de prueba
      var telefono = campo('telefono').value.replace(/\D/g, '');
      ponerSiVacio('yapeCelular', /^9[0-9]{8}$/.test(telefono) ? telefono : '987654321');
      ponerSiVacio('yapeCodigo', '123456');
    } else {
      ponerSiVacio('transOperacion', '12345678');
    }
  }

  modal.addEventListener('keydown', function (evento) {
    if (evento.altKey && !evento.ctrlKey && !evento.shiftKey && evento.key.toLowerCase() === 'r') {
      evento.preventDefault();
      rellenarPrueba();
    }
  });

  /* ── Validaciones ─────────────────────────────────────────────────────── */
  function luhn(digitos) {
    var suma = 0;
    var doble = false;
    for (var i = digitos.length - 1; i >= 0; i--) {
      var n = parseInt(digitos.charAt(i), 10);
      if (doble) {
        n *= 2;
        if (n > 9) n -= 9;
      }
      suma += n;
      doble = !doble;
    }
    return suma % 10 === 0;
  }

  function vencimientoValido(texto) {
    var m = /^(\d{2})\/(\d{2})$/.exec(texto);
    if (!m) return false;
    var mes = parseInt(m[1], 10);
    var anio = 2000 + parseInt(m[2], 10);
    if (mes < 1 || mes > 12) return false;
    var hoy = new Date();
    return anio > hoy.getFullYear() || (anio === hoy.getFullYear() && mes >= hoy.getMonth() + 1);
  }

  /** Devuelve la lista de { id, mensaje } de lo que falta o está mal. */
  function validar() {
    var errores = [];
    function falla(id, mensaje) {
      errores.push({ id: id, mensaje: mensaje });
    }

    if (!campo('nombres').value.trim()) falla('nombres', 'Ingresa tus nombres.');
    if (!campo('apellidos').value.trim()) falla('apellidos', 'Ingresa tus apellidos.');
    if (!/^[0-9]{8}$/.test(campo('documento').value.trim())) falla('documento', 'El DNI debe tener 8 dígitos.');
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(campo('correo').value.trim())) falla('correo', 'Ingresa un correo válido.');
    var telefono = campo('telefono').value.trim();
    if (telefono && !/^[0-9+ ]{6,20}$/.test(telefono)) {
      falla('telefono', 'El teléfono solo puede tener números, espacios o +.');
    }

    var medio = medioSeleccionado();
    if (medio === 'TARJETA') {
      var numero = soloDigitos(campo('tarjNumero').value);
      if (numero.length !== 16 || !luhn(numero)) falla('tarjNumero', 'El número de tarjeta no es válido.');
      if (!vencimientoValido(campo('tarjVence').value)) falla('tarjVence', 'La fecha de vencimiento no es válida.');
      if (!/^[0-9]{3,4}$/.test(campo('tarjCvv').value)) falla('tarjCvv', 'El CVV debe tener 3 o 4 dígitos.');
    } else if (medio === 'YAPE') {
      if (!/^9[0-9]{8}$/.test(campo('yapeCelular').value)) falla('yapeCelular', 'El celular debe tener 9 dígitos y empezar con 9.');
      if (!/^[0-9]{6}$/.test(campo('yapeCodigo').value)) falla('yapeCodigo', 'El código de aprobación tiene 6 dígitos.');
    } else if (!/^[0-9]{6,12}$/.test(campo('transOperacion').value)) {
      falla('transOperacion', 'El número de operación debe tener entre 6 y 12 dígitos.');
    }
    return errores;
  }

  function limpiarMarcas() {
    formulario.querySelectorAll('.invalido').forEach(function (el) {
      el.classList.remove('invalido');
    });
  }

  function mostrarErrores(errores) {
    limpiarMarcas();
    listaErrores.innerHTML = '';
    errores.forEach(function (error) {
      var li = document.createElement('li');
      li.textContent = error.mensaje;
      listaErrores.appendChild(li);
      if (error.id) campo(error.id).classList.add('invalido');
    });
    cajaErrores.hidden = errores.length === 0;
    if (errores.length) {
      modal.querySelector('.cq-cuerpo').scrollTop = 0;
      var primero = errores[0].id && campo(errores[0].id);
      if (primero) primero.focus();
    }
  }

  /* ── Envío: validar, simular el cobro y enviar ────────────────────────── */
  function reiniciarProceso() {
    procesando.hidden = true;
    spinner.hidden = false;
    iconoOk.hidden = true;
    textoProcesando.textContent = 'Procesando tu pago…';
    botonPagar.disabled = false;
  }

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    var errores = validar();
    mostrarErrores(errores);
    if (errores.length) return;

    botonPagar.disabled = true;
    procesando.hidden = false;

    setTimeout(function () {
      var rechazada = medioSeleccionado() === 'TARJETA' && soloDigitos(campo('tarjNumero').value) === '4000000000000002';
      if (rechazada) {
        reiniciarProceso();
        mostrarErrores([{ id: 'tarjNumero', mensaje: 'La tarjeta fue rechazada por el banco emisor. Prueba con otra tarjeta.' }]);
        return;
      }
      spinner.hidden = true;
      iconoOk.hidden = false;
      textoProcesando.textContent = '¡Pago aprobado!';
      setTimeout(function () {
        // submit() no dispara de nuevo el evento "submit", así que no hay bucle
        formulario.submit();
      }, 900);
    }, 1700);
  });

  modal.addEventListener('hidden.bs.modal', function () {
    reiniciarProceso();
  });

  /* Si el servidor devolvió errores de los datos, se vuelve a abrir el modal con lo que ya se había escrito */
  if (modal.getAttribute('data-abrir') === 'true' && window.bootstrap) {
    window.bootstrap.Modal.getOrCreateInstance(modal).show();
  }
})();
