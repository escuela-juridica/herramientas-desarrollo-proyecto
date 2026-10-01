# Juan — Búsqueda dinámica, editar y eliminar cursos (rama `feature/admin-editar-eliminar-cursos`)

## Qué vas a lograr

La segunda mitad del CRUD de `/admin/cursos`: poder editar un curso que ya existe, poder "eliminarlo" sin que desaparezca de la base de datos de verdad, y que el buscador de la tabla filtre mientras se escribe, sin recargar la página.

**Necesitas que Joel ya haya terminado su parte antes de empezar la tuya** — él deja armada la tabla, el formulario compartido, y los repositorios nuevos. Tu código se construye encima de lo que él deja, no lo reemplaza.

## Archivos que vas a tocar

- `AdminCursoController.java` (**compartido con Joel — coordina con él, vas a agregar métodos nuevos a un archivo que él ya habrá dejado funcionando**)
- `admin/cursos.html` (**compartido con Joel**)
- `admin-cursos.js` (nuevo)
- `admin-cursos.css`

## Paso 1: mostrar el formulario con los datos ya puestos

Para editar, necesitas una ruta que reciba el identificador de un curso (como parte de la URL), lo busque, y se lo pase a la misma vista de formulario que armó Joel. Esto es casi idéntico a lo que él hizo para "crear", con una diferencia conceptual importante: en vez de pasarle a la vista un curso recién creado y vacío, le pasas el curso real que ya existe en la base de datos. Como el formulario de Joel está conectado a un objeto completo (y no campo por campo con atributos escritos a mano), el solo hecho de pasarle un curso que ya tiene datos hace que todos los campos aparezcan precargados — no tienes que escribir ninguna lógica adicional para "rellenar" el formulario, eso ya viene resuelto por cómo está construido.

No olvides que esta ruta también necesita las listas de tipos de curso y de docentes para los menús desplegables, exactamente igual que la de crear — sin eso, el formulario se vería, pero los `<select>` aparecerían vacíos.

## Paso 2: guardar los cambios de una edición

Acá hay una decisión de diseño importante, y vale la pena que la entiendas bien antes de escribirla. Cuando el formulario de edición se envía, **no conviene tomar el objeto que llega del formulario y guardarlo directamente como si fuera el curso final**. La razón es que ese objeto recién llegado solo tiene los campos que el formulario conoce y envía — pero el curso real en la base de datos puede tener información que el formulario no trae (quién lo creó originalmente, por ejemplo). Si simplemente reemplazas el curso completo por el objeto que llega del formulario, corres el riesgo de perder esos datos sin darte cuenta, porque en el objeto nuevo esos campos llegarían vacíos.

El camino correcto es: buscar el curso real por su identificador (tienes el método para esto, lo dejó listo Joel en el servicio), y después, campo por campo, copiar los valores nuevos desde el objeto que llegó del formulario hacia el curso real que ya existía. Es un poco más de código que simplemente guardar lo que llega, pero es la forma de asegurarte de que solo cambia lo que el formulario realmente permite cambiar.

## Paso 3: la imagen en una edición es un caso distinto al de creación

Cuando Joel construyó la creación, la imagen era obligatoria (un curso nuevo siempre necesita una foto). En edición, en cambio, el administrador probablemente **no** quiera cambiar la foto cada vez que edita cualquier otro dato del curso — así que el campo de imagen en el formulario tiene que ser opcional. Esto tiene una consecuencia concreta en tu código: tienes que revisar si realmente llegó un archivo nuevo (si el campo viene vacío porque la persona no seleccionó nada, el archivo que recibes va a estar presente pero sin contenido). Solo si hay un archivo de verdad, lo guardas en la carpeta de subidas (mismo mecanismo que usó Joel) y actualizas el campo `imagen` del curso. Si no llegó nada, simplemente no toques ese campo — el curso se queda con la imagen que ya tenía.

## Paso 4: eliminar — por qué nunca es un borrado real

La entidad `Curso` tiene un campo `estado` pensado exactamente para este propósito. "Eliminar" un curso desde el CRUD nunca debería significar borrar la fila de la base de datos — significa marcarlo como inactivo. Esto se llama borrado lógico, y la razón de hacerlo así (en vez de un borrado real) es que el curso puede tener referencias desde otras partes del sistema, y además mantiene la posibilidad de reactivarlo después si fue un error. Tu trabajo acá es simple en código (buscar el curso, cambiarle el estado a inactivo usando la constante correspondiente del proyecto, guardarlo) pero importante en concepto: no uses el método del repositorio que borra una fila completa, ni se te ocurra, aunque parezca "más directo".

Como esta acción modifica datos (no es solo "mostrar algo"), tiene que dispararse con una petición de tipo POST, nunca con un simple enlace — un enlace HTML común solo puede generar peticiones GET, que están pensadas para pedir información, no para cambiarla. Vas a necesitar envolver el botón de eliminar en su propio formulario pequeño dentro de la tabla, uno por cada fila. De paso, conviene pedir confirmación antes de que esa acción se dispare de verdad — el navegador tiene una forma simple de mostrar una ventana de "¿estás seguro?" justo antes de que un formulario se envíe, y si la persona cancela, el envío no ocurre.

## Paso 5: la búsqueda dinámica dentro de la tabla

Esto es, conceptualmente, exactamente lo mismo que hizo Paolo en la página pública, aplicado a la tabla del admin en vez de a la grilla de tarjetas. Necesitas una ruta que reciba el texto de búsqueda y devuelva, no la página completa, sino solo el fragmento correspondiente a las filas de la tabla — usando el mismo mecanismo de fragmentos de Thymeleaf que ya se explicó en la guía de Paolo. Envuelve el `th:each` que recorre los cursos dentro de la tabla (el que dejó Joel) con ese fragmento, dejando el `<tbody>` que lo contiene fuera del fragmento — por la misma razón que en la página pública: ese es el contenedor estable donde tu JavaScript va a insertar los resultados, y necesita seguir existiendo entre una búsqueda y la siguiente.

Tu script de este lado repite la misma estructura que el de Paolo: una referencia al campo de búsqueda, una al contenedor de filas, una función que hace `fetch` a tu nueva ruta y reemplaza el contenido, disparada con el mecanismo de espera después de la última tecla (revisa su guía si necesitas repasar el porqué de esa espera).

## Coordinación con Joel

Comparten el controlador completo y la plantilla de la tabla. Esto significa que es muy probable que, si ambos trabajan al mismo tiempo sin avisarse, terminen generando conflictos al momento de fusionar las ramas. Avísense cada vez que uno suba cambios importantes, y antes de abrir tu Pull Request, trae a tu rama lo último que Joel ya haya integrado en `develop` (revisa el paso correspondiente en `CONTRIBUTING.md` sobre cómo actualizar tu rama con lo nuevo de `develop`) — así resuelves cualquier conflicto tú mismo, en tu propia rama, en vez de que aparezca de sorpresa al momento de la revisión.

## Cómo probar que funciona

Escribe en el buscador de la tabla y confirma que las filas se filtran solas, sin que la página se recargue. Edita un curso existente, cambia algún dato (y prueba también sin cambiar la imagen, para confirmar que no se borra ni se rompe), guarda, y confirma que el cambio se refleja en la tabla. Elimina un curso y confirma dos cosas: que en la tabla aparece marcado como inactivo (no que desaparece de la lista sin explicación), y que sigue existiendo en la base de datos si lo revisas con una consulta directa.
