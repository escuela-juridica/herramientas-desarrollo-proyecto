# Escuela Jurídica — estructura del proyecto

El proyecto separa contenido, presentación, comportamiento y recursos para que varias personas puedan trabajar por módulo con menos conflictos.

## Estructura

```text
guia pagina/
├── inicio.html
├── login.html
├── css/
│   ├── base.css
│   ├── inicio.css
│   ├── login.css
├── js/
│   ├── inicio.js
│   ├── login.js
├── img/
└── video/
```

`base.css` contiene los tokens de color, tipografías, espaciado y componentes compartidos. `inicio.css` y `login.css` contienen únicamente reglas de su página. Bootstrap 5.3.8 y Font Awesome 6.4.0 se cargan mediante CDN.

Las imágenes se organizan por función dentro de `img/`:

- `identidad/`: logos institucionales.
- `portada/`: fondos del carrusel y del acceso administrativo.
- `institucional/`: fotografías de presentación y portadas de video.
- `productos/`: imágenes de programas y servicios.
- `clientes/`: logos de clientes.
- `blog/`: portadas de artículos.
- `aliados/`: logos de instituciones aliadas.

## Convenciones

- Usar nombres de clase en minúsculas y con guiones, por ejemplo `cliente-card`.
- Usar `--` para una variante del mismo componente, por ejemplo `surface-section--top`.
- Colocar una regla compartida por varias páginas en `base.css`; mantener en el CSS de página aquello que solo usa esa vista.
- No usar estilos inline ni bloques `<style>` o `<script>` dentro del HTML.
- Mantener el orden semántico `<header>`, `<main>` y `<footer>`. El `<nav>` pertenece al `<header>` e incluye el logo; el banner también permanece dentro del encabezado.
- No usar clases de grid, botones ni utilidades de Bootstrap. Bootstrap está reservado para los elementos `.carousel`.
- Guardar cada imagen en la subcarpeta funcional correspondiente de `img/` y los videos en `video/`, siempre con rutas locales relativas.
- Mantener un archivo JavaScript por página. `inicio.js` contiene únicamente el cierre del menú móvil al seleccionar un enlace; `login.js` permanece vacío. Bootstrap controla el carrusel mediante atributos `data-bs-*`.

## Reparto sugerido para seis integrantes

1. Sistema visual y componentes comunes en `base.css`.
2. Cabecera, navegación y portada de `inicio.html`.
3. Secciones Nosotros, Misión/Visión y Programas.
4. Clientes, video, blog y pie de página.
5. Pantalla `login.html` y su estilo exclusivo.
6. Recursos locales, accesibilidad y revisión responsive.

Antes de integrar cambios, cada integrante debe limitar sus ediciones al módulo asignado y verificar `inicio.html` y `login.html` en escritorio y móvil.
