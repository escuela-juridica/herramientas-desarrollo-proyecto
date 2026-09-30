package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

  @GetMapping("/")
  public String redirigirAInicio() {
    return "redirect:/inicio";
  }

  @GetMapping("/inicio")
  public String inicio(Model model) {
    model.addAttribute("active", "inicio");
    return "inicio";
  }
}
