package pe.edu.escuela.app.controller;

import java.io.IOException;
import java.util.Base64;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.escuela.app.dto.Reporte;
import pe.edu.escuela.app.service.ExportacionService;
import pe.edu.escuela.app.service.ReporteService;

/** Reportes estadisticos del panel administrativo y su descarga como informe Word, Excel o PDF. */
@Controller
public class AdminReporteController {

  private static final String PREFIJO_PNG = "data:image/png;base64,";
  private static final int MAXIMO_IMAGEN_BASE64 = 3_000_000;

  private final ReporteService reporteService;
  private final ExportacionService exportacionService;

  public AdminReporteController(ReporteService reporteService, ExportacionService exportacionService) {
    this.reporteService = reporteService;
    this.exportacionService = exportacionService;
  }

  @GetMapping("/admin/dashboard")
  public String ver(Model model) {
    model.addAttribute("activeAdmin", "dashboard");
    model.addAttribute("tituloAdmin", "Dashboard");
    model.addAttribute("reportes", reporteService.generarReportes());
    model.addAttribute("totales", reporteService.totales());
    return "admin/reportes";
  }

  /**
   * Descarga el informe de un reporte. El navegador envia ademas la imagen del grafico (PNG en base64),
   * porque el grafico se dibuja en el cliente.
   */
  @PostMapping("/admin/reportes/{numero}/descargar")
  public ResponseEntity<byte[]> descargar(
      @PathVariable int numero,
      @RequestParam("formato") String formato,
      @RequestParam(name = "imagen", required = false) String imagenBase64) throws IOException {
    Reporte reporte;
    try {
      reporte = reporteService.obtener(numero);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
    byte[] imagen = decodificarImagen(imagenBase64);

    byte[] contenido;
    MediaType tipo;
    String extension;
    switch (formato) {
      case "excel" -> {
        contenido = exportacionService.excel(reporte, imagen);
        tipo = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        extension = "xlsx";
      }
      case "word" -> {
        contenido = exportacionService.word(reporte, imagen);
        tipo = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extension = "docx";
      }
      case "pdf" -> {
        contenido = exportacionService.pdf(reporte, imagen);
        tipo = MediaType.APPLICATION_PDF;
        extension = "pdf";
      }
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de descarga no válido.");
    }

    return ResponseEntity.ok()
        .contentType(tipo)
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
            .filename("reporte-" + numero + "-" + nombreArchivo(reporte.getTitulo()) + "." + extension).build().toString())
        .body(contenido);
  }

  private byte[] decodificarImagen(String imagenBase64) {
    if (imagenBase64 == null || imagenBase64.isBlank()) {
      return null;
    }
    if (!imagenBase64.startsWith(PREFIJO_PNG) || imagenBase64.length() > MAXIMO_IMAGEN_BASE64) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen del gráfico no es válida.");
    }
    try {
      return Base64.getDecoder().decode(imagenBase64.substring(PREFIJO_PNG.length()));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen del gráfico no es válida.");
    }
  }

  /** Titulo en minusculas sin tildes ni espacios, para el nombre del archivo descargado. */
  private String nombreArchivo(String titulo) {
    String sinTildes = java.text.Normalizer.normalize(titulo, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
    return sinTildes.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
  }
}
