package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_curso")
public class TipoCurso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_tipo_curso")
  private Integer idTipoCurso;

  @Column(name = "nombre", nullable = false, unique = true, length = 100)
  private String nombre;

  @Column(name = "descripcion", length = 300)
  private String descripcion;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_modificacion")
  private LocalDateTime fechaModificacion;

  @PrePersist
  protected void alCrear() {
    this.fechaCreacion = LocalDateTime.now();
  }

  @PreUpdate
  protected void alActualizar() {
    this.fechaModificacion = LocalDateTime.now();
  }

  public Integer getIdTipoCurso() {
    return idTipoCurso;
  }

  public void setIdTipoCurso(Integer idTipoCurso) {
    this.idTipoCurso = idTipoCurso;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public LocalDateTime getFechaModificacion() {
    return fechaModificacion;
  }
}
