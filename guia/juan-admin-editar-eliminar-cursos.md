# Juan — Búsqueda dinámica, editar y eliminar cursos (rama `feature/admin-editar-eliminar-cursos`)

## Qué vas a lograr

La otra mitad del CRUD de `/admin/cursos`: editar un curso existente, "eliminarlo" sin borrarlo de verdad, y que el buscador de la tabla filtre mientras se escribe.

**Necesitas que Joel ya haya terminado su parte primero** (la tabla, el formulario, los repositorios nuevos) — tu código se agrega encima del suyo.

## Archivos que vas a tocar

- `AdminCursoController.java` (compartido con Joel — coordina antes de tocarlo).
- `admin/cursos.html` (compartido con Joel).
- `admin-cursos.js` (nuevo).
- `admin-cursos.css`.

## Cómo pensarlo

**Editar — mostrar el formulario**: es casi idéntico a lo que hizo Joel para "crear", con una diferencia: en vez de pasarle al formulario un curso vacío, le pasas el curso real que ya existe (buscado por su id). Como el formulario que armó Joel usa `th:field`, todos los campos se van a precargar solos con los datos actuales — no necesitas hacer nada especial para eso.

**Editar — guardar los cambios**: aquí hay una decisión importante: en vez de reemplazar el curso completo con lo que llega del formulario, busca el curso real por su id y actualiza campo por campo. La razón es que hay datos que el formulario no trae (como quién lo creó originalmente) y que no quieres perder por accidente si los sobreescribes con un objeto nuevo vacío en esos campos.

**La imagen en edición es opcional**: si el admin no seleccionó una foto nueva, el archivo que llega viene vacío — en ese caso, no toques el campo `imagen` del curso, déjalo con el valor que ya tenía. Solo si viene un archivo de verdad, guárdalo (mismo mecanismo que usó Joel para crear) y ahí sí actualiza el campo.

**Eliminar**: la entidad tiene un campo `estado` pensado exactamente para esto — "eliminar" un curso nunca debería ser un borrado real de la fila. Busca el curso, cámbiale el estado a inactivo (usa la constante correspondiente, no un string suelto), y guárdalo. Como es una acción que modifica datos, tiene que dispararse con un método `POST`, no con un simple enlace — un `<a>` no sirve para eso, vas a necesitar un pequeño formulario alrededor del botón de eliminar en la tabla. De paso, es buena idea pedir confirmación antes de enviarlo (el navegador tiene una forma simple de preguntar "¿estás seguro?" antes de que un formulario se envíe).

**La búsqueda dinámica de la tabla**: es el mismo concepto que usó Paolo en la página pública — un endpoint que recibe el texto y devuelve, no la página completa, sino solo el pedacito de la tabla que cambia (las filas). Envuelve el `th:each` de las filas en un fragmento, igual que se hizo con las tarjetas de la página pública, dejando el `<tbody>` de afuera sin tocar — ese es el contenedor estable donde tu JavaScript va a insertar los resultados nuevos.

**El JavaScript**: mismo patrón que el de Paolo — escuchar el evento de escritura en el input de búsqueda, esperar un momentito después de la última tecla (para no saturar al servidor), pedir los resultados con `fetch`, y reemplazar el contenido del `<tbody>` con lo que llegue.

## Coordinación con Joel

Compartes el controlador y la tabla con él. Avísense mutuamente cuando suban cambios, y antes de fusionar tu rama trae lo último de `develop` para no pisar su trabajo (revisa el paso correspondiente en `CONTRIBUTING.md`).

## Cómo probar

Escribe en el buscador y confirma que la tabla se filtra sola. Edita un curso y confirma que los cambios se reflejan. Elimina un curso y confirma que queda marcado como inactivo en la tabla, pero que sigue existiendo en la base de datos (revísalo con una consulta directa si quieres estar seguro).
