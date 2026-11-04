package pe.edu.escuela.app.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Datos del comprador que se piden al pagar. Se valida a mano, igual que el formulario de cursos del panel. */
public class DatosCompra implements java.io.Serializable {

  public static final Set<String> MEDIOS_PAGO = Set.of("TARJETA", "YAPE", "TRANSFERENCIA");

  private static final Pattern DNI = Pattern.compile("^[0-9]{8}$");
  private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final Pattern TELEFONO = Pattern.compile("^[0-9+ ]{6,20}$");

  private String nombres = "";
  private String apellidos = "";
  private String documento = "";
  private String correo = "";
  private String telefono = "";
  private String medioPago = "TARJETA";

  /** Devuelve los mensajes de error; la lista vacia significa que los datos son validos. */
  public List<String> validar() {
    List<String> errores = new ArrayList<>();
    if (nombres == null || nombres.isBlank()) {
      errores.add("Ingresa tus nombres.");
    }
    if (apellidos == null || apellidos.isBlank()) {
      errores.add("Ingresa tus apellidos.");
    }
    if (documento == null || !DNI.matcher(documento.trim()).matches()) {
      errores.add("El DNI debe tener 8 dígitos.");
    }
    if (correo == null || !CORREO.matcher(correo.trim()).matches()) {
      errores.add("Ingresa un correo válido.");
    }
    if (telefono != null && !telefono.isBlank() && !TELEFONO.matcher(telefono.trim()).matches()) {
      errores.add("El teléfono solo puede tener números, espacios o +.");
    }
    if (medioPago == null || !MEDIOS_PAGO.contains(medioPago)) {
      errores.add("Selecciona un medio de pago.");
    }
    return errores;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String documento) {
    this.documento = documento;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getMedioPago() {
    return medioPago;
  }

  public void setMedioPago(String medioPago) {
    this.medioPago = medioPago;
  }
}
