/**
 * catalogo.js — Búsqueda estática y dinámica de cursos en conjunto
 * Rama: feature/busqueda-productos (Paolo Añorga)
 *
 * BÚSQUEDA ESTÁTICA  : input de nombre + select de tipo de curso + botón "Buscar".
 *                      Se dispara al hacer clic en el botón (o al pulsar Enter en el input).
 *
 * BÚSQUEDA DINÁMICA  : input de descripción con debounce de 300 ms.
 *                      Los resultados se filtran en tiempo real mientras el usuario escribe.
 *
 * BÚSQUEDA CONJUNTA  : Ambos buscadores operan en conjunto: si el usuario ingresa
 *                      parámetros en la búsqueda estática (nombre y/o tipo) y en la dinámica
 *                      (descripción), se consultan los 3 campos a la vez.
 *
 * Endpoint: GET /catalogo/buscar?texto=...&descripcion=...&idTipoCurso=...
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
   * @param {string}  descripcion - Término de descripción (puede ser vacío).
   * @param {string}  tipoCurso   - ID del tipo de curso seleccionado (puede ser vacío/"").
   */
  function buscar(texto, descripcion, tipoCurso) {
    const params = new URLSearchParams();
    if (texto && texto.trim())              params.set('texto', texto.trim());
    if (descripcion && descripcion.trim()) params.set('descripcion', descripcion.trim());
    if (tipoCurso)                         params.set('idTipoCurso', tipoCurso);

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
        cursosGrid.innerHTML =
          '<div class="busqueda-sin-resultados">' +
          '<i class="fa-solid fa-triangle-exclamation busqueda-sin-resultados-icon"></i>' +
          '<h3 class="busqueda-sin-resultados-titulo">Error al realizar la búsqueda</h3>' +
          '<p class="busqueda-sin-resultados-texto">Ocurrió un inconveniente al conectar con el servidor. Por favor, intenta de nuevo.</p>' +
          '</div>';
      });
  }

  /**
   * Ejecuta la búsqueda unificada leyendo los valores actuales
   * de los campos estáticos (nombre, tipo) y del campo dinámico (descripción).
   */
  function ejecutarBusqueda() {
    var texto       = campoEstatico ? campoEstatico.value : '';
    var tipo        = selectTipo    ? selectTipo.value    : '';
    var descripcion = campoDinamico ? campoDinamico.value : '';
    buscar(texto, descripcion, tipo);
  }

  /* ══════════════════════════════════════════════════════════════════ */
  /* BÚSQUEDA ESTÁTICA                                                  */
  /* input de nombre + select de tipo + botón Buscar / Quitar filtros   */
  /* ══════════════════════════════════════════════════════════════════ */

  if (btnEstatico) {
    btnEstatico.addEventListener('click', function () {
      ejecutarBusqueda();
    });
  }

  /* Enter dentro del input de nombre también dispara la búsqueda */
  if (campoEstatico) {
    campoEstatico.addEventListener('keydown', function (evento) {
      if (evento.key === 'Enter') {
        ejecutarBusqueda();
      }
    });
  }

  /* Cambio directo en el selector de tipo: aplica el filtro inmediatamente */
  if (selectTipo) {
    selectTipo.addEventListener('change', function () {
      ejecutarBusqueda();
    });
  }

  /* Botón Quitar Filtros: restablece ambos filtros y restaura todos los cursos */
  if (btnLimpiar) {
    btnLimpiar.addEventListener('click', function () {
      if (campoEstatico) campoEstatico.value = '';
      if (selectTipo)    selectTipo.value = '';
      if (campoDinamico) campoDinamico.value = '';
      if (btnLimpiarDinamico) btnLimpiarDinamico.style.display = 'none';
      buscar('', '', '');
    });
  }

  /* ══════════════════════════════════════════════════════════════════ */
  /* BÚSQUEDA DINÁMICA                                                  */
  /* input en tiempo real → debounce 300 ms → busca por descripción     */
  /* ══════════════════════════════════════════════════════════════════ */

  function actualizarBotonLimpiarDinamico() {
    if (!btnLimpiarDinamico || !campoDinamico) return;
    btnLimpiarDinamico.style.display = campoDinamico.value.trim().length > 0 ? 'inline-flex' : 'none';
  }

  if (campoDinamico) {
    campoDinamico.addEventListener('input', function () {
      actualizarBotonLimpiarDinamico();
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(function () {
        ejecutarBusqueda();
      }, 300);
    });

    /* Enter inmediato en campo dinámico */
    campoDinamico.addEventListener('keydown', function (evento) {
      if (evento.key === 'Enter') {
        clearTimeout(debounceTimer);
        ejecutarBusqueda();
      }
    });
  }

  if (btnLimpiarDinamico) {
    btnLimpiarDinamico.addEventListener('click', function () {
      if (campoDinamico) {
        campoDinamico.value = '';
        campoDinamico.focus();
      }
      actualizarBotonLimpiarDinamico();
      ejecutarBusqueda();
    });
  }

})();
