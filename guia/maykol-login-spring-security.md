# Maykol — Login con Spring Security (rama `feature/login-spring-security`)

## Qué vas a lograr

Ahora mismo, el botón "Ingresar" de `/login` simplemente te manda a `/admin` sin revisar nada (era una maqueta, a propósito). Tu trabajo es que valide de verdad contra la tabla `administrador`, y que nadie pueda entrar a `/admin` sin haber iniciado sesión.

Vas a crear 4 archivos nuevos y modificar 2 que ya existen:

1. `pom.xml` (agregar una dependencia)
2. `AdministradorRepository.java` (nuevo)
3. `AdminUserDetailsService.java` (nuevo)
4. `AdminAuthenticationSuccessHandler.java` (nuevo)
5. `SecurityConfig.java` (nuevo)
6. `login.html` (modificar el formulario)

## Paso 1 — Agregar Spring Security al proyecto

Abre `pom.xml`, y dentro de `<dependencies>` agrega:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Ponla junto a las otras (cerca de `spring-boot-starter-data-jpa`, por ejemplo). Después de guardar, si usas IntelliJ, dale click a "Load Maven Changes" (o el icono del elefantito azul que aparece arriba a la derecha) para que descargue la dependencia.

## Paso 2 — `AdministradorRepository.java` (nuevo)

Créalo en `src/main/java/pe/edu/escuela/app/repository/AdministradorRepository.java`:

```java
package pe.edu.escuela.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

  Optional<Administrador> findByCorreo(String correo);
}
```

**¿Qué es `Optional`?** En vez de devolver `null` cuando no encuentra a nadie con ese correo (lo cual puede causar errores si te olvidas de revisarlo), `Optional` te obliga a manejar explícitamente el caso "no encontré nada" — lo vas a ver usado en el siguiente paso con `.orElseThrow(...)`.

## Paso 3 — `AdminUserDetailsService.java` (nuevo)

Este archivo es el que le explica a Spring Security **cómo buscar un usuario y qué significa su contraseña/estado** en nuestro proyecto (Spring Security no sabe nada de tu tabla `administrador` — tienes que decírselo tú).

Créalo en `src/main/java/pe/edu/escuela/app/security/AdminUserDetailsService.java` (vas a crear la carpeta `security` nueva):

```java
package pe.edu.escuela.app.security;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.Administrador;
import pe.edu.escuela.app.repository.AdministradorRepository;
import pe.edu.escuela.app.util.Constantes;

@Service
public class AdminUserDetailsService implements UserDetailsService {

  private final AdministradorRepository administradorRepository;

  public AdminUserDetailsService(AdministradorRepository administradorRepository) {
    this.administradorRepository = administradorRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
    Administrador admin = administradorRepository.findByCorreo(correo)
        .orElseThrow(() -> new UsernameNotFoundException("No existe un administrador con ese correo"));

    return User.builder()
        .username(admin.getCorreo())
        .password(admin.getClave())
        .disabled(!Constantes.ESTADO_ACTIVO.equals(admin.getEstado()))
        .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .build();
  }
}
```

**Explicación:**

- `implements UserDetailsService` — es la interfaz que Spring Security reconoce automáticamente. Al ponerle `@Service`, Spring la detecta sola y la usa cuando alguien intenta iniciar sesión.
- `loadUserByUsername(String correo)` — Spring Security llama a este método pasándole lo que la persona escribió en el campo de correo. Tú buscas al admin, y si no existe, lanzas `UsernameNotFoundException` (Spring Security la captura y muestra "credenciales incorrectas", sin decir si fue el correo o la clave lo que falló — por seguridad).
- `.password(admin.getClave())` — le pasas la clave **ya hasheada con BCrypt** tal como está guardada en la base de datos (revisa `script/init.sql`, la clave semilla ya viene así). Spring Security se encarga de comparar lo que la persona escribió contra este hash, tú nunca comparas contraseñas a mano.
- `.disabled(!Constantes.ESTADO_ACTIVO.equals(admin.getEstado()))` — si el admin tiene `estado = 'I'`, esta cuenta queda deshabilitada automáticamente, sin que tengas que escribir lógica aparte para bloquear el login.
- `.authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))` — le da a cualquier fila de `administrador` el permiso fijo `ROLE_ADMIN`. No hace falta una columna de rol en la base de datos porque en este proyecto **todo el que está en esa tabla es administrador**, no hay otro tipo de usuario.

## Paso 4 — `AdminAuthenticationSuccessHandler.java` (nuevo)

Esto es lo que actualiza `fecha_ultimo_login` cada vez que alguien entra con éxito. Créalo en `src/main/java/pe/edu/escuela/app/security/AdminAuthenticationSuccessHandler.java`:

```java
package pe.edu.escuela.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pe.edu.escuela.app.repository.AdministradorRepository;

@Component
public class AdminAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final AdministradorRepository administradorRepository;

  public AdminAuthenticationSuccessHandler(AdministradorRepository administradorRepository) {
    this.administradorRepository = administradorRepository;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    administradorRepository.findByCorreo(authentication.getName()).ifPresent(admin -> {
      admin.setFechaUltimoLogin(LocalDateTime.now());
      administradorRepository.save(admin);
    });

    response.sendRedirect("/admin");
  }
}
```

**Explicación:** `authentication.getName()` te da el correo de la persona que acaba de iniciar sesión (Spring Security lo guarda ahí). Buscas de nuevo al admin, le pones la fecha actual, lo guardas, y al final rediriges manualmente a `/admin` con `response.sendRedirect(...)` — esto reemplaza lo que en Spring Security normalmente se llama `defaultSuccessUrl`, porque acá además necesitas ejecutar tu propio código (guardar la fecha) antes de redirigir.

## Paso 5 — `SecurityConfig.java` (nuevo)

Este es el archivo central: le dice a Spring Security qué rutas son públicas, cuáles requieren estar logueado, y cómo es la pantalla de login. Créalo en `src/main/java/pe/edu/escuela/app/config/SecurityConfig.java`:

```java
package pe.edu.escuela.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.escuela.app.security.AdminAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AdminAuthenticationSuccessHandler successHandler;

  public SecurityConfig(AdminAuthenticationSuccessHandler successHandler) {
    this.successHandler = successHandler;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().permitAll()
      )
      .formLogin(form -> form
        .loginPage("/login")
        .usernameParameter("correo")
        .passwordParameter("clave")
        .successHandler(successHandler)
        .permitAll()
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/inicio")
        .permitAll()
      );

    return http.build();
  }
}
```

**Explicación de cada parte:**

- `.csrf(csrf -> csrf.disable())` — Spring Security por defecto exige un "token" anti-falsificación en cada formulario POST. Para no complicar el formulario de login con eso (necesitaría una dependencia extra de integración con Thymeleaf), lo desactivamos para este proyecto. **Esto es una simplificación válida para un proyecto de curso, no es lo que harías en una aplicación real en producción** — coméntalo si te preguntan.
- `.requestMatchers("/admin/**").hasRole("ADMIN")` — cualquier ruta que empiece con `/admin/` (incluyendo `/admin/cursos`, y todo lo que Joel/Juan agreguen debajo) exige estar logueado como admin.
- `.anyRequest().permitAll()` — todo lo demás (el sitio público: inicio, catálogo, nosotros, contacto, los CSS/imágenes, etc.) queda libre, sin login.
- `.loginPage("/login")` — le dice a Spring Security que use tu página (`login.html`, la que ya existe) en vez de una genérica fea que trae por defecto.
- `.usernameParameter("correo")` / `.passwordParameter("clave")` — por defecto Spring Security espera que el formulario tenga campos llamados `username`/`password`; con esto le dices que en tu HTML se llaman `correo`/`clave` (coincide con el Paso 6).
- `.successHandler(successHandler)` — usa el que hiciste en el Paso 4 en vez del comportamiento por defecto.

## Paso 6 — Modificar `login.html`

Busca el `<form>` (creado como maqueta, con `action="/admin" method="get"`):

```html
<form action="/admin" method="get">
  <div class="field">
    <label for="l-mail"><i class="fa-solid fa-envelope"></i> Correo electrónico</label>
    <input id="l-mail" type="text" placeholder="tucorreo@ejemplo.com" autocomplete="username">
  </div>
  <div class="field">
    <label for="l-pass"><i class="fa-solid fa-key"></i> Contraseña</label>
    <input id="l-pass" type="password" placeholder="••••••••" autocomplete="current-password">
  </div>
  <button type="submit" class="site-button button-primary"><i class="fa-solid fa-right-to-bracket"></i> Ingresar</button>
  <a class="help-link">¿Necesitas recuperar el acceso?</a>
</form>
```

Cámbialo por esto (fíjate bien en los `name="correo"` y `name="clave"` que se agregan — sin eso, Spring Security no puede leer lo que la persona escribió):

```html
<form action="/login" method="post">
  <div class="field">
    <label for="l-mail"><i class="fa-solid fa-envelope"></i> Correo electrónico</label>
    <input id="l-mail" name="correo" type="text" placeholder="tucorreo@ejemplo.com" autocomplete="username">
  </div>
  <div class="field">
    <label for="l-pass"><i class="fa-solid fa-key"></i> Contraseña</label>
    <input id="l-pass" name="clave" type="password" placeholder="••••••••" autocomplete="current-password">
  </div>
  <button type="submit" class="site-button button-primary"><i class="fa-solid fa-right-to-bracket"></i> Ingresar</button>
  <a class="help-link">¿Necesitas recuperar el acceso?</a>
</form>
```

**No necesitas tocar `LoginController.java`** — su único método (`@GetMapping("/login")`, que muestra la página) se queda igual. El `POST` que procesa el login lo intercepta Spring Security automáticamente, no pasa por tu controlador.

## Cómo probar que funciona

1. Corre la app.
2. Entra a `/admin` directo, **sin** haber iniciado sesión — te debería redirigir solo a `/login` (eso confirma que la protección funciona).
3. En `/login`, usa el correo y clave semilla: `admin@escuelajuridica.edu.pe` / `1234` (revisa `script/init.sql` si cambiaste algo).
4. Debería llevarte a `/admin` y mostrar el panel.
5. Ve a `/logout` — debería cerrar la sesión y mandarte a `/inicio`. Si intentas entrar de nuevo a `/admin`, te debería volver a pedir login.
6. Revisa en la base de datos (`SELECT fecha_ultimo_login FROM administrador;`) que la fecha se haya actualizado después de un login exitoso.

## Nota para el equipo

Una vez que esto esté andando, **Joel y Juan ya no necesitan preocuparse por proteger `/admin/cursos` manualmente** — la regla `.requestMatchers("/admin/**").hasRole("ADMIN")` ya cubre automáticamente cualquier ruta que ellos agreguen bajo `/admin/`.
