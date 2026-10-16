package pe.edu.escuela.app.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.escuela.app.model.Administrador;
import pe.edu.escuela.app.repository.AdministradorRepository;

/**
 * Agrega el administrador autenticado (si lo hay) al modelo de cualquier vista,
 * para que el sidebar del panel muestre a quien inició sesión de verdad
 * en vez de un nombre quemado en el HTML.
 */
@ControllerAdvice
public class AdminContextoAdvice {

  private final AdministradorRepository administradorRepository;

  public AdminContextoAdvice(AdministradorRepository administradorRepository) {
    this.administradorRepository = administradorRepository;
  }

  @ModelAttribute("adminActual")
  public Administrador adminActual() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return null;
    }
    return administradorRepository.findByCorreo(authentication.getName()).orElse(null);
  }
}
