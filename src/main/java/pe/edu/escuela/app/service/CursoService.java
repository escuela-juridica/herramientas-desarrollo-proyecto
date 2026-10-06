package pe.edu.escuela.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.model.Docente;
import pe.edu.escuela.app.model.TipoCurso;
import pe.edu.escuela.app.repository.AdministradorRepository;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.repository.DocenteRepository;
import pe.edu.escuela.app.repository.TipoCursoRepository;
import pe.edu.escuela.app.util.Constantes;

@Service
public class CursoService {

  private final CursoRepository cursoRepository;
  private final TipoCursoRepository tipoCursoRepository;
  private final DocenteRepository docenteRepository;
  private final AdministradorRepository administradorRepository;

  public CursoService(
      CursoRepository cursoRepository,
      TipoCursoRepository tipoCursoRepository,
      DocenteRepository docenteRepository,
      AdministradorRepository administradorRepository) {
    this.cursoRepository = cursoRepository;
    this.tipoCursoRepository = tipoCursoRepository;
    this.docenteRepository = docenteRepository;
    this.administradorRepository = administradorRepository;
  }

  public List<Curso> obtenerDestacados() {
    return cursoRepository.findByDestacadoTrueAndEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }

  public List<Curso> obtenerActivos() {
    return cursoRepository.findByEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }

  public List<Curso> listarParaAdministracion(String busqueda) {
    if (busqueda == null || busqueda.isBlank()) {
      return cursoRepository.findAllByOrderByIdCursoAsc();
    }
    return cursoRepository.findByNombreContainingIgnoreCaseOrderByIdCursoAsc(busqueda.trim());
  }

  public Curso obtenerPorId(Integer idCurso) {
    return cursoRepository.findById(idCurso)
        .orElseThrow(() -> new IllegalArgumentException("No existe el curso solicitado."));
  }

  public List<TipoCurso> obtenerTiposCurso() {
    return tipoCursoRepository.findAll();
  }

  public List<Docente> obtenerDocentes() {
    return docenteRepository.findAll();
  }

  public Curso guardarNuevo(Curso curso) {
    Integer idTipoCurso = curso.getTipoCurso() == null
        ? null
        : curso.getTipoCurso().getIdTipoCurso();
    Integer idDocente = curso.getDocente() == null
        ? null
        : curso.getDocente().getIdDocente();

    if (idTipoCurso == null || idDocente == null) {
      throw new IllegalArgumentException("Debes seleccionar un tipo de curso y un docente.");
    }

    curso.setIdCurso(null);
    curso.setTipoCurso(tipoCursoRepository.findById(idTipoCurso)
        .orElseThrow(() -> new IllegalArgumentException("El tipo de curso seleccionado no existe.")));
    curso.setDocente(docenteRepository.findById(idDocente)
        .orElseThrow(() -> new IllegalArgumentException("El docente seleccionado no existe.")));
    curso.setAdministrador(administradorRepository.findFirstByOrderByIdAdminAsc()
        .orElseThrow(() -> new IllegalStateException(
            "No existe un administrador para registrar el curso.")));
    curso.setEstado(Constantes.ESTADO_ACTIVO);

    return cursoRepository.save(curso);
  }
}
