package pe.edu.escuela.app.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.escuela.app.dto.DatosCompra;
import pe.edu.escuela.app.model.Comprobante;
import pe.edu.escuela.app.service.CarritoService;
import pe.edu.escuela.app.service.VentaService;

/**
 * Carrito de compras (basado en el carrito de referencia de la carpeta recurso/avance3): agregar desde el catalogo,
 * cambiar cantidades, eliminar, vaciar y confirmar la compra, que genera la boleta.
 * Los mensajes de exito, error o info se muestran una sola vez (atributos flash).
 */
@Controller
public class CarritoController {

  private final CarritoService carritoService;
  private final VentaService ventaService;

  public CarritoController(CarritoService carritoService, VentaService ventaService) {
    this.carritoService = carritoService;
    this.ventaService = ventaService;
  }

  @GetMapping("/carrito")
  public String ver(HttpSession sesion, Model model) {
    if (!model.containsAttribute("datos")) {
      model.addAttribute("datos", new DatosCompra());
    }
    model.addAttribute("active", "carrito");
    model.addAttribute("carrito", carritoService.resumen(sesion));
    return "carrito";
  }

  /** Lo llama el catalogo con fetch; responde JSON con el mensaje y la nueva cantidad para el icono del carrito. */
  @PostMapping("/carrito/agregar")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> agregar(
      @RequestParam("idCurso") Integer idCurso, HttpSession sesion) {
    try {
      String mensaje = carritoService.agregar(sesion, idCurso);
      return ResponseEntity.ok(Map.of("mensaje", mensaje, "cantidad", carritoCantidad(sesion)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
    }
  }

  /** Botones de la tabla del carrito: aumentar (+), disminuir (-) y eliminar. */
  @PostMapping("/carrito/accion")
  public String accion(
      @RequestParam("idCurso") Integer idCurso,
      @RequestParam("accion") String accion,
      HttpSession sesion,
      RedirectAttributes redirectAttributes) {
    try {
      switch (accion) {
        case "aumentar" -> carritoService.aumentar(sesion, idCurso);
        case "disminuir" -> carritoService.disminuir(sesion, idCurso);
        case "eliminar" -> mensaje(redirectAttributes, "info", carritoService.eliminar(sesion, idCurso));
        default -> mensaje(redirectAttributes, "error", "La acción solicitada no es válida.");
      }
    } catch (IllegalArgumentException e) {
      mensaje(redirectAttributes, "error", e.getMessage());
    }
    return "redirect:/carrito";
  }

  @PostMapping("/carrito/vaciar")
  public String vaciar(HttpSession sesion, RedirectAttributes redirectAttributes) {
    carritoService.vaciar(sesion);
    mensaje(redirectAttributes, "info", "El carrito fue vaciado.");
    return "redirect:/carrito";
  }

  /** "Confirmar compra": valida los datos del comprador y los cupos, registra el pedido y emite la boleta. */
  @PostMapping("/carrito/comprar")
  public String comprar(
      @ModelAttribute("datos") DatosCompra datos,
      HttpSession sesion,
      RedirectAttributes redirectAttributes) {
    Map<Integer, Integer> cantidades = carritoService.cantidades(sesion);
    List<String> errores = cantidades.isEmpty() ? List.of("No puedes comprar con el carrito vacío.") : datos.validar();
    // Si fallan los datos del comprador se vuelve a abrir el modal de pago; los demás errores se ven en la pagina
    boolean erroresDeDatos = !cantidades.isEmpty() && !errores.isEmpty();
    if (errores.isEmpty()) {
      try {
        Comprobante comprobante = ventaService.comprar(datos, cantidades);
        carritoService.vaciar(sesion);
        return "redirect:/comprobante?codigo=" + comprobante.getCodigoAcceso();
      } catch (IllegalArgumentException e) {
        errores = List.of(e.getMessage());
      }
    }
    // Se redirige en vez de dibujar la vista aqui: el layout usa rutas relativas (css/base.css) y desde
    // /carrito/comprar el navegador buscaria los estilos en /carrito/css/base.css.
    redirectAttributes.addFlashAttribute("errores", errores);
    redirectAttributes.addFlashAttribute("datos", datos);
    if (erroresDeDatos) {
      redirectAttributes.addFlashAttribute("abrirPago", true);
    }
    return "redirect:/carrito";
  }

  private int carritoCantidad(HttpSession sesion) {
    return carritoService.cantidades(sesion).values().stream().mapToInt(Integer::intValue).sum();
  }

  private void mensaje(RedirectAttributes redirectAttributes, String tipo, String texto) {
    redirectAttributes.addFlashAttribute("mensajeTipo", tipo);
    redirectAttributes.addFlashAttribute("mensajeTexto", texto);
  }
}
