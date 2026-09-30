package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.escuela.app.service.CursoService;

@Controller
public class InicioController {

  private final CursoService cursoService;

  public InicioController(CursoService cursoService) {
    this.cursoService = cursoService;
  }

  @GetMapping("/")
  public String redirigirAInicio() {
    return "redirect:/inicio";
  }

  @GetMapping("/inicio")
  public String inicio(Model model) {
    model.addAttribute("active", "inicio");
    model.addAttribute("destacados", cursoService.obtenerDestacados());
    return "inicio";
  }
}
