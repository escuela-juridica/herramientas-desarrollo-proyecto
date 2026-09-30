package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

  @GetMapping("/")
  public String redirigirAInicio() {
    return "redirect:/inicio";
  }

  @GetMapping("/inicio")
  public String inicio() {
    return "inicio";
  }
}
