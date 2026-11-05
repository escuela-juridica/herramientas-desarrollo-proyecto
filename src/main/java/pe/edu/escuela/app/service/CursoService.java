package pe.edu.escuela.app.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.model.Docente;
import pe.edu.escuela.app.model.TematicaOferta;
import pe.edu.escuela.app.model.TipoCurso;
import pe.edu.escuela.app.repository.AdministradorRepository;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.repository.DocenteRepository;
import pe.edu.escuela.app.repository.TematicaOfertaRepository;
import pe.edu.escuela.app.repository.TipoCursoRepository;
import pe.edu.escuela.app.util.Constantes;

@Service
public class CursoService {

  private final CursoRepository cursoRepository;
  private final TipoCursoRepository tipoCursoRepository;
  private final DocenteRepository docenteRepository;
  private final AdministradorRepository administradorRepository;
  private final TematicaOfertaRepository tematicaOfertaRepository;

  public CursoService(
      CursoRepository cursoRepository,
      TipoCursoRepository tipoCursoRepository,
      DocenteRepository docenteRepository,
      AdministradorRepository administradorRepository,
      TematicaOfertaRepository tematicaOfertaRepository) {
    this.cursoRepository = cursoRepository;
    this.tipoCursoRepository = tipoCursoRepository;
    this.docenteRepository = docenteRepository;
    this.administradorRepository = administradorRepository;
    this.tematicaOfertaRepository = tematicaOfertaRepository;
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

  public List<Curso> buscarEnCatalogo(String texto, String codigo, Integer idTipoCurso) {
    String nombre = (texto == null || texto.isBlank()) ? null : texto.trim();
    String cod = (codigo == null || codigo.isBlank()) ? null : codigo.trim();
    return cursoRepository.buscarConFiltros(nombre, cod, idTipoCurso, Constantes.ESTADO_ACTIVO);
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

  public List<TematicaOferta> obtenerTematicasOferta() {
    return tematicaOfertaRepository.findByEstadoOrderByIdTematicaOfertaAsc(Constantes.ESTADO_ACTIVO);
  }

  /**
   * Deja la oferta del curso consistente. Sin descuento no hay tematica ni fecha limite; con descuento se exigen un
   * porcentaje de 10 a 50 %, la tematica y la fecha limite.
   */
  private void aplicarOferta(Curso destino, Curso datos) {
    BigDecimal descuento = datos.getDescuentoPorcentaje();
    if (descuento == null || descuento.signum() == 0) {
      destino.setDescuentoPorcentaje(BigDecimal.ZERO);
      destino.setTematicaOferta(null);
      destino.setOfertaHasta(null);
      return;
    }
    if (descuento.compareTo(BigDecimal.TEN) < 0 || descuento.compareTo(BigDecimal.valueOf(50)) > 0) {
      throw new IllegalArgumentException("El descuento de la oferta debe estar entre 10 y 50 %.");
    }
    if (datos.getPrecio() == null || datos.getPrecio().signum() <= 0) {
      throw new IllegalArgumentException("Para activar una oferta el curso debe tener un precio mayor a S/ 0.");
    }
    Integer idTematica = datos.getTematicaOferta() == null ? null : datos.getTematicaOferta().getIdTematicaOferta();
    if (idTematica == null) {
      throw new IllegalArgumentException("Selecciona la temática de la oferta.");
    }
    if (datos.getOfertaHasta() == null) {
      throw new IllegalArgumentException("Indica hasta cuándo es válida la oferta.");
    }
    destino.setDescuentoPorcentaje(descuento);
    destino.setTematicaOferta(tematicaOfertaRepository.findById(idTematica)
        .orElseThrow(() -> new IllegalArgumentException("La temática seleccionada no existe.")));
    destino.setOfertaHasta(datos.getOfertaHasta());
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
    aplicarOferta(curso, curso);

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
    aplicarOferta(existente, datos);
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
