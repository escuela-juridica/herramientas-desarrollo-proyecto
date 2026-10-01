# Paolo — Búsqueda estática y dinámica en Productos (rama `feature/busqueda-productos`)

## Qué vas a lograr

En `/catalogo`, la guía del Avance 2 pide dos formas de buscar: **estática** (el usuario escribe y presiona un botón para buscar) y **dinámica** (los resultados cambian solos mientras escribe, sin recargar la página ni apretar nada). Las dos tienen que consultar la base de datos de verdad, no filtrar algo que ya esté cargado en el navegador.

**Antes de empezar, necesitas que Kelvin ya haya terminado su parte** — él arma la grilla de tarjetas con los 20 cursos; tú construyes el buscador sobre esa grilla ya existente.

## Archivos que vas a tocar

- `CursoRepository.java` (un método de búsqueda nuevo)
- `CursoService.java` (la lógica de qué hacer según si hay texto o no)
- `CatalogoController.java` (un endpoint nuevo — **compartido con Kelvin, coordina con él antes de editarlo**)
- `catalogo.html` (el input de búsqueda + convertir la grilla en un fragmento reusable)
- `catalogo.js` (nuevo)

## La idea central, antes de tocar código

Imagina que decides resolver esto armando las tarjetas de los resultados con JavaScript puro: el `fetch` trae los datos en formato JSON, y tu script arma a mano el HTML de cada tarjeta (`document.createElement`, o un template string con `innerHTML`). El problema de ese camino es que terminarías con **la misma tarjeta definida dos veces**: una vez en Thymeleaf (lo que hizo Kelvin, para la carga inicial de la página) y otra vez en JavaScript (para los resultados de búsqueda). El día que alguien cambie el diseño de la tarjeta en un lado y se olvide del otro, vas a tener dos versiones distintas de la misma tarjeta en la misma página, y nadie se va a dar cuenta hasta que alguien busque algo y vea que el resultado se ve distinto al resto.

La forma correcta de evitar ese problema es que **el servidor siga siendo el único que sabe cómo se ve una tarjeta**. Tu JavaScript nunca arma HTML por su cuenta — solo le pide al servidor "dame el HTML de las tarjetas que coincidan con esta búsqueda, ya armado", y lo pega directamente en la página. Esto se logra con un mecanismo de Thymeleaf llamado **fragmentos**: puedes marcar un pedazo de una plantilla con un nombre, y un controlador puede devolver *solo ese pedazo* en vez de la página completa.

## Paso 1: el repositorio — buscar por texto

Necesitas un método que encuentre cursos cuyo nombre contenga un texto dado, sin importar mayúsculas o minúsculas, y que además respete que estén activos. En el lenguaje de las queries derivadas de Spring Data, esto se arma combinando palabras clave: una que signifique "que el campo contenga este texto" junto con una que signifique "sin distinguir mayúsculas", encadenada con el filtro de estado que ya se usa en el resto del repositorio. Es el mismo mecanismo que ya conoces de los otros métodos — solo cambia qué palabras clave usas para describir la condición.

## Paso 2: el servicio — decidir qué pasa si no hay texto

Piensa en el caso borde: ¿qué debería pasar si alguien borra todo lo que escribió en el buscador? No tiene sentido "buscar un texto vacío" contra la base de datos — lo razonable es, en ese caso, mostrar todos los cursos otra vez, exactamente igual que al cargar la página por primera vez. Por eso, tu método del servicio necesita primero revisar si el texto que le llega es nulo o está vacío, y si es así, delegar al método que ya armó Kelvin para traer todos los activos. Solo si hay texto de verdad, usar el método de búsqueda que creaste en el Paso 1.

## Paso 3: el controlador — un endpoint que no devuelve la página completa

Agrega una ruta nueva en `CatalogoController` (algo como `/catalogo/buscar`) que reciba el texto escrito como parámetro de la URL y llame a tu método del servicio. Hasta acá es parecido a lo que ya conoces de otros controladores. La diferencia importante está en qué devuelve: en vez de devolver el nombre de una vista completa (como `"catalogo"`, que renderiza toda la página), vas a devolver una referencia a **un fragmento específico dentro de esa vista**, usando la notación `nombreDeVista :: nombreDelFragmento`. Esa sintaxis con `::` le dice a Thymeleaf "renderiza solo esta parte marcada, no la página entera". El nombre del fragmento lo defines tú en el HTML en el siguiente paso — tiene que coincidir exactamente entre los dos lados.

También conviene pensar qué pasa si el parámetro de texto no llega (por ejemplo, si alguien visita esa ruta sin parámetros): usa la opción que marca un parámetro como no obligatorio, para que en ese caso simplemente llegue como vacío/nulo en vez de que la aplicación falle.

## Paso 4: la plantilla — separar lo que cambia de lo que se queda fijo

Busca el contenedor que Kelvin dejó envolviendo todas las tarjetas (el `div` con el `id` del que te avisó). Adentro de ese `div` está el `th:each` que recorre los cursos. Lo que tienes que hacer es envolver **solo ese `th:each` y lo que genera** (no el `div` que lo contiene) dentro de un bloque marcado como fragmento. Para envolver algo sin agregar una etiqueta HTML extra que no estaba planeada en el diseño, Thymeleaf tiene una etiqueta especial que no genera ningún HTML visible por sí misma, solo sirve para agrupar — es la herramienta ideal para marcar el fragmento sin ensuciar la estructura.

¿Por qué el `div` exterior se queda afuera del fragmento, y no envuelves todo junto? Porque ese `div` tiene que seguir existiendo siempre en la página, sin importar cuántas veces se busque algo — es tu "contenedor fijo". Tu JavaScript, en el siguiente paso, solo va a reemplazar lo que hay *adentro* de ese contenedor. Si el `div` mismo formara parte de lo que el servidor te devuelve y lo reemplazaras completo cada vez, tendrías que volver a buscar la referencia a ese elemento en el DOM después de cada búsqueda (porque el elemento viejo ya no existiría), lo cual complica el JavaScript sin necesidad.

Además de esto, agrega el campo de texto y el botón de búsqueda en el HTML, en algún lugar visible antes de la grilla de resultados.

## Paso 5: el JavaScript — pedir, esperar y reemplazar

Tu script necesita, como mínimo, tres piezas: una referencia al campo de texto, una referencia al contenedor fijo de las tarjetas, y una función que haga la búsqueda. Esa función usa `fetch` para pedirle al servidor el resultado de la ruta que creaste en el Paso 3, pasándole el texto actual como parámetro de la URL. Como la respuesta es HTML (no JSON), tienes que leerla como texto plano, y una vez que la tengas, reemplazar el contenido del contenedor fijo con ese texto — eso hace que las tarjetas nuevas aparezcan en pantalla sin recargar nada.

Para la búsqueda **dinámica**, esa función se dispara cada vez que el usuario escribe algo en el campo de texto. Pero si disparas una búsqueda por cada tecla presionada, vas a mandar muchísimas peticiones innecesarias al servidor mientras la persona todavía está escribiendo. La técnica para resolver esto se llama **debounce**: cada vez que se presiona una tecla, cancelas cualquier búsqueda que hubieras programado antes, y programas una nueva para dentro de un ratito (algo así como 300 milisegundos). Si la persona sigue escribiendo rápido, la búsqueda programada se cancela una y otra vez, y solo termina ejecutándose la última, cuando por fin hay una pausa real en la escritura. Investiga `setTimeout` y `clearTimeout` de JavaScript — son las dos funciones que necesitas para armar este mecanismo.

Para la búsqueda **estática**, es literalmente la misma función de búsqueda, pero conectada al evento de clic del botón en vez de al evento de escritura del campo de texto — no necesitas duplicar la lógica del lado del servidor para esto, solo cambiar qué evento del navegador la dispara.

## Coordinación con Kelvin

Necesitas que la estructura interna de la tarjeta (lo que hay dentro de cada `article` del `th:each`) ya esté definida por él antes de envolverla en el fragmento — no cambies el diseño de la tarjeta en sí, tu trabajo es agregar el fragmento alrededor de lo que él ya construyó, y agregar el buscador. Si necesitas que la tarjeta muestre o calcule algo distinto para que la búsqueda tenga sentido, converza con él antes de modificarla directamente.

## Cómo probar que funciona

Escribe algo que sepas que existe en el nombre de un curso (por ejemplo, una palabra como "civil" o "registral") y espera un momento sin tocar nada más — las tarjetas deberían filtrarse solas, mostrando menos cursos que los 20 originales. Borra el texto y usa el botón de buscar — deberían volver a aparecer los 20. Si nada cambia, abre las herramientas de desarrollador del navegador (tecla F12), ve a la pestaña de red ("Network"), y revisa si la petición a tu endpoint se está disparando y qué está devolviendo.
