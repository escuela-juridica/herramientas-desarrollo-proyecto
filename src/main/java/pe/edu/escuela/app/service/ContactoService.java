package pe.edu.escuela.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.MensajeContacto;
import pe.edu.escuela.app.repository.MensajeContactoRepository;

/** Guarda los mensajes del formulario de la pagina Contacto. */
@Service
public class ContactoService {

  public static final Set<String> TIPOS = Set.of(
      MensajeContacto.TIPO_PEDIDO, MensajeContacto.TIPO_OPINION, MensajeContacto.TIPO_CONSULTA);

  private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final Pattern TELEFONO = Pattern.compile("^[0-9+ ]{6,20}$");

  private final MensajeContactoRepository mensajeRepository;

  public ContactoService(MensajeContactoRepository mensajeRepository) {
    this.mensajeRepository = mensajeRepository;
  }

  public List<String> validar(MensajeContacto mensaje) {
    List<String> errores = new ArrayList<>();
    if (mensaje.getNombres() == null || mensaje.getNombres().isBlank()) {
      errores.add("Ingresa tu nombre.");
    }
    if (mensaje.getCorreo() == null || !CORREO.matcher(mensaje.getCorreo().trim()).matches()) {
      errores.add("Ingresa un correo válido.");
    }
    if (mensaje.getTelefono() != null && !mensaje.getTelefono().isBlank()
        && !TELEFONO.matcher(mensaje.getTelefono().trim()).matches()) {
      errores.add("El teléfono solo puede tener números, espacios o +.");
    }
    if (mensaje.getTipo() == null || !TIPOS.contains(mensaje.getTipo())) {
      errores.add("Selecciona el motivo de tu mensaje.");
    }
    if (mensaje.getMensaje() == null || mensaje.getMensaje().isBlank()) {
      errores.add("Escribe tu mensaje.");
    } else if (mensaje.getMensaje().trim().length() > 1000) {
      errores.add("El mensaje no puede superar los 1000 caracteres.");
    }
    return errores;
  }

  public MensajeContacto guardar(MensajeContacto mensaje) {
    mensaje.setNombres(mensaje.getNombres().trim());
    mensaje.setCorreo(mensaje.getCorreo().trim());
    mensaje.setMensaje(mensaje.getMensaje().trim());
    mensaje.setTelefono(mensaje.getTelefono() == null || mensaje.getTelefono().isBlank()
        ? null
        : mensaje.getTelefono().trim());
    return mensajeRepository.save(mensaje);
  }
}
