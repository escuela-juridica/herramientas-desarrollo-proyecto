
document.addEventListener("DOMContentLoaded", () => {
  const buscador = document.getElementById("busquedaCurso");
  const buscadorDinamico = document.getElementById("busquedaCursoDinamica");
  const formularioBusqueda = document.querySelector(".cursos-toolbar");
  const filasCursos = document.getElementById("filasCursos");
  const totalCursos = document.querySelector(".cursos-total");

  if (!filasCursos) {
    console.error("No se encontró la tabla de cursos.");
    return;
  }

  let temporizadorDinamico;
  let controladorPeticion;

  // BUSQUEDA ESTATICA
  if (formularioBusqueda && buscador) {
    formularioBusqueda.addEventListener("submit", (evento) => {
      evento.preventDefault();
      buscarCursos(buscador.value.trim());
    });
  }

  // BUSQUEDA DINAMICA
  if (buscadorDinamico) {
    buscadorDinamico.addEventListener("input", () => {
      clearTimeout(temporizadorDinamico);

      temporizadorDinamico = setTimeout(() => {
        buscarCursos(buscadorDinamico.value.trim());
      }, 300);
    });
  }

  // LIMPIAR BUSQUEDAS
  const botonLimpiar = document.getElementById("limpiarBusquedaCurso");

  if (botonLimpiar) {
    botonLimpiar.addEventListener("click", () => {
      buscador.value = "";
      buscadorDinamico.value = "";

      clearTimeout(temporizadorDinamico);

      buscarCursos("");
    });
  }
  // DESACTIVAR CURSO
  filasCursos.addEventListener("click", (evento) => {
    const botonEliminar = evento.target.closest(".curso-action-delete");

    if (!botonEliminar) return;

    if (!confirm("¿Deseas marcar este curso como inactivo?")) {
      evento.preventDefault();
    }
  });

  // BUSCAR CURSOS
  async function buscarCursos(busqueda) {
    if (controladorPeticion) {
      controladorPeticion.abort();
    }

    controladorPeticion = new AbortController();

    try {
      const respuesta = await fetch(
        `/admin/cursos/buscar?busqueda=${encodeURIComponent(busqueda)}`,
        {
          headers: {
            "X-Requested-With": "XMLHttpRequest"
          },
          signal: controladorPeticion.signal
        }
      );

      if (!respuesta.ok) {
        throw new Error(`Error HTTP: ${respuesta.status}`);
      }

      const html = await respuesta.text();

      // LEER EL FRAGMENTO HTML
      const tablaTemporal = document.createElement("table");
      tablaTemporal.innerHTML = html;

      const nuevasFilas = tablaTemporal.querySelector("#filasCursos");

      if (!nuevasFilas) {
        console.error("No se encontró #filasCursos en la respuesta.");
        console.log("HTML recibido:", html);
        return;
      }

      // ACTUALIZAR TABLA
      filasCursos.innerHTML = nuevasFilas.innerHTML;

      
      actualizarTotal();

      console.log("Búsqueda completada:", busqueda);

    } catch (error) {
      if (error.name !== "AbortError") {
        console.error("Error al buscar cursos:", error);
      }
    }
  }


  function actualizarTotal() {
    const filas = filasCursos.querySelectorAll("tr[data-curso]");

    if (totalCursos) {
      totalCursos.textContent = `${filas.length} resultado(s)`;
    }
  }
});