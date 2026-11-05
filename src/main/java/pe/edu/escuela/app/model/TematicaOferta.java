package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Temporada o motivo de una oferta (Navidad, Fiestas Patrias, Black Friday...). Estado: A = activa, I = inactiva. */
@Entity
@Table(name = "tematica_oferta")
public class TematicaOferta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_tematica_oferta")
  private Integer idTematicaOferta;

  @Column(name = "nombre", nullable = false, unique = true, length = 60)
  private String nombre;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "estado", nullable = false, length = 1)
  private String estado = "A";

  public Integer getIdTematicaOferta() {
    return idTematicaOferta;
  }

  public void setIdTematicaOferta(Integer idTematicaOferta) {
    this.idTematicaOferta = idTematicaOferta;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
}
