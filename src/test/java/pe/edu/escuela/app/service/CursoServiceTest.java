package pe.edu.escuela.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.escuela.app.model.Administrador;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.model.Docente;
import pe.edu.escuela.app.model.TipoCurso;
import pe.edu.escuela.app.repository.AdministradorRepository;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.repository.DocenteRepository;
import pe.edu.escuela.app.repository.TipoCursoRepository;
import pe.edu.escuela.app.util.Constantes;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

  @Mock
  private CursoRepository cursoRepository;

  @Mock
  private TipoCursoRepository tipoCursoRepository;

  @Mock
  private DocenteRepository docenteRepository;

  @Mock
  private AdministradorRepository administradorRepository;

  @InjectMocks
  private CursoService cursoService;

  @Test
  void listarParaAdministracionSinTextoDevuelveTodosLosCursos() {
    List<Curso> cursos = List.of(new Curso(), new Curso());
    when(cursoRepository.findAllByOrderByIdCursoAsc()).thenReturn(cursos);

    assertThat(cursoService.listarParaAdministracion("  ")).isSameAs(cursos);
    verify(cursoRepository).findAllByOrderByIdCursoAsc();
  }

  @Test
  void listarParaAdministracionConTextoBuscaElNombreLimpio() {
    List<Curso> cursos = List.of(new Curso());
    when(cursoRepository.findByNombreContainingIgnoreCaseOrderByIdCursoAsc("Civil"))
        .thenReturn(cursos);

    assertThat(cursoService.listarParaAdministracion("  Civil  ")).isSameAs(cursos);
    verify(cursoRepository).findByNombreContainingIgnoreCaseOrderByIdCursoAsc("Civil");
  }

  @Test
  void buscarEnCatalogoConParametrosLimpiaEspaciosYFiltraConEstadoActivo() {
    List<Curso> cursos = List.of(new Curso());
    when(cursoRepository.buscarConFiltros("Civil", "Notarial", 2, Constantes.ESTADO_ACTIVO))
        .thenReturn(cursos);

    List<Curso> resultado = cursoService.buscarEnCatalogo("  Civil  ", "  Notarial  ", 2);

    assertThat(resultado).isSameAs(cursos);
    verify(cursoRepository).buscarConFiltros("Civil", "Notarial", 2, Constantes.ESTADO_ACTIVO);
  }

  @Test
  void buscarEnCatalogoConValoresVaciosEnviaParametrosNulos() {
    List<Curso> cursos = List.of(new Curso());
    when(cursoRepository.buscarConFiltros(null, null, null, Constantes.ESTADO_ACTIVO))
        .thenReturn(cursos);

    List<Curso> resultado = cursoService.buscarEnCatalogo("   ", "", null);

    assertThat(resultado).isSameAs(cursos);
    verify(cursoRepository).buscarConFiltros(null, null, null, Constantes.ESTADO_ACTIVO);
  }

  @Test
  void buscarEnCatalogoSobrecargadoDosParametrosInvocaConDescripcionNula() {
    List<Curso> cursos = List.of(new Curso());
    when(cursoRepository.buscarConFiltros("Civil", null, 1, Constantes.ESTADO_ACTIVO))
        .thenReturn(cursos);

    List<Curso> resultado = cursoService.buscarEnCatalogo("Civil", 1);

    assertThat(resultado).isSameAs(cursos);
    verify(cursoRepository).buscarConFiltros("Civil", null, 1, Constantes.ESTADO_ACTIVO);
  }

  @Test
  void obtenerPorIdInexistenteInformaElProblema() {
    when(cursoRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cursoService.obtenerPorId(99))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No existe el curso solicitado.");
  }

  @Test
  void guardarNuevoResuelveRelacionesYAsignaElPrimerAdministrador() {
    TipoCurso tipoRecibido = new TipoCurso();
    tipoRecibido.setIdTipoCurso(2);
    Docente docenteRecibido = new Docente();
    docenteRecibido.setIdDocente(5);
    Curso curso = new Curso();
    curso.setIdCurso(80);
    curso.setTipoCurso(tipoRecibido);
    curso.setDocente(docenteRecibido);

    TipoCurso tipoGuardado = new TipoCurso();
    tipoGuardado.setIdTipoCurso(2);
    Docente docenteGuardado = new Docente();
    docenteGuardado.setIdDocente(5);
    Administrador administrador = new Administrador();
    administrador.setIdAdmin(1);

    when(tipoCursoRepository.findById(2)).thenReturn(Optional.of(tipoGuardado));
    when(docenteRepository.findById(5)).thenReturn(Optional.of(docenteGuardado));
    when(administradorRepository.findFirstByOrderByIdAdminAsc())
        .thenReturn(Optional.of(administrador));
    when(cursoRepository.save(curso)).thenReturn(curso);

    Curso resultado = cursoService.guardarNuevo(curso);

    assertThat(resultado.getIdCurso()).isNull();
    assertThat(resultado.getTipoCurso()).isSameAs(tipoGuardado);
    assertThat(resultado.getDocente()).isSameAs(docenteGuardado);
    assertThat(resultado.getAdministrador()).isSameAs(administrador);
    assertThat(resultado.getEstado()).isEqualTo(Constantes.ESTADO_ACTIVO);
  }
}
