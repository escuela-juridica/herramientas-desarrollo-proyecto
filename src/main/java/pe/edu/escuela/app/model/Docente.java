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
@Table(name = "docente")
public class Docente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_docente")
  private Integer idDocente;

  @Column(name = "nombres", nullable = false, length = 100)
  private String nombres;

  @Column(name = "apellidos", nullable = false, length = 100)
  private String apellidos;

  @Column(name = "especialidad", length = 100)
  private String especialidad;

  @Column(name = "cargo", length = 100)
  private String cargo;

  @Column(name = "estado", nullable = false, columnDefinition = "CHAR(1)")
  private String estado = "A";

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

  public Integer getIdDocente() {
    return idDocente;
  }

  public void setIdDocente(Integer idDocente) {
    this.idDocente = idDocente;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getEspecialidad() {
    return especialidad;
  }

  public void setEspecialidad(String especialidad) {
    this.especialidad = especialidad;
  }

  public String getCargo() {
    return cargo;
  }

  public void setCargo(String cargo) {
    this.cargo = cargo;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public LocalDateTime getFechaModificacion() {
    return fechaModificacion;
  }
}
