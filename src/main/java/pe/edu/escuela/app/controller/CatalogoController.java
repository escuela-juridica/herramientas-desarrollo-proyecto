package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    return "catalogo";
  }
}
