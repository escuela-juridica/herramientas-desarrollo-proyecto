# Escuela Jurídica — Proyecto (Herramientas de Desarrollo, UTP)

Sitio web de una institución jurídica real: catálogo de cursos, página institucional y panel administrativo con base de datos. **Spring Boot + Thymeleaf + PostgreSQL** — la versión anterior (solo HTML/CSS/JS estático, Avance 1) quedó atrás.

## Composición del proyecto

- **Spring Boot 3.3.5** + **Java 21** + **Maven**.
- **Thymeleaf** + **Thymeleaf Layout Dialect** — dos layouts compartidos: uno para el sitio público (`layout.html`) y otro para el panel administrativo (`admin/layout.html`).
- **Spring Security 6** — login contra la tabla `administrador` (BCrypt), protege todo `/admin/**`, logout real.
- **Spring Data JPA + Hibernate** sobre **PostgreSQL**. El esquema de la base de datos **no lo crea Hibernate** (`ddl-auto=validate`) — lo crea el script `script/init.sql`, a mano.
- **Lombok** está en el `pom.xml` como dependencia, pero las entidades actuales usan getters/setters escritos a mano a propósito, para que cualquiera del equipo los entienda sin conocer Lombok.
- **Bootstrap 5.3.8** (CDN) — `.carousel` (portadas y clientes) y los modales de detalle de curso.
- **Font Awesome 6.4.0** (CDN) — íconos.
- **Sin build de frontend** — nada de npm/webpack, los estilos y JS son planos, un archivo por página.

## Cómo correrlo

1. Tener una base de datos PostgreSQL accesible (local o el VPS del equipo).
2. Ejecutar **`script/init.sql`** completo contra esa base — crea las tablas y las siembra con datos (20 cursos, 11 docentes, 1 administrador). El script empieza con `DROP TABLE IF EXISTS`, así que se puede correr las veces que haga falta para resetear todo.
3. Completar `spring.datasource.*` en `src/main/resources/application-dev.properties` (o `application-local.properties` si usas tu propio Postgres local) con la URL/usuario/clave reales.
4. Levantar la app desde IntelliJ (`EscuelaJuridicaApplication`) o con `mvnw spring-boot:run`. Por defecto corre el perfil activo configurado en `application.properties` (`spring.profiles.active=...`); para forzar uno: `-Dspring-boot.run.profiles=local`.
5. Abrir `http://localhost:8500` — redirige a `/inicio`.

**Login de administrador** (sembrado por `init.sql`): correo `admin@escuelajuridica.edu.pe`, clave `1234`. El login valida de verdad contra la base de datos (Spring Security + BCrypt); credenciales incorrectas o vacías muestran un aviso en `/login`.

## Estructura de carpetas

```text
herramientas-desarrollo-proyecto/
├── pom.xml
├── script/
│   └── init.sql                        DROP + CREATE + INSERT, en un solo archivo
├── uploads/
│   └── cursos/                         imágenes de los cursos (semilla + las que suba el admin)
└── src/main/
    ├── java/pe/edu/escuela/app/
    │   ├── EscuelaJuridicaApplication.java
    │   ├── config/
    │   │   ├── WebConfig.java              mapea /uploads/cursos/** a la carpeta externa
    │   │   ├── SecurityConfig.java         SecurityFilterChain, BCryptPasswordEncoder
    │   │   └── AdminContextoAdvice.java    @ControllerAdvice: expone el admin logueado (adminActual) a toda vista
    │   ├── security/
    │   │   └── AdminAuthenticationSuccessHandler.java   actualiza fecha_ultimo_login, redirige a /admin
    │   ├── controller/
    │   │   ├── InicioController, CatalogoController, NosotrosController, ContactoController
    │   │   ├── LoginController
    │   │   ├── AdminController                redirige /admin → /admin/cursos
    │   │   └── AdminCursoController            CRUD completo de cursos (crear/editar/eliminar/reactivar/buscar)
    │   ├── model/                          Administrador, TipoCurso, Docente, Curso (entidades JPA)
    │   ├── repository/                     CursoRepository, AdministradorRepository, TipoCursoRepository, DocenteRepository
    │   ├── service/
    │   │   ├── CursoService                capa entre controller y repository
    │   │   └── AdministradorUserDetailsService   UserDetailsService para Spring Security
    │   └── util/Constantes.java            ESTADO_ACTIVO / ESTADO_INACTIVO ('A'/'I')
    └── resources/
        ├── application.properties          común + perfil activo
        ├── application-dev.properties      conexión al Postgres remoto (VPS del equipo)
        ├── application-local.properties    conexión a Postgres en tu propia máquina
        ├── templates/
        │   ├── layout.html                 layout público (navbar con login/avatar dinámico + footer)
        │   ├── inicio.html                 hero, nosotros, misión/visión, destacados (BD) + su modal
        │   ├── catalogo.html                catálogo completo: búsqueda estática + dinámica + modal de detalle
        │   ├── nosotros.html, contacto.html
        │   ├── login.html                  standalone, no usa ningún layout
        │   └── admin/
        │       ├── layout.html             sidebar + topbar + logout real + identidad del admin logueado
        │       ├── cursos.html             listado + búsqueda estática/dinámica (AJAX) + acciones
        │       └── curso-form.html         formulario compartido crear/editar
        └── static/
            ├── css/
            │   ├── base.css                 SOLO lo global (reset, navbar, footer, banner compartido)
            │   ├── inicio.css, catalogo.css, nosotros.css, contacto.css, login.css
            │   └── admin/
            │       ├── admin.css             shell del panel (sidebar, topbar, nav, responsive)
            │       └── admin-cursos.css      tabla, formulario y buscadores de /admin/cursos
            ├── js/
            │   ├── inicio.js, catalogo.js   catalogo.js maneja ambas búsquedas del catálogo público
            │   ├── cursos.js                sin uso, no referenciado por ningún template
            │   └── admin/
            │       ├── admin-alerts.js       auto-cierre de los avisos de éxito/error del panel
            │       ├── admin-cursos.js       búsqueda estática/dinámica (AJAX) de la tabla de cursos
            │       └── curso-imagen-preview.js  vista previa de la imagen al crear/editar
            └── img/                          identidad/, portada/, institucional/, clientes/, blog/, aliados/
```

## Rutas

| Ruta | Método | Qué hace |
|---|---|---|
| `/` | GET | Redirige a `/inicio` |
| `/inicio` | GET | Página de inicio: hero, nosotros, misión/visión, cursos destacados desde la BD (con modal de detalle) |
| `/catalogo` | GET | Catálogo completo de cursos activos, con buscadores estático y dinámico |
| `/catalogo/buscar` | GET | AJAX — devuelve el fragmento de tarjetas filtradas por nombre, código y/o tipo |
| `/nosotros`, `/contacto` | GET | Páginas institucionales |
| `/login` | GET | Formulario de acceso (muestra aviso si las credenciales fallan) |
| `/login` | POST | Procesado por Spring Security (no tiene controlador propio) |
| `/logout` | POST | Cierra sesión (Spring Security) |
| `/admin` | GET | Redirige a `/admin/cursos` (requiere sesión de administrador) |
| `/admin/cursos` | GET | Listado de cursos con búsqueda estática (nombre) y dinámica (código) |
| `/admin/cursos/buscar` | GET | AJAX — devuelve el fragmento de filas filtradas |
| `/admin/cursos/nuevo` | GET | Formulario de creación |
| `/admin/cursos/crear` | POST | Crea el curso (valida datos + sube la imagen) |
| `/admin/cursos/editar/{idCurso}` | GET | Formulario de edición, precargado |
| `/admin/cursos/editar/{idCurso}` | POST | Guarda los cambios (imagen opcional) |
| `/admin/cursos/eliminar/{idCurso}` | POST | Desactiva el curso (borrado lógico, `estado = 'I'`) |
| `/admin/cursos/reactivar/{idCurso}` | POST | Reactiva un curso desactivado |

Todo lo bajo `/admin/**` exige rol ADMIN (Spring Security); sin sesión, redirige a `/login`.

## Base de datos

4 tablas, todas creadas por `script/init.sql`:

- **`administrador`** — login por `correo` (no hay campo `usuario` separado), `clave` hasheada con BCrypt, `estado` (`A`/`I`), `fecha_ultimo_login` se actualiza en cada login exitoso.
- **`tipo_curso`** — catálogo de referencia: Diplomado, Curso Corto, Seminario, Programa.
- **`docente`** — 11 docentes sembrados (4 con nombres reales del equipo/footer, el resto inventados para cubrir más áreas del derecho).
- **`curso`** — 20 cursos sembrados, con `codigo` (visible, `EJ-2026-001`..`020`, intercalando los 4 tipos en vez de agruparlos) distinto de `id_curso` (interno), `precio`, `modalidad` (`V`/`P`), `destacado` (6 marcados `true`, se muestran en `/inicio`), `estado` (borrado lógico — el CRUD nunca hace `DELETE` real, solo desactiva/reactiva).

Relaciones: `administrador` (1) —registra→ (N) `curso`, `tipo_curso` (1) —clasifica→ (N) `curso`, `docente` (1) —dicta→ (N) `curso`.

Las imágenes de los 20 cursos semilla viven en `uploads/cursos/` (no en `static/`, porque esa carpeta se empaqueta dentro del `.jar` y no se puede escribir ahí en tiempo de ejecución) — la columna `curso.imagen` guarda la ruta completa (`/uploads/cursos/curso-01.jpg`), así que cuando el admin reemplace una foto no importa si viene de la semilla o de una subida nueva.

## Convenciones de código

- **Un CSS por página, `base.css` solo para lo global** (navbar, footer, banner de héroe compartido). Si una regla es específica de una sola vista, va en el CSS de esa vista, no en `base.css`. Mismo criterio para JS: un archivo por página/función, nada de un bundle único.
- **`ddl-auto=validate`** — nunca cambiar a `create`/`update`. El esquema se modifica editando `script/init.sql`, no dejando que Hibernate lo intente generar.
- Los campos `CHAR(1)` de Postgres (`estado`, `modalidad`) se mapean en las entidades con `@JdbcTypeCode(SqlTypes.CHAR)` — sin eso, Hibernate espera `VARCHAR` y la app no arranca (`Schema-validation: wrong column type`).
- Controladores → `Service` → `Repository`. Los controladores no llaman al repositorio directo.
- Inyección de dependencias por **constructor con `private final`**, no `@Autowired` en campos.
- Estados usan las constantes de `util/Constantes.java` (`ESTADO_ACTIVO`/`ESTADO_INACTIVO`), nunca el string `"A"`/`"I"` suelto.
- Las páginas bajo `/admin/**` usan rutas de asset **absolutas** (`/css/...`, `/img/...`), no relativas — como esas URLs tienen más de un segmento (`/admin/cursos`), una ruta relativa como `css/admin.css` se resuelve mal.
- Las búsquedas AJAX (catálogo público y tabla del admin) devuelven un **fragmento Thymeleaf** (`vista :: fragmento`), nunca JSON — el JS solo reemplaza `innerHTML`, no arma HTML a mano.

## Estado actual

**Avance 2 completo.** Las 5 historias quedaron implementadas y en `develop`:

- **Login con Spring Security** — autenticación real contra `administrador`, `/admin/**` protegido, logout funcional, feedback de error en `/login`.
- **Listado de cursos** (`/catalogo`, `/inicio`) — cursos reales desde la BD, con modal de detalle (código, duración, modalidad, precio).
- **Búsqueda estática y dinámica** — tanto en el catálogo público (nombre + tipo + código) como en la tabla del admin (nombre + código), vía AJAX con fragmentos Thymeleaf.
- **Crear curso** (`/admin/cursos/nuevo`) — formulario validado, con subida de imagen.
- **Editar / eliminar / reactivar curso** — CRUD completo en `/admin/cursos`, con borrado lógico.

El repositorio usa **GitHub Rulesets** en `main`/`develop` (PR obligatorio + al menos 1 aprobación de otra persona) para evitar que alguien mergee su propio trabajo sin revisión.
