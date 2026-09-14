# Plantilla del proyecto — Escuela Jurídica

Esta carpeta es la base para que cada integrante pegue su parte. El navbar y el footer (Enrique) ya están completos y funcionando. El resto son zonas marcadas con borde punteado de color y el texto "AQUÍ VA: ... — [nombre]".

## Cómo usarla

1. Busca tu nombre en `inicio.html` (o `login.html` si eres Maykol) — cada zona tiene un comentario `<!-- ZONA: ... -->` con tu nombre y tu rama.
2. Reemplaza ese bloque completo (incluyendo el `<section style="border:3px dashed...">` de ejemplo) por tu HTML real.
3. Pega tu CSS en el bloque con tu nombre en `css/inicio.css` (o `css/login.css`), donde dice "👉 Pega aquí...".
4. Si tu sección necesita JS, revisa si ya existe un archivo para ti en `js/` (por ahora solo Paolo tiene `cursos.js`, con una función vacía lista para implementar).
5. No toques el navbar, el footer, ni el bloque de otro compañero.

## Estructura

```text
plantilla/
├── inicio.html
├── login.html
├── css/
│   ├── base.css      (navbar + footer — completo, de Enrique)
│   ├── inicio.css    (un bloque vacío por sección, con tu nombre)
│   └── login.css     (bloque de Maykol)
├── js/
│   ├── inicio.js     (menú móvil — completo, de Enrique)
│   └── cursos.js     (función vacía — de Paolo)
├── img/               (recursos ya descargados, organizados por carpeta)
└── video/
```

Para el detalle de cómo funciona cada cosa (el truco del checkbox del menú, los atributos de Bootstrap, aspect-ratio, etc.) revisa los comentarios del Avance 1 — aquí no se repiten a propósito.
