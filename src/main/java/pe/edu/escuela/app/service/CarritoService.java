package pe.edu.escuela.app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.dto.LineaCarrito;
import pe.edu.escuela.app.dto.ResumenCarrito;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.util.Constantes;

/**
 * Carrito de compras. Vive en la sesion HTTP como un mapa [id del curso -> cantidad de cupos], asi no hace falta
 * una tabla: la compra se guarda recien al confirmarla. Los cupos del curso hacen de "stock": no se puede pedir
 * mas cupos de los que tiene el curso (si no tiene limite, cupos es nulo).
 */
@Service
public class CarritoService {

  static final String ATRIBUTO_SESION = "CARRITO_CURSOS";

  private final CursoRepository cursoRepository;

  public CarritoService(CursoRepository cursoRepository) {
    this.cursoRepository = cursoRepository;
  }

  /** Cupos en el carrito (suma de cantidades); no crea la sesion si el visitante aun no tiene una. */
  public int cantidad(HttpServletRequest request) {
    HttpSession sesion = request.getSession(false);
    return sesion == null ? 0 : contenido(sesion).values().stream().mapToInt(Integer::intValue).sum();
  }

  /** Copia de lo que hay en el carrito: id del curso y cantidad, en el orden en que se agregaron. */
  public Map<Integer, Integer> cantidades(HttpSession sesion) {
    return new LinkedHashMap<>(contenido(sesion));
  }

  /** Agrega un cupo del curso. Devuelve el mensaje de exito; si no se puede, lanza IllegalArgumentException. */
  public String agregar(HttpSession sesion, Integer idCurso) {
    Curso curso = cursoActivo(idCurso);
    int actual = contenido(sesion).getOrDefault(idCurso, 0);
    validarCupos(curso, actual + 1);
    contenido(sesion).put(idCurso, actual + 1);
    return "«" + curso.getNombre() + "» se agregó al carrito.";
  }

  public void aumentar(HttpSession sesion, Integer idCurso) {
    agregar(sesion, idCurso);
  }

  public void disminuir(HttpSession sesion, Integer idCurso) {
    Map<Integer, Integer> contenido = contenido(sesion);
    int actual = contenido.getOrDefault(idCurso, 0);
    if (actual > 1) {
      contenido.put(idCurso, actual - 1);
    } else {
      contenido.remove(idCurso);
    }
  }

  /** Quita el curso del carrito y devuelve el mensaje para mostrar. */
  public String eliminar(HttpSession sesion, Integer idCurso) {
    contenido(sesion).remove(idCurso);
    return cursoRepository.findById(idCurso)
        .map(curso -> "«" + curso.getNombre() + "» se eliminó del carrito.")
        .orElse("El curso se eliminó del carrito.");
  }

  public void vaciar(HttpSession sesion) {
    sesion.removeAttribute(ATRIBUTO_SESION);
  }

  /** Lineas del carrito con sus montos. Los cursos que se desactivaron mientras tanto se sacan solos. */
  public ResumenCarrito resumen(HttpSession sesion) {
    Map<Integer, Integer> contenido = contenido(sesion);
    List<LineaCarrito> lineas = new ArrayList<>();
    for (Map.Entry<Integer, Integer> entrada : new LinkedHashMap<>(contenido).entrySet()) {
      Curso curso = cursoRepository.findById(entrada.getKey())
          .filter(c -> Constantes.ESTADO_ACTIVO.equals(c.getEstado()))
          .orElse(null);
      if (curso == null) {
        contenido.remove(entrada.getKey());
      } else {
        lineas.add(new LineaCarrito(curso, entrada.getValue()));
      }
    }
    return ResumenCarrito.de(lineas);
  }

  private Curso cursoActivo(Integer idCurso) {
    return cursoRepository.findById(idCurso)
        .filter(c -> Constantes.ESTADO_ACTIVO.equals(c.getEstado()))
        .orElseThrow(() -> new IllegalArgumentException("El curso solicitado no está disponible."));
  }

  private void validarCupos(Curso curso, int pedidos) {
    Integer cupos = curso.getCupos();
    if (cupos == null) {
      return;
    }
    if (cupos <= 0) {
      throw new IllegalArgumentException("«" + curso.getNombre() + "» no tiene cupos disponibles.");
    }
    if (pedidos > cupos) {
      throw new IllegalArgumentException("Solo quedan " + cupos + " cupos de «" + curso.getNombre() + "».");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, Integer> contenido(HttpSession sesion) {
    // Si la sesion trae un carrito de una version anterior (otro tipo de objeto), se descarta y se empieza de cero.
    if (!(sesion.getAttribute(ATRIBUTO_SESION) instanceof Map)) {
      sesion.setAttribute(ATRIBUTO_SESION, new LinkedHashMap<Integer, Integer>());
    }
    return (Map<Integer, Integer>) sesion.getAttribute(ATRIBUTO_SESION);
  }
}
