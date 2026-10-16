const imagenArchivo = document.getElementById('imagenArchivo');
const nuevaImagenVista = document.getElementById('nuevaImagenVista');
const nuevaImagenMiniatura = document.getElementById('nuevaImagenMiniatura');
const nuevaImagenCompleta = document.getElementById('nuevaImagenCompleta');

if (imagenArchivo && nuevaImagenVista && nuevaImagenMiniatura && nuevaImagenCompleta) {
  let imagenTemporal = null;

  imagenArchivo.addEventListener('change', () => {
    if (imagenTemporal) {
      URL.revokeObjectURL(imagenTemporal);
      imagenTemporal = null;
    }

    const archivo = imagenArchivo.files?.[0];
    if (!archivo || !['image/jpeg', 'image/png'].includes(archivo.type)) {
      nuevaImagenMiniatura.removeAttribute('src');
      nuevaImagenCompleta.removeAttribute('href');
      nuevaImagenVista.hidden = true;
      return;
    }

    imagenTemporal = URL.createObjectURL(archivo);
    nuevaImagenMiniatura.src = imagenTemporal;
    nuevaImagenCompleta.href = imagenTemporal;
    nuevaImagenVista.hidden = false;
  });
}

/* ── Atajo de teclado: autocompletar datos de prueba (solo en "Nuevo curso") ──
 * Alt+R rellena todos los campos con datos de ejemplo, EXCEPTO la imagen:
 * por seguridad del navegador, JS no puede asignarle un valor a un <input type="file">,
 * así que esa siempre hay que seleccionarla a mano.
 */
const formularioCurso = document.querySelector('.curso-form');
const esFormularioNuevo = formularioCurso && !document.getElementById('idCurso');

if (esFormularioNuevo) {
  document.addEventListener('keydown', (evento) => {
    if (evento.altKey && evento.key.toUpperCase() === 'R') {
      evento.preventDefault();
      rellenarDatosDePrueba();
    }
  });
}

function rellenarDatosDePrueba() {
  const datos = {
    codigo: 'EJ-2026-021',
    nombre: 'Diplomado en Derecho Civil',
    descripcion: 'Profundiza en las instituciones del derecho civil peruano: contratos, '
      + 'obligaciones y responsabilidad civil, con un enfoque práctico para el ejercicio profesional.',
    institucion: 'Escuela Jurídica',
    modalidad: 'V',
    duracionHoras: 80,
    cupos: 30,
    precio: 350,
    fechaInicio: obtenerFechaISO(30),
    fechaFin: obtenerFechaISO(120),
  };

  Object.entries(datos).forEach(([id, valor]) => {
    const campo = document.getElementById(id);
    if (campo) campo.value = valor;
  });

  seleccionarPrimeraOpcionValida('tipoCurso');
  seleccionarPrimeraOpcionValida('docente');
}

function seleccionarPrimeraOpcionValida(idSelect) {
  const select = document.getElementById(idSelect);
  if (!select) return;
  const primeraOpcion = Array.from(select.options).find((opcion) => opcion.value !== '');
  if (primeraOpcion) select.value = primeraOpcion.value;
}

function obtenerFechaISO(diasDesdeHoy) {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + diasDesdeHoy);
  return fecha.toISOString().split('T')[0];
}
