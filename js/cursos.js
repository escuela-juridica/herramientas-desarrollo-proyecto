// Responsable: Paolo Añorga (feature/mision-cursos)
// Genera las 6 tarjetas de la sección "Servicios" (#servicios) a partir
// de instancias de la clase Curso, en vez de tenerlas escritas a mano en
// el HTML. Vive en su propio archivo (no en js/inicio.js, que es de
// Enrique) para que nadie más tenga que tocar este script ni al revés.

// Cada Curso solo guarda los datos que cambian de uno a otro (tipo,
// duración, imagen, título, descripción). Todo lo demás — el color del
// badge según el tipo, el texto "N hrs", el alt de la imagen y el HTML
// final de la tarjeta — se resuelve por lógica dentro de la clase, para
// no repetirlo a mano en cada objeto del array.
class Curso {
  constructor(tipo, duracion, imagenUrl, titulo, descripcion) {
    this.tipo = tipo;
    this.duracion = duracion;
    this.imagenUrl = imagenUrl;
    this.titulo = titulo;
    this.descripcion = descripcion;
  }

  // Deriva el color del badge a partir del tipo de curso.
  get badgeClase() {
    if (this.tipo === "Curso") return "badge-orange";
    if (this.tipo === "Seminario") return "badge-burgundy";
    return "badge-navy";
  }

  get horasTexto() {
    return `${this.duracion} hrs`;
  }

  // Alt genérico para todas las imágenes de curso (no se guarda por dato).
  get altImagen() {
    return "Portada del curso";
  }

  // Arma el HTML de la tarjeta: misma estructura y clases que antes en
  // el HTML (curso-head, curso-badge, curso-cover...).
  render() {
    return `
      <article class="content-card curso">
        <div class="curso-head">
          <span class="curso-badge ${this.badgeClase}">${this.tipo}</span>
          <span class="curso-hours"><i class="fa-regular fa-clock"></i> ${this.horasTexto}</span>
        </div>
        <div class="curso-cover"><img src="${this.imagenUrl}" alt="${this.altImagen}"></div>
        <div class="curso-body">
          <h3>${this.titulo}</h3>
          <p>${this.descripcion}</p>
        </div>
        <div class="curso-foot"><a href="#" class="curso-button">Más Información</a></div>
      </article>
    `;
  }
}

const cursos = [
  new Curso("Diplomado", 120, "img/cursos/curso-1-notarial.jpg", "Diplomado en Derecho Notarial y Registral", "Especialízate en trámites de escrituras públicas, transferencias de propiedad y calificación registral en la SUNARP."),
  new Curso("Curso", 40, "img/cursos/curso-2-saneamiento.jpg", "Saneamiento Físico Legal de Predios", "Aprende los procedimientos técnicos y legales para la regularización de inmuebles rústicos y urbanos."),
  new Curso("Seminario", 12, "img/cursos/curso-3-precedentes.jpg", "Precedentes de Observancia Obligatoria", "Análisis exhaustivo de los fallos y resoluciones del Tribunal Registral con carácter vinculante y obligatorio."),
  new Curso("Diplomado", 140, "img/cursos/curso-4-contratos.jpg", "Derecho de Contratos y Obligaciones Civiles", "Redacción, interpretación y resolución de controversias en contratos típicos y atípicos en el ordenamiento civil."),
  new Curso("Curso", 48, "img/cursos/curso-5-administrativo.jpg", "Derecho Administrativo y Procedimiento Sancionador", "Estudio integral de la Ley N° 27444, actos administrativos y estrategias de defensa ante entidades estatales."),
  new Curso("Seminario", 16, "img/cursos/curso-6-garantias.jpg", "Garantías Reales y Ejecución de Hipotecas", "Constitución de hipotecas y anticresis, calificación registral y procesos judiciales de ejecución de garantías.")
];

function renderCursos() {
  const cursosGrid = document.querySelector("#cursosGrid");
  if (!cursosGrid) return;
  cursosGrid.innerHTML = cursos.map((curso) => curso.render()).join("");
}

renderCursos();
