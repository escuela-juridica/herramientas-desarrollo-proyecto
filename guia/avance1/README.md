# Escuela Jurídica — estructura del proyecto

El proyecto separa contenido, presentación, comportamiento y recursos para que varias personas puedan trabajar por módulo con menos conflictos.

## Estructura

```text
avance1/
├── inicio.html
├── login.html
├── css/
│   ├── base.css      (compartido: variables, reset, utilidades, navbar, footer)
│   ├── inicio.css    (solo inicio.html: hero+nosotros, misión+cursos, clientes+video, blog)
│   └── login.css     (solo login.html)
├── js/
│   └── inicio.js     (cierre del menú móvil; login.js no existe todavía, ver Convenciones)
├── img/
│   ├── identidad/    (logos institucionales, favicon)
│   ├── portada/      (fondos del carrusel y del panel de login)
│   ├── institucional/ (foto de Nosotros, poster del video)
│   ├── cursos/       (portadas de los 6 cursos/diplomados/seminarios)
│   ├── clientes/     (logos de clientes)
│   ├── blog/         (portadas de artículos)
│   └── aliados/      (logos de instituciones aliadas: CAL, CAL Sur, AMAG)
└── video/
    └── video-institucional.webm
```

`base.css` contiene los tokens de color, tipografías, espaciado y componentes compartidos entre `inicio.html` y `login.html`. `inicio.css` y `login.css` contienen únicamente reglas de su propia página. Bootstrap 5.3.8 y Font Awesome 6.4.0 se cargan mediante CDN.

## Convenciones

- Usar nombres de clase en minúsculas y con guiones, por ejemplo `cliente-card`, `curso-cover`.
- Usar `--` para una variante del mismo componente, por ejemplo `surface-section--top`.
- Colocar una regla compartida por varias páginas en `base.css`; mantener en el CSS de página aquello que solo usa esa vista.
- No usar estilos inline ni bloques `<style>` o `<script>` dentro del HTML.
- Mantener el orden semántico `<header>`, `<main>` y `<footer>`. El `<nav>` pertenece al `<header>` e incluye el logo; el banner también permanece dentro del encabezado.
- No usar clases de grid, botones ni utilidades de Bootstrap. Bootstrap está reservado para los elementos `.carousel`.
- Guardar cada imagen en la subcarpeta funcional correspondiente de `img/` (ver tabla arriba) y los videos en `video/`, siempre con rutas locales relativas.
- La escuela ofrece **servicios** (cursos, diplomados, seminarios), no productos: por eso ninguna clase, carpeta ni archivo debe usar la palabra "producto"/"prod" — el prefijo correcto es `curso-*` y la carpeta de imágenes es `img/cursos/`.
- Un archivo JavaScript por página. `inicio.js` contiene únicamente el cierre del menú móvil al seleccionar un enlace. `login.html` no tiene JS propio por ahora (Avance 1: el formulario no envía datos a ningún backend); cuando se implemente la lógica de acceso, se crea `js/login.js` y se referencia desde `login.html`. Bootstrap controla los carruseles mediante atributos `data-bs-*`, sin JS propio.

## Cómo está dividido el trabajo (Gitflow)

Cada bloque de `inicio.html` está delimitado con comentarios `<!-- SECCIÓN: ... -->` que indican el responsable, qué hace la sección y un diagrama de cómo debe quedar estructurado su HTML. Lo mismo en `base.css`/`inicio.css`/`login.css`: cada archivo empieza con un índice y cada bloque dice su responsable. **Antes de escribir código, busca tu nombre en esos comentarios** — ahí está la referencia exacta de qué construir y dónde.

| Rama                        | Responsable      | Qué construye (en `inicio.html`, de arriba hacia abajo) |
|-----------------------------|------------------|----------------------------------------------------------|
| `feature/base-navbar-footer`| Enrique Prada    | Navbar, footer y `base.css` (estructura/base del proyecto) |
| `feature/hero-nosotros`     | Joel Saldaña     | Carrusel de portada + sección Nosotros |
| `feature/mision-programas`  | Paolo Añorga     | Misión y Visión + sección Servicios (los 6 cursos) |
| `feature/clientes-video`    | Juan Morales     | Carrusel de Clientes + Video representativo |
| `feature/blog`              | Kelvin Acevedo   | Sección Blog |
| `feature/login`             | Maykol Calle     | `login.html` completo (página aparte, con su propio `login.css`) |

Flujo de ramas:

- `main`: solo el estado listo para sustentación. Nadie hace commit directo aquí.
- `develop`: rama de integración. Todas las `feature/*` se mergean aquí vía Pull Request.
- Cada integrante trabaja en su propia `feature/<nombre>` a partir de `develop`, y solo edita el bloque marcado con su nombre en los comentarios (evita tocar código de otra sección para no generar conflictos).
- Antes de abrir un PR: hacer pull de `develop`, resolver conflictos localmente, y verificar visualmente `inicio.html`/`login.html` en escritorio y en móvil (ver siguiente sección).

## Antes de integrar cambios

- Limitar las ediciones al módulo asignado (el bloque marcado con tu nombre en los comentarios de `inicio.html`/`inicio.css`).
- Verificar `inicio.html` y `login.html` abriéndolos en el navegador, en escritorio y en una ventana angosta (menú móvil, carruseles, tarjetas).
- No renombrar clases, ids o rutas de imágenes de otro bloque sin avisar al responsable — otras secciones o el CSS compartido pueden depender de ellas.
