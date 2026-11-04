package pe.edu.escuela.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.escuela.app.dto.DatosCompra;
import pe.edu.escuela.app.model.Cliente;
import pe.edu.escuela.app.model.Comprobante;
import pe.edu.escuela.app.model.Curso;
import pe.edu.escuela.app.model.Pedido;
import pe.edu.escuela.app.model.PedidoDetalle;
import pe.edu.escuela.app.repository.ClienteRepository;
import pe.edu.escuela.app.repository.ComprobanteRepository;
import pe.edu.escuela.app.repository.CursoRepository;
import pe.edu.escuela.app.repository.PedidoRepository;
import pe.edu.escuela.app.util.Constantes;

/**
 * Confirma la compra: valida los cupos de cada curso (con la fila bloqueada), registra cliente, pedido y detalle,
 * descuenta los cupos y emite la boleta. Todo en una transaccion: si algo falla no queda nada a medias.
 */
@Service
public class VentaService {

  private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

  private final ClienteRepository clienteRepository;
  private final CursoRepository cursoRepository;
  private final PedidoRepository pedidoRepository;
  private final ComprobanteRepository comprobanteRepository;

  public VentaService(
      ClienteRepository clienteRepository,
      CursoRepository cursoRepository,
      PedidoRepository pedidoRepository,
      ComprobanteRepository comprobanteRepository) {
    this.clienteRepository = clienteRepository;
    this.cursoRepository = cursoRepository;
    this.pedidoRepository = pedidoRepository;
    this.comprobanteRepository = comprobanteRepository;
  }

  /**
   * @param cantidades id del curso y cupos que se compran.
   * @throws IllegalArgumentException si el carrito esta vacio, un curso ya no esta disponible o no alcanzan los cupos.
   */
  @Transactional
  public Comprobante comprar(DatosCompra datos, Map<Integer, Integer> cantidades) {
    if (cantidades.isEmpty()) {
      throw new IllegalArgumentException("No puedes comprar con el carrito vacío.");
    }

    Pedido pedido = new Pedido();
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal total = BigDecimal.ZERO;

    for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
      int cantidad = entrada.getValue();
      Curso curso = cursoRepository.buscarParaActualizar(entrada.getKey())
          .filter(c -> Constantes.ESTADO_ACTIVO.equals(c.getEstado()))
          .orElseThrow(() -> new IllegalArgumentException("Un curso del carrito ya no está disponible."));
      if (curso.getCupos() != null && cantidad > curso.getCupos()) {
        throw new IllegalArgumentException("Cupos insuficientes de «" + curso.getNombre() + "»: pediste "
            + cantidad + " y quedan " + curso.getCupos() + ".");
      }

      BigDecimal precioFinal = curso.getPrecioFinal();
      PedidoDetalle detalle = new PedidoDetalle();
      detalle.setCurso(curso);
      detalle.setNombreCurso(curso.getNombre());
      detalle.setCantidad(cantidad);
      detalle.setPrecioLista(curso.getPrecio());
      detalle.setDescuentoPorcentaje(curso.getDescuentoVigente());
      detalle.setPrecioFinal(precioFinal);
      detalle.setSubtotal(precioFinal.multiply(BigDecimal.valueOf(cantidad)));
      pedido.agregarDetalle(detalle);

      subtotal = subtotal.add(curso.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
      total = total.add(detalle.getSubtotal());

      if (curso.getCupos() != null) {
        curso.setCupos(curso.getCupos() - cantidad);
      }
    }

    Cliente cliente = clienteRepository.findByDocumento(datos.getDocumento().trim())
        .orElseGet(Cliente::new);
    cliente.setDocumento(datos.getDocumento().trim());
    cliente.setNombres(datos.getNombres().trim());
    cliente.setApellidos(datos.getApellidos().trim());
    cliente.setCorreo(datos.getCorreo().trim());
    cliente.setTelefono(datos.getTelefono() == null || datos.getTelefono().isBlank()
        ? null
        : datos.getTelefono().trim());

    pedido.setCliente(clienteRepository.save(cliente));
    pedido.setMedioPago(datos.getMedioPago());
    pedido.setSubtotal(subtotal);
    pedido.setDescuentoTotal(subtotal.subtract(total));
    pedido.setTotal(total);
    pedido.setIgv(total.subtract(total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP)));
    pedido = pedidoRepository.save(pedido);

    Comprobante comprobante = new Comprobante();
    comprobante.setPedido(pedido);
    comprobante.setCodigoAcceso(UUID.randomUUID().toString().replace("-", ""));
    return comprobanteRepository.save(comprobante);
  }

  public Comprobante obtenerComprobante(String codigoAcceso) {
    return comprobanteRepository.buscarPorCodigoAcceso(codigoAcceso)
        .orElseThrow(() -> new IllegalArgumentException("No se encontró la boleta solicitada."));
  }
}
