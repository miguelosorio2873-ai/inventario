package IG;

import DAO.ProductoDAO;
import DAO.FacturaDAO;
import Modelo.Producto;
import Modelo.Factura;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PanelReportes extends JPanel {

    public PanelReportes() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titulo = new JLabel("Reportes y Exportación");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/chart-pie.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 10, 10, 10);
        gc.weightx = 1.0;

        // --- Tarjeta: Exportar Inventario ---
        JPanel cardInv = crearTarjeta("Inventario de Productos");
        JLabel descInv = new JLabel("<html>Exporta un archivo en formato Excel con el catálogo de productos y su nivel de stock actual.</html>");
        descInv.setForeground(new Color(180, 180, 180));
        
        JButton btnExportInv = PanelProductos.crearBoton("📊 Exportar a Excel (Productos)", new Color(16, 185, 129));
        btnExportInv.addActionListener(e -> exportarProductos());
        
        JPanel pnlInv = new JPanel(new BorderLayout(0, 15));
        pnlInv.setOpaque(false);
        pnlInv.add(descInv, BorderLayout.NORTH);
        pnlInv.add(btnExportInv, BorderLayout.WEST);
        cardInv.add(pnlInv, BorderLayout.CENTER);

        // --- Tarjeta: Exportar Facturas ---
        JPanel cardFac = crearTarjeta("Historial de Facturas");
        JLabel descFac = new JLabel("<html>Genera un reporte en Excel con todas las facturas emitidas, sus clientes, importes y estado.</html>");
        descFac.setForeground(new Color(180, 180, 180));
        
        JButton btnExportFac = PanelProductos.crearBoton("🧾 Exportar a Excel (Facturas)", new Color(59, 130, 246));
        btnExportFac.addActionListener(e -> exportarFacturas());
        
        JPanel pnlFac = new JPanel(new BorderLayout(0, 15));
        pnlFac.setOpaque(false);
        pnlFac.add(descFac, BorderLayout.NORTH);
        pnlFac.add(btnExportFac, BorderLayout.WEST);
        cardFac.add(pnlFac, BorderLayout.CENTER);

        // Añadir tarjetas al body
        gc.gridy = 0; body.add(cardInv, gc);
        gc.gridy = 1; body.add(cardFac, gc);
        
        // Espaciador
        gc.gridy = 2; gc.weighty = 1.0;
        body.add(Box.createGlue(), gc);

        add(body, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(39, 39, 42));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 20, 20)
        ));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        card.add(lblTitulo, BorderLayout.NORTH);
        return card;
    }

    private void exportarProductos() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Productos");
        fileChooser.setSelectedFile(new File("Reporte_Productos.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fileChooser.getSelectedFile();
            if (!dest.getName().endsWith(".xlsx")) {
                dest = new File(dest.getAbsolutePath() + ".xlsx");
            }
            
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Productos");
                
                // Estilo Header
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                // Nombres columnas
                String[] columns = {"ID", "SKU", "Nombre", "Categoría", "Precio Venta", "Costo", "Stock Mínimo", "Estado"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Datos
                ProductoDAO pdao = new ProductoDAO();
                List<Producto> productos = pdao.listarTodos();
                int rowNum = 1;
                for (Producto p : productos) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(p.getId());
                    row.createCell(1).setCellValue(p.getSku() != null ? p.getSku() : "");
                    row.createCell(2).setCellValue(p.getNombre());
                    row.createCell(3).setCellValue(p.getCategoriaNombre() != null ? p.getCategoriaNombre() : "");
                    row.createCell(4).setCellValue(p.getPrecioVenta());
                    row.createCell(5).setCellValue(p.getCostoPromedio());
                    row.createCell(6).setCellValue(p.getStockMinimo());
                    row.createCell(7).setCellValue(p.isState() ? "Activo" : "Inactivo");
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                    workbook.write(outputStream);
                }
                JOptionPane.showMessageDialog(this, "✅ Reporte exportado con éxito a:\n" + dest.getAbsolutePath());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar el reporte:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void exportarFacturas() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Facturas");
        fileChooser.setSelectedFile(new File("Reporte_Facturas.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fileChooser.getSelectedFile();
            if (!dest.getName().endsWith(".xlsx")) {
                dest = new File(dest.getAbsolutePath() + ".xlsx");
            }
            
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Facturas");
                
                // Estilo Header
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                // Nombres columnas
                String[] columns = {"ID", "N° Factura", "Cliente", "Fecha", "Método Pago", "Subtotal", "Impuestos", "Total", "Estado"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Datos
                FacturaDAO fdao = new FacturaDAO();
                List<Factura> facturas = fdao.listarTodas();
                int rowNum = 1;
                for (Factura f : facturas) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(f.getId());
                    row.createCell(1).setCellValue(f.getNumeroFactura());
                    row.createCell(2).setCellValue(f.getClienteNombre() != null ? f.getClienteNombre() : "");
                    row.createCell(3).setCellValue(f.getFechaEmision() != null ? f.getFechaEmision().toString() : "");
                    row.createCell(4).setCellValue(f.getMetodoPago() != null ? f.getMetodoPago() : "");
                    row.createCell(5).setCellValue(f.getSubtotal());
                    row.createCell(6).setCellValue(f.getImpuestos());
                    row.createCell(7).setCellValue(f.getTotal());
                    row.createCell(8).setCellValue(f.getEstado());
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                    workbook.write(outputStream);
                }
                JOptionPane.showMessageDialog(this, "✅ Reporte exportado con éxito a:\n" + dest.getAbsolutePath());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar el reporte:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
