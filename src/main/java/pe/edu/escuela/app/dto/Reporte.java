package pe.edu.escuela.app.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Un reporte del panel: los datos de un grafico estadistico, la descripcion de lo que muestra
 * y la conclusion que se desprende de los numeros. Se usa tanto en pantalla como en los informes descargables.
 */
public class Reporte {

  private final int numero;
  private final String titulo;
  private final String tipoGrafico;
  private final boolean horizontal;
  private final String columnaEtiqueta;
  private final String columnaValor;
  private final boolean monetario;
  private final List<String> etiquetas;
  private final List<BigDecimal> valores;
  private final String descripcion;
  private final String conclusion;

  public Reporte(
      int numero,
      String titulo,
      String tipoGrafico,
      boolean horizontal,
      String columnaEtiqueta,
      String columnaValor,
      boolean monetario,
      List<String> etiquetas,
      List<BigDecimal> valores,
      String descripcion,
      String conclusion) {
    this.numero = numero;
    this.titulo = titulo;
    this.tipoGrafico = tipoGrafico;
    this.horizontal = horizontal;
    this.columnaEtiqueta = columnaEtiqueta;
    this.columnaValor = columnaValor;
    this.monetario = monetario;
    this.etiquetas = etiquetas;
    this.valores = valores;
    this.descripcion = descripcion;
    this.conclusion = conclusion;
  }

  public BigDecimal getTotal() {
    return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public boolean isSinDatos() {
    return getTotal().signum() == 0;
  }

  public int getNumero() {
    return numero;
  }

  public String getTitulo() {
    return titulo;
  }

  /** Tipo de Chart.js: line, bar, doughnut o pie. */
  public String getTipoGrafico() {
    return tipoGrafico;
  }

  public boolean isHorizontal() {
    return horizontal;
  }

  public String getColumnaEtiqueta() {
    return columnaEtiqueta;
  }

  public String getColumnaValor() {
    return columnaValor;
  }

  /** True si los valores son soles; false si son cantidades. */
  public boolean isMonetario() {
    return monetario;
  }

  public List<String> getEtiquetas() {
    return etiquetas;
  }

  public List<BigDecimal> getValores() {
    return valores;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getConclusion() {
    return conclusion;
  }
}
