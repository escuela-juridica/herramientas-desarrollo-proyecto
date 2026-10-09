package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.escuela.app.service.CursoService;

@Controller
public class CatalogoController {

  private final CursoService cursoService;

  public CatalogoController(CursoService cursoService) {
    this.cursoService = cursoService;
  }

  @GetMapping("/catalogo")
  public String catalogo(Model model) {
    model.addAttribute("active", "catalogo");
    model.addAttribute("cursos", cursoService.obtenerActivos());
    model.addAttribute("tiposCurso", cursoService.obtenerTiposCurso());
    return "catalogo";
  }

  @GetMapping("/catalogo/buscar")
  public String buscar(
      @RequestParam(required = false) String texto,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Integer idTipoCurso,
      Model model) {
    model.addAttribute("cursos", cursoService.buscarEnCatalogo(texto, descripcion, idTipoCurso));
    return "catalogo :: resultadosCursos";
  }
}
