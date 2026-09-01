package Modelo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private long id;
    private Long movimientoId;
    private Long clienteId;
    private String numeroFactura;
    private Timestamp fechaEmision;
    private String metodoPago; // Efectivo, Tarjeta, Transferencia
    private String estado; // Pagada, Pendiente, Anulada
    private double subtotal;
    private double impuestos;
    private double total;
    // Campos calculados
    private String clienteNombre;
    // Detalle (line items)
    private List<DetalleFactura> detalles = new ArrayList<>();

    public Factura() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getMovimientoId() { return movimientoId; }
    public void setMovimientoId(Long movimientoId) { this.movimientoId = movimientoId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Timestamp getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Timestamp fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getMetodoPago() {
        if (metodoPago == null) return null;
        return metodoPago
            .replace("Ã³", "ó")
            .replace("Ã©", "é")
            .replace("Ã­", "í")
            .replace("Ã¡", "á")
            .replace("Ãº", "ú");
    }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getImpuestos() { return impuestos; }
    public void setImpuestos(double impuestos) { this.impuestos = impuestos; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }
    public void agregarDetalle(DetalleFactura df) { this.detalles.add(df); }
}
