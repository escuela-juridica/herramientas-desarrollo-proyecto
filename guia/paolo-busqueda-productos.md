# Paolo — Búsqueda estática y dinámica en Productos (rama `feature/busqueda-productos`)

## Qué vas a lograr

En `/catalogo`, dos formas de buscar: **estática** (botón, busca cuando lo presionas) y **dinámica** (busca mientras escribes, sin recargar). Las dos consultan la base de datos — necesitas que Kelvin ya haya armado la grilla de tarjetas antes de empezar.

## Archivos que vas a tocar

- `CursoRepository.java` — un método de búsqueda por nombre.
- `CursoService.java` — la lógica de "si no hay texto, muestra todo; si hay, filtra".
- `CatalogoController.java` — un endpoint nuevo (compartido con Kelvin, coordina antes de tocarlo).
- `catalogo.html` — el input de búsqueda + convertir la grilla en un fragmento reusable.
- `catalogo.js` (nuevo).

## Cómo pensarlo

**La idea central, antes de escribir nada**: la tarjeta de un curso (el HTML que arma Kelvin) no debería existir en dos lugares — una vez en Thymeleaf y otra vez armada a mano en JavaScript. Si la construyes dos veces, en algún momento se van a desincronizar. La forma correcta acá es que **el servidor siga siendo el único que arma el HTML de las tarjetas**, y que JavaScript solo le pida al servidor ese HTML ya listo y lo pegue en la página — nunca que JavaScript intente construir las tarjetas por su cuenta.

**En el repositorio**: necesitas un método que busque por nombre, sin importar mayúsculas/minúsculas, y que además respete el estado activo. Piensa en el nombre del método como una frase: "encuéntrame por nombre que contenga esto, y que el estado sea tal" — Spring Data lo traduce solo si el nombre está bien armado (mismo mecanismo que ya usan los métodos existentes).

**En el servicio**: un método que reciba el texto escrito. Si viene vacío o nulo, no tiene sentido "buscar nada" — en ese caso, mejor devolver lo mismo que ya devuelve tu método de "todos los activos" (el que hizo Kelvin). Si viene con texto, ahí sí usa el método de búsqueda del repositorio.

**En el controlador**: agrega una ruta nueva (pueden llamarla `/catalogo/buscar`) que reciba el texto como parámetro de la URL y llame a tu método del servicio. La parte importante: **no le devuelvas la página completa** — Thymeleaf permite devolver solo un pedacito marcado de una vista, usando la notación `nombreDeVista :: nombreDelFragmento`. Investiga cómo se usa (`th:fragment` del lado del HTML, y el `::` del lado del controlador) — es exactamente lo que necesitas para que el buscador no recargue todo.

**En la vista**: envuelve las tarjetas (el `th:each` que hizo Kelvin) en un fragmento con nombre, pero **dejando el contenedor de afuera sin tocar** (el `div` con el `id` que le pusieron). La razón: ese `div` tiene que seguir existiendo siempre — es donde tu JavaScript va a meter los resultados nuevos. Solo lo de adentro cambia.

**En el JavaScript**: vas a necesitar `fetch()` para pedirle al servidor el resultado de la búsqueda, y reemplazar el contenido del contenedor con lo que llegue. Dos detalles importantes:
1. Para la búsqueda dinámica, no quieres mandar una petición por cada tecla — investiga la técnica de "debounce" (esperar un ratito después de la última tecla antes de buscar).
2. Para la estática, es el mismo `fetch`, pero disparado por el evento de click de un botón en vez del evento de escritura.

## Coordinación con Kelvin

Necesitas que la estructura de la tarjeta ya esté definida por él antes de envolverla en un fragmento — no cambies el diseño de la tarjeta en sí, solo agrégale el fragmento alrededor.

## Cómo probar

Escribe algo en el buscador y espera un momento sin tocar nada más — las tarjetas deberían cambiar solas. Borra el texto y usa el botón — deberían volver a aparecer los 20 cursos. Si el `fetch` falla, revísalo con las herramientas de desarrollador del navegador (pestaña "Network").
