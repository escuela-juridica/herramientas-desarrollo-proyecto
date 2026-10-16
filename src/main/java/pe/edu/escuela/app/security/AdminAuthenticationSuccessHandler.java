package pe.edu.escuela.app.security;

import jakarta.servlet.ServletException;
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
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    administradorRepository.findByCorreo(authentication.getName()).ifPresent(administrador -> {
      administrador.setFechaUltimoLogin(LocalDateTime.now());
      administradorRepository.save(administrador);
    });

    response.sendRedirect(request.getContextPath() + "/admin");
  }
}
