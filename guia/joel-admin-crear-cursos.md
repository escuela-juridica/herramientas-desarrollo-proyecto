# Joel — Listado y creación de cursos en el CRUD (rama `feature/admin-crear-cursos`)

## Qué vas a lograr

`/admin/cursos` hoy muestra un cartel de "en construcción" (el panel administrativo en sí, con su sidebar y su diseño, ya existe — solo falta el contenido real de esta página). Te toca la primera mitad del CRUD: una tabla que muestre todos los cursos de verdad (activos e inactivos, el administrador necesita ver ambos), la posibilidad de crear un curso nuevo a través de un formulario, y un buscador estático sobre la tabla.

## Archivos que vas a tocar o crear

- `CursoRepository.java` y `CursoService.java` (ampliarlos)
- `TipoCursoRepository.java` y `DocenteRepository.java` (nuevos, muy simples)
- `AdminCursoController.java` (**compartido con Juan — avísale antes de empezar a tocarlo**)
- `admin/cursos.html` (la tabla, **compartido con Juan**)
- `admin/curso-form.html` (nuevo — el formulario de creación; Juan lo va a reusar para editar, así que piénsalo con eso en mente desde el principio)
- `admin-cursos.css`

## Paso 1: dos repositorios nuevos, pero muy pequeños

Para que el formulario de creación tenga de dónde elegir el tipo de curso y el docente (son relaciones obligatorias de la entidad `Curso`), necesitas poder consultar todos los tipos y todos los docentes que existen. No hace falta ningún método propio para esto — con que la interfaz extienda `JpaRepository`, ya viene incluido un método que trae todas las filas de la tabla. Es la forma más simple de repositorio que vas a escribir en todo el proyecto: una línea de declaración de la interfaz, sin ningún método adicional.

## Paso 2: ampliar `CursoRepository`

A diferencia de la página pública (donde solo interesan los cursos activos), acá el administrador necesita ver también los inactivos — para eso existe el borrado lógico, para poder revisarlos y eventualmente reactivarlos. Necesitas dos métodos nuevos: uno que traiga absolutamente todos los cursos ordenados, y otro que busque por coincidencia de texto en el nombre, también sin filtrar por estado. Son queries derivadas, igual que las que ya usaron Kelvin y Paolo — la diferencia es simplemente que estas no incluyen la condición de estado en el nombre del método.

## Paso 3: ampliar `CursoService`

Acá replicas la misma idea que usó Paolo para la búsqueda pública: un método que, si no recibe texto, devuelve todos los cursos; si recibe texto, usa el método de búsqueda. También vas a necesitar un método que traiga un único curso a partir de su identificador — lo vas a usar tú mismo para cargar los datos en el formulario cuando alguien quiera editar... espera, en realidad eso es parte de lo que hace Juan, pero como ambos comparten el servicio, conviene que tú dejes ese método ya armado (es solo una línea: buscar por id y, si no existe, lanzar un error), para que Juan no tenga que volver a tocar este archivo después de ti.

## Paso 4: el controlador — tres responsabilidades distintas

`AdminCursoController` va a necesitar responder a varias rutas relacionadas, cada una con un propósito distinto: una para mostrar la tabla (la que ya existe, hoy sin datos reales), una para mostrar el formulario vacío de un curso nuevo, y una que reciba los datos de ese formulario cuando se envía y los guarde. Vale la pena pensarlas por separado, porque mezclarlas en un solo método confuso es justo el tipo de cosa que hace un controlador difícil de mantener.

La ruta de listar necesita, además de traer los cursos, aceptar un parámetro opcional con el texto de búsqueda (para la búsqueda estática) y pasárselo a tu método del `CursoService`. La ruta que muestra el formulario vacío necesita, además de darle a la vista un curso recién creado y todavía sin datos, pasarle también las listas de tipos y de docentes que trajiste en el Paso 1 — sin esas listas, el formulario no tiene de dónde sacar las opciones para sus menús desplegables.

## Paso 5: la imagen — por qué va separada del resto de los datos

Este es un punto donde conviene pensar antes de escribir. La entidad `Curso` no guarda la imagen en sí — guarda únicamente el *nombre del archivo* (un texto, como cualquier otro campo). El archivo real vive en el sistema de archivos, en la carpeta configurada en `application.properties` bajo la propiedad `app.upload-dir`. Esto significa que cuando el formulario de creación se envía, lo que llega del navegador son dos cosas distintas al mismo tiempo: por un lado, los datos normales del curso (nombre, precio, descripción...) que Spring puede convertir automáticamente en un objeto `Curso`; por otro lado, el archivo de imagen en sí, que necesitas recibir con un tipo especial pensado para archivos subidos, completamente aparte del objeto `Curso`.

Tu trabajo en el método que procesa la creación es: recibir esos dos elementos por separado, guardar físicamente el archivo en la carpeta configurada (dándole un nombre que nunca se repita — investiga cómo generar identificadores únicos en Java, vas a necesitar algo así para no pisar un archivo existente si dos imágenes se llaman igual), y recién después de guardarlo, poner esa ruta como el valor del campo `imagen` del curso, antes de guardar el curso completo en la base de datos.

**Sobre `id_admin`**: la entidad exige que todo curso tenga un administrador asociado (es una relación obligatoria), pero en este momento del desarrollo el login real de Maykol probablemente todavía no esté integrado. Como solución temporal y explícita, puedes usar el primer administrador que encuentres en la base de datos como dueño de cualquier curso que se cree desde este formulario. Esto no es la solución final — es un apaño para poder seguir avanzando en paralelo sin bloquear a nadie. Avísale a Maykol de esta decisión, porque cuando su parte esté lista, hay que volver a este punto y reemplazarlo por el administrador real de la sesión.

## Paso 6: el formulario compartido — pensarlo para dos usos desde el día uno

Vas a crear `admin/curso-form.html` pensando en que Juan lo va a reusar tal cual para editar un curso existente, no solo para crear uno nuevo. La clave para que un mismo formulario sirva para los dos casos es conectar todo el formulario a un objeto `Curso` (usando el mecanismo de Thymeleaf para formularios ligados a un objeto, que te permite declarar una sola vez cuál es el objeto y después, campo por campo, decir a qué propiedad de ese objeto corresponde cada input, sin tener que escribir manualmente los atributos `name`). La ventaja de hacerlo así es que si el objeto `Curso` que le llega al formulario ya tiene datos (porque viene de "editar"), los campos se van a precargar solos con esos valores; si el objeto está recién creado y vacío (porque viene de "crear"), los campos van a aparecer en blanco — sin que tengas que escribir ninguna lógica para distinguir los dos casos dentro del HTML.

Lo único que sí necesita decidirse según el caso es a dónde se envía el formulario cuando se presiona guardar: si el curso todavía no tiene un identificador asignado (porque es nuevo), tiene que ir a la ruta de creación; si ya tiene identificador (porque se está editando), tiene que ir a la ruta de edición con ese identificador incluido en la URL. Esa decisión se puede expresar directamente en el atributo que define a dónde se envía el formulario, usando una condición simple.

No olvides que el formulario necesita indicar explícitamente que puede contener archivos (no es el comportamiento por defecto de un formulario HTML), y que el campo de la imagen, al no ser parte de los datos del objeto `Curso`, no se conecta de la misma forma que los demás campos — necesita su propio nombre explícito, que tiene que coincidir con el que uses para recibirlo en el controlador.

## Paso 7: la tabla

Columnas razonables: una miniatura de la imagen, el código, el nombre, el tipo de curso, el precio, el estado, y una columna de acciones. El campo `imagen` del curso ya contiene la ruta completa lista para usar directamente como origen de una imagen — no necesitas construir esa ruta a mano concatenando texto, simplemente úsala tal como está guardada.

## Coordinación con Juan

Él va a agregar "editar", "eliminar" y la búsqueda dinámica encima de lo que tú construyas, en los mismos archivos. Avísale apenas termines y subas tus cambios, para que él parta de tu versión más reciente y no de una vieja — y revisen juntos el paso de `CONTRIBUTING.md` sobre traer los cambios de `develop` antes de abrir cada Pull Request, porque van a estar tocando los mismos archivos varias veces.

## Sobre el diseño

El panel ya tiene su propio sistema visual armado (`admin/layout.html` para la estructura, `admin.css` para el sidebar y la barra superior) — no necesitas, ni deberías, tocar esos archivos. Tu trabajo de diseño se concentra en `admin-cursos.css`, usando la misma paleta de colores y las mismas variables que ya se usan en el resto del panel, para que la tabla y el formulario se sientan parte del mismo sistema y no como una página aparte.

## Cómo probar que funciona

Entra a `/admin/cursos` y confirma que aparecen los 20 cursos semilla con datos reales. Prueba el buscador con un texto que sepas que existe. Crea un curso nuevo completo, con imagen incluida, guárdalo, y confirma dos cosas: que aparece en la tabla con sus datos correctos, y que el archivo de la imagen efectivamente se guardó dentro de la carpeta de subidas del proyecto (no solo que la base de datos tiene una ruta apuntando a un archivo que en realidad no existe).
