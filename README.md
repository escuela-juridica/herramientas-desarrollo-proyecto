# Escuela Jurídica — Proyecto (Herramientas de Desarrollo, UTP)

Sitio web de una institución real (Escuela Jurídica) con página de inicio y acceso administrativo. Este README es autosuficiente: no hace falta abrir nada más para saber qué hacer.

## Despliegue

Cada push a `main` **o** `develop` despliega el sitio automáticamente a GitHub Pages (workflow en `.github/workflows/deploy.yml`, sin build ni dependencias — solo sube los archivos tal cual). No se despliega desde ramas `feature/*`, así que la versión pública siempre refleja la última integración en `develop` o lo que ya llegó a `main`. Si ambas ramas reciben push casi al mismo tiempo, el sitio publicado queda con lo último que terminó de desplegar (no necesariamente en orden).

URL del sitio: `https://escuela-juridica.github.io/herramientas-desarrollo-proyecto/`

## Estructura de carpetas

```text
herramientas-desarrollo-proyecto/
├── inicio.html          página de inicio
├── login.html           acceso administrativo
├── css/
│   ├── base.css         compartido (navbar, footer, variables, utilidades) — Enrique, completo
│   ├── inicio.css       estilos de inicio.html, un bloque vacío por sección
│   └── login.css        estilos de login.html — Maykol, bloque vacío
├── js/
│   ├── inicio.js        cierre del menú móvil — Enrique, completo
│   └── cursos.js        genera las tarjetas de Servicios — Paolo, función vacía
├── img/
│   ├── identidad/       logos institucionales, favicon
│   ├── portada/         fondos del carrusel y del panel de login
│   ├── institucional/   foto de Nosotros, poster del video
│   ├── cursos/          portadas de los 6 cursos/diplomados/seminarios
│   ├── clientes/        logos de clientes
│   ├── blog/            portadas de artículos
│   └── aliados/         logos de instituciones aliadas (CAL, CAL Sur, AMAG)
├── video/
│   └── video-institucional.webm
└── guia/                material de referencia del curso (no es obligatorio abrirlo)
```

## Cómo está repartido el trabajo (Gitflow)

| Rama                        | Responsable      | Qué construye |
|-----------------------------|------------------|----------------|
| `feature/base-navbar-footer`| Enrique Prada    | Navbar + footer + `base.css` — **ya está hecho** |
| `feature/hero-nosotros`     | Joel Saldaña     | Banner/carrusel de portada + sección Nosotros |
| `feature/mision-cursos`     | Paolo Añorga     | Misión y Visión + sección Servicios (6 cursos) |
| `feature/clientes-video`    | Juan Morales     | Carrusel de Clientes + Video representativo |
| `feature/blog`              | Kelvin Acevedo   | Sección Blog (3 artículos) |
| `feature/login`             | Maykol Calle     | `login.html` completo |

### Flujo de ramas

1. `main`: solo el estado listo para sustentación. Nadie hace commit directo aquí.
2. `develop`: rama de integración. Todas las `feature/*` se mergean aquí vía Pull Request.
3. Cada integrante crea su rama `feature/<nombre>` a partir de `develop`, trabaja solo en su zona (ver abajo) y abre PR hacia `develop` cuando termine.
4. Antes de abrir el PR: hacer `pull` de `develop`, resolver conflictos si aparecen, y probar `inicio.html`/`login.html` en el navegador (escritorio y ventana angosta).
5. `develop` se mergea a `main` antes de cada sustentación.

## Cómo usar los placeholders

- En `inicio.html` y `login.html`, cada zona a llenar tiene un borde punteado de color, fondo tintado y el texto **"AQUÍ VA: [sección] — [nombre]"**, con un comentario arriba (`<!-- ZONA: ... -->`) que dice tu rama y qué debes pegar ahí.
- Reemplaza el bloque completo (el `<section style="border:3px dashed...">` de ejemplo incluido) por tu HTML real.
- Esos bordes de colores son solo estilos **inline** (no hay ninguna clase CSS de por medio) — al borrar el bloque no queda ningún rastro en ningún archivo CSS.
- Tu CSS va en el bloque con tu nombre dentro de `css/inicio.css` (o `css/login.css` si eres Maykol), donde dice "👉 Pega aquí...". Los media queries de tu sección ya están abiertos y vacíos justo debajo del bloque.
- Si tu sección necesita JS, revisa `js/` (por ahora solo Paolo tiene `cursos.js`, con una función vacía).
- **No toques** el navbar, el footer, ni la zona de otro compañero.

## Qué debe llevar cada sección

**Hero + Nosotros (Joel)**
- Banner/carrusel de portada con 3 a 5 slides (imagen de fondo + texto + botón), dentro del `<header>`, arriba del `<main>`.
- Sección "Nosotros": texto institucional (quiénes somos, especialización, respaldo) + una foto.

**Misión + Cursos (Paolo)**
- Sección "Misión y Visión": dos bloques de texto (Misión / Visión).
- Sección "Servicios" (`id="servicios"`): grilla de 6 cursos/diplomados/seminarios, cada uno con tipo (badge), duración, imagen, título, descripción y botón. El HTML final de cada tarjeta lo genera `js/cursos.js` (función `renderCursos`, hoy vacía) a partir de un array o clase — no hace falta escribir las 6 tarjetas a mano en el HTML.

**Clientes + Video (Juan)**
- Carrusel de logos de clientes (instituciones que confían en Escuela Jurídica: cortes, notarías, SUNARP, etc.), con flechas de navegación.
- Sección "Video representativo": un solo video institucional usando la etiqueta nativa `<video controls>` — **no** usar `<iframe>` de YouTube.

**Blog (Kelvin)**
- Grilla de 3 artículos: imagen de portada, fecha, título, extracto y enlace "Leer más".

**Login (Maykol)**
- Pantalla de acceso completa: en escritorio, panel con foto institucional a un lado y formulario de correo/contraseña al otro; en móvil, el panel de la foto se oculta y solo queda el formulario.
- El formulario no envía datos a ningún backend todavía (es solo interfaz).

## Convenciones de código

- Clases en minúsculas y con guiones (`cliente-card`, `curso-cover`). Usar `--` para una variante del mismo componente (`surface-section--top`).
- No usar estilos inline ni bloques `<style>`/`<script>` dentro del HTML — **excepto** los placeholders de esta plantilla, que se eliminan al pegar el contenido real.
- Mantener el orden semántico `<header>`, `<main>`, `<footer>`. El `<nav>` y el banner van dentro del `<header>`.
- Bootstrap (ya cargado por CDN) está reservado para los elementos `.carousel` — no usar sus clases de grid, botones ni utilidades para otra cosa.
- Cada imagen va en la subcarpeta funcional que le corresponde dentro de `img/` (ver tabla arriba), y los videos en `video/`, siempre con rutas relativas.
- La escuela ofrece **servicios** (cursos, diplomados, seminarios), no productos — ninguna clase, carpeta ni archivo debe decir "producto"/"prod".
- Reglas compartidas por varias páginas van en `base.css`; lo que solo usa una vista va en el CSS de esa página.

## Antes de abrir un Pull Request

- Confirmar que solo se editó el bloque/archivo asignado.
- Verificar `inicio.html`/`login.html` en el navegador, en escritorio y en una ventana angosta (menú móvil, carruseles, tarjetas).
- No dejar clases, ids o imágenes sin usar.
- No renombrar clases o ids de otra sección sin avisar a su responsable.

## Referencia opcional

En `guia/avance1/` hay una versión previa ya terminada del sitio, con comentarios más detallados de cómo funciona cada mecanismo (el truco del checkbox del menú móvil, los atributos de Bootstrap, `aspect-ratio`, etc.). No es obligatorio abrirla para trabajar, pero puede servir de ejemplo si algo no queda claro.
