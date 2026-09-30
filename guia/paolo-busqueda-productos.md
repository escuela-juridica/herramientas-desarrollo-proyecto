# Paolo — Búsqueda estática y dinámica en Productos (rama `feature/busqueda-productos`)

## Qué vas a lograr

En `/catalogo`, agregar un buscador con dos comportamientos:
- **Dinámica**: mientras escribes, las tarjetas se filtran solas, sin recargar la página ni apretar nada.
- **Estática**: un botón "Buscar" que hace lo mismo pero solo cuando lo presionas.

Las dos consultan la base de datos (no se hace el filtro en el navegador con JavaScript puro, sino pidiéndole al servidor).

**Importante:** necesitas que Kelvin ya haya armado la grilla de tarjetas en `catalogo.html` antes de empezar (la guía de él explica cómo). Tu trabajo parte de ahí.

## Archivos que vas a tocar

1. `CursoRepository.java` (agregar una línea)
2. `CursoService.java` (agregar un método)
3. `CatalogoController.java` (agregar un método — **archivo compartido con Kelvin, avísale antes de editarlo**)
4. `catalogo.html` (agregar el input de búsqueda + convertir la grilla en un fragmento — **compartido con Kelvin**)
5. `catalogo.js` (nuevo)

## Paso 1 — `CursoRepository.java`

Agrega esta línea dentro de la interfaz (junto a las que ya dejó Kelvin):

```java
List<Curso> findByNombreContainingIgnoreCaseAndEstado(String texto, String estado);
```

**¿Qué hace?** `Containing` = que el nombre "contenga" ese texto en cualquier parte (no que sea exactamente igual). `IgnoreCase` = no importa mayúscula/minúscula. Entonces si alguien escribe "civil", va a encontrar "Diplomado en Derecho Civil Patrimonial" aunque no haya escrito la palabra completa ni con mayúscula.

## Paso 2 — `CursoService.java`

Agrega este método (no borres los que ya están):

```java
  public List<Curso> buscar(String texto) {
    if (texto == null || texto.isBlank()) {
      return obtenerActivos();
    }
    return cursoRepository.findByNombreContainingIgnoreCaseAndEstado(texto, Constantes.ESTADO_ACTIVO);
  }
```

**¿Por qué el `if`?** Si el campo de búsqueda está vacío (la persona borró todo lo que había escrito), no tiene sentido buscar "nada" — en ese caso simplemente mostramos todos los cursos otra vez, reusando el método `obtenerActivos()` que ya armó Kelvin.

## Paso 3 — `CatalogoController.java`

Este archivo ya lo modificó Kelvin (le agregó el constructor con `CursoService`). Tú le agregas un método nuevo al final, **sin borrar lo que él ya puso**:

```java
  @GetMapping("/catalogo/buscar")
  public String buscar(@RequestParam(required = false) String texto, Model model) {
    model.addAttribute("cursos", cursoService.buscar(texto));
    return "catalogo :: tarjetas";
  }
```

**¿Qué es `"catalogo :: tarjetas"`?** Normalmente un controlador devuelve el nombre de una página completa (como `"catalogo"`, que renderiza todo `catalogo.html`). Con `::` le dices "no me devuelvas la página entera, solo el pedacito marcado con el nombre `tarjetas`". Ese pedacito lo vas a marcar tú mismo en el Paso 4. Esto es clave para que el buscador no recargue toda la página — el navegador solo recibe las tarjetas nuevas, no el HTML completo otra vez.

`@RequestParam(required = false) String texto` — recibe el texto que el usuario escribió, desde la URL (`/catalogo/buscar?texto=civil`). `required = false` es importante: si en algún momento se llama a esta ruta sin el parámetro `texto`, no debe explotar, simplemente `texto` llega como `null` (por eso el `if` del Paso 2 revisa `null`).

## Paso 4 — `catalogo.html`

Busca el bloque que armó Kelvin:

```html
<div class="grid6" id="cursosGrid">
  <article th:each="curso : ${cursos}" class="content-card curso">
    ...
  </article>
</div>
```

Tienes que marcar **solo las tarjetas** (no el `div` que las contiene) como el fragmento `"tarjetas"`, usando un `<th:block>` — que es una etiqueta invisible, no genera ningún HTML propio, solo agrupa:

```html
<div class="grid6" id="cursosGrid">
  <th:block th:fragment="tarjetas">
    <article th:each="curso : ${cursos}" class="content-card curso">
      ...
    </article>
  </th:block>
</div>
```

(Todo lo que va adentro del `<article>` se queda exactamente igual — no lo toques, solo agrega las dos líneas del `<th:block>` envolviendo el `<article th:each...>`.)

**¿Por qué el `<div id="cursosGrid">` queda AFUERA del fragmento?** Porque ese `div` tiene que seguir existiendo siempre en la página — es el "contenedor fijo" donde tu JavaScript va a ir metiendo las tarjetas nuevas cada vez que alguien busca algo. Si el `div` mismo formara parte de lo que se reemplaza, se complica más de la cuenta. Dejándolo afuera, tu JS solo tiene que cambiar lo que hay *adentro* del `div`, nunca el `div` en sí.

Ahora agrega el buscador, justo antes del `<div id="cursosGrid">`:

```html
<div class="buscador-cursos">
  <input type="text" id="buscarCurso" placeholder="Buscar curso por nombre...">
  <button type="button" id="botonBuscar" class="site-button button-primary">Buscar</button>
</div>
```

## Paso 5 — `catalogo.js` (nuevo archivo)

Créalo en `src/main/resources/static/js/catalogo.js`:

```javascript
const input = document.querySelector('#buscarCurso');
const boton = document.querySelector('#botonBuscar');
const grid = document.querySelector('#cursosGrid');

function buscarCursos() {
  const texto = input.value;
  fetch('/catalogo/buscar?texto=' + encodeURIComponent(texto))
    .then(function (respuesta) {
      return respuesta.text();
    })
    .then(function (html) {
      grid.innerHTML = html;
    });
}

// Búsqueda ESTÁTICA: solo busca cuando se presiona el botón.
boton.addEventListener('click', buscarCursos);

// Búsqueda DINÁMICA: busca mientras se escribe, esperando un poquito
// después de la última tecla para no saturar al servidor.
let temporizador;
input.addEventListener('input', function () {
  clearTimeout(temporizador);
  temporizador = setTimeout(buscarCursos, 300);
});
```

**Explicación de cada parte:**

- `document.querySelector('#cursosGrid')` — agarra el `div` que ya existe en el HTML (el que Kelvin armó), para poder cambiarlo después.
- `fetch('/catalogo/buscar?texto=...')` — le pide al navegador que haga una petición HTTP a esa ruta, sin recargar la página. Es justo el endpoint que creaste en el Paso 3.
- `.then(respuesta => respuesta.text())` — la respuesta llega como texto plano (el HTML del fragmento `tarjetas`), hay que "leerla" antes de usarla.
- `grid.innerHTML = html` — reemplaza todo lo que hay adentro del `div#cursosGrid` por las tarjetas nuevas que llegaron del servidor.
- `setTimeout(buscarCursos, 300)` + `clearTimeout(temporizador)` — esto es el "debounce": cada vez que escribes una letra, cancela la búsqueda anterior (si todavía no se había disparado) y programa una nueva para dentro de 300 milisegundos. Si sigues escribiendo rápido, nunca llega a completarse la búsqueda vieja — solo se ejecuta la última, cuando dejas de escribir un momentito. Sin esto, cada tecla generaría una petición al servidor, lo cual es innecesario y lento.

Ahora agrega el script al final de `catalogo.html`, dentro del bloque de JS (revisa cómo está estructurado, cerca de donde dice `layout:fragment="js"` si existe, o agrégalo si no existe — mira `inicio.html` como referencia de cómo se agrega un script propio de una página):

```html
<th:block layout:fragment="js">
  <script src="js/catalogo.js" defer></script>
</th:block>
```

## Cómo probar que funciona

1. Entra a `/catalogo`.
2. Escribe algo en el buscador (ej. "civil") y espera un momento sin tocar nada más — las tarjetas deberían cambiar solas (dinámica).
3. Borra el texto y presiona el botón "Buscar" — deberían volver a aparecer los 20 cursos (estática, con texto vacío).
4. Si no pasa nada, abre las herramientas de desarrollador del navegador (F12), pestaña "Console" o "Network", para ver si el `fetch` está fallando y por qué.

## Coordinación con Kelvin

No cambies la estructura interna de la tarjeta (`<article class="content-card curso">...`) que él armó — solo la envuelves con el `<th:block th:fragment="tarjetas">`. Si necesitas ajustar algo del diseño de la tarjeta, avísale primero.
