# Kelvin — Listado de los 20 cursos (rama `feature/listado-cursos`)

## Qué vas a lograr

`/catalogo` hoy muestra un banner y, debajo, el mensaje "Página en construcción". Tu tarea es reemplazar ese mensaje por los 20 cursos reales que ya están sembrados en la base de datos, cada uno mostrando su imagen, nombre, tipo, precio y descripción. Esto es literalmente el requerimiento "Página de productos/servicios: mínimo 20 productos cargados desde la base de datos, con sus imágenes" que pide la guía del Avance 2.

## Archivos que vas a tocar

- `src/main/java/pe/edu/escuela/app/repository/CursoRepository.java`
- `src/main/java/pe/edu/escuela/app/service/CursoService.java`
- `src/main/java/pe/edu/escuela/app/controller/CatalogoController.java`
- `src/main/resources/templates/catalogo.html`
- `src/main/resources/static/css/catalogo.css`

## Paso 1: el repositorio — cómo pedirle a la base de datos "todos los cursos activos"

`CursoRepository` ya tiene un método que trae solo los destacados (`findByDestacadoTrueAndEstadoOrderByIdCursoAsc`). Fíjate en algo importante: ese método **no tiene cuerpo** — es solo una firma dentro de una interfaz. Spring Data JPA lee el nombre del método, lo descompone en palabras clave (`findBy`, el nombre de un campo, `And`, otro campo, `OrderBy`, otro campo, `Asc`) y arma la consulta SQL él solo, sin que nadie escriba una sola línea de SQL. Es el mismo mecanismo de "query derivada" que ya se usó para los destacados, y Thymeleaf/Spring Boot lo validan al arrancar la aplicación: si te equivocas en el nombre de un campo, el proyecto directamente no arranca y te avisa dónde está el error, en vez de fallar en silencio más adelante.

Para tu caso, necesitas lo mismo que el método de destacados, pero sin el filtro de "destacado" — solo filtrando por `estado`. Piensa en el nombre del método como una oración: "encuéntrame por estado, ordenado por id de curso, ascendente". Antes de escribirlo, mira también `src/main/java/pe/edu/escuela/app/util/Constantes.java` — ahí están definidas `ESTADO_ACTIVO` y `ESTADO_INACTIVO`, que es lo que vas a usar como valor del parámetro `estado` cuando llames a este método desde el servicio (nunca pases el string `"A"` escrito a mano).

## Paso 2: el servicio — por qué no se llama al repositorio directo desde el controlador

Abre `CursoService.java`. Ya tiene un método `obtenerDestacados()` que llama al repositorio y le pasa la constante de estado activo. Tu trabajo es agregar un método hermano (sin borrar el que ya existe) que haga lo mismo pero llamando al método nuevo que creaste en el Paso 1.

Esto no es solo una formalidad: el proyecto sigue la arquitectura en capas **Controller → Service → Repository**, donde cada capa tiene una responsabilidad clara. El `Repository` solo sabe hablar con la base de datos. El `Service` es donde iría la lógica de negocio (reglas, decisiones, combinaciones de datos) si hiciera falta — en este caso es simple, pero mañana si alguien necesita, por ejemplo, "traer los cursos activos pero excluyendo los que ya empezaron", ese tipo de regla se agrega acá, no en el controlador ni en el repositorio. El `Controller` solo coordina: recibe la petición HTTP, le pide los datos al `Service`, y decide qué vista mostrar. Si saltaras directo de `CatalogoController` a `CursoRepository`, estarías rompiendo esa separación y sería más difícil para el resto del equipo entender dónde está cada cosa.

## Paso 3: el controlador — cómo le llega la lista al HTML

Abre `CatalogoController.java`. Ahora mismo no tiene ningún campo ni constructor — solo un método que devuelve el nombre de la vista. Fíjate cómo está armado `InicioController.java`: tiene un campo `private final CursoService cursoService`, un constructor que lo recibe como parámetro, y dentro del método que atiende la petición, usa `model.addAttribute("nombreQueElijas", loQueSeaQueQuieraMostrar)`.

Replica exactamente esa misma forma en `CatalogoController`: agrega el campo, el constructor, y dentro del método que responde a `/catalogo`, agrega al `Model` la lista que te devuelve tu método nuevo del `CursoService`, con un nombre descriptivo (por ejemplo `"cursos"`). Ese nombre es el que vas a usar del lado del HTML en el próximo paso — tienen que coincidir exactamente, porque Thymeleaf busca en el modelo una variable con ese nombre literal.

**¿Por qué recibir el `CursoService` por constructor y no crearlo con `new CursoService()`?** Porque `CursoService` está marcado con `@Service`, lo que significa que Spring ya creó una instancia de esa clase al arrancar la aplicación y la tiene guardada, lista para repartir a quien la necesite. Si tú escribieras `new CursoService(...)`, estarías creando una copia nueva y separada, que no comparte nada con la que usa el resto del proyecto — y además tendrías que crear tú mismo el `CursoRepository` que `CursoService` necesita, lo cual se vuelve un lío. Dejar que Spring te lo entregue por el constructor (esto se llama "inyección de dependencias") es más simple y es el estándar del proyecto.

## Paso 4: la plantilla HTML — cómo repetir un bloque por cada curso

Este es el paso más largo de explicar, porque es donde más gente se traba la primera vez que usa Thymeleaf.

Dentro de `catalogo.html`, busca el bloque que dice "Página en construcción". Vas a reemplazarlo por un contenedor que recorra la lista `cursos` y, por cada uno, dibuje una tarjeta. La herramienta para "recorrer una lista y repetir HTML" en Thymeleaf es el atributo `th:each`, que se escribe sobre la etiqueta que quieres repetir: algo como `th:each="curso : ${cursos}"` puesto sobre un `<article>` hace que ese `<article>` completo se repita una vez por cada elemento de la lista `cursos`, y dentro de ese bloque repetido, la palabra `curso` (la que pusiste a la izquierda del `:`) representa "el curso actual de esta vuelta".

Una vez que tienes esa variable `curso` disponible, puedes acceder a cualquier campo de la entidad usando un punto, igual que en Java: `curso.nombre`, `curso.precio`, `curso.imagen`. Para mostrar ese valor dentro de una etiqueta, se usa `th:text`, por ejemplo sobre un `<h3>` para el nombre, o sobre un `<p>` para la descripción. Para poner un valor dentro de un atributo HTML en vez de como texto visible (como el `src` de una imagen), se usa `th:src` en vez de un `src` normal.

Abre `inicio.html` y busca la sección de "Destacados" (la que ya está implementada) — ahí vas a ver exactamente este patrón ya funcionando: un `th:each` sobre la tarjeta, y varios `th:text`/`th:src` adentro accediendo a campos del curso. No necesitas inventar nada nuevo: es la misma estructura, aplicada a tu propia lista de cursos y en tu propia página. Cópiate la idea (no necesariamente el archivo completo), y ajusta los campos a lo que quieras mostrar en el catálogo.

Un detalle que te vas a encontrar: el campo `tipoCurso` de un curso no es un texto simple, es una relación hacia otra entidad (`TipoCurso`). Para mostrar su nombre, necesitas bajar un nivel más: `curso.tipoCurso.nombre`. Esto funciona porque dentro de la clase `Curso` existe un campo `tipoCurso` de tipo `TipoCurso`, y esa clase a su vez tiene un campo `nombre` — Thymeleaf te deja encadenar el acceso con puntos, igual que harías en Java con `curso.getTipoCurso().getNombre()`.

Otro detalle: el precio es un número con decimales (como `450.00`), y seguramente no quieras mostrar esos dos ceros. Busca en `inicio.html` cómo se formatea el precio en la sección de destacados — vas a encontrar el uso de una utilidad de formato de números que ya trae Thymeleaf (`#numbers`). Reutiliza esa misma expresión, no hace falta que inventes tu propia forma de formatear números.

## Paso 5: los estilos — reutilizar antes de crear

Antes de escribir una sola línea nueva en `catalogo.css`, revisa `inicio.css`. Es muy probable que ya existan ahí clases pensadas para una "tarjeta de curso" (busca algo como `.content-card`, `.curso-head`, `.curso-cover`, `.curso-body`) porque la sección de Destacados de inicio ya las usa. El proyecto sigue la regla de "un CSS por página para lo específico, pero sin duplicar diseño si ya existe algo reutilizable" — si esas clases ya resuelven el problema de cómo se ve una tarjeta de curso, cópialas (no las muevas, inicio las sigue necesitando) a `catalogo.css`, en vez de inventar un sistema de tarjetas distinto desde cero. Si en algún momento el equipo nota que la misma estructura de tarjeta se repite en tres páginas distintas, ahí sí tendría sentido conversar sobre moverla a `base.css` — pero esa decisión es de todo el equipo, no la tomes solo.

## Coordinación con Paolo

Paolo va a agregar el buscador en esta misma página, y su JavaScript va a necesitar reemplazar el contenido del contenedor donde pusiste las tarjetas, sin tocar el resto de la página. Para que eso funcione, el contenedor que envuelve todas las tarjetas (el `div` que las agrupa) necesita un `id` fijo y estable — avísale a Paolo cuál le pusiste antes de que él empiece su parte, porque su código va a apuntar exactamente a ese `id`. No lo cambies después sin avisarle, o su búsqueda va a dejar de funcionar sin que sea obvio por qué.

## Cómo probar que funciona

Levanta la aplicación y entra a `/catalogo`. Deberías ver 20 tarjetas, cada una con una imagen real (no rota), un nombre, un tipo de curso, un precio y una descripción — todo distinto entre sí, porque son datos reales de la base de datos, no texto repetido. Si la página se ve vacía o solo aparece el encabezado sin tarjetas, revisa la consola donde corre la aplicación: Spring Boot imprime ahí el error exacto (por ejemplo, si te equivocaste en el nombre de un campo al escribir `th:text`, o si el método del repositorio no compiló).
