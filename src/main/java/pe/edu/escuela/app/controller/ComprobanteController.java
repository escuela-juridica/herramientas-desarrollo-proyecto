package pe.edu.escuela.app.controller;

import java.io.IOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.escuela.app.config.DatosEmisor;
import pe.edu.escuela.app.model.Comprobante;
import pe.edu.escuela.app.service.ExportacionService;
import pe.edu.escuela.app.service.VentaService;

/**
 * Boleta de una compra. Se abre con el codigo de acceso que se entrega al pagar, no con el numero de la boleta,
 * para que nadie pueda ver los datos de otro cliente probando numeros.
 */
@Controller
public class ComprobanteController {

  private final VentaService ventaService;
  private final ExportacionService exportacionService;
  private final DatosEmisor emisor;

  public ComprobanteController(
      VentaService ventaService, ExportacionService exportacionService, DatosEmisor emisor) {
    this.ventaService = ventaService;
    this.exportacionService = exportacionService;
    this.emisor = emisor;
  }

  @GetMapping("/comprobante")
  public String ver(@RequestParam("codigo") String codigo, Model model) {
    model.addAttribute("comprobante", buscar(codigo));
    model.addAttribute("emisor", emisor);
    return "comprobante";
  }

  @GetMapping("/comprobante/pdf")
  public ResponseEntity<byte[]> descargar(@RequestParam("codigo") String codigo) throws IOException {
    Comprobante comprobante = buscar(codigo);
    byte[] pdf = exportacionService.boletaPdf(comprobante);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
            .filename("boleta-" + comprobante.getNumero() + ".pdf").build().toString())
        .body(pdf);
  }

  private Comprobante buscar(String codigo) {
    try {
      return ventaService.obtenerComprobante(codigo);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }
}
