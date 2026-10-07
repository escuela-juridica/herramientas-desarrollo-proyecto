package pe.edu.escuela.app.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.Administrador;
import pe.edu.escuela.app.repository.AdministradorRepository;
import pe.edu.escuela.app.util.Constantes;

@Service
public class AdministradorUserDetailsService implements UserDetailsService {

  private final AdministradorRepository administradorRepository;

  public AdministradorUserDetailsService(AdministradorRepository administradorRepository) {
    this.administradorRepository = administradorRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
    Administrador administrador = administradorRepository.findByCorreo(correo)
        .orElseThrow(() -> new UsernameNotFoundException("Administrador no encontrado"));

    boolean activo = Constantes.ESTADO_ACTIVO.equalsIgnoreCase(administrador.getEstado());

    return User.withUsername(administrador.getCorreo())
        .password(administrador.getClave())
        .roles("ADMIN")
        .disabled(!activo)
        .build();
  }
}
