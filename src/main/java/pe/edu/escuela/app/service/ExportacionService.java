package pe.edu.escuela.app.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;
import pe.edu.escuela.app.config.DatosEmisor;
import pe.edu.escuela.app.dto.Reporte;
import pe.edu.escuela.app.model.Comprobante;
import pe.edu.escuela.app.model.Pedido;
import pe.edu.escuela.app.model.PedidoDetalle;

/**
 * Genera los archivos descargables: el informe de cada reporte en Excel, Word y PDF, y la boleta en PDF.
 * La imagen del grafico llega desde el navegador (PNG); si no llega, el informe se genera sin ella.
 */
@Service
public class ExportacionService {

  private final DatosEmisor emisor;

  public ExportacionService(DatosEmisor emisor) {
    this.emisor = emisor;
  }

  private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final String COLOR_AZUL = "0F355D";
  private static final String COLOR_CLARO = "EAF0F7";
  private static final Color PDF_AZUL = new Color(15, 53, 93);
  private static final Color PDF_NARANJA = new Color(245, 130, 32);
  private static final Color PDF_CLARO = new Color(234, 240, 247);

  // ------------------------------------------------------------------ Excel

  public byte[] excel(Reporte reporte, byte[] imagen) throws IOException {
    try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      XSSFSheet hoja = libro.createSheet("Reporte " + reporte.getNumero());
      hoja.setColumnWidth(0, 34 * 256);
      hoja.setColumnWidth(1, 20 * 256);

      XSSFFont negritaBlanca = libro.createFont();
      negritaBlanca.setBold(true);
      negritaBlanca.setColor(IndexedColors.WHITE.getIndex());
      XSSFFont titulo = libro.createFont();
      titulo.setBold(true);
      titulo.setFontHeightInPoints((short) 16);
      XSSFFont negrita = libro.createFont();
      negrita.setBold(true);

      CellStyle estiloTitulo = libro.createCellStyle();
      estiloTitulo.setFont(titulo);
      CellStyle estiloCabecera = libro.createCellStyle();
      estiloCabecera.setFont(negritaBlanca);
      estiloCabecera.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(
          new byte[] {(byte) 0x0F, (byte) 0x35, (byte) 0x5D}, null));
      estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      CellStyle estiloTexto = libro.createCellStyle();
      estiloTexto.setBorderBottom(BorderStyle.THIN);
      DataFormat formato = libro.createDataFormat();
      CellStyle estiloMonto = libro.createCellStyle();
      estiloMonto.setDataFormat(formato.getFormat("\"S/\" #,##0.00"));
      estiloMonto.setBorderBottom(BorderStyle.THIN);
      CellStyle estiloCantidad = libro.createCellStyle();
      estiloCantidad.setDataFormat(formato.getFormat("#,##0"));
      estiloCantidad.setBorderBottom(BorderStyle.THIN);
      CellStyle estiloTotal = libro.createCellStyle();
      estiloTotal.setFont(negrita);
      estiloTotal.setDataFormat(reporte.isMonetario() ? formato.getFormat("\"S/\" #,##0.00") : formato.getFormat("#,##0"));
      CellStyle estiloTotalTexto = libro.createCellStyle();
      estiloTotalTexto.setFont(negrita);
      CellStyle estiloLargo = libro.createCellStyle();
      estiloLargo.setWrapText(true);
      estiloLargo.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);

      int fila = 0;
      Row filaTitulo = hoja.createRow(fila++);
      filaTitulo.createCell(0).setCellValue(reporte.getTitulo());
      filaTitulo.getCell(0).setCellStyle(estiloTitulo);
      hoja.createRow(fila++).createCell(0).setCellValue("Escuela Jurídica · Generado el " + FECHA_HORA.format(LocalDateTime.now()));
      fila++;

      Row filaDescripcion = hoja.createRow(fila);
      filaDescripcion.createCell(0).setCellValue(reporte.getDescripcion());
      filaDescripcion.getCell(0).setCellStyle(estiloLargo);
      hoja.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(fila, fila, 0, 1));
      filaDescripcion.setHeightInPoints(60);
      fila += 2;

      Row cabecera = hoja.createRow(fila++);
      cabecera.createCell(0).setCellValue(reporte.getColumnaEtiqueta());
      cabecera.createCell(1).setCellValue(reporte.getColumnaValor());
      cabecera.getCell(0).setCellStyle(estiloCabecera);
      cabecera.getCell(1).setCellStyle(estiloCabecera);

      for (int i = 0; i < reporte.getEtiquetas().size(); i++) {
        Row datos = hoja.createRow(fila++);
        datos.createCell(0).setCellValue(reporte.getEtiquetas().get(i));
        datos.getCell(0).setCellStyle(estiloTexto);
        datos.createCell(1).setCellValue(reporte.getValores().get(i).doubleValue());
        datos.getCell(1).setCellStyle(reporte.isMonetario() ? estiloMonto : estiloCantidad);
      }
      Row total = hoja.createRow(fila++);
      total.createCell(0).setCellValue("Total");
      total.getCell(0).setCellStyle(estiloTotalTexto);
      total.createCell(1).setCellValue(reporte.getTotal().doubleValue());
      total.getCell(1).setCellStyle(estiloTotal);
      fila++;

      Row filaConclusion = hoja.createRow(fila);
      filaConclusion.createCell(0).setCellValue("Conclusión: " + reporte.getConclusion());
      filaConclusion.getCell(0).setCellStyle(estiloLargo);
      hoja.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(fila, fila, 0, 1));
      filaConclusion.setHeightInPoints(110);

      if (imagen != null) {
        int indice = libro.addPicture(imagen, Workbook.PICTURE_TYPE_PNG);
        CreationHelper ayuda = libro.getCreationHelper();
        XSSFDrawing dibujo = hoja.createDrawingPatriarch();
        XSSFClientAnchor ancla = (XSSFClientAnchor) ayuda.createClientAnchor();
        ancla.setCol1(3);
        ancla.setRow1(3);
        XSSFPicture imagenHoja = dibujo.createPicture(ancla, indice);
        double[] medidas = medidas(imagen, 620);
        imagenHoja.resize(medidas[0] / anchoOriginal(imagen));
      }

      libro.write(salida);
      return salida.toByteArray();
    }
  }

  // ------------------------------------------------------------------ Word

  public byte[] word(Reporte reporte, byte[] imagen) throws IOException {
    try (XWPFDocument documento = new XWPFDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      parrafo(documento, "ESCUELA JURÍDICA", 10, true, COLOR_AZUL, ParagraphAlignment.LEFT, 0);
      parrafo(documento, "Informe: " + reporte.getTitulo(), 20, true, COLOR_AZUL, ParagraphAlignment.LEFT, 80);
      parrafo(documento, "Generado el " + FECHA_HORA.format(LocalDateTime.now()), 9, false, "666666",
          ParagraphAlignment.LEFT, 200);

      parrafo(documento, "Descripción", 13, true, COLOR_AZUL, ParagraphAlignment.LEFT, 60);
      parrafo(documento, reporte.getDescripcion(), 11, false, "222222", ParagraphAlignment.LEFT, 200);

      if (imagen != null) {
        double[] medidas = medidas(imagen, 450);
        XWPFParagraph contenedor = documento.createParagraph();
        contenedor.setAlignment(ParagraphAlignment.CENTER);
        contenedor.setSpacingAfter(200);
        try {
          contenedor.createRun().addPicture(new ByteArrayInputStream(imagen), XWPFDocument.PICTURE_TYPE_PNG,
              "grafico.png", Units.toEMU(medidas[0]), Units.toEMU(medidas[1]));
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
          throw new IOException("No se pudo insertar el gráfico en el documento Word.", e);
        }
      }

      parrafo(documento, "Datos del reporte", 13, true, COLOR_AZUL, ParagraphAlignment.LEFT, 80);
      XWPFTable tabla = documento.createTable(reporte.getEtiquetas().size() + 2, 2);
      tabla.setWidth("100%");
      celda(tabla.getRow(0).getCell(0), reporte.getColumnaEtiqueta(), true, true, COLOR_AZUL);
      celda(tabla.getRow(0).getCell(1), reporte.getColumnaValor(), true, true, COLOR_AZUL);
      for (int i = 0; i < reporte.getEtiquetas().size(); i++) {
        XWPFTableRow datos = tabla.getRow(i + 1);
        String relleno = i % 2 == 1 ? COLOR_CLARO : "FFFFFF";
        celda(datos.getCell(0), reporte.getEtiquetas().get(i), false, false, relleno);
        celda(datos.getCell(1), formato(reporte, reporte.getValores().get(i)), false, false, relleno);
        datos.getCell(1).getParagraphs().get(0).setAlignment(ParagraphAlignment.RIGHT);
      }
      XWPFTableRow filaTotal = tabla.getRow(reporte.getEtiquetas().size() + 1);
      celda(filaTotal.getCell(0), "Total", true, false, COLOR_CLARO);
      celda(filaTotal.getCell(1), formato(reporte, reporte.getTotal()), true, false, COLOR_CLARO);
      filaTotal.getCell(1).getParagraphs().get(0).setAlignment(ParagraphAlignment.RIGHT);

      parrafo(documento, "", 6, false, "000000", ParagraphAlignment.LEFT, 120);
      parrafo(documento, "Conclusión", 13, true, COLOR_AZUL, ParagraphAlignment.LEFT, 60);
      parrafo(documento, reporte.getConclusion(), 11, false, "222222", ParagraphAlignment.LEFT, 0);

      documento.write(salida);
      return salida.toByteArray();
    }
  }

  private void parrafo(XWPFDocument documento, String texto, int tamano, boolean negrita, String color,
      ParagraphAlignment alineacion, int espacioDespues) {
    XWPFParagraph parrafo = documento.createParagraph();
    parrafo.setAlignment(alineacion);
    parrafo.setSpacingAfter(espacioDespues);
    XWPFRun corrida = parrafo.createRun();
    corrida.setText(texto);
    corrida.setFontFamily("Calibri");
    corrida.setFontSize(tamano);
    corrida.setBold(negrita);
    corrida.setColor(color);
  }

  private void celda(XWPFTableCell celda, String texto, boolean negrita, boolean blanco, String relleno) {
    celda.setColor(relleno);
    XWPFParagraph parrafo = celda.getParagraphs().get(0);
    XWPFRun corrida = parrafo.createRun();
    corrida.setText(texto);
    corrida.setFontFamily("Calibri");
    corrida.setFontSize(10);
    corrida.setBold(negrita);
    corrida.setColor(blanco ? "FFFFFF" : "222222");
  }

  // ------------------------------------------------------------------ PDF del informe

  public byte[] pdf(Reporte reporte, byte[] imagen) throws IOException {
    try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
      PdfWriter.getInstance(documento, salida);
      documento.open();

      Font marca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_NARANJA);
      Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PDF_AZUL);
      Font fecha = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
      Font seccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PDF_AZUL);
      Font texto = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(34, 34, 34));
      Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
      Font celda = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(34, 34, 34));
      Font celdaNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(34, 34, 34));

      documento.add(new Paragraph("ESCUELA JURÍDICA", marca));
      Paragraph encabezado = new Paragraph("Informe: " + reporte.getTitulo(), titulo);
      encabezado.setSpacingAfter(4);
      documento.add(encabezado);
      Paragraph generado = new Paragraph("Generado el " + FECHA_HORA.format(LocalDateTime.now()), fecha);
      generado.setSpacingAfter(16);
      documento.add(generado);

      documento.add(new Paragraph("Descripción", seccion));
      Paragraph descripcion = new Paragraph(reporte.getDescripcion(), texto);
      descripcion.setSpacingBefore(4);
      descripcion.setSpacingAfter(14);
      documento.add(descripcion);

      if (imagen != null) {
        Image grafico = Image.getInstance(imagen);
        double[] medidas = medidas(imagen, 440);
        grafico.scaleAbsolute((float) medidas[0], (float) medidas[1]);
        grafico.setAlignment(Element.ALIGN_CENTER);
        grafico.setSpacingAfter(14);
        documento.add(grafico);
      }

      Paragraph datos = new Paragraph("Datos del reporte", seccion);
      datos.setSpacingAfter(6);
      documento.add(datos);
      PdfPTable tabla = new PdfPTable(new float[] {3f, 1.4f});
      tabla.setWidthPercentage(100);
      tabla.addCell(celdaPdf(reporte.getColumnaEtiqueta(), cabecera, PDF_AZUL, Element.ALIGN_LEFT));
      tabla.addCell(celdaPdf(reporte.getColumnaValor(), cabecera, PDF_AZUL, Element.ALIGN_RIGHT));
      for (int i = 0; i < reporte.getEtiquetas().size(); i++) {
        Color fondo = i % 2 == 1 ? PDF_CLARO : Color.WHITE;
        tabla.addCell(celdaPdf(reporte.getEtiquetas().get(i), celda, fondo, Element.ALIGN_LEFT));
        tabla.addCell(celdaPdf(formato(reporte, reporte.getValores().get(i)), celda, fondo, Element.ALIGN_RIGHT));
      }
      tabla.addCell(celdaPdf("Total", celdaNegrita, PDF_CLARO, Element.ALIGN_LEFT));
      tabla.addCell(celdaPdf(formato(reporte, reporte.getTotal()), celdaNegrita, PDF_CLARO, Element.ALIGN_RIGHT));
      documento.add(tabla);

      Paragraph conclusionTitulo = new Paragraph("Conclusión", seccion);
      conclusionTitulo.setSpacingBefore(16);
      documento.add(conclusionTitulo);
      Paragraph conclusion = new Paragraph(reporte.getConclusion(), texto);
      conclusion.setSpacingBefore(4);
      documento.add(conclusion);

      documento.close();
      return salida.toByteArray();
    } catch (DocumentException e) {
      throw new IOException("No se pudo generar el PDF del informe.", e);
    }
  }

  // ------------------------------------------------------------------ Boleta

  public byte[] boletaPdf(Comprobante comprobante) throws IOException {
    Pedido pedido = comprobante.getPedido();
    try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
      PdfWriter.getInstance(documento, salida);
      documento.open();

      Font marca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PDF_AZUL);
      Font menor = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
      Font numero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PDF_NARANJA);
      Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PDF_AZUL);
      Font texto = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(34, 34, 34));
      Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
      Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PDF_AZUL);

      PdfPTable encabezado = new PdfPTable(new float[] {3f, 2f});
      encabezado.setWidthPercentage(100);
      PdfPCell izquierda = new PdfPCell();
      izquierda.setBorder(Rectangle.NO_BORDER);
      Paragraph razon = new Paragraph(emisor.getRazonSocial(), marca);
      razon.setSpacingAfter(4);
      izquierda.addElement(razon);
      izquierda.addElement(new Paragraph("Nombre comercial: " + emisor.getNombreComercial(), menor));
      izquierda.addElement(new Paragraph("Domicilio fiscal: " + emisor.getDomicilioFiscal(), menor));
      izquierda.addElement(new Paragraph("eventos@escuelajuridica.edu.pe · WhatsApp 926 812 819", menor));
      encabezado.addCell(izquierda);
      PdfPCell derecha = new PdfPCell();
      derecha.setBorderColor(PDF_AZUL);
      derecha.setPadding(8);
      Paragraph rucEmisor = new Paragraph("RUC " + emisor.getRuc(), etiqueta);
      rucEmisor.setAlignment(Element.ALIGN_CENTER);
      derecha.addElement(rucEmisor);
      Paragraph tipo = new Paragraph("BOLETA DE VENTA", etiqueta);
      tipo.setAlignment(Element.ALIGN_CENTER);
      Paragraph nro = new Paragraph(comprobante.getNumero(), numero);
      nro.setAlignment(Element.ALIGN_CENTER);
      derecha.addElement(tipo);
      derecha.addElement(nro);
      encabezado.addCell(derecha);
      documento.add(encabezado);

      Paragraph demo = new Paragraph("Documento de demostración del proyecto académico UTP, sin validez tributaria.", menor);
      demo.setSpacingBefore(6);
      demo.setSpacingAfter(14);
      documento.add(demo);

      PdfPTable cliente = new PdfPTable(new float[] {1f, 3f});
      cliente.setWidthPercentage(100);
      filaDato(cliente, "Fecha de emisión", FECHA_HORA.format(comprobante.getFechaEmision()), etiqueta, texto);
      filaDato(cliente, "Cliente", pedido.getCompradorNombreCompleto(), etiqueta, texto);
      filaDato(cliente, "DNI", pedido.getCompradorDocumento(), etiqueta, texto);
      filaDato(cliente, "Correo", pedido.getCompradorCorreo(), etiqueta, texto);
      filaDato(cliente, "Medio de pago", nombreMedioPago(pedido.getMedioPago()), etiqueta, texto);
      documento.add(cliente);

      PdfPTable items = new PdfPTable(new float[] {4f, 1.2f, 1f, 0.9f, 1.3f});
      items.setWidthPercentage(100);
      items.setSpacingBefore(16);
      items.addCell(celdaPdf("Curso", cabecera, PDF_AZUL, Element.ALIGN_LEFT));
      items.addCell(celdaPdf("P. lista", cabecera, PDF_AZUL, Element.ALIGN_RIGHT));
      items.addCell(celdaPdf("Dscto.", cabecera, PDF_AZUL, Element.ALIGN_RIGHT));
      items.addCell(celdaPdf("Cant.", cabecera, PDF_AZUL, Element.ALIGN_RIGHT));
      items.addCell(celdaPdf("Importe", cabecera, PDF_AZUL, Element.ALIGN_RIGHT));
      List<PedidoDetalle> detalles = pedido.getDetalles();
      for (int i = 0; i < detalles.size(); i++) {
        PedidoDetalle detalle = detalles.get(i);
        Color fondo = i % 2 == 1 ? PDF_CLARO : Color.WHITE;
        items.addCell(celdaPdf(detalle.getNombreCurso(), texto, fondo, Element.ALIGN_LEFT));
        items.addCell(celdaPdf(monto(detalle.getPrecioLista()), texto, fondo, Element.ALIGN_RIGHT));
        items.addCell(celdaPdf(detalle.getDescuentoPorcentaje().signum() > 0
            ? detalle.getDescuentoPorcentaje().stripTrailingZeros().toPlainString() + " %"
            : "-", texto, fondo, Element.ALIGN_RIGHT));
        items.addCell(celdaPdf(String.valueOf(detalle.getCantidad()), texto, fondo, Element.ALIGN_RIGHT));
        items.addCell(celdaPdf(monto(detalle.getSubtotal()), texto, fondo, Element.ALIGN_RIGHT));
      }
      documento.add(items);

      PdfPTable totales = new PdfPTable(new float[] {3f, 1.5f});
      totales.setWidthPercentage(45);
      totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totales.setSpacingBefore(12);
      filaTotal(totales, "Base imponible", monto(pedido.getBaseImponible()), texto);
      filaTotal(totales, "IGV (18 %)", monto(pedido.getIgv()), texto);
      filaTotal(totales, "TOTAL A PAGAR", monto(pedido.getTotal()), negrita);
      documento.add(totales);

      Paragraph gracias = new Paragraph("Gracias por tu compra. Los precios incluyen IGV.", menor);
      gracias.setSpacingBefore(24);
      documento.add(gracias);

      documento.close();
      return salida.toByteArray();
    } catch (DocumentException e) {
      throw new IOException("No se pudo generar el PDF de la boleta.", e);
    }
  }

  public static String nombreMedioPago(String codigo) {
    return switch (codigo) {
      case "TARJETA" -> "Tarjeta de crédito o débito";
      case "YAPE" -> "Yape";
      case "TRANSFERENCIA" -> "Transferencia bancaria";
      default -> codigo;
    };
  }

  // ------------------------------------------------------------------ utilidades

  private void filaDato(PdfPTable tabla, String nombre, String valor, Font fuenteNombre, Font fuenteValor) {
    PdfPCell izquierda = new PdfPCell(new Phrase(nombre, fuenteNombre));
    izquierda.setBorder(Rectangle.BOTTOM);
    izquierda.setBorderColor(PDF_CLARO);
    izquierda.setPadding(5);
    PdfPCell derecha = new PdfPCell(new Phrase(valor, fuenteValor));
    derecha.setBorder(Rectangle.BOTTOM);
    derecha.setBorderColor(PDF_CLARO);
    derecha.setPadding(5);
    tabla.addCell(izquierda);
    tabla.addCell(derecha);
  }

  private void filaTotal(PdfPTable tabla, String nombre, String valor, Font fuente) {
    PdfPCell izquierda = new PdfPCell(new Phrase(nombre, fuente));
    izquierda.setBorder(Rectangle.NO_BORDER);
    izquierda.setPadding(3);
    PdfPCell derecha = new PdfPCell(new Phrase(valor, fuente));
    derecha.setBorder(Rectangle.NO_BORDER);
    derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
    derecha.setPadding(3);
    tabla.addCell(izquierda);
    tabla.addCell(derecha);
  }

  private PdfPCell celdaPdf(String texto, Font fuente, Color fondo, int alineacion) {
    PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
    celda.setBackgroundColor(fondo);
    celda.setHorizontalAlignment(alineacion);
    celda.setPadding(6);
    celda.setBorderColor(new Color(220, 224, 230));
    return celda;
  }

  private String formato(Reporte reporte, BigDecimal valor) {
    return reporte.isMonetario() ? monto(valor) : String.format(Locale.US, "%,d", valor.longValue());
  }

  private String monto(BigDecimal valor) {
    return String.format(Locale.US, "S/ %,.2f", valor);
  }

  /** Ancho y alto con que se inserta el grafico, conservando su proporcion. */
  private double[] medidas(byte[] imagen, double anchoMaximo) throws IOException {
    BufferedImage leida = ImageIO.read(new ByteArrayInputStream(imagen));
    if (leida == null) {
      throw new IOException("La imagen del gráfico no es un PNG válido.");
    }
    double ancho = Math.min(anchoMaximo, leida.getWidth());
    return new double[] {ancho, ancho * leida.getHeight() / leida.getWidth()};
  }

  private double anchoOriginal(byte[] imagen) throws IOException {
    BufferedImage leida = ImageIO.read(new ByteArrayInputStream(imagen));
    return leida.getWidth();
  }
}
