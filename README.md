# Escuela Jurídica — Proyecto (Herramientas de Desarrollo, UTP)

Sitio web de una institución jurídica real: catálogo de cursos, página institucional y panel administrativo con base de datos. Avance 2 del curso — la versión anterior (solo HTML/CSS/JS estático) quedó atrás; esto ya es **Spring Boot + Thymeleaf + PostgreSQL**.

## Composición del proyecto

- **Spring Boot 3.3.5** + **Java 21** + **Maven**.
- **Thymeleaf** + **Thymeleaf Layout Dialect** — dos layouts compartidos: uno para el sitio público (`layout.html`) y otro para el panel administrativo (`admin/layout.html`).
- **Spring Data JPA + Hibernate** sobre **PostgreSQL**. El esquema de la base de datos **no lo crea Hibernate** (`ddl-auto=validate`) — lo crea el script `script/init.sql`, a mano.
- **Lombok** está en el `pom.xml` como dependencia, pero las entidades actuales usan getters/setters escritos a mano a propósito, para que cualquiera del equipo los entienda sin conocer Lombok.
- **Bootstrap 5.3.8** (CDN) — solo para los `.carousel` (portadas y clientes).
- **Font Awesome 6.4.0** (CDN) — íconos.
- **Sin build de frontend** — nada de npm/webpack, los estilos y JS son planos.

## Cómo correrlo

1. Tener una base de datos PostgreSQL accesible (local o el VPS del equipo).
2. Ejecutar **`script/init.sql`** completo contra esa base — crea las tablas y las siembra con datos (20 cursos, 11 docentes, 1 administrador). El script empieza con `DROP TABLE IF EXISTS`, así que se puede correr las veces que haga falta para resetear todo.
3. Completar `spring.datasource.*` en `src/main/resources/application-dev.properties` (o `application-local.properties` si usas tu propio Postgres local) con la URL/usuario/clave reales.
4. Levantar la app desde IntelliJ (`EscuelaJuridicaApplication`) o con `mvnw spring-boot:run`. Por defecto corre el perfil **`dev`** (`spring.profiles.active=dev` en `application.properties`); para usar el local: `-Dspring-boot.run.profiles=local`.
5. Abrir `http://localhost:8500` — redirige a `/inicio`.

**Login de prueba** (sembrado por `init.sql`): correo `admin@escuelajuridica.edu.pe`, clave `1234`. El login todavía **no valida contra la base de datos** (ver "Pendiente" abajo) — el botón solo redirige a `/admin`.

## Estructura de carpetas

```text
herramientas-desarrollo-proyecto/
├── pom.xml
├── script/
│   └── init.sql                  DROP + CREATE + INSERT, en un solo archivo
├── uploads/
│   └── cursos/                   imágenes de los cursos (semilla + las que suba el admin)
└── src/main/
    ├── java/pe/edu/escuela/app/
    │   ├── EscuelaJuridicaApplication.java
    │   ├── config/WebConfig.java        mapea /uploads/cursos/** a la carpeta externa
    │   ├── controller/                  InicioController, CatalogoController, NosotrosController,
    │   │                                 ContactoController, LoginController, AdminController,
    │   │                                 AdminCursoController
    │   ├── model/                       Administrador, TipoCurso, Docente, Curso (entidades JPA)
    │   ├── repository/                  CursoRepository (JpaRepository + queries derivadas)
    │   ├── service/                     CursoService (capa entre controller y repository)
    │   └── util/Constantes.java         ESTADO_ACTIVO / ESTADO_INACTIVO ('A'/'I')
    └── resources/
        ├── application.properties       común + perfil activo por defecto (dev)
        ├── application-dev.properties   conexión al Postgres remoto (VPS del equipo)
        ├── application-local.properties conexión a Postgres en tu propia máquina
        ├── templates/
        │   ├── layout.html               layout público (navbar + footer)
        │   ├── inicio.html, catalogo.html, nosotros.html, contacto.html   decoran layout.html
        │   ├── login.html                 standalone, no usa ningún layout
        │   └── admin/
        │       ├── layout.html            layout del panel (sidebar + topbar + hamburguesa en mobile)
        │       └── cursos.html             decora admin/layout.html
        └── static/
            ├── css/
            │   ├── base.css               SOLO lo global (reset, navbar, footer, banner compartido)
            │   ├── inicio.css, catalogo.css, nosotros.css, contacto.css, login.css
            │   └── admin/
            │       ├── admin.css           shell del panel (sidebar, topbar, nav, responsive)
            │       └── admin-cursos.css    específico de /admin/cursos
            ├── js/                         inicio.js, cursos.js (cursos.js ya no se usa, quedó suelto)
            └── img/                        identidad/, portada/, institucional/, clientes/, blog/, aliados/
```

## Rutas

| Ruta | Qué muestra |
|---|---|
| `/` | Redirige a `/inicio` |
| `/inicio` | Página de inicio (hero, nosotros, misión/visión, destacados desde la BD, clientes, video, blog) |
| `/catalogo` | Banner de 5 slides; el catálogo real de cursos todavía es un placeholder |
| `/nosotros`, `/contacto` | Placeholders ("página en construcción") |
| `/login` | Formulario de acceso (todavía no autentica, solo redirige a `/admin`) |
| `/admin` | Redirige a `/admin/cursos` |
| `/admin/cursos` | Panel del CRUD de cursos — hoy es un placeholder ("en construcción") |

## Base de datos

4 tablas, todas creadas por `script/init.sql`:

- **`administrador`** — login por `correo` (no hay campo `usuario` separado), `clave` ya hasheada con BCrypt, `estado` (`A`/`I`).
- **`tipo_curso`** — catálogo de referencia: Diplomado, Curso Corto, Seminario, Programa.
- **`docente`** — 11 docentes sembrados (4 con nombres reales del equipo/footer, el resto inventados para cubrir más áreas del derecho).
- **`curso`** — 20 cursos sembrados, con `codigo` (visible, tipo `EJ-2026-001`) distinto de `id_curso` (interno), `precio`, `modalidad` (`V`/`P`, hoy todos `V`), `destacado` (6 marcados `true`, se muestran en `/inicio`), `estado` (borrado lógico — el CRUD nunca hace `DELETE` real).

Relaciones: `administrador` (1) —registra→ (N) `curso`, `tipo_curso` (1) —clasifica→ (N) `curso`, `docente` (1) —dicta→ (N) `curso`.

Las imágenes de los 20 cursos semilla viven en `uploads/cursos/` (no en `static/`, porque esa carpeta se empaqueta dentro del `.jar` y no se puede escribir ahí en tiempo de ejecución) — la columna `curso.imagen` guarda la ruta completa (`/uploads/cursos/curso-01.jpg`), así que cuando el admin reemplace una foto no importa si viene de la semilla o de una subida nueva.

## Convenciones de código

- **Un CSS por página, `base.css` solo para lo global** (navbar, footer, banner de héroe compartido). Si una regla es específica de una sola vista, va en el CSS de esa vista, no en `base.css`.
- **`ddl-auto=validate`** — nunca cambiar a `create`/`update`. El esquema se modifica editando `script/init.sql`, no dejando que Hibernate lo intente generar.
- Los campos `CHAR(1)` de Postgres (`estado`, `modalidad`) se mapean en las entidades con `@JdbcTypeCode(SqlTypes.CHAR)` — sin eso, Hibernate espera `VARCHAR` y la app no arranca (`Schema-validation: wrong column type`).
- Controladores → `Service` → `Repository`. Los controladores no llaman al repositorio directo.
- Inyección de dependencias por **constructor con `private final`**, no `@Autowired` en campos.
- Estados usan las constantes de `util/Constantes.java` (`ESTADO_ACTIVO`/`ESTADO_INACTIVO`), nunca el string `"A"`/`"I"` suelto.
- Las páginas bajo `/admin/**` usan rutas de asset **absolutas** (`/css/...`, `/img/...`), no relativas — como esas URLs tienen más de un segmento (`/admin/cursos`), una ruta relativa como `css/admin.css` se resuelve mal.

## Estado actual (Avance 2)

**Hecho:**
- Esquema de base de datos completo + 20 cursos semilla.
- Entidades JPA + repositorio/servicio de `Curso`.
- `/inicio` ya lista los cursos destacados desde la base de datos (ya no usa `cursos.js`).
- Estructura y diseño del panel administrativo (`/admin/cursos`), con su propio layout reutilizable.
- Conexión a PostgreSQL configurada (perfiles `dev`/`local`), pool de conexiones ajustado.

**Pendiente:**
- Login real con Spring Security (autenticación contra `administrador`, proteger `/admin/**`).
- `/catalogo` (o donde termine viviendo el listado): traer los 20 cursos desde la base de datos + búsqueda estática y dinámica.
- CRUD completo en `/admin/cursos` (crear, editar, eliminar, buscar) — hoy solo muestra el placeholder.
- Subida de imágenes nuevas desde el CRUD (el `WebConfig`/`uploads/cursos` ya están listos para recibirlas).

## Nota sobre `CONTRIBUTING.md`

Ese archivo documenta el reparto de ramas del **Avance 1** (sitio estático, estructura de archivos vieja: `inicio.html`, `css/inicio.css` en la raíz). Con la migración a Spring Boot esa distribución quedó obsoleta — el flujo de Git en sí (`checkout` → `pull` → rama → commit → push → PR hacia `develop`) sigue siendo válido, pero la tabla de integrantes/ramas/archivos hay que rehacerla para esta fase.
