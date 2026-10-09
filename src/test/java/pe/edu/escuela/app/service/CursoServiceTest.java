package pe.edu.escuela.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
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
  void buscarParaAdministracionPorCodigoUsaElCodigoLimpio() {
    List<Curso> cursos = List.of(new Curso());
    when(cursoRepository.findByCodigoContainingIgnoreCaseOrderByIdCursoAsc("EJ-2026"))
        .thenReturn(cursos);

    assertThat(cursoService.buscarParaAdministracionPorCodigo("  EJ-2026  "))
        .isSameAs(cursos);
    verify(cursoRepository).findByCodigoContainingIgnoreCaseOrderByIdCursoAsc("EJ-2026");
  }

  @Test
  void filtrarParaAdministracionCombinaNombreYCodigo() {
    List<Curso> cursos = List.of(new Curso());
    when(cursoRepository
        .findByNombreContainingIgnoreCaseAndCodigoContainingIgnoreCaseOrderByIdCursoAsc(
            "Civil", "EJ-2026"))
        .thenReturn(cursos);

    assertThat(cursoService.filtrarParaAdministracion("  Civil  ", "  EJ-2026  "))
        .isSameAs(cursos);
  }

  @Test
  void filtrarParaAdministracionConUnSoloCriterioUsaEseCriterio() {
    when(cursoRepository.findByNombreContainingIgnoreCaseOrderByIdCursoAsc("Civil"))
        .thenReturn(List.of(new Curso()));
    when(cursoRepository.findByCodigoContainingIgnoreCaseOrderByIdCursoAsc("EJ-2026"))
        .thenReturn(List.of(new Curso()));

    assertThat(cursoService.filtrarParaAdministracion("Civil", " ")).hasSize(1);
    assertThat(cursoService.filtrarParaAdministracion(" ", "EJ-2026")).hasSize(1);
  }

  @Test
  void obtenerPorIdInexistenteInformaElProblema() {
    when(cursoRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cursoService.obtenerPorId(99))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No existe el curso solicitado.");
  }

  @Test
  void guardarNuevoResuelveRelacionesYAsignaElAdministradorAutenticado() {
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
    when(administradorRepository.findByCorreo("admin@escuelajuridica.edu.pe"))
        .thenReturn(Optional.of(administrador));
    when(cursoRepository.save(curso)).thenReturn(curso);

    Curso resultado = cursoService.guardarNuevo(curso, "admin@escuelajuridica.edu.pe");

    assertThat(resultado.getIdCurso()).isNull();
    assertThat(resultado.getTipoCurso()).isSameAs(tipoGuardado);
    assertThat(resultado.getDocente()).isSameAs(docenteGuardado);
    assertThat(resultado.getAdministrador()).isSameAs(administrador);
    assertThat(resultado.getEstado()).isEqualTo(Constantes.ESTADO_ACTIVO);
  }

  @Test
  void actualizarGuardaTodosLosCamposEditablesSinCambiarEstadoNiAdministrador() {
    Curso existente = new Curso();
    existente.setIdCurso(7);
    existente.setEstado(Constantes.ESTADO_INACTIVO);
    existente.setImagen("/uploads/cursos/original.jpg");
    Administrador administrador = new Administrador();
    existente.setAdministrador(administrador);

    TipoCurso tipoElegido = new TipoCurso();
    tipoElegido.setIdTipoCurso(2);
    Docente docenteElegido = new Docente();
    docenteElegido.setIdDocente(3);
    Curso datos = new Curso();
    datos.setCodigo("EJ-2026-007");
    datos.setNombre("Derecho civil");
    datos.setDescripcion("Descripción actualizada");
    datos.setInstitucion("UTP");
    datos.setModalidad("P");
    datos.setDuracionHoras(48);
    datos.setCupos(25);
    datos.setFechaInicio(LocalDate.of(2026, 11, 1));
    datos.setFechaFin(LocalDate.of(2026, 12, 1));
    datos.setPrecio(new BigDecimal("150.00"));
    datos.setDestacado(true);
    datos.setTipoCurso(tipoElegido);
    datos.setDocente(docenteElegido);

    TipoCurso tipoPersistido = new TipoCurso();
    Docente docentePersistido = new Docente();
    when(cursoRepository.findById(7)).thenReturn(Optional.of(existente));
    when(tipoCursoRepository.findById(2)).thenReturn(Optional.of(tipoPersistido));
    when(docenteRepository.findById(3)).thenReturn(Optional.of(docentePersistido));
    when(cursoRepository.saveAndFlush(existente)).thenReturn(existente);

    Curso actualizado = cursoService.actualizar(7, datos, null);

    assertThat(actualizado).isSameAs(existente);
    assertThat(actualizado.getCodigo()).isEqualTo(datos.getCodigo());
    assertThat(actualizado.getNombre()).isEqualTo(datos.getNombre());
    assertThat(actualizado.getDescripcion()).isEqualTo(datos.getDescripcion());
    assertThat(actualizado.getInstitucion()).isEqualTo(datos.getInstitucion());
    assertThat(actualizado.getModalidad()).isEqualTo(datos.getModalidad());
    assertThat(actualizado.getDuracionHoras()).isEqualTo(datos.getDuracionHoras());
    assertThat(actualizado.getCupos()).isEqualTo(datos.getCupos());
    assertThat(actualizado.getFechaInicio()).isEqualTo(datos.getFechaInicio());
    assertThat(actualizado.getFechaFin()).isEqualTo(datos.getFechaFin());
    assertThat(actualizado.getPrecio()).isEqualTo(datos.getPrecio());
    assertThat(actualizado.isDestacado()).isTrue();
    assertThat(actualizado.getTipoCurso()).isSameAs(tipoPersistido);
    assertThat(actualizado.getDocente()).isSameAs(docentePersistido);
    assertThat(actualizado.getEstado()).isEqualTo(Constantes.ESTADO_INACTIVO);
    assertThat(actualizado.getAdministrador()).isSameAs(administrador);
    assertThat(actualizado.getImagen()).isEqualTo("/uploads/cursos/original.jpg");
    verify(cursoRepository).saveAndFlush(existente);
  }

  @Test
  void desactivarConservaElCursoYMarcaSuEstadoInactivo() {
    Curso curso = new Curso();
    when(cursoRepository.findById(7)).thenReturn(Optional.of(curso));

    cursoService.desactivar(7);

    assertThat(curso.getEstado()).isEqualTo(Constantes.ESTADO_INACTIVO);
    verify(cursoRepository).save(curso);
  }

  @Test
  void reactivarConservaElCursoYLoDevuelveAlCatalogo() {
    Curso curso = new Curso();
    curso.setEstado(Constantes.ESTADO_INACTIVO);
    when(cursoRepository.findById(7)).thenReturn(Optional.of(curso));

    cursoService.reactivar(7);

    assertThat(curso.getEstado()).isEqualTo(Constantes.ESTADO_ACTIVO);
    verify(cursoRepository).save(curso);
  }
}
