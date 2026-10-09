package pe.edu.escuela.app.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.model.Docente;
import pe.edu.escuela.app.model.TipoCurso;
import pe.edu.escuela.app.service.CursoService;


/**
 * Controlador del CRUD de cursos dentro del panel administrativo (/admin/cursos).
 */
@Controller
public class AdminCursoController {

  private static final Set<String> EXTENSIONES_IMAGEN_PERMITIDAS =
          Set.of("jpg", "jpeg", "png", "webp", "gif");

  private final CursoService cursoService;
  private final Path uploadDir;

  public AdminCursoController(
      CursoService cursoService,
      @Value("${app.upload-dir}") String uploadDir) {
    this.cursoService = cursoService;
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @GetMapping("/admin/cursos")
  public String listar(
          @RequestParam(name = "busqueda", required = false) String busqueda,
          Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Cursos");
    model.addAttribute("cursos", cursoService.listarParaAdministracion(busqueda));
    model.addAttribute("busqueda", busqueda == null ? "" : busqueda.trim());
    return "admin/cursos";
  }
  @GetMapping("/admin/cursos/buscar")
  public String buscarCursos(
      @RequestParam(name = "busqueda", required = false) String busqueda,
      @RequestParam(name = "codigo", required = false) String codigo,
      Model model) {

    model.addAttribute("cursos", codigo == null
        ? cursoService.listarParaAdministracion(busqueda)
        : cursoService.buscarParaAdministracionPorCodigo(codigo));

    return "admin/cursos :: filasCursos";
  }
  @GetMapping("/admin/cursos/nuevo")
  public String mostrarFormularioCreacion(Model model) {
    Curso curso = new Curso();
    curso.setTipoCurso(new TipoCurso());
    curso.setDocente(new Docente());
    model.addAttribute("curso", curso);
    prepararFormulario(model);
    return "admin/curso-form";
  }

  @PostMapping("/admin/cursos/crear")
  public String crear(
      @ModelAttribute("curso") Curso curso,
      BindingResult bindingResult,
      @RequestParam(name = "imagenArchivo", required = false) MultipartFile imagenArchivo,
      Model model,
      RedirectAttributes redirectAttributes,
      Authentication authentication) {

    validarCurso(curso, imagenArchivo, bindingResult);
    if (bindingResult.hasErrors()) {
      prepararFormulario(model);
      return "admin/curso-form";
    }

    ImagenGuardada imagenGuardada = null;
    try {
      imagenGuardada = guardarImagen(imagenArchivo);
      curso.setImagen(imagenGuardada.rutaPublica());
      cursoService.guardarNuevo(curso, authentication.getName());
    } catch (IllegalArgumentException | IllegalStateException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", e.getMessage());
      prepararFormulario(model);
      return "admin/curso-form";
    } catch (DataIntegrityViolationException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute(
              "errorGuardado",
              "No se pudo guardar el curso. Verifica que el código no esté repetido.");
      prepararFormulario(model);
      return "admin/curso-form";
    } catch (IOException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", "No se pudo guardar la imagen. Inténtalo nuevamente.");
      prepararFormulario(model);
      return "admin/curso-form";
    } catch (RuntimeException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", "No se pudo guardar el curso. Inténtalo nuevamente.");
      prepararFormulario(model);
      return "admin/curso-form";
    }

    redirectAttributes.addFlashAttribute("mensajeExito", "Curso creado correctamente.");
    return "redirect:/admin/cursos";
  }

  private void prepararFormulario(Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Nuevo curso");
    model.addAttribute("tiposCurso", cursoService.obtenerTiposCurso());
    model.addAttribute("docentes", cursoService.obtenerDocentes());
  }

  private void validarCurso(
          Curso curso,
          MultipartFile imagenArchivo,
          BindingResult bindingResult) {
    validarDatosCurso(curso, bindingResult);
    if (imagenArchivo == null || imagenArchivo.isEmpty()) {
      bindingResult.reject("curso.imagen.vacia", "Selecciona una imagen para el curso.");
    }
  }

  private void validarDatosCurso(Curso curso, BindingResult bindingResult) {
    if (curso.getCodigo() == null || curso.getCodigo().isBlank()) {
      bindingResult.rejectValue("codigo", "curso.codigo.vacio", "El código es obligatorio.");
    }
    if (curso.getNombre() == null || curso.getNombre().isBlank()) {
      bindingResult.rejectValue("nombre", "curso.nombre.vacio", "El nombre es obligatorio.");
    }
    if (curso.getDescripcion() == null || curso.getDescripcion().isBlank()) {
      bindingResult.rejectValue(
              "descripcion", "curso.descripcion.vacia", "La descripción es obligatoria.");
    }
    if (curso.getPrecio() == null || curso.getPrecio().signum() < 0) {
      bindingResult.rejectValue("precio", "curso.precio.invalido", "Ingresa un precio válido.");
    }
    if (curso.getTipoCurso() == null || curso.getTipoCurso().getIdTipoCurso() == null) {
      bindingResult.rejectValue(
              "tipoCurso.idTipoCurso", "curso.tipo.vacio", "Selecciona un tipo de curso.");
    }
    if (curso.getDocente() == null || curso.getDocente().getIdDocente() == null) {
      bindingResult.rejectValue(
              "docente.idDocente", "curso.docente.vacio", "Selecciona un docente.");
    }
    if (curso.getFechaInicio() != null && curso.getFechaFin() != null
            && curso.getFechaFin().isBefore(curso.getFechaInicio())) {
      bindingResult.rejectValue(
              "fechaFin", "curso.fechaFin.invalida", "La fecha de fin no puede ser anterior al inicio.");
    }
  }

  private ImagenGuardada guardarImagen(MultipartFile archivo) throws IOException {
    String nombreOriginal = archivo.getOriginalFilename();
    String extension = obtenerExtension(nombreOriginal);
    String tipoContenido = archivo.getContentType();

    if (tipoContenido == null || !tipoContenido.toLowerCase(Locale.ROOT).startsWith("image/")
            || !EXTENSIONES_IMAGEN_PERMITIDAS.contains(extension)) {
      throw new IllegalArgumentException(
              "El archivo debe ser una imagen JPG, PNG, WEBP o GIF.");
    }

    Files.createDirectories(uploadDir);
    String nombreUnico = UUID.randomUUID() + "." + extension;
    Path destino = uploadDir.resolve(nombreUnico).normalize();
    if (!destino.getParent().equals(uploadDir)) {
      throw new IllegalArgumentException("El nombre del archivo no es válido.");
    }

    try (var entrada = archivo.getInputStream()) {
      Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      try {
        Files.deleteIfExists(destino);
      } catch (IOException ignored) {
        // Se conserva la excepción original de escritura.
      }
      throw e;
    }
    return new ImagenGuardada(destino, "/uploads/cursos/" + nombreUnico);
  }

  private String obtenerExtension(String nombreArchivo) {
    if (nombreArchivo == null) {
      return "";
    }
    int posicionPunto = nombreArchivo.lastIndexOf('.');
    if (posicionPunto < 0 || posicionPunto == nombreArchivo.length() - 1) {
      return "";
    }
    return nombreArchivo.substring(posicionPunto + 1).toLowerCase(Locale.ROOT);
  }

  private void eliminarImagenSiExiste(ImagenGuardada imagenGuardada) {
    if (imagenGuardada == null) {
      return;
    }
    try {
      Files.deleteIfExists(imagenGuardada.destino());
    } catch (IOException ignored) {
      // El fallo de limpieza no debe ocultar el error original que impidió crear el curso.
    }
  }

  private record ImagenGuardada(Path destino, String rutaPublica) {
  }

  @GetMapping("/admin/cursos/editar/{idCurso}")
  public String formularioEditar(
          @PathVariable Integer idCurso,
          Model model,
          RedirectAttributes redirectAttributes) {

    try {
      Curso curso = cursoService.obtenerPorId(idCurso);

      model.addAttribute("curso", curso);
      prepararFormularioEdicion(model);

      return "admin/curso-form";

    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/admin/cursos";
    }
  }

  @PostMapping("/admin/cursos/editar/{idCurso}")
  public String editar(
          @PathVariable Integer idCurso,
          @ModelAttribute("curso") Curso cursoFormulario,
          BindingResult bindingResult,
          @RequestParam(name = "imagenArchivo", required = false) MultipartFile imagenArchivo,
          Model model,
          RedirectAttributes redirectAttributes) {

    Curso existente;
    try {
      existente = cursoService.obtenerPorId(idCurso);
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/admin/cursos";
    }

    cursoFormulario.setIdCurso(idCurso);
    cursoFormulario.setImagen(existente.getImagen());
    validarDatosCurso(cursoFormulario, bindingResult);

    if (bindingResult.hasErrors()) {
      prepararFormularioEdicion(model);
      return "admin/curso-form";
    }

    ImagenGuardada imagenGuardada = null;

    try {
      if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
        imagenGuardada = guardarImagen(imagenArchivo);
      }
      cursoService.actualizar(idCurso, cursoFormulario,
          imagenGuardada == null ? null : imagenGuardada.rutaPublica());

      redirectAttributes.addFlashAttribute(
              "mensajeExito",
              "Curso actualizado correctamente.");

      return "redirect:/admin/cursos";

    } catch (DataIntegrityViolationException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", "No se pudo guardar el curso. Verifica que el código no esté repetido.");
    } catch (IOException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", "No se pudo guardar la imagen. Inténtalo nuevamente.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", e.getMessage());
    } catch (RuntimeException e) {
      eliminarImagenSiExiste(imagenGuardada);
      model.addAttribute("errorGuardado", "No se pudo guardar el curso. Inténtalo nuevamente.");
    }
    prepararFormularioEdicion(model);
    return "admin/curso-form";
  }

  private void prepararFormularioEdicion(Model model) {
    model.addAttribute("activeAdmin", "cursos");
    model.addAttribute("tituloAdmin", "Editar curso");
    model.addAttribute("tiposCurso", cursoService.obtenerTiposCurso());
    model.addAttribute("docentes", cursoService.obtenerDocentes());
  }

  @PostMapping("/admin/cursos/eliminar/{idCurso}")
  public String eliminar(
      @PathVariable Integer idCurso,
      RedirectAttributes redirectAttributes) {

    try {
      cursoService.desactivar(idCurso);
      redirectAttributes.addFlashAttribute("mensajeExito", "Curso desactivado correctamente.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/cursos";
  }

  @PostMapping("/admin/cursos/reactivar/{idCurso}")
  public String reactivar(
      @PathVariable Integer idCurso,
      RedirectAttributes redirectAttributes) {
    try {
      cursoService.reactivar(idCurso);
      redirectAttributes.addFlashAttribute("mensajeExito", "Curso reactivado correctamente.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/cursos";
  }
}
