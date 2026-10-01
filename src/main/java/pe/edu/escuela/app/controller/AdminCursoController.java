package pe.edu.escuela.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del CRUD de cursos dentro del panel administrativo (/admin/cursos).
 * Por ahora solo sirve la página; listar/crear/editar/eliminar y las búsquedas van aquí.
 */
@Controller
public class AdminCursoController {

  @GetMapping("/admin/cursos")
  public String listar(Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Cursos");
    return "admin/cursos";
  }
}
