/**
 * catalogo.js — Búsqueda estática y dinámica de cursos
 * Rama: feature/busqueda-productos (Paolo Añorga)
 *
 * BÚSQUEDA ESTÁTICA  : input grande de nombre + select de tipo de curso + botón "Buscar".
 *                      Solo se dispara al hacer clic en el botón (o al pulsar Enter en el input).
 *
 * BÚSQUEDA DINÁMICA  : input pequeño con debounce de 300 ms.
 *                      Los resultados cambian solos mientras el usuario escribe.
 *
 * Ambas usan el mismo endpoint GET /catalogo/buscar?texto=...&idTipoCurso=...
 * El servidor devuelve el fragmento Thymeleaf "resultadosCursos" (HTML puro, no JSON).
 * El JS solo reemplaza el innerHTML de #cursosGrid — nunca arma tarjetas por su cuenta.
 */

(function () {
  'use strict';

  /* ── Referencias al DOM ──────────────────────────────────────────── */
  const campoEstatico      = document.getElementById('campoEstatico');
  const selectTipo         = document.getElementById('selectTipo');
  const btnEstatico        = document.getElementById('btnEstatico');
  const btnLimpiar         = document.getElementById('btnLimpiar');
  const campoDinamico      = document.getElementById('campoDinamico');
  const btnLimpiarDinamico = document.getElementById('btnLimpiarDinamico');
  const cursosGrid         = document.getElementById('cursosGrid');

  if (!cursosGrid) return; // protección mínima

  let debounceTimer = null;

  /* ── Función central de búsqueda ─────────────────────────────────── */
  /**
   * Pide al servidor el fragmento de tarjetas filtradas y lo pega en #cursosGrid.
   *
   * @param {string}  texto       - Término de nombre (puede ser vacío).
   * @param {string}  tipoCurso   - ID del tipo de curso seleccionado (puede ser vacío/"").
   */
  function buscar(texto, tipoCurso) {
    const params = new URLSearchParams();
    if (texto && texto.trim())  params.set('texto', texto.trim());
    if (tipoCurso)              params.set('idTipoCurso', tipoCurso);

    fetch('/catalogo/buscar?' + params.toString())
      .then(function (respuesta) {
        if (!respuesta.ok) {
          throw new Error('Error del servidor: ' + respuesta.status);
        }
        return respuesta.text();
      })
      .then(function (html) {
        cursosGrid.innerHTML = html;
      })
      .catch(function (error) {
        console.error('[catalogo.js] Error en la búsqueda:', error);
      });
  }

  /* ══════════════════════════════════════════════════════════════════ */
  /* BÚSQUEDA ESTÁTICA                                                  */
  /* input de nombre + select de tipo + botón Buscar / Quitar filtros   */
  /* ══════════════════════════════════════════════════════════════════ */

  if (btnEstatico) {
    btnEstatico.addEventListener('click', function () {
      var texto = campoEstatico ? campoEstatico.value : '';
      var tipo  = selectTipo    ? selectTipo.value    : '';
      if (campoDinamico) campoDinamico.value = '';
      if (btnLimpiarDinamico) btnLimpiarDinamico.style.display = 'none';
      buscar(texto, tipo);
    });
  }

  /* Enter dentro del input grande también dispara la búsqueda estática */
  if (campoEstatico) {
    campoEstatico.addEventListener('keydown', function (evento) {
      if (evento.key === 'Enter') {
        var tipo = selectTipo ? selectTipo.value : '';
        if (campoDinamico) campoDinamico.value = '';
        if (btnLimpiarDinamico) btnLimpiarDinamico.style.display = 'none';
        buscar(campoEstatico.value, tipo);
      }
    });
  }

  /* Botón Quitar Filtros: restablece ambos filtros y restaura todos los cursos */
  if (btnLimpiar) {
    btnLimpiar.addEventListener('click', function () {
      if (campoEstatico) campoEstatico.value = '';
      if (selectTipo)    selectTipo.value = '';
      if (campoDinamico) campoDinamico.value = '';
      if (btnLimpiarDinamico) btnLimpiarDinamico.style.display = 'none';
      buscar('', '');
    });
  }

  /* ══════════════════════════════════════════════════════════════════ */
  /* BÚSQUEDA DINÁMICA                                                  */
  /* input en tiempo real → debounce 300 ms → busca por nombre          */
  /* ══════════════════════════════════════════════════════════════════ */

  function actualizarBotonLimpiarDinamico() {
    if (!btnLimpiarDinamico || !campoDinamico) return;
    btnLimpiarDinamico.style.display = campoDinamico.value.trim().length > 0 ? 'inline-flex' : 'none';
  }

  if (campoDinamico) {
    campoDinamico.addEventListener('input', function () {
      actualizarBotonLimpiarDinamico();
      clearTimeout(debounceTimer);
      var textoActual = campoDinamico.value;
      debounceTimer = setTimeout(function () {
        buscar(textoActual, ''); // sin filtro de tipo en la búsqueda dinámica
      }, 300);
    });
  }

  if (btnLimpiarDinamico) {
    btnLimpiarDinamico.addEventListener('click', function () {
      if (campoDinamico) {
        campoDinamico.value = '';
        campoDinamico.focus();
      }
      actualizarBotonLimpiarDinamico();
      buscar('', '');
    });
  }

})();
