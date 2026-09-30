# Juan — Búsqueda dinámica, editar y eliminar cursos (rama `feature/admin-editar-eliminar-cursos`)

## Qué vas a lograr

La otra mitad del CRUD de `/admin/cursos`: poder editar un curso existente, "eliminarlo" (sin borrarlo de la base de datos de verdad), y que el buscador de la tabla filtre mientras se escribe, sin recargar la página.

**Importante: necesitas que Joel ya haya terminado su parte primero** (la tabla, el formulario `admin/curso-form.html`, y los repositorios nuevos) — tu código se agrega encima del suyo, no lo reemplaza.

Archivos que vas a tocar:

1. `AdminCursoController.java` (agregar métodos — **compartido con Joel, coordina con él**)
2. `admin/cursos.html` (agregar `id` al buscador + convertir las filas en fragmento — **compartido con Joel**)
3. `admin-cursos.js` (nuevo)
4. `admin-cursos.css` (estilos del botón eliminar)

## Paso 1 — `AdminCursoController.java`

Abre el archivo que dejó Joel. Al final de la clase (antes de la última llave `}`), agrega estos 4 métodos nuevos:

```java
  @GetMapping("/admin/cursos/editar/{id}")
  public String formularioEditar(@PathVariable Integer id, Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Editar curso");
    model.addAttribute("curso", cursoService.obtenerPorId(id));
    model.addAttribute("tipos", tipoCursoRepository.findAll());
    model.addAttribute("docentes", docenteRepository.findAll());
    return "admin/curso-form";
  }

  @PostMapping("/admin/cursos/editar/{id}")
  public String editar(@PathVariable Integer id, @ModelAttribute Curso datosFormulario,
      @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile) throws IOException {

    Curso curso = cursoService.obtenerPorId(id);
    curso.setCodigo(datosFormulario.getCodigo());
    curso.setNombre(datosFormulario.getNombre());
    curso.setDescripcion(datosFormulario.getDescripcion());
    curso.setPrecio(datosFormulario.getPrecio());
    curso.setDuracionHoras(datosFormulario.getDuracionHoras());
    curso.setTipoCurso(datosFormulario.getTipoCurso());
    curso.setDocente(datosFormulario.getDocente());

    if (imagenFile != null && !imagenFile.isEmpty()) {
      curso.setImagen(guardarImagen(imagenFile));
    }

    cursoService.guardar(curso);
    return "redirect:/admin/cursos";
  }

  @PostMapping("/admin/cursos/eliminar/{id}")
  public String eliminar(@PathVariable Integer id) {
    Curso curso = cursoService.obtenerPorId(id);
    curso.setEstado(Constantes.ESTADO_INACTIVO);
    cursoService.guardar(curso);
    return "redirect:/admin/cursos";
  }

  @GetMapping("/admin/cursos/buscar-dinamico")
  public String buscarDinamico(@RequestParam(required = false) String texto, Model model) {
    model.addAttribute("cursos", cursoService.buscarTodos(texto));
    return "admin/cursos :: filas";
  }
```

Y agrega este import arriba, junto a los demás (si no está ya):

```java
import pe.edu.escuela.app.util.Constantes;
```

**Explicación de cada método:**

- **`formularioEditar`** — casi idéntico al `formularioNuevo` de Joel, pero en vez de `new Curso()` le pasa al `Model` el curso real que ya existe (`cursoService.obtenerPorId(id)`). Como reusas el mismo template `admin/curso-form.html`, Thymeleaf automáticamente precarga todos los campos con los datos actuales (eso lo hace el `th:field` que armó Joel).
- **`editar` (el POST)** — fíjate que **no** reemplaza el objeto completo: busca el curso real por su `id`, y copia campo por campo los valores nuevos que llegaron del formulario. Esto es a propósito — así el `id_admin` original (quién lo creó) no se pierde ni se sobreescribe por accidente.
- **La imagen en edición es opcional** — por eso `imagenFile` tiene `required = false` y se revisa `if (imagenFile != null && !imagenFile.isEmpty())` antes de reemplazarla. Si el admin no seleccionó ninguna foto nueva, el curso simplemente se queda con la que ya tenía.
- **`eliminar` — nunca hace un `DELETE` real.** Solo cambia `estado` a `'I'` (usando la constante, nunca el string suelto) y guarda. Es lo que se llama "borrado lógico": el curso deja de aparecer como disponible, pero la fila sigue existiendo en la base de datos — no se rompe ninguna relación con `tipo_curso`/`docente`/`administrador`, y se puede reactivar después si hace falta (poniendo `estado = 'A'` de nuevo).
- **`buscarDinamico`** — igual que hizo Paolo en la página pública: recibe el texto, busca con el método que ya armó Joel (`cursoService.buscarTodos(texto)`), y en vez de devolver la página completa, devuelve solo un pedacito marcado como `"filas"` (lo armas en el Paso 2).

## Paso 2 — `admin/cursos.html`

Necesitas dos cambios en el archivo que dejó Joel.

**Cambio A — dale un `id` al input de búsqueda que él ya puso**, para que tu JavaScript lo pueda encontrar (no crees un input nuevo, solo agrégale el `id` al que ya existe):

```html
<input type="text" id="buscarCursoAdmin" name="buscar" placeholder="Buscar curso por nombre..." th:value="${param.buscar}">
```

**Cambio B — envuelve las filas de la tabla (el `<tr th:each=...>`) en un fragmento**, igual que se hizo en la página pública con Paolo. Busca:

```html
<tbody>
  <tr th:each="curso : ${cursos}">
    ...
  </tr>
</tbody>
```

Y cámbialo por:

```html
<tbody id="cuerpoTabla">
  <th:block th:fragment="filas">
    <tr th:each="curso : ${cursos}">
      ...
    </tr>
  </th:block>
</tbody>
```

(Todo lo que va adentro de cada `<tr>` se queda igual — no lo toques, solo agrega el `id="cuerpoTabla"` al `<tbody>` y envuelve el `<tr th:each...>` con el `<th:block th:fragment="filas">`.)

**También agrega el botón de eliminar como un formulario**, no como un simple link (un `<a>` no puede mandar un `POST`, que es lo que necesita tu método `eliminar`). Busca la celda de acciones:

```html
<td class="admin-tabla-acciones">
  <a href="#" title="Ver"><i class="fa-solid fa-eye"></i></a>
  <a th:href="@{'/admin/cursos/editar/' + ${curso.idCurso}}" title="Editar"><i class="fa-solid fa-pen"></i></a>
  <a href="#" title="Eliminar" class="admin-accion-eliminar"><i class="fa-solid fa-trash"></i></a>
</td>
```

Y reemplaza el último `<a>` (el de eliminar) por esto:

```html
<td class="admin-tabla-acciones">
  <a href="#" title="Ver"><i class="fa-solid fa-eye"></i></a>
  <a th:href="@{'/admin/cursos/editar/' + ${curso.idCurso}}" title="Editar"><i class="fa-solid fa-pen"></i></a>
  <form th:action="@{'/admin/cursos/eliminar/' + ${curso.idCurso}}" method="post" class="admin-form-eliminar-inline"
        onsubmit="return confirm('¿Seguro que quieres eliminar este curso?');">
    <button type="submit" title="Eliminar" class="admin-accion-eliminar"><i class="fa-solid fa-trash"></i></button>
  </form>
</td>
```

`onsubmit="return confirm(...)"` — antes de enviar el formulario, el navegador muestra una ventanita de "¿estás seguro?". Si la persona presiona "Cancelar", el formulario no se envía.

## Paso 3 — `admin-cursos.js` (nuevo)

Créalo en `src/main/resources/static/js/admin-cursos.js`:

```javascript
const inputBuscar = document.querySelector('#buscarCursoAdmin');
const cuerpoTabla = document.querySelector('#cuerpoTabla');

if (inputBuscar && cuerpoTabla) {
  let temporizador;

  inputBuscar.addEventListener('input', function () {
    clearTimeout(temporizador);
    temporizador = setTimeout(function () {
      fetch('/admin/cursos/buscar-dinamico?texto=' + encodeURIComponent(inputBuscar.value))
        .then(function (respuesta) {
          return respuesta.text();
        })
        .then(function (html) {
          cuerpoTabla.innerHTML = html;
        });
    }, 300);
  });
}
```

Es el mismo patrón exacto que usó Paolo en la página pública (revisa su guía si quieres el detalle de por qué funciona así: `fetch` + `setTimeout`/`clearTimeout` para esperar a que la persona deje de escribir antes de buscar).

Agrega el script en `admin/layout.html` (no en `admin/cursos.html` — así queda disponible en todo el panel, no solo en esta página), dentro de un bloque `<th:block layout:fragment="js">` antes de cerrar `</body>` (si ese bloque no existe todavía en `admin/layout.html`, créalo):

```html
<th:block layout:fragment="js"></th:block>
```

Y en `admin/cursos.html`, dentro de `<head>` o después del `<div layout:fragment="content">`, agrega:

```html
<th:block layout:fragment="js">
  <script src="/js/admin-cursos.js" defer></script>
</th:block>
```

(Recuerda: las páginas de `/admin/**` usan rutas **absolutas** con `/` adelante, como ya se explicó en el resto del proyecto — `/js/admin-cursos.js`, no `js/admin-cursos.js`.)

## Paso 4 — `admin-cursos.css`

Agrégale estilos a `.admin-form-eliminar-inline` para que el formulario no se vea como un bloque separado, sino como un ícono más en línea con "Ver"/"Editar":

```css
.admin-form-eliminar-inline {
  display: inline;
}

.admin-form-eliminar-inline button {
  background: none;
  border: 0;
  padding: 0;
  font: inherit;
  cursor: pointer;
}
```

## Cómo probar que funciona

1. Entra a `/admin/cursos`.
2. Escribe algo en el buscador sin presionar nada — las filas deberían filtrarse solas después de un momentito (búsqueda dinámica).
3. Haz clic en el lápiz de "Editar" de algún curso — debería abrir el formulario con los datos ya puestos. Cambia algo y guarda — debería reflejarse en la tabla.
4. Haz clic en el tacho de "Eliminar" — debería preguntar confirmación, y al aceptar, el curso debería aparecer como "Inactivo" en la tabla (no debería desaparecer de la base de datos — revisa con una consulta `SELECT` directa si quieres confirmarlo).

## Coordinación con Joel

Ya que ambos editan `AdminCursoController.java` y `admin/cursos.html`, avísense cada vez que uno suba cambios a `develop`, y antes de mergear hagan `git pull`/`git merge develop` en su rama para traer lo último del otro (revisa el paso 7 de `CONTRIBUTING.md`).
