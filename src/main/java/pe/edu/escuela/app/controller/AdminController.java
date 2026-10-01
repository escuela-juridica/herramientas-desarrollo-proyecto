package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

  @GetMapping("/admin")
  public String panel() {
    return "redirect:/admin/cursos";
  }
}
