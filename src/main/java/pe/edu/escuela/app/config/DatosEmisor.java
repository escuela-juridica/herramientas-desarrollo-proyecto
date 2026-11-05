package pe.edu.escuela.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Datos tributarios del emisor de la boleta (quien vende). Se configuran en application.properties con las claves
 * app.emisor.*, asi se cambian en un solo lugar sin tocar el codigo.
 */
@Component
public class DatosEmisor {

  private final String razonSocial;
  private final String nombreComercial;
  private final String ruc;
  private final String domicilioFiscal;

  public DatosEmisor(
      @Value("${app.emisor.razon-social}") String razonSocial,
      @Value("${app.emisor.nombre-comercial}") String nombreComercial,
      @Value("${app.emisor.ruc}") String ruc,
      @Value("${app.emisor.domicilio-fiscal}") String domicilioFiscal) {
    this.razonSocial = razonSocial;
    this.nombreComercial = nombreComercial;
    this.ruc = ruc;
    this.domicilioFiscal = domicilioFiscal;
  }

  public String getRazonSocial() {
    return razonSocial;
  }

  public String getNombreComercial() {
    return nombreComercial;
  }

  public String getRuc() {
    return ruc;
  }

  public String getDomicilioFiscal() {
    return domicilioFiscal;
  }
}
