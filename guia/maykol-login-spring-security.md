# Maykol — Login con Spring Security (rama `feature/login-spring-security`)

## Qué vas a lograr

El botón "Ingresar" de `/login` hoy solo redirige a `/admin` sin revisar absolutamente nada — fue una maqueta a propósito, mientras el resto del proyecto se armaba. Tu tarea es reemplazar eso por una autenticación real: que valide el correo y la clave contra la tabla `administrador`, que nadie pueda entrar a ninguna ruta debajo de `/admin` sin haber iniciado sesión, y que después de un login exitoso solo se pueda llegar al panel administrativo (no a cualquier otra parte).

Esta es, de las cinco partes, la que usa más piezas de un framework externo (Spring Security) en vez de patrones que ya puedas copiar de otro lugar del proyecto. Vas a tener que leer documentación oficial de Spring Security además de esta guía — acá te explico qué rol cumple cada pieza, para que sepas qué buscar.

## Archivos que vas a crear

- `AdministradorRepository.java`
- Una clase que implemente `UserDetailsService`
- Una clase que implemente `AuthenticationSuccessHandler`
- Una clase de configuración de seguridad (convencionalmente se llama `SecurityConfig`)
- Vas a modificar `login.html`

## Paso 1: el repositorio del administrador

Hasta ahora, el proyecto solo tiene `CursoRepository`. Necesitas uno equivalente para `Administrador`, con un método para buscarlo por su correo (revisa la entidad `Administrador` — el login en este proyecto es por correo, no existe un campo `usuario` separado, eso se decidió explícitamente al diseñar la base de datos). Es exactamente el mismo tipo de interfaz simple que ya conoces, extendiendo `JpaRepository`.

## Paso 2: enseñarle a Spring Security cómo es "un usuario" en este proyecto

Spring Security no tiene ni idea de que existe una tabla `administrador` — para él, un "usuario" es un concepto genérico con un nombre, una contraseña, unos permisos, y si la cuenta está habilitada o no. La pieza que traduce entre "lo que hay en tu base de datos" y "lo que Spring Security entiende" es una clase que implementa la interfaz `UserDetailsService`, que tiene un único método: recibe el texto que la persona escribió como identificador (en tu caso, el correo), y tiene que devolver un objeto `UserDetails` armado a partir de esos datos.

Dentro de ese método vas a hacer tres cosas: buscar al administrador con tu repositorio del Paso 1 (y si no existe ninguno con ese correo, lanzar la excepción que Spring Security espera para esos casos, `UsernameNotFoundException`); decidir qué contraseña usar para la comparación (la que ya está guardada en la base de datos, hasheada con BCrypt — nunca compares contraseñas en texto plano a mano, eso lo hace Spring Security por ti si le das la contraseña hasheada correcta); y decidir qué permiso darle. Sobre esto último: como en este proyecto **todo el que existe en la tabla `administrador` es administrador** (no hay una tabla de roles ni una columna que distinga tipos de usuario), el permiso que le asignes puede ser el mismo valor fijo para cualquier fila — no necesitas leer ningún campo de rol de la base de datos porque no existe, y está bien que no exista: agregar esa columna ahora sería complejidad que el proyecto no necesita todavía.

También vale la pena usar el campo `estado` del administrador acá: si está marcado como inactivo, puedes decirle a Spring Security que esa cuenta está deshabilitada, y el login va a fallar automáticamente sin que tengas que escribir ninguna lógica extra para eso.

## Paso 3: qué hacer justo después de un login exitoso

El requerimiento dice que el login debe actualizar cuándo fue el último acceso del administrador. Para ejecutar código tuyo justo en el momento en que alguien se autentica correctamente (ni antes, ni en cada petición normal), Spring Security tiene el concepto de `AuthenticationSuccessHandler` — una clase que implementas y que Spring llama automáticamente apenas valida las credenciales.

Dentro de ese método vas a tener acceso a quién acaba de iniciar sesión (el framework te da esa información como parte del objeto `Authentication` que recibe el método). Con eso, buscas al administrador de nuevo con tu repositorio, le actualizas el campo de fecha de último acceso con la fecha y hora actuales, lo guardas, y recién al final rediriges manualmente hacia `/admin` — es tu responsabilidad hacer esa redirección dentro de este método, porque al usar un manejador personalizado, reemplazas el comportamiento por defecto de a dónde ir después de un login exitoso.

## Paso 4: la configuración central de seguridad

Esta es la pieza que junta todo. En una clase de configuración, vas a definir un `SecurityFilterChain` — el objeto donde se describe, para toda la aplicación, qué rutas son públicas y cuáles exigen sesión iniciada. El patrón general que vas a usar es: todo lo que empiece con `/admin/` exige el permiso de administrador que definiste en el Paso 2; todo lo demás (el sitio público completo: inicio, catálogo, nosotros, contacto, el propio login, y los archivos estáticos como CSS/imágenes) queda accesible sin iniciar sesión.

En esa misma configuración le dices a Spring Security que use tu página de login (`login.html`) en vez de la página genérica que trae por defecto, que el formulario use los nombres de campo que decidas (por defecto espera `username`/`password`, pero puedes configurar que acepte otros nombres — vas a necesitar que coincidan con los `name` de los inputs de tu formulario), y que use el `AuthenticationSuccessHandler` que armaste en el Paso 3 en vez del comportamiento por defecto.

También necesitas registrar, en algún lugar de la configuración (puede ser la misma clase), cuál es el algoritmo que se usa para verificar contraseñas — tiene que ser el mismo que se usó para generar el hash que está guardado en la base de datos (BCrypt, revisa cómo se generó la clave semilla en `script/init.sql` si quieres confirmar el formato).

**Sobre CSRF**: por defecto, Spring Security exige que cualquier formulario que envíe datos con POST incluya un token especial anti-falsificación. Conectar esto correctamente con un formulario de Thymeleaf normal requiere una dependencia adicional de integración entre ambos frameworks. Para mantener esta parte simple dado el alcance del proyecto, puedes desactivar esa protección específicamente en la configuración — es una simplificación aceptable para un trabajo de curso (no es lo que harías en un sistema que vaya a producción real), y conviene que lo tengas claro para poder explicarlo si te preguntan en la sustentación, en vez de que parezca un descuido.

## Paso 5: ajustar el formulario de login

El formulario que existe hoy manda los datos con `method="get"` hacia `/admin` directamente, como la maqueta que era. Ahora necesita enviar los datos con `method="post"`, hacia la ruta que procesa el login (la misma URL que usaste como página de login, Spring Security intercepta automáticamente las peticiones POST hacia ahí, sin que tengas que escribir un controlador para procesarlas). Los campos de correo y contraseña necesitan un atributo `name` que coincida exactamente con lo que configuraste en el Paso 4.

Importante: **no toques `LoginController.java`**. Su único trabajo es mostrar la página cuando alguien visita `/login` con una petición GET, y eso se queda exactamente igual. El envío del formulario (la petición POST) nunca llega a ese controlador — lo intercepta un filtro de Spring Security antes de que el framework de controladores normales se entere.

## Cómo probar que funciona

Primero, sin haber iniciado sesión, intenta entrar directo a `/admin` escribiendo la URL — deberías terminar redirigido a `/login`, lo cual confirma que la protección está funcionando. Después, inicia sesión con el correo y la clave que están sembrados en `script/init.sql` — deberías llegar al panel. Busca cómo cerrar la sesión (Spring Security trae una ruta de logout por defecto) y confirma que, después de cerrarla, `/admin` te vuelve a pedir login. Por último, revisa directamente en la base de datos que el campo de último acceso del administrador se haya actualizado con la fecha y hora reales de tu prueba.

## Nota para el equipo

En cuanto esto quede funcionando, avísale a Joel y a Juan: ellos van a dejar un apaño temporal en su controlador (usan "el primer administrador que exista" como dueño de cada curso que se crea o edita, porque todavía no había una sesión real de la cual sacar ese dato). Una vez que tu parte esté integrada en `develop`, hay que volver a ese controlador y reemplazar ese apaño por el administrador que esté realmente logueado en la sesión — Spring Security te da una forma de consultar quién es el usuario autenticado actual desde cualquier controlador, revísalo con ellos cuando llegue el momento.
