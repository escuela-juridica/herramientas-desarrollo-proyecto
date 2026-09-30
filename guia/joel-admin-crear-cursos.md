# Joel — Listado y creación de cursos en el CRUD (rama `feature/admin-crear-cursos`)

## Qué vas a lograr

`/admin/cursos` hoy muestra un cartel de "en construcción". Tu parte: una tabla con todos los cursos (activos e inactivos), un botón para crear uno nuevo con su formulario, y un buscador estático (botón) sobre esa tabla.

Archivos que vas a crear o modificar:

1. `CursoRepository.java` (agregar 2 líneas)
2. `CursoService.java` (agregar 2-3 métodos)
3. `TipoCursoRepository.java` (nuevo)
4. `DocenteRepository.java` (nuevo)
5. `AdminCursoController.java` (reescribirlo — **compartido con Juan, avísale antes**)
6. `admin/cursos.html` (la tabla — **compartido con Juan**)
7. `admin/curso-form.html` (nuevo — el formulario, lo vas a compartir con Juan para editar)
8. `admin-cursos.css`

## Paso 1 — `CursoRepository.java`

Agrega estas dos líneas (junto a la que ya existe):

```java
  List<Curso> findAllByOrderByIdCursoAsc();

  List<Curso> findByNombreContainingIgnoreCaseOrderByIdCursoAsc(String texto);
```

**¿Por qué sin filtro de `estado` acá?** Porque el admin necesita ver también los cursos inactivos (los "eliminados" lógicamente), para poder reactivarlos si hace falta. La página pública (la de Kelvin/Paolo) sí filtra solo los activos; el admin ve todo.

## Paso 2 — `TipoCursoRepository.java` (nuevo)

Necesitas esto para el formulario de creación (el admin tiene que elegir el tipo de curso desde una lista desplegable). Créalo en `src/main/java/pe/edu/escuela/app/repository/TipoCursoRepository.java`:

```java
package pe.edu.escuela.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.TipoCurso;

public interface TipoCursoRepository extends JpaRepository<TipoCurso, Integer> {
}
```

(No necesitas ningún método propio — `JpaRepository` ya te da `findAll()` gratis, que es lo único que vas a usar.)

## Paso 3 — `DocenteRepository.java` (nuevo)

Mismo motivo, para elegir el docente. Créalo en `src/main/java/pe/edu/escuela/app/repository/DocenteRepository.java`:

```java
package pe.edu.escuela.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Docente;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {
}
```

## Paso 4 — `CursoService.java`

Agrega estos métodos (no borres `obtenerDestacados` ni lo que ya hayan agregado Kelvin/Paolo):

```java
  public List<Curso> obtenerTodos() {
    return cursoRepository.findAllByOrderByIdCursoAsc();
  }

  public List<Curso> buscarTodos(String texto) {
    if (texto == null || texto.isBlank()) {
      return obtenerTodos();
    }
    return cursoRepository.findByNombreContainingIgnoreCaseOrderByIdCursoAsc(texto);
  }

  public Curso guardar(Curso curso) {
    return cursoRepository.save(curso);
  }

  public Curso obtenerPorId(Integer id) {
    return cursoRepository.findById(id).orElseThrow();
  }
```

`obtenerPorId` no lo usas tú directamente en este paso, pero Juan sí lo va a necesitar para cargar el formulario de editar — mejor dejarlo ya puesto acá, ya que estás en el archivo.

## Paso 5 — `AdminCursoController.java`

Reemplaza el archivo completo por esto:

```java
package pe.edu.escuela.app.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.repository.DocenteRepository;
import pe.edu.escuela.app.repository.TipoCursoRepository;
import pe.edu.escuela.app.service.CursoService;

@Controller
public class AdminCursoController {

  private final CursoService cursoService;
  private final TipoCursoRepository tipoCursoRepository;
  private final DocenteRepository docenteRepository;

  @Value("${app.upload-dir}")
  private String uploadDir;

  public AdminCursoController(CursoService cursoService, TipoCursoRepository tipoCursoRepository,
      DocenteRepository docenteRepository) {
    this.cursoService = cursoService;
    this.tipoCursoRepository = tipoCursoRepository;
    this.docenteRepository = docenteRepository;
  }

  @GetMapping("/admin/cursos")
  public String listar(@RequestParam(required = false) String buscar, Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Cursos");
    model.addAttribute("cursos", cursoService.buscarTodos(buscar));
    return "admin/cursos";
  }

  @GetMapping("/admin/cursos/nuevo")
  public String formularioNuevo(Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Nuevo curso");
    model.addAttribute("curso", new Curso());
    model.addAttribute("tipos", tipoCursoRepository.findAll());
    model.addAttribute("docentes", docenteRepository.findAll());
    return "admin/curso-form";
  }

  @PostMapping("/admin/cursos/nuevo")
  public String crear(@ModelAttribute Curso curso, @RequestParam("imagenFile") MultipartFile imagenFile)
      throws IOException {
    curso.setImagen(guardarImagen(imagenFile));
    cursoService.guardar(curso);
    return "redirect:/admin/cursos";
  }

  private String guardarImagen(MultipartFile archivo) throws IOException {
    String nombreArchivo = UUID.randomUUID() + "-" + archivo.getOriginalFilename();
    Path destino = Path.of(uploadDir, nombreArchivo);
    Files.copy(archivo.getInputStream(), destino);
    return "/uploads/cursos/" + nombreArchivo;
  }
}
```

**Explicación de las partes nuevas/raras:**

- `@Value("${app.upload-dir}") private String uploadDir;` — lee el valor de `app.upload-dir` que ya está definido en `application.properties` (`uploads/cursos`). Así no escribes la ruta a mano en el código.
- `formularioNuevo()` — además de mostrar el formulario vacío (`new Curso()`), le pasa al `Model` la lista de tipos y de docentes, porque el formulario necesita mostrarlos como opciones para elegir (lo vas a ver en el Paso 7).
- `@ModelAttribute Curso curso` en el método `crear` — Spring arma automáticamente un objeto `Curso` con los valores que vengan del formulario (que los `name` de los inputs coincidan con los nombres de los campos de la entidad, ej. `name="nombre"` llena `curso.nombre`).
- `@RequestParam("imagenFile") MultipartFile imagenFile` — la imagen va **aparte** del objeto `Curso` (la entidad solo guarda el nombre del archivo como texto, no el archivo en sí).
- `guardarImagen(...)` — genera un nombre único con `UUID` (para que dos imágenes no se pisen si se llaman igual), la copia físicamente a la carpeta `uploads/cursos`, y devuelve la ruta que se va a guardar en la base de datos (`/uploads/cursos/xxxxx-nombre.jpg`) — el mismo formato que ya usan los 20 cursos semilla.

**Importante — sobre `id_admin`:** la entidad `Curso` exige un administrador (`id_admin NOT NULL`), pero en este paso todavía no existe un admin "logueado" real conectado al formulario (eso lo arma Maykol con Spring Security, en paralelo). Por ahora, agrega esta línea dentro de `crear(...)`, antes de `cursoService.guardar(curso)`, como solución temporal:

```java
    // TEMPORAL: hasta que el login de Maykol esté integrado, se usa el primer admin que exista.
    curso.setAdministrador(administradorRepository.findAll().get(0));
```

Para que compile necesitas inyectar también `AdministradorRepository` (el que crea Maykol) en el constructor, igual que los otros dos repositorios. **Avísale a Maykol de este apaño** para que cuando termine su parte, reemplacen esa línea por el admin real que está logueado (lo obtendría de `Authentication`, que él va a manejar).

## Paso 6 — `admin/cursos.html`

Reemplaza el bloque de "en construcción" por esto:

```html
<div class="admin-tabla-toolbar">
  <form method="get" action="/admin/cursos" class="admin-buscar">
    <i class="fa-solid fa-magnifying-glass"></i>
    <input type="text" name="buscar" placeholder="Buscar curso por nombre..." th:value="${param.buscar}">
  </form>
  <a href="/admin/cursos/nuevo" class="admin-nuevo"><i class="fa-solid fa-plus"></i> Nuevo curso</a>
</div>

<div class="admin-tabla-wrap">
  <table class="admin-tabla">
    <thead>
      <tr>
        <th>Imagen</th>
        <th>Código</th>
        <th>Nombre</th>
        <th>Tipo</th>
        <th>Precio</th>
        <th>Estado</th>
        <th>Acciones</th>
      </tr>
    </thead>
    <tbody>
      <tr th:each="curso : ${cursos}">
        <td><img th:src="@{${curso.imagen}}" alt="Portada del curso" class="admin-tabla-img"></td>
        <td th:text="${curso.codigo}"></td>
        <td class="admin-tabla-nombre" th:text="${curso.nombre}"></td>
        <td th:text="${curso.tipoCurso.nombre}"></td>
        <td th:text="'S/ ' + ${#numbers.formatDecimal(curso.precio, 1, 0)}"></td>
        <td>
          <span class="admin-badge-estado"
            th:classappend="${curso.estado == 'A'} ? 'admin-estado-activo' : 'admin-estado-inactivo'"
            th:text="${curso.estado == 'A'} ? 'Activo' : 'Inactivo'"></span>
        </td>
        <td class="admin-tabla-acciones">
          <a href="#" title="Ver"><i class="fa-solid fa-eye"></i></a>
          <a th:href="@{'/admin/cursos/editar/' + ${curso.idCurso}}" title="Editar"><i class="fa-solid fa-pen"></i></a>
          <a href="#" title="Eliminar" class="admin-accion-eliminar"><i class="fa-solid fa-trash"></i></a>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

**Notas:**
- `th:value="${param.buscar}"` — si ya se buscó algo, deja el texto escrito en el input (para que no se borre al recargar la página con los resultados).
- El link de "Editar" ya está armado apuntando a `/admin/cursos/editar/{id}` — esa ruta todavía no existe, la va a crear Juan. El de "Eliminar" lo dejas como `href="#"` por ahora, es parte de Juan.
- El de "Ver" queda como `href="#"` — no es obligatorio (es opcional según la guía del profesor), si sobra tiempo se puede armar una página de detalle.

## Paso 7 — `admin/curso-form.html` (nuevo)

Este formulario lo vas a compartir con Juan (él lo reusa para editar). Créalo en `src/main/resources/templates/admin/curso-form.html`:

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layout}">

<head>
  <title th:text="${tituloAdmin}">Curso</title>
  <th:block layout:fragment="css">
    <link rel="stylesheet" href="/css/admin/admin-cursos.css">
  </th:block>
</head>

<body>
  <div layout:fragment="content">
    <form th:action="${curso.idCurso == null} ? @{/admin/cursos/nuevo} : @{'/admin/cursos/editar/' + ${curso.idCurso}}"
          th:object="${curso}" method="post" enctype="multipart/form-data" class="admin-form">

      <div class="admin-form-campo">
        <label>Código</label>
        <input type="text" th:field="*{codigo}" required>
      </div>

      <div class="admin-form-campo">
        <label>Nombre</label>
        <input type="text" th:field="*{nombre}" required>
      </div>

      <div class="admin-form-campo">
        <label>Descripción</label>
        <textarea th:field="*{descripcion}" required></textarea>
      </div>

      <div class="admin-form-campo">
        <label>Precio (S/)</label>
        <input type="number" step="0.01" th:field="*{precio}" required>
      </div>

      <div class="admin-form-campo">
        <label>Duración (horas)</label>
        <input type="number" th:field="*{duracionHoras}">
      </div>

      <div class="admin-form-campo">
        <label>Tipo de curso</label>
        <select th:field="*{tipoCurso}">
          <option th:each="tipo : ${tipos}" th:value="${tipo.idTipoCurso}" th:text="${tipo.nombre}"></option>
        </select>
      </div>

      <div class="admin-form-campo">
        <label>Docente</label>
        <select th:field="*{docente}">
          <option th:each="d : ${docentes}" th:value="${d.idDocente}"
                  th:text="${d.nombres} + ' ' + ${d.apellidos}"></option>
        </select>
      </div>

      <div class="admin-form-campo">
        <label>Imagen</label>
        <input type="file" name="imagenFile">
        <p th:if="${curso.imagen != null}">Imagen actual: <span th:text="${curso.imagen}"></span></p>
      </div>

      <button type="submit" class="site-button button-primary">Guardar</button>
      <a href="/admin/cursos" class="admin-salir">Cancelar</a>
    </form>
  </div>
</body>
</html>
```

**Cosas importantes de este formulario:**

- `th:object="${curso}"` + `th:field="*{nombre}"` — esta es la forma "oficial" de Thymeleaf de conectar un formulario a un objeto Java. `th:field="*{nombre}"` genera automáticamente el `name="nombre"` y el `id="nombre"`, y si `curso.nombre` ya tiene un valor (edición), lo precarga solo en el input. No necesitas escribir `name=` a mano en ningún campo que use `th:field`.
- La línea del `th:action` es un `if/else` de una sola línea: si `curso.idCurso` es `null` (un curso nuevo, recién creado con `new Curso()`), manda el formulario a `/admin/cursos/nuevo`; si ya tiene id (viene de "editar", que hace Juan), lo manda a `/admin/cursos/editar/{id}`. Así el mismo archivo sirve para las dos cosas.
- `th:field="*{tipoCurso}"` en un `<select>` — Thymeleaf es capaz de convertir el id elegido de vuelta a un objeto `TipoCurso` completo automáticamente (usando el método `getIdTipoCurso()`/`setTipoCurso()` de la entidad `Curso`), no necesitas hacer esa conversión a mano.
- El campo `imagenFile` **no** tiene `th:field` porque no es parte de la entidad `Curso` — es el archivo que se sube aparte (revisa el Paso 5, el controlador lo recibe como `@RequestParam` separado).

## Paso 8 — `admin-cursos.css`

Agrégale estilos para `.admin-tabla-toolbar`, `.admin-buscar`, `.admin-nuevo`, `.admin-tabla-wrap`, `.admin-tabla`, `.admin-tabla-img`, `.admin-badge-estado`, `.admin-estado-activo`, `.admin-estado-inactivo`, `.admin-tabla-acciones`, `.admin-form`, `.admin-form-campo` — son las clases nuevas que usaste en los pasos 6 y 7. El diseño (colores, espaciado) queda a tu criterio, pero reutiliza las variables de `base.css` (`var(--navy)`, `var(--orange)`, `var(--line)`, etc.) para que se sienta parte del mismo panel.

## Cómo probar que funciona

1. Entra a `/admin/cursos` — deberías ver la tabla con los 20 cursos semilla.
2. Prueba el buscador escribiendo algo y presionando Enter (o el botón, si le agregaste uno) — debería filtrar.
3. Haz clic en "Nuevo curso", llena el formulario, sube una imagen, y guarda — debería aparecer en la tabla.
4. Revisa que el archivo de la imagen haya aparecido de verdad dentro de la carpeta `uploads/cursos/` del proyecto.

## Coordinación con Juan

Él va a agregar `editar()`, `eliminar()` y la búsqueda dinámica en este mismo `AdminCursoController.java` y va a reusar `admin/curso-form.html`. Avísale cuando termines para que parta de tu versión, no de una vieja.
