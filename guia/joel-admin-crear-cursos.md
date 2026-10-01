# Joel — Listado y creación de cursos en el CRUD (rama `feature/admin-crear-cursos`)

## Qué vas a lograr

`/admin/cursos` hoy es un placeholder. Tu parte: una tabla con todos los cursos (activos e inactivos), un botón para crear uno nuevo con su formulario, y un buscador estático sobre la tabla.

## Archivos que vas a tocar o crear

- `CursoRepository.java` y `CursoService.java` — para listar y buscar.
- `TipoCursoRepository.java` y `DocenteRepository.java` (nuevos, simples).
- `AdminCursoController.java` (compartido con Juan — coordina antes de tocarlo).
- `admin/cursos.html` (la tabla, compartido con Juan).
- `admin/curso-form.html` (nuevo — el formulario, Juan lo va a reusar para editar).
- `admin-cursos.css`.

## Cómo pensarlo

**Los repositorios nuevos**: `TipoCursoRepository` y `DocenteRepository` no necesitan ningún método propio por ahora — solo con extender `JpaRepository` ya tienes `findAll()`, que es lo único que vas a usar (para llenar los `<select>` del formulario con las opciones disponibles).

**En `CursoRepository`**: necesitas dos búsquedas nuevas, sin filtrar por estado esta vez — a diferencia de la página pública, el admin necesita ver también los cursos inactivos (para poder reactivarlos si hace falta). Piensa en "traer todos, ordenados" y "traer los que el nombre contenga tal texto, ordenados" — mismo mecanismo de nombres de método que ya se usa en el resto del proyecto.

**En `CursoService`**: agrega la lógica de "si no hay texto de búsqueda, trae todos; si hay, filtra" (mismo patrón que Paolo usa en la página pública, pero sin el filtro de estado). También te conviene un método que traiga un curso por su id — lo vas a usar tú para el formulario, y Juan lo va a necesitar para editar.

**El controlador**: necesita responder a varias rutas relacionadas — listar la tabla, mostrar el formulario vacío, y guardar lo que llega de ese formulario. Al listar, recibe el texto de búsqueda como parámetro opcional de la URL. Al mostrar el formulario, además de un curso vacío, tienes que pasarle al `Model` las listas de tipos y docentes (para las opciones del `<select>`).

**Sobre la imagen**: la entidad `Curso` solo guarda el *nombre* del archivo como texto, no el archivo en sí — así que en el formulario, el campo de la imagen va **aparte** del resto de los datos del curso (no es parte del objeto `Curso` que arma Spring automáticamente al leer el formulario). Necesitas recibirlo como un tipo especial para archivos subidos, guardarlo físicamente en la carpeta que ya está configurada (revisa `application.properties`, la propiedad `app.upload-dir`), con un nombre que no se repita nunca (piensa en algo que genere identificadores únicos), y guardar esa ruta en el campo `imagen` del curso antes de guardarlo en la base de datos.

**Sobre `id_admin`**: la entidad exige un administrador dueño del curso, pero el login real todavía no existe en este punto (Maykol lo está armando en paralelo). Como solución temporal, usa el primer administrador que encuentres en la base de datos — **avísale a Maykol de este apaño**, para que cuando su parte esté lista, lo reemplacen por el admin que esté realmente logueado.

**El formulario (`admin/curso-form.html`)**: va a ser compartido con Juan (él lo reusa para editar), así que piensa en él desde el principio como "un formulario que puede venir vacío (crear) o ya lleno (editar)". Investiga cómo Thymeleaf conecta un formulario completo a un objeto (`th:object` + `th:field`) — con eso, no necesitas escribir `name=` a mano en cada campo, y si el objeto ya trae datos, los campos se precargan solos. Para que el mismo formulario sirva para crear y editar, la acción del formulario (a dónde se manda) puede decidirse según si el curso ya tiene un `id` o no.

**La tabla**: columnas sugeridas — imagen, código, nombre, tipo, precio, estado, acciones. El campo `imagen` del curso ya trae la ruta completa lista para usar en un `src`, no necesitas armarla a mano.

## Coordinación con Juan

Él agrega "editar", "eliminar" y la búsqueda dinámica sobre este mismo controlador y esta misma tabla. Avísale cuando termines tu parte para que parta de tu versión.

## Sobre el diseño

Reutiliza el shell que ya existe (`admin/layout.html`, `admin.css`) — tú solo agregas lo específico de la tabla y el formulario en `admin-cursos.css`, siguiendo la misma paleta de colores del resto del panel.

## Cómo probar

Entra a `/admin/cursos`, confirma que aparecen los 20 cursos semilla. Prueba el buscador. Crea un curso nuevo con una imagen y confirma que aparece en la tabla y que el archivo realmente se guardó en la carpeta de subidas.
