# Maykol — Login con Spring Security (rama `feature/login-spring-security`)

## Qué vas a lograr

El botón "Ingresar" de `/login` hoy solo redirige a `/admin` sin validar nada (era una maqueta). Tiene que autenticar de verdad contra la tabla `administrador`, y que nadie entre a `/admin` sin haber iniciado sesión.

## Archivos que vas a crear

- `AdministradorRepository.java` — para buscar un admin por correo.
- Un `UserDetailsService` propio — la clase que le explica a Spring Security cómo es "un usuario" en este proyecto.
- Un `AuthenticationSuccessHandler` — para actualizar `fecha_ultimo_login` cuando alguien entra con éxito.
- Una clase de configuración de seguridad (`SecurityConfig`).
- Ajustar `login.html` (el formulario).

## Cómo pensarlo

Esta parte usa piezas específicas de Spring Security que no se adivinan por lógica — hay que buscarlas en la documentación oficial (busca "Spring Security 6 form login" o revisa la guía oficial de Spring). Pero la idea general de cada pieza es esta:

**`AdministradorRepository`**: es igual de simple que `CursoRepository` — una interfaz con un método para buscar por correo. El login de este proyecto es por correo (revisa la entidad `Administrador`, no tiene campo `usuario` separado).

**El `UserDetailsService`**: Spring Security no sabe nada de tu tabla `administrador` — esta clase es la que le enseña. Tiene un solo método que recibe el correo escrito en el login, busca al admin con tu repositorio, y arma un objeto que Spring Security entiende (`UserDetails`). Ahí es donde decides: qué contraseña usar para comparar (la que ya está hasheada con BCrypt en la base de datos — nunca la compares tú a mano), si la cuenta está habilitada según el campo `estado`, y qué permiso darle (como en este proyecto todo el que está en esa tabla es administrador, el permiso puede ser fijo, no necesitas una columna de rol nueva).

**La `SecurityConfig`**: es donde defines qué rutas son públicas (el sitio entero: inicio, catálogo, nosotros, contacto, login, css/imágenes) y cuáles exigen sesión iniciada (`/admin/**` y todo lo que cuelgue de ahí). También configuras que la página de login sea la tuya (`login.html`, no la genérica de Spring Security), y qué pasa después de un login exitoso.

**El `AuthenticationSuccessHandler`**: se ejecuta justo después de que alguien inicia sesión correctamente. Ahí es donde buscas al admin de nuevo, le pones la fecha/hora actual en `fecha_ultimo_login`, lo guardas, y recién ahí rediriges a `/admin`.

**`login.html`**: el formulario necesita que el `method` sea `post` (no `get`, como está ahora de prueba), que la acción apunte a la ruta que procesa el login, y que los campos de correo/contraseña tengan un `name` que coincida con lo que configures en `SecurityConfig` (por defecto Spring Security espera `username`/`password`, pero se puede decirle que use otros nombres — investiga cómo).

**Sobre CSRF**: Spring Security por defecto exige un token anti-falsificación en los formularios POST. Conectar eso correctamente con Thymeleaf requiere una dependencia adicional. Para este proyecto, la alternativa más simple es desactivar esa protección (investiga cómo se hace en la configuración) — es una simplificación razonable para un trabajo de curso, no la harías así en un sistema real en producción. Coméntalo si te preguntan en la sustentación, no lo escondas.

**No necesitas tocar `LoginController.java`** — su único trabajo (mostrar la página) se queda igual. El envío del formulario lo intercepta Spring Security directamente, no pasa por tu controlador.

## Cómo probar

1. Entra a `/admin` sin haber iniciado sesión — debería mandarte solo a `/login`.
2. Inicia sesión con el correo y clave semilla (revisa `script/init.sql`).
3. Deberías llegar a `/admin`.
4. Busca una ruta de "salir"/logout — confirma que después de usarla, `/admin` te vuelve a pedir login.
5. Revisa en la base de datos que `fecha_ultimo_login` se haya actualizado.

## Nota para el equipo

Una vez que esto funcione, Joel y Juan **no necesitan proteger `/admin/cursos` por su cuenta** — la regla que pongas sobre `/admin/**` ya cubre cualquier ruta nueva que ellos agreguen debajo. Avísales cuando esté listo, porque además van a necesitar reemplazar un apaño temporal que van a dejar (usan "el primer admin que exista" en vez del admin realmente logueado, hasta que tu parte esté integrada).
