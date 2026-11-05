package pe.edu.escuela.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.escuela.app.service.CarritoService;

/** Agrega la cantidad de cursos del carrito al modelo para mostrarla en el icono de la barra de navegacion. */
@ControllerAdvice
public class CarritoContextoAdvice {

  private final CarritoService carritoService;

  public CarritoContextoAdvice(CarritoService carritoService) {
    this.carritoService = carritoService;
  }

  @ModelAttribute("carritoCantidad")
  public int carritoCantidad(HttpServletRequest request) {
    return carritoService.cantidad(request);
  }
}
