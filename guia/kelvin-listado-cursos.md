# Kelvin — Listado de los 20 cursos (rama `feature/listado-cursos`)

## Qué vas a lograr

`/catalogo` hoy muestra "Página en construcción". Tiene que mostrar los 20 cursos reales de la base de datos, con imagen, nombre, precio, etc. — eso es lo que pide el Avance 2 en "Página de productos/servicios".

## Archivos que vas a tocar

- `CursoRepository.java` — para poder pedir "todos los cursos activos".
- `CursoService.java` — el método que el controlador va a llamar.
- `CatalogoController.java` — que le pase la lista de cursos a la vista.
- `catalogo.html` — donde se dibuja cada curso como una tarjeta.
- `catalogo.css` — si hace falta ajustar estilos.

## Cómo pensarlo

**En el repositorio**: ya existe un método (`findByDestacadoTrueAndEstadoOrderByIdCursoAsc`) que trae solo los destacados. Tú necesitas algo parecido, pero sin el filtro de "destacado" — solo por estado. Spring Data arma la consulta sola con el nombre del método, siguiendo el mismo esquema (`findBy` + nombre del campo + `OrderBy` + campo + `Asc`). No hace falta escribir SQL, solo nombrar bien el método.

**En el servicio**: agrega un método (no borres `obtenerDestacados()`) que llame a ese nuevo método del repositorio, pasándole `Constantes.ESTADO_ACTIVO` — nunca el string `"A"` suelto (revisa por qué en `util/Constantes.java`, ya se usa así en todo el proyecto).

**En el controlador**: sigue el mismo patrón que ya tiene `InicioController` — recibe `CursoService` por constructor (no lo crees con `new`), y dentro del método que responde a `/catalogo`, agrega la lista de cursos al `Model` con algún nombre claro (por ejemplo `"cursos"`) para poder usarla en el HTML.

**En la vista**: donde hoy está el mensaje de "en construcción", necesitas recorrer la lista con `th:each` y, por cada curso, mostrar su imagen, nombre, tipo, precio y descripción. Abre `inicio.html` y mira cómo está armada la sección "Destacados" — usa exactamente esa misma idea (`th:each`, `th:text`, `th:src`) aplicada a tu propia lista. No necesitas inventar la sintaxis de Thymeleaf, solo copiar el patrón y cambiar los nombres de campo (revisa `model/Curso.java` para ver qué campos existen: `nombre`, `precio`, `imagen`, `tipoCurso.nombre`, etc.).

Para el precio, hay un detalle: es un número con decimales (`450.00`), y probablemente no quieras mostrar los decimales. Busca en `inicio.html` cómo se formatea ahí (usa una utilidad de Thymeleaf para números) y reutiliza la misma idea.

## Sobre el diseño

Es tu criterio armar la grilla, pero antes de inventar clases CSS nuevas, revisa si `inicio.css` ya tiene algo parecido para tarjetas de curso (`.content-card`, `.curso-*`) — si existen, reutilízalas o cópialas a `catalogo.css`, en vez de escribir el mismo diseño de tarjeta dos veces con nombres distintos.

## Coordinación con Paolo

Él va a agregar el buscador y un script que reemplaza el contenido de tu grilla cuando alguien busca algo. Antes de terminar tu parte, ponte de acuerdo con él sobre el `id` que le vas a poner al contenedor de las tarjetas — necesita ser estable, porque su JavaScript va a apuntar exactamente a ese `id`.

## Cómo probar

Entra a `/catalogo` y confirma que aparezcan las 20 tarjetas con datos reales (no inventados). Si algo no aparece o sale vacío, revisa la consola de la aplicación — ahí sale el error exacto.
