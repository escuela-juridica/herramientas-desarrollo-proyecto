package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "curso")
public class Curso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_curso")
  private Integer idCurso;

  @Column(name = "codigo", nullable = false, unique = true, length = 20)
  private String codigo;

  @Column(name = "nombre", nullable = false, length = 150)
  private String nombre;

  @Column(name = "descripcion", nullable = false, length = 500)
  private String descripcion;

  @Column(name = "institucion", length = 100)
  private String institucion = "Escuela Jurídica";

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "modalidad", length = 1)
  private String modalidad;

  @Column(name = "duracion_horas")
  private Integer duracionHoras;

  @Column(name = "cupos")
  private Integer cupos;

  @Column(name = "fecha_inicio", nullable = false)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @Column(name = "fecha_fin", nullable = false)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  @Column(name = "precio", nullable = false, precision = 10, scale = 2)
  private BigDecimal precio;

  @Column(name = "descuento_porcentaje", nullable = false, precision = 5, scale = 2)
  private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;

  @Column(name = "oferta_hasta")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate ofertaHasta;

  @Column(name = "imagen", nullable = false, length = 255)
  private String imagen;

  @Column(name = "destacado", nullable = false)
  private boolean destacado = false;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "estado", nullable = false, length = 1)
  private String estado = "A";

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_modificacion")
  private LocalDateTime fechaModificacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_tematica_oferta")
  private TematicaOferta tematicaOferta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_tipo_curso", nullable = false)
  private TipoCurso tipoCurso;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_admin", nullable = false)
  private Administrador administrador;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_docente", nullable = false)
  private Docente docente;

  @PrePersist
  protected void alCrear() {
    this.fechaCreacion = LocalDateTime.now();
  }

  @PreUpdate
  protected void alActualizar() {
    this.fechaModificacion = LocalDateTime.now();
  }

  public Integer getIdCurso() {
    return idCurso;
  }

  public void setIdCurso(Integer idCurso) {
    this.idCurso = idCurso;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
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

  public String getInstitucion() {
    return institucion;
  }

  public void setInstitucion(String institucion) {
    this.institucion = institucion;
  }

  public String getModalidad() {
    return modalidad;
  }

  public void setModalidad(String modalidad) {
    this.modalidad = modalidad;
  }

  public Integer getDuracionHoras() {
    return duracionHoras;
  }

  public void setDuracionHoras(Integer duracionHoras) {
    this.duracionHoras = duracionHoras;
  }

  public Integer getCupos() {
    return cupos;
  }

  public void setCupos(Integer cupos) {
    this.cupos = cupos;
  }

  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  public BigDecimal getDescuentoPorcentaje() {
    return descuentoPorcentaje;
  }

  public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
    this.descuentoPorcentaje = descuentoPorcentaje == null ? BigDecimal.ZERO : descuentoPorcentaje;
  }

  public LocalDate getOfertaHasta() {
    return ofertaHasta;
  }

  public void setOfertaHasta(LocalDate ofertaHasta) {
    this.ofertaHasta = ofertaHasta;
  }

  public TematicaOferta getTematicaOferta() {
    return tematicaOferta;
  }

  public void setTematicaOferta(TematicaOferta tematicaOferta) {
    this.tematicaOferta = tematicaOferta;
  }

  /** Nombre de la temporada de la oferta (por ejemplo Navidad), o null si el curso no tiene tematica. */
  public String getNombreTematica() {
    return tematicaOferta == null ? null : tematicaOferta.getNombre();
  }

  /** Un curso esta en oferta si tiene descuento y la fecha limite (si existe) no ha pasado. */
  public boolean isEnOferta() {
    return descuentoPorcentaje != null
        && descuentoPorcentaje.signum() > 0
        && (ofertaHasta == null || !ofertaHasta.isBefore(LocalDate.now()));
  }

  /** Descuento que se aplica hoy: el configurado si hay oferta vigente, cero si no. */
  public BigDecimal getDescuentoVigente() {
    return isEnOferta() ? descuentoPorcentaje : BigDecimal.ZERO;
  }

  /** Precio que paga el cliente hoy (precio de lista menos el descuento vigente). */
  public BigDecimal getPrecioFinal() {
    if (!isEnOferta()) {
      return precio;
    }
    return precio.multiply(BigDecimal.valueOf(100).subtract(descuentoPorcentaje))
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  public String getImagen() {
    return imagen;
  }

  public void setImagen(String imagen) {
    this.imagen = imagen;
  }

  public boolean isDestacado() {
    return destacado;
  }

  public void setDestacado(boolean destacado) {
    this.destacado = destacado;
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

  public TipoCurso getTipoCurso() {
    return tipoCurso;
  }

  public void setTipoCurso(TipoCurso tipoCurso) {
    this.tipoCurso = tipoCurso;
  }

  public Administrador getAdministrador() {
    return administrador;
  }

  public void setAdministrador(Administrador administrador) {
    this.administrador = administrador;
  }

  public Docente getDocente() {
    return docente;
  }

  public void setDocente(Docente docente) {
    this.docente = docente;
  }
}
