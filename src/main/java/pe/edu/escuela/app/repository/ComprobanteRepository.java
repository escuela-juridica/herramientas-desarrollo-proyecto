package pe.edu.escuela.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.escuela.app.model.Comprobante;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

  @Query("SELECT c FROM Comprobante c JOIN FETCH c.pedido p WHERE c.codigoAcceso = :codigo")
  Optional<Comprobante> buscarPorCodigoAcceso(@Param("codigo") String codigo);
}
