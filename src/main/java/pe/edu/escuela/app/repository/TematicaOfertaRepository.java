package pe.edu.escuela.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.TematicaOferta;

public interface TematicaOfertaRepository extends JpaRepository<TematicaOferta, Integer> {

  List<TematicaOferta> findByEstadoOrderByIdTematicaOfertaAsc(String estado);
}
