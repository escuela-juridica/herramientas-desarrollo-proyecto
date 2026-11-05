/**
 * admin-reportes.js — Dibuja los 6 gráficos de /admin/dashboard con Chart.js y prepara las descargas.
 *
 * Los datos llegan del servidor en window.REPORTES (lista de reportes: tipoGrafico, etiquetas, valores…).
 * Al descargar un informe (Word, Excel o PDF) se manda también la imagen del gráfico en PNG, porque el gráfico
 * se dibuja aquí, en el navegador, y el servidor la inserta en el documento.
 */

(function () {
  'use strict';

  var reportes = window.REPORTES || [];

  // Colores categóricos validados (distinguibles con daltonismo y con contraste suficiente sobre blanco).
  var AZUL = '#2f6fb5';
  var NARANJA = '#e8710a';
  var VERDE = '#1f9d8f';
  var MORADO = '#8a4f9e';
  var CATEGORIAS = [AZUL, NARANJA, VERDE, MORADO];

  var COLOR_TEXTO = '#4b5565';
  var COLOR_REJILLA = '#e6e9ef';

  // Un color por reporte cuando hay una sola serie
  var COLOR_SERIE = { 1: AZUL, 2: VERDE, 5: NARANJA, 6: AZUL };

  var graficos = {};

  function soles(valor) {
    return 'S/ ' + Number(valor).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function formato(reporte, valor) {
    return reporte.monetario ? soles(valor) : Number(valor).toLocaleString('en-US');
  }

  function recortar(texto) {
    return texto.length > 30 ? texto.slice(0, 29) + '…' : texto;
  }

  // Fondo blanco para que la imagen exportada no salga transparente en Word, Excel o PDF
  var fondoBlanco = {
    id: 'fondoBlanco',
    beforeDraw: function (grafico) {
      var ctx = grafico.ctx;
      ctx.save();
      ctx.globalCompositeOperation = 'destination-over';
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, grafico.width, grafico.height);
      ctx.restore();
    }
  };

  function configuracion(reporte) {
    var circular = reporte.tipoGrafico === 'pie' || reporte.tipoGrafico === 'doughnut';
    var color = COLOR_SERIE[reporte.numero] || AZUL;
    var total = reporte.valores.reduce(function (a, b) { return a + Number(b); }, 0);

    var dataset = {
      label: reporte.columnaValor,
      data: reporte.valores,
      backgroundColor: circular ? CATEGORIAS.slice(0, reporte.valores.length) : color,
      borderColor: circular ? '#ffffff' : color,
      borderWidth: circular ? 3 : 2
    };

    if (reporte.tipoGrafico === 'bar') {
      dataset.borderRadius = 4;
      dataset.maxBarThickness = 38;
      dataset.borderWidth = 0;
    }
    if (reporte.tipoGrafico === 'line') {
      dataset.tension = 0.3;
      dataset.fill = false;
      dataset.pointRadius = 4;
      dataset.pointBackgroundColor = '#ffffff';
      dataset.pointBorderWidth = 2;
      dataset.pointHoverRadius = 6;
    }

    var opciones = {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 500 },
      plugins: {
        legend: {
          display: circular,
          position: 'bottom',
          labels: { color: COLOR_TEXTO, usePointStyle: true, boxWidth: 8, padding: 16, font: { size: 12 } }
        },
        tooltip: {
          callbacks: {
            label: function (contexto) {
              var valor = circular ? contexto.parsed : (reporte.horizontal ? contexto.parsed.x : contexto.parsed.y);
              var texto = formato(reporte, valor);
              if (circular && total > 0) {
                texto += ' (' + (valor * 100 / total).toFixed(1) + ' %)';
              }
              return (circular ? contexto.label + ': ' : '') + texto;
            }
          }
        }
      }
    };

    if (!circular) {
      var ejeValores = {
        beginAtZero: true,
        grid: { color: COLOR_REJILLA },
        border: { display: false },
        ticks: {
          color: COLOR_TEXTO,
          precision: 0,
          callback: function (valor) { return reporte.monetario ? 'S/ ' + valor : valor; }
        }
      };
      var ejeEtiquetas = {
        grid: { display: false },
        border: { color: COLOR_REJILLA },
        ticks: {
          color: COLOR_TEXTO,
          maxRotation: 0,
          autoSkip: false,
          callback: function (valor) { return recortar(String(this.getLabelForValue(valor))); }
        }
      };
      if (reporte.horizontal) {
        opciones.indexAxis = 'y';
        opciones.scales = { x: ejeValores, y: ejeEtiquetas };
      } else {
        opciones.scales = { x: ejeEtiquetas, y: ejeValores };
        if (reporte.etiquetas.length > 8) {
          ejeEtiquetas.ticks.maxRotation = 45;
          ejeEtiquetas.ticks.minRotation = 45;
        }
      }
    } else if (reporte.tipoGrafico === 'doughnut') {
      opciones.cutout = '58%';
    }

    return {
      type: reporte.tipoGrafico,
      data: { labels: reporte.etiquetas, datasets: [dataset] },
      options: opciones,
      plugins: [fondoBlanco]
    };
  }

  function dibujar(reporte) {
    var canvas = document.getElementById('grafico-' + reporte.numero);
    if (!canvas) return;
    if (reporte.sinDatos) {
      canvas.hidden = true;
      var aviso = canvas.parentElement.querySelector('.reporte-sin-datos');
      if (aviso) aviso.hidden = false;
      return;
    }
    graficos[reporte.numero] = new Chart(canvas, configuracion(reporte));
  }

  if (window.Chart) {
    reportes.forEach(dibujar);
  } else {
    document.querySelectorAll('.reporte-grafico').forEach(function (caja) {
      caja.innerHTML = '<p class="reporte-sin-datos">No se pudo cargar la librería de gráficos. Revisa tu conexión a internet.</p>';
    });
  }

  // Antes de enviar el formulario de descarga, se adjunta la imagen del gráfico
  document.querySelectorAll('form[data-descarga]').forEach(function (formulario) {
    formulario.addEventListener('submit', function () {
      var tarjeta = formulario.closest('.reporte-card');
      var numero = tarjeta ? tarjeta.id.replace('reporte-', '') : null;
      var campo = formulario.querySelector('input[name="imagen"]');
      var grafico = numero ? graficos[numero] : null;
      campo.value = grafico ? grafico.toBase64Image('image/png', 1) : '';
    });
  });
})();
