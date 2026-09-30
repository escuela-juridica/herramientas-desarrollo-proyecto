# Kelvin — Listado de los 20 cursos (rama `feature/listado-cursos`)

## Qué vas a lograr

Ahora mismo, si abres `http://localhost:8500/catalogo`, ves un banner y debajo un mensaje que dice "Página en construcción". Tu trabajo es reemplazar ese mensaje por los 20 cursos reales, cargados desde la base de datos, cada uno con su imagen, nombre, precio, etc.

Vas a tocar 4 archivos, en este orden:

1. `CursoRepository.java` (agregar una línea)
2. `CursoService.java` (agregar un método)
3. `CatalogoController.java` (modificarlo)
4. `catalogo.html` (reemplazar el placeholder)
5. `catalogo.css` (estilos, si hace falta)

## Paso 1 — `CursoRepository.java`

Ábrelo en `src/main/java/pe/edu/escuela/app/repository/CursoRepository.java`. Ahora mismo tiene una sola línea dentro de la interfaz. Agrégale otra debajo:

```java
package pe.edu.escuela.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

  List<Curso> findByDestacadoTrueAndEstadoOrderByIdCursoAsc(String estado);

  List<Curso> findByEstadoOrderByIdCursoAsc(String estado);
}
```

**¿Qué hace esa línea?** No escribes SQL. Spring Data lee el nombre del método y arma la consulta sola: `findBy` + `Estado` (filtra por la columna `estado`) + `OrderByIdCursoAsc` (ordena por `id_curso` de menor a mayor). Es exactamente el mismo truco que ya usa la línea de arriba, solo que sin el filtro de "destacado".

## Paso 2 — `CursoService.java`

Ábrelo en `src/main/java/pe/edu/escuela/app/service/CursoService.java`. Se ve así ahora mismo:

```java
package pe.edu.escuela.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.util.Constantes;

@Service
public class CursoService {

  private final CursoRepository cursoRepository;

  public CursoService(CursoRepository cursoRepository) {
    this.cursoRepository = cursoRepository;
  }

  public List<Curso> obtenerDestacados() {
    return cursoRepository.findByDestacadoTrueAndEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }
}
```

Agrégale un método nuevo (no borres el que ya existe), quedando así:

```java
  public List<Curso> obtenerDestacados() {
    return cursoRepository.findByDestacadoTrueAndEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }

  public List<Curso> obtenerActivos() {
    return cursoRepository.findByEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }
```

**¿Por qué pasar por `CursoService` y no llamar a `CursoRepository` directo desde el controlador?** Es la arquitectura que ya sigue todo el proyecto: Controller → Service → Repository. El `Service` es el único que habla con la base de datos; el controlador solo le pide datos al `Service`.

**¿Por qué `Constantes.ESTADO_ACTIVO` y no simplemente `"A"`?** Porque si en algún momento alguien escribe mal el string (`"a"` minúscula, por ejemplo), el proyecto no compila y te avisa al toque — en cambio si escribes `"a"` suelto, el error pasa desapercibido y la página simplemente no muestra nada, sin explicación. La constante ya existe en `src/main/java/pe/edu/escuela/app/util/Constantes.java`, solo la usas.

## Paso 3 — `CatalogoController.java`

Ábrelo en `src/main/java/pe/edu/escuela/app/controller/CatalogoController.java`. Hoy se ve así:

```java
package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {

  @GetMapping("/catalogo")
  public String catalogo(Model model) {
    model.addAttribute("active", "catalogo");
    return "catalogo";
  }
}
```

Cámbialo por esto (copia el archivo completo, es más fácil que editar a pedazos):

```java
package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.escuela.app.service.CursoService;

@Controller
public class CatalogoController {

  private final CursoService cursoService;

  public CatalogoController(CursoService cursoService) {
    this.cursoService = cursoService;
  }

  @GetMapping("/catalogo")
  public String catalogo(Model model) {
    model.addAttribute("active", "catalogo");
    model.addAttribute("cursos", cursoService.obtenerActivos());
    return "catalogo";
  }
}
```

**¿Qué cambió?** Se agregó un campo `cursoService`, un constructor que lo recibe (eso se llama "inyección de dependencias por constructor" — Spring se lo entrega solo, no lo creas tú con `new`), y dentro del método `catalogo()` se agrega al `Model` la lista de cursos con el nombre `"cursos"`. Ese nombre (`"cursos"`) es el que vas a usar en el HTML en el siguiente paso.

## Paso 4 — `catalogo.html`

Ábrelo en `src/main/resources/templates/catalogo.html`. Busca el bloque que dice:

```html
<section class="band" id="catalogo">
  <div class="wrap">
    <div class="section-heading">
      <p class="eyebrow">Nuestros Cursos</p>
      <h2 class="tit">Catálogo de cursos</h2>
      <p class="section-lead">Página en construcción — próximamente el catálogo completo.</p>
    </div>
  </div>
</section>
```

Cámbialo por esto:

```html
<section class="band" id="catalogo">
  <div class="wrap">
    <div class="section-heading">
      <p class="eyebrow">Nuestros Cursos</p>
      <h2 class="tit">Catálogo de cursos</h2>
      <p class="section-lead">Explora nuestros cursos, diplomados, seminarios y programas.</p>
    </div>

    <div class="grid6" id="cursosGrid">
      <article th:each="curso : ${cursos}" class="content-card curso">
        <div class="curso-head">
          <span class="curso-badge" th:text="${curso.tipoCurso.nombre}"></span>
          <span class="curso-hours">
            <i class="fa-solid fa-sack-dollar"></i>
            <span th:text="'S/ ' + ${#numbers.formatDecimal(curso.precio, 1, 0)}"></span>
          </span>
        </div>
        <div class="curso-cover">
          <img th:src="@{${curso.imagen}}" alt="Portada del curso">
        </div>
        <div class="curso-body">
          <h3 th:text="${curso.nombre}"></h3>
          <p th:text="${curso.descripcion}"></p>
        </div>
        <div class="curso-foot">
          <a href="#" class="curso-button">Más Información</a>
        </div>
      </article>
    </div>
  </div>
</section>
```

**Explicación línea por línea de lo nuevo:**

- `th:each="curso : ${cursos}"` — repite el `<article>` completo una vez por cada curso que venga en la lista `cursos` (la que agregaste en el Paso 3). Si hay 20 cursos, esto genera 20 tarjetas automáticamente.
- `th:text="${curso.tipoCurso.nombre}"` — escribe adentro del `<span>` el texto que resulte de esa expresión. `curso.tipoCurso.nombre` funciona porque en la entidad `Curso` hay una relación hacia `TipoCurso`, y esa a su vez tiene un campo `nombre` (revisa `src/main/java/pe/edu/escuela/app/model/Curso.java` si quieres ver todos los campos disponibles).
- `${#numbers.formatDecimal(curso.precio, 1, 0)}` — formatea el precio (que es un número con decimales, ej. `450.00`) para que se muestre sin decimales (`450`). El `'S/ ' + ...` simplemente le pega el texto "S/ " adelante.
- `th:src="@{${curso.imagen}}"` — pone la ruta de la imagen en el atributo `src` de la etiqueta `<img>`. El campo `curso.imagen` ya trae la ruta completa guardada (algo como `/uploads/cursos/curso-01.jpg`), así que no necesitas armar la ruta a mano.

Este mismo patrón (`th:each`, `th:text`, `th:src`) ya está usado en `inicio.html`, en la sección de "Destacados" — si algo no te queda claro, ábrelo y compáralo, es literalmente la misma idea aplicada a una lista distinta.

## Paso 5 — `catalogo.css`

Si las tarjetas se ven sin estilo o mal alineadas, es porque las clases `.content-card`, `.curso-head`, `.curso-badge`, `.curso-cover`, `.curso-body`, `.curso-foot`, `.grid6` ya están definidas, pero en `inicio.css`, no en `catalogo.css` (porque hasta ahora solo `inicio.html` las usaba). Tienes dos opciones:

- **Opción simple**: copia esas clases de `inicio.css` a `catalogo.css` (cópialas, no las muevas — `inicio.html` las sigue necesitando).
- **Opción más prolija**: si ves que son clases genéricas de "tarjeta de curso" que van a usar varias páginas, pregúntale al grupo si conviene moverlas a `base.css` (que es el archivo para estilos compartidos entre páginas).

## Cómo probar que funciona

1. Corre la app (o pídele a alguien que la tenga corriendo).
2. Entra a `http://localhost:8500/catalogo`.
3. Deberías ver 20 tarjetas, cada una con imagen, tipo de curso, precio, nombre y descripción.
4. Si no aparece nada, revisa la consola de la aplicación (la terminal donde corre) — ahí sale el error exacto si algo falló.

## Coordinación con Paolo

Él va a agregar el buscador y un JavaScript que reemplaza el contenido de `#cursosGrid` cuando alguien busca algo. **No cambies el `id="cursosGrid"`** una vez que lo pongas — avísale a Paolo que ya existe con ese nombre exacto, para que su código sepa qué reemplazar.
