# Guías de implementación — Avance 2

Esta carpeta tiene un archivo por cada integrante con la parte de código que le toca. **No son tutoriales paso a paso ni código listo para copiar** — son recomendaciones de qué tecnología usar, en qué archivos trabajar y cómo abordarlo, para que cada quien lo programe, lo entienda y lo pueda explicar si el profesor pregunta.

## Archivos

- `kelvin-listado-cursos.md`
- `paolo-busqueda-productos.md`
- `maykol-login-spring-security.md`
- `joel-admin-crear-cursos.md`
- `juan-admin-editar-eliminar-cursos.md`

## Reglas generales para todos

1. **El diseño lo decide cada quien, pero tiene que sentirse parte del mismo sitio.** Reutilicen los tokens de `base.css` (`var(--navy)`, `var(--orange)`, `var(--serif)`, etc.), las clases de tarjeta/botón que ya existen, y el patrón de "un CSS por página" (si tu parte necesita estilos propios, van en el CSS de esa página, no metidos en `base.css`).
2. **No cambien `script/init.sql`** sin avisar al resto — todos dependen de esas mismas tablas.
3. **Dos parejas comparten archivos**, así que coordinen antes de tocar:
   - Kelvin y Paolo: ambos trabajan sobre `catalogo.html` / `CatalogoController.java`.
   - Joel y Juan: ambos trabajan sobre `admin/cursos.html` / `AdminCursoController.java`.
4. Revisen el `README.md` del proyecto (sección "Convenciones de código") antes de empezar — ahí está por qué `ddl-auto=validate`, por qué las entidades usan `@JdbcTypeCode`, y el patrón Controller → Service → Repository que ya siguen `InicioController`/`CursoService`/`CursoRepository` (cópienles la forma, no necesariamente el contenido).
