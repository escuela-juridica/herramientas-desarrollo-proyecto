/**
 * carrito.js — Agrega cursos al carrito desde los modales del catálogo y de Inicio.
 *
 * Cada botón "Agregar al carrito" lleva data-agregar-carrito y data-id (id del curso).
 * Se manda un POST a /carrito/agregar con el token CSRF; el servidor responde JSON { cantidad }
 * y aquí solo se actualiza el contador del menú y se muestra un aviso.
 */

(function () {
  'use strict';

  var badge = document.getElementById('carritoBadge');

  function leerMeta(nombre) {
    var meta = document.querySelector('meta[name="' + nombre + '"]');
    return meta ? meta.getAttribute('content') : '';
  }

  function actualizarBadge(cantidad) {
    if (!badge) return;
    badge.textContent = cantidad;
    badge.hidden = cantidad === 0;
    badge.classList.remove('carrito-badge-salto');
    // reinicia la animación
    void badge.offsetWidth;
    badge.classList.add('carrito-badge-salto');
  }

  function aviso(html, esError) {
    var anterior = document.querySelector('.toast-tienda');
    if (anterior) anterior.remove();
    var toast = document.createElement('div');
    toast.className = 'toast-tienda' + (esError ? ' toast-error' : '');
    toast.setAttribute('role', 'status');
    toast.innerHTML = html;
    document.body.appendChild(toast);
    requestAnimationFrame(function () { toast.classList.add('visible'); });
    setTimeout(function () {
      toast.classList.remove('visible');
      setTimeout(function () { toast.remove(); }, 300);
    }, 4000);
  }

  function agregar(boton) {
    var idCurso = boton.getAttribute('data-id');
    var cuerpo = new URLSearchParams();
    cuerpo.set('idCurso', idCurso);
    var cabeceras = { 'Content-Type': 'application/x-www-form-urlencoded' };
    cabeceras[leerMeta('_csrf_header') || 'X-CSRF-TOKEN'] = leerMeta('_csrf');

    boton.disabled = true;
    fetch('/carrito/agregar', { method: 'POST', headers: cabeceras, body: cuerpo.toString() })
      .then(function (respuesta) {
        return respuesta.json().then(function (datos) {
          if (!respuesta.ok) throw new Error(datos.mensaje || 'No se pudo agregar el curso.');
          return datos;
        });
      })
      .then(function (datos) {
        actualizarBadge(datos.cantidad);
        var modal = boton.closest('.modal');
        if (modal && window.bootstrap) {
          var instancia = window.bootstrap.Modal.getInstance(modal);
          if (instancia) instancia.hide();
        }
        aviso('<span><i class="fa-solid fa-circle-check"></i> ' + datos.mensaje + '</span>' +
          '<a href="/carrito">Ver carrito</a>', false);
      })
      .catch(function (error) {
        aviso('<span><i class="fa-solid fa-triangle-exclamation"></i> ' + error.message + '</span>', true);
      })
      .finally(function () {
        boton.disabled = false;
      });
  }

  // Delegación: los modales se renderizan una vez, pero así funciona con cualquier botón nuevo.
  document.addEventListener('click', function (evento) {
    var boton = evento.target.closest('[data-agregar-carrito]');
    if (boton) agregar(boton);
  });
})();
