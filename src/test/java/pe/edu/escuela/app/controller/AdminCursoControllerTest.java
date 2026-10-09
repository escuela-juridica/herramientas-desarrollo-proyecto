package pe.edu.escuela.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.model.Docente;
import pe.edu.escuela.app.model.TipoCurso;
import pe.edu.escuela.app.service.CursoService;

@ExtendWith(MockitoExtension.class)
class AdminCursoControllerTest {

  private static final String CORREO_ADMIN = "admin@escuelajuridica.edu.pe";

  @Mock
  private CursoService cursoService;

  @Mock
  private Authentication authentication;

  @TempDir
  private Path uploadDir;

  @Test
  void crearGuardaLaImagenConNombreUnicoYAsignaSuRutaPublica() throws Exception {
    AdminCursoController controller =
        new AdminCursoController(cursoService, uploadDir.toString());
    Curso curso = crearCursoValido();
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(curso, "curso");
    MockMultipartFile imagen = new MockMultipartFile(
        "imagenArchivo", "portada.JPG", "image/jpeg", new byte[] {1, 2, 3});
    when(authentication.getName()).thenReturn(CORREO_ADMIN);

    String vista = controller.crear(
        curso,
        bindingResult,
        imagen,
        new ExtendedModelMap(),
        new RedirectAttributesModelMap(),
        authentication);

    assertThat(vista).isEqualTo("redirect:/admin/cursos");
    assertThat(curso.getImagen())
        .startsWith("/uploads/cursos/")
        .endsWith(".jpg");
    String nombreGuardado = curso.getImagen().substring("/uploads/cursos/".length());
    assertThat(Files.readAllBytes(uploadDir.resolve(nombreGuardado)))
        .containsExactly(1, 2, 3);
    verify(cursoService).guardarNuevo(curso, CORREO_ADMIN);
  }

  @Test
  void crearEliminaLaImagenSiElCursoNoPuedeGuardarse() throws Exception {
    AdminCursoController controller =
        new AdminCursoController(cursoService, uploadDir.toString());
    Curso curso = crearCursoValido();
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(curso, "curso");
    MockMultipartFile imagen = new MockMultipartFile(
        "imagenArchivo", "portada.png", "image/png", new byte[] {4, 5, 6});
    when(authentication.getName()).thenReturn(CORREO_ADMIN);
    doThrow(new IllegalStateException("Base no disponible"))
        .when(cursoService).guardarNuevo(curso, CORREO_ADMIN);

    String vista = controller.crear(
        curso,
        bindingResult,
        imagen,
        new ExtendedModelMap(),
        new RedirectAttributesModelMap(),
        authentication);

    assertThat(vista).isEqualTo("admin/curso-form");
    try (var archivos = Files.list(uploadDir)) {
      assertThat(archivos).isEmpty();
    }
  }

  private Curso crearCursoValido() {
    TipoCurso tipoCurso = new TipoCurso();
    tipoCurso.setIdTipoCurso(1);
    Docente docente = new Docente();
    docente.setIdDocente(1);

    Curso curso = new Curso();
    curso.setCodigo("EJ-TEST-001");
    curso.setNombre("Curso de prueba");
    curso.setDescripcion("Descripción válida para probar la carga de imagen.");
    curso.setPrecio(new BigDecimal("100.00"));
    curso.setTipoCurso(tipoCurso);
    curso.setDocente(docente);
    return curso;
  }
}
