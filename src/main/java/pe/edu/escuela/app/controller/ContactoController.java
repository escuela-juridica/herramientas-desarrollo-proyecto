package pe.edu.escuela.app.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.escuela.app.model.MensajeContacto;
import pe.edu.escuela.app.service.ContactoService;

@Controller
public class ContactoController {

  private final ContactoService contactoService;

  public ContactoController(ContactoService contactoService) {
    this.contactoService = contactoService;
  }

  @GetMapping("/contacto")
  public String contacto(Model model) {
    if (!model.containsAttribute("contacto")) {
      MensajeContacto mensaje = new MensajeContacto();
      mensaje.setTipo(MensajeContacto.TIPO_CONSULTA);
      model.addAttribute("contacto", mensaje);
    }
    return vista(model);
  }

  @PostMapping("/contacto")
  public String enviar(
      @ModelAttribute("contacto") MensajeContacto mensaje,
      Model model,
      RedirectAttributes redirectAttributes) {
    List<String> errores = contactoService.validar(mensaje);
    if (!errores.isEmpty()) {
      model.addAttribute("errores", errores);
      return vista(model);
    }
    contactoService.guardar(mensaje);
    redirectAttributes.addFlashAttribute("mensajeEnviado", true);
    return "redirect:/contacto#formulario";
  }

  private String vista(Model model) {
    model.addAttribute("active", "contacto");
    return "contacto";
  }
}
