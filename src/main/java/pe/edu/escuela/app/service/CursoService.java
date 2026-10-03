package pe.edu.escuela.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.util.Constantes;

@Service
public class CursoService {

  private final CursoRepository cursoRepository;

  public CursoService(CursoRepository cursoRepository) {
    this.cursoRepository = cursoRepository;
  }

  public List<Curso> obtenerDestacados() {
    return cursoRepository.findByDestacadoTrueAndEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }

  public List<Curso> obtenerActivos() {
    return cursoRepository.findByEstadoOrderByIdCursoAsc(Constantes.ESTADO_ACTIVO);
  }
}
