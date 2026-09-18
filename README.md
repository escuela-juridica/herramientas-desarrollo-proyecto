# Escuela Jurídica — Proyecto (Herramientas de Desarrollo, UTP)

Sitio web de una institución real (Escuela Jurídica): página de inicio y acceso administrativo. Este README es autosuficiente: no hace falta abrir nada más para saber qué hay y cómo está organizado.

## Composición del proyecto

- **Frontend puro**: HTML5 + CSS3 + JavaScript sin frameworks (no React, no Vue, nada que compilar).
- **Bootstrap 5.3.8** (CDN) — usado únicamente para los `.carousel` (portada y clientes).
- **Font Awesome 6.4.0** (CDN) — íconos.
- **Sin backend ni base de datos todavía** — el login es solo interfaz, no autentica a nadie de verdad.
- **Sin build ni dependencias que instalar** — se abre `inicio.html` directo en el navegador, o se sirve tal cual desde GitHub Pages.
- **Hosting**: GitHub Pages, desplegado por GitHub Actions (ver "Despliegue" abajo).

## Despliegue

Cada push a `main` **o** `develop` despliega el sitio automáticamente a GitHub Pages (workflow en `.github/workflows/deploy.yml`, sin build ni dependencias — solo sube los archivos tal cual). No se despliega desde ramas `feature/*`.

URL del sitio: `https://escuela-juridica.github.io/herramientas-desarrollo-proyecto/`

## Estructura de carpetas

```text
herramientas-desarrollo-proyecto/
├── index.html            redirige a inicio.html (lo exige GitHub Pages)
├── inicio.html            página de inicio
├── login.html             acceso administrativo
├── css/
│   ├── base.css           compartido: variables, navbar, footer
│   ├── inicio.css         estilos de inicio.html, por sección
│   └── login.css          estilos de login.html
├── js/
│   ├── inicio.js          cierre del menú móvil + enlaces href="#" inertes
│   └── cursos.js          genera las 6 tarjetas de Servicios (clase Curso)
├── img/
│   ├── identidad/         logos institucionales, favicon
│   ├── portada/           fondos del carrusel y del panel de login
│   ├── institucional/     foto de Nosotros, poster del video
│   ├── cursos/             portadas de los 6 cursos/diplomados/seminarios
│   ├── clientes/           logos de clientes
│   ├── blog/               portadas de artículos
│   └── aliados/             logos de instituciones aliadas (CAL, CAL Sur, AMAG)
├── video/
│   └── video-institucional.mp4
└── guia/                  material de referencia del curso
```

## Cómo está repartido el trabajo (Gitflow)

| Rama                          | Responsable      | Qué construyó |
|--------------------------------|------------------|----------------|
| `feature/base-navbar-footer`   | Enrique Prada    | Navbar + footer + `base.css` |
| `feature/hero-nosotros`        | Joel Saldaña     | Banner/carrusel de portada + sección Nosotros |
| `feature/mision-cursos`        | Paolo Añorga     | Misión y Visión + sección Servicios (6 cursos) |
| `feature/clientes-video`       | Juan Morales     | Carrusel de Clientes + Video representativo |
| `feature/blog`                 | Kelvin Acevedo   | Sección Blog (3 artículos) |
| `feature/login`                | Maykol Calle     | `login.html` completo |

Todas las secciones ya están integradas en `develop`. Para el flujo completo de ramas (clonar, crear rama, commit, push, Pull Request, resolver conflictos) ver **[CONTRIBUTING.md](CONTRIBUTING.md)**.

`main` y `develop` están protegidas (ruleset): no se puede hacer push directo, todo entra por Pull Request.

## Qué incluye el sitio

**Hero + Nosotros** — carrusel de portada (5 slides) + sección institucional (quiénes somos, especialización, respaldo).

**Misión + Cursos** — Misión y Visión + grilla de 6 cursos/diplomados/seminarios (tipo, duración, imagen, título, descripción, botón). Las tarjetas las genera `js/cursos.js` a partir de la clase `Curso`, no están escritas a mano en el HTML.

**Clientes + Video** — carrusel de logos de clientes (cortes, notarías, SUNARP, etc.) + video institucional con la etiqueta nativa `<video controls>` (no iframe de YouTube).

**Blog** — grilla de 3 artículos con imagen, fecha, título, extracto y enlace "Leer más".

**Login** — pantalla de acceso: en escritorio, panel con foto institucional + formulario de correo/contraseña; en móvil, el panel de la foto se oculta y solo queda el formulario. El formulario no envía datos a ningún backend todavía (es solo interfaz).

## Convenciones de código

- Clases en minúsculas y con guiones (`cliente-card`, `curso-cover`). Usar `--` para una variante del mismo componente (`surface-section--top`).
- No usar estilos inline ni bloques `<style>`/`<script>` dentro del HTML.
- Mantener el orden semántico `<header>`, `<main>`, `<footer>`. El `<nav>` y el banner van dentro del `<header>`.
- Bootstrap (cargado por CDN) está reservado para los elementos `.carousel` — no usar sus clases de grid, botones ni utilidades para otra cosa.
- Cada imagen va en la subcarpeta funcional que le corresponde dentro de `img/` (ver tabla arriba), y los videos en `video/`, siempre con rutas relativas.
- La escuela ofrece **servicios** (cursos, diplomados, seminarios), no productos — ninguna clase, carpeta ni archivo dice "producto"/"prod".
- Reglas compartidas por varias páginas van en `base.css`; lo que solo usa una vista va en el CSS de esa página.
- Los comentarios en HTML/CSS/JS son mínimos: solo marcan a qué sección corresponde cada bloque (`SECCIÓN: X`), sin explicar comportamiento.

## Antes de abrir un Pull Request

- Verificar `inicio.html`/`login.html` en el navegador, en escritorio y en una ventana angosta (menú móvil, carruseles, tarjetas).
- No dejar clases, ids o imágenes sin usar.
- No renombrar clases o ids de una sección que no es la propia sin avisar a su responsable.

## Referencia opcional

En `guia/avance1/` hay una versión previa del sitio con comentarios más detallados de cómo funciona cada mecanismo (el truco del checkbox del menú móvil, los atributos de Bootstrap, `aspect-ratio`, etc.).
