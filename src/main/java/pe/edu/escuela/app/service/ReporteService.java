package pe.edu.escuela.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.escuela.app.dto.Reporte;
import pe.edu.escuela.app.repository.PedidoDetalleRepository;
import pe.edu.escuela.app.repository.PedidoRepository;

/**
 * Arma los 6 reportes estadisticos del panel a partir de las ventas (pedidos y su detalle).
 * Cada reporte trae sus datos, la descripcion de lo que muestra y una conclusion calculada con esos mismos datos.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

  public static final int CANTIDAD_REPORTES = 6;

  private static final int MESES = 12;
  private static final int TOP = 8;
  private static final String[] MESES_ABREVIADOS =
      {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

  private final PedidoRepository pedidoRepository;
  private final PedidoDetalleRepository detalleRepository;

  public ReporteService(PedidoRepository pedidoRepository, PedidoDetalleRepository detalleRepository) {
    this.pedidoRepository = pedidoRepository;
    this.detalleRepository = detalleRepository;
  }

  public List<Reporte> generarReportes() {
    List<Object[]> meses = pedidoRepository.resumenPorMes(
        YearMonth.now().minusMonths(MESES - 1).atDay(1).atStartOfDay());
    return List.of(
        ventasMensuales(meses),
        cursosMasVendidos(),
        ingresosPorTipo(),
        inscripcionesPorModalidad(),
        descuentosPorMes(meses),
        ingresosPorDocente());
  }

  public Reporte obtener(int numero) {
    if (numero < 1 || numero > CANTIDAD_REPORTES) {
      throw new IllegalArgumentException("El reporte solicitado no existe.");
    }
    return generarReportes().get(numero - 1);
  }

  /** Totales para las tarjetas del encabezado: ingresos, descuentos y pedidos. */
  public Map<String, Object> totales() {
    Object[] fila = pedidoRepository.totalesGenerales().get(0);
    Map<String, Object> totales = new LinkedHashMap<>();
    totales.put("ingresos", decimal(fila[0]));
    totales.put("descuentos", decimal(fila[1]));
    totales.put("pedidos", ((Number) fila[2]).longValue());
    return totales;
  }

  // ------------------------------------------------------------------ 1. Ventas mensuales

  private Reporte ventasMensuales(List<Object[]> meses) {
    List<YearMonth> periodo = periodo();
    Map<YearMonth, BigDecimal> ventas = new LinkedHashMap<>();
    periodo.forEach(m -> ventas.put(m, BigDecimal.ZERO));
    for (Object[] fila : meses) {
      ventas.put(YearMonth.of(((Number) fila[0]).intValue(), ((Number) fila[1]).intValue()), decimal(fila[2]));
    }
    List<String> etiquetas = periodo.stream().map(this::etiqueta).toList();
    List<BigDecimal> valores = new ArrayList<>(ventas.values());

    BigDecimal total = suma(valores);
    String conclusion;
    if (total.signum() == 0) {
      conclusion = "Todavía no se registran ventas en los últimos 12 meses.";
    } else {
      int mejor = indiceMaximo(valores);
      BigDecimal promedio = total.divide(BigDecimal.valueOf(MESES), 2, RoundingMode.HALF_UP);
      BigDecimal actual = valores.get(MESES - 1);
      BigDecimal anterior = valores.get(MESES - 2);
      conclusion = "En los últimos 12 meses se vendieron " + soles(total) + ", con un promedio de "
          + soles(promedio) + " por mes. El mejor mes fue " + etiquetas.get(mejor) + " con "
          + soles(valores.get(mejor)) + " (" + porcentaje(valores.get(mejor), total) + " del total). "
          + "El mes en curso (" + etiquetas.get(MESES - 1) + ") lleva " + soles(actual) + ", frente a "
          + soles(anterior) + " de " + etiquetas.get(MESES - 2) + ".";
    }
    return new Reporte(1, "Ventas mensuales", "line", false, "Mes", "Ingresos (S/)", true, etiquetas, valores,
        "Ingresos por la venta de cursos en cada uno de los últimos 12 meses. Los montos incluyen IGV y ya tienen "
            + "los descuentos aplicados.",
        conclusion);
  }

  // ------------------------------------------------------------------ 2. Cursos mas vendidos

  private Reporte cursosMasVendidos() {
    List<Object[]> filas = detalleRepository.cursosMasVendidos(PageRequest.of(0, TOP));
    List<String> etiquetas = new ArrayList<>();
    List<BigDecimal> valores = new ArrayList<>();
    for (Object[] fila : filas) {
      etiquetas.add((String) fila[0]);
      valores.add(decimal(fila[1]));
    }
    BigDecimal total = suma(valores);
    String conclusion;
    if (total.signum() == 0) {
      conclusion = "Todavía no hay inscripciones registradas.";
    } else {
      BigDecimal tresPrimeros = suma(valores.subList(0, Math.min(3, valores.size())));
      conclusion = "«" + etiquetas.get(0) + "» es el curso más vendido con " + valores.get(0).intValue()
          + inscripciones(valores.get(0)) + " (" + porcentaje(valores.get(0), total) + " del ranking). Los tres primeros reúnen "
          + porcentaje(tresPrimeros, total) + " de las " + total.intValue() + inscripciones(total) + " del ranking.";
    }
    return new Reporte(2, "Cursos más vendidos", "bar", true, "Curso", "Inscripciones", false, etiquetas, valores,
        "Ranking de los " + TOP + " cursos con más inscripciones, contando cada curso comprado como una inscripción.",
        conclusion);
  }

  // ------------------------------------------------------------------ 3. Ingresos por tipo de curso

  private Reporte ingresosPorTipo() {
    List<String> etiquetas = new ArrayList<>();
    List<BigDecimal> valores = new ArrayList<>();
    for (Object[] fila : detalleRepository.ingresosPorTipo()) {
      etiquetas.add((String) fila[0]);
      valores.add(decimal(fila[2]));
    }
    BigDecimal total = suma(valores);
    String conclusion;
    if (total.signum() == 0) {
      conclusion = "Todavía no hay ingresos registrados.";
    } else {
      conclusion = "«" + etiquetas.get(0) + "» aporta la mayor parte de los ingresos con " + soles(valores.get(0))
          + " (" + porcentaje(valores.get(0), total) + ")";
      if (etiquetas.size() > 1) {
        int ultimo = etiquetas.size() - 1;
        conclusion += ", mientras que «" + etiquetas.get(ultimo) + "» es el que menos aporta con "
            + soles(valores.get(ultimo)) + " (" + porcentaje(valores.get(ultimo), total) + ")";
      }
      conclusion += ". Ingreso total: " + soles(total) + ".";
    }
    return new Reporte(3, "Ingresos por tipo de curso", "doughnut", false, "Tipo de curso", "Ingresos (S/)", true,
        etiquetas, valores,
        "Participación de cada tipo de curso (diplomado, curso corto, seminario o programa) en los ingresos totales "
            + "por ventas.",
        conclusion);
  }

  // ------------------------------------------------------------------ 4. Inscripciones por modalidad

  private Reporte inscripcionesPorModalidad() {
    List<String> etiquetas = new ArrayList<>();
    List<BigDecimal> valores = new ArrayList<>();
    for (Object[] fila : detalleRepository.ventasPorModalidad()) {
      etiquetas.add(nombreModalidad((String) fila[0]));
      valores.add(decimal(fila[1]));
    }
    BigDecimal total = suma(valores);
    String conclusion;
    if (total.signum() == 0) {
      conclusion = "Todavía no hay inscripciones registradas.";
    } else {
      StringBuilder texto = new StringBuilder();
      for (int i = 0; i < etiquetas.size(); i++) {
        if (i > 0) {
          texto.append(i == etiquetas.size() - 1 ? " y " : ", ");
        }
        texto.append(etiquetas.get(i)).append(" con ").append(valores.get(i).intValue())
            .append(" (").append(porcentaje(valores.get(i), total)).append(")");
      }
      conclusion = "De las " + total.intValue() + inscripciones(total) + ", " + texto + ". La modalidad "
          + etiquetas.get(0).toLowerCase(Locale.ROOT) + " es la preferida por los estudiantes.";
    }
    return new Reporte(4, "Inscripciones por modalidad", "pie", false, "Modalidad", "Inscripciones", false, etiquetas,
        valores,
        "Cantidad de inscripciones según la modalidad del curso comprado: virtual o presencial.",
        conclusion);
  }

  // ------------------------------------------------------------------ 5. Descuentos por mes

  private Reporte descuentosPorMes(List<Object[]> meses) {
    List<YearMonth> periodo = periodo();
    Map<YearMonth, BigDecimal> descuentos = new LinkedHashMap<>();
    periodo.forEach(m -> descuentos.put(m, BigDecimal.ZERO));
    BigDecimal vendido = BigDecimal.ZERO;
    for (Object[] fila : meses) {
      descuentos.put(YearMonth.of(((Number) fila[0]).intValue(), ((Number) fila[1]).intValue()), decimal(fila[3]));
      vendido = vendido.add(decimal(fila[2]));
    }
    List<String> etiquetas = periodo.stream().map(this::etiqueta).toList();
    List<BigDecimal> valores = new ArrayList<>(descuentos.values());

    BigDecimal total = suma(valores);
    String conclusion;
    if (total.signum() == 0) {
      conclusion = "No se otorgaron descuentos en los últimos 12 meses.";
    } else {
      int mayor = indiceMaximo(valores);
      BigDecimal valorLista = vendido.add(total);
      conclusion = "Se otorgaron " + soles(total) + " en descuentos, que equivalen al "
          + porcentaje(total, valorLista) + " del valor de lista de lo vendido (" + soles(valorLista) + "). "
          + "El mes con más descuentos fue " + etiquetas.get(mayor) + " con " + soles(valores.get(mayor))
          + ".";
    }
    return new Reporte(5, "Descuentos otorgados por mes", "bar", false, "Mes", "Descuentos (S/)", true, etiquetas,
        valores,
        "Monto que se dejó de cobrar cada mes por las ofertas aplicadas en los cursos, durante los últimos 12 meses.",
        conclusion);
  }

  // ------------------------------------------------------------------ 6. Ingresos por docente

  private Reporte ingresosPorDocente() {
    List<String> etiquetas = new ArrayList<>();
    List<BigDecimal> valores = new ArrayList<>();
    for (Object[] fila : detalleRepository.ingresosPorDocente(PageRequest.of(0, TOP))) {
      etiquetas.add((String) fila[0]);
      valores.add(decimal(fila[2]));
    }
    BigDecimal total = suma(valores);
    String conclusion;
    if (total.signum() == 0) {
      conclusion = "Todavía no hay ingresos registrados.";
    } else {
      BigDecimal dos = suma(valores.subList(0, Math.min(2, valores.size())));
      conclusion = etiquetas.get(0) + " lidera con " + soles(valores.get(0)) + " ("
          + porcentaje(valores.get(0), total) + " de los ingresos del ranking). Los dos primeros docentes reúnen "
          + porcentaje(dos, total) + ", por lo que los ingresos se concentran en pocos docentes.";
    }
    return new Reporte(6, "Ingresos por docente", "bar", true, "Docente", "Ingresos (S/)", true, etiquetas, valores,
        "Ingresos generados por los cursos de cada docente (ranking de los " + TOP + " primeros).",
        conclusion);
  }

  // ------------------------------------------------------------------ utilidades

  private List<YearMonth> periodo() {
    List<YearMonth> periodo = new ArrayList<>();
    YearMonth actual = YearMonth.now();
    for (int i = MESES - 1; i >= 0; i--) {
      periodo.add(actual.minusMonths(i));
    }
    return periodo;
  }

  private String etiqueta(YearMonth mes) {
    return MESES_ABREVIADOS[mes.getMonthValue() - 1] + " " + mes.getYear();
  }

  private String inscripciones(BigDecimal cantidad) {
    return cantidad.intValue() == 1 ? " inscripción" : " inscripciones";
  }

  private String nombreModalidad(String codigo) {
    if ("V".equals(codigo)) {
      return "Virtual";
    }
    if ("P".equals(codigo)) {
      return "Presencial";
    }
    return "Sin definir";
  }

  private BigDecimal decimal(Object valor) {
    return valor instanceof BigDecimal d ? d : new BigDecimal(valor.toString());
  }

  private BigDecimal suma(List<BigDecimal> valores) {
    return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private int indiceMaximo(List<BigDecimal> valores) {
    int mayor = 0;
    for (int i = 1; i < valores.size(); i++) {
      if (valores.get(i).compareTo(valores.get(mayor)) > 0) {
        mayor = i;
      }
    }
    return mayor;
  }

  static String soles(BigDecimal monto) {
    return String.format(Locale.US, "S/ %,.2f", monto);
  }

  static String porcentaje(BigDecimal parte, BigDecimal total) {
    if (total.signum() == 0) {
      return "0 %";
    }
    BigDecimal valor = parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    return String.format(Locale.US, "%.1f %%", valor);
  }
}
