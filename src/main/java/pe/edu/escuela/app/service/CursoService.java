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

  public List<Curso> buscarEnCatalogo(String texto, Integer idTipoCurso) {
    return buscarEnCatalogo(texto, null, idTipoCurso);
  }

  public List<Curso> buscarEnCatalogo(String texto, String descripcion, Integer idTipoCurso) {
    String nombre = (texto == null || texto.isBlank()) ? null : texto.trim();
    String desc = (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim();
    return cursoRepository.buscarConFiltros(nombre, desc, idTipoCurso, Constantes.ESTADO_ACTIVO);
  }

  public List<Curso> listarParaAdministracion(String busqueda) {
    if (busqueda == null || busqueda.isBlank()) {
      return cursoRepository.findAllByOrderByIdCursoAsc();
    }
    return cursoRepository.findByNombreContainingIgnoreCaseOrderByIdCursoAsc(busqueda.trim());
  }

  public List<Curso> buscarParaAdministracionPorCodigo(String codigo) {
    if (codigo == null || codigo.isBlank()) {
      return cursoRepository.findAllByOrderByIdCursoAsc();
    }
    return cursoRepository.findByCodigoContainingIgnoreCaseOrderByIdCursoAsc(codigo.trim());
  }

  public List<Curso> filtrarParaAdministracion(String nombre, String codigo) {
    String nombreLimpio = nombre == null ? "" : nombre.trim();
    String codigoLimpio = codigo == null ? "" : codigo.trim();
    if (nombreLimpio.isEmpty()) {
      return buscarParaAdministracionPorCodigo(codigoLimpio);
    }
    if (codigoLimpio.isEmpty()) {
      return listarParaAdministracion(nombreLimpio);
    }
    return cursoRepository
        .findByNombreContainingIgnoreCaseAndCodigoContainingIgnoreCaseOrderByIdCursoAsc(
            nombreLimpio, codigoLimpio);
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

  public Curso guardarNuevo(Curso curso, String correoAdmin) {
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
    curso.setAdministrador(administradorRepository.findByCorreo(correoAdmin)
        .orElseThrow(() -> new IllegalStateException(
            "No se encontró el administrador autenticado.")));
    curso.setEstado(Constantes.ESTADO_ACTIVO);

    return cursoRepository.save(curso);
  }

  public Curso actualizar(Integer idCurso, Curso datos, String nuevaImagen) {
    Curso existente = obtenerPorId(idCurso);
    Integer idTipo = datos.getTipoCurso() == null ? null : datos.getTipoCurso().getIdTipoCurso();
    Integer idDocente = datos.getDocente() == null ? null : datos.getDocente().getIdDocente();
    if (idTipo == null || idDocente == null) {
      throw new IllegalArgumentException("Debes seleccionar un tipo de curso y un docente.");
    }

    TipoCurso tipo = tipoCursoRepository.findById(idTipo)
        .orElseThrow(() -> new IllegalArgumentException("El tipo de curso seleccionado no existe."));
    Docente docente = docenteRepository.findById(idDocente)
        .orElseThrow(() -> new IllegalArgumentException("El docente seleccionado no existe."));

    existente.setCodigo(datos.getCodigo());
    existente.setNombre(datos.getNombre());
    existente.setDescripcion(datos.getDescripcion());
    existente.setInstitucion(datos.getInstitucion());
    existente.setModalidad(datos.getModalidad());
    existente.setDuracionHoras(datos.getDuracionHoras());
    existente.setCupos(datos.getCupos());
    existente.setFechaInicio(datos.getFechaInicio());
    existente.setFechaFin(datos.getFechaFin());
    existente.setPrecio(datos.getPrecio());
    existente.setDestacado(datos.isDestacado());
    existente.setTipoCurso(tipo);
    existente.setDocente(docente);
    if (nuevaImagen != null) {
      existente.setImagen(nuevaImagen);
    }

    return cursoRepository.saveAndFlush(existente);
  }

  public void desactivar(Integer idCurso) {
    Curso curso = obtenerPorId(idCurso);
    curso.setEstado(Constantes.ESTADO_INACTIVO);
    cursoRepository.save(curso);
  }

  public void reactivar(Integer idCurso) {
    Curso curso = obtenerPorId(idCurso);
    curso.setEstado(Constantes.ESTADO_ACTIVO);
    cursoRepository.save(curso);
  }
}
