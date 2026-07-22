package Modelo;

import java.sql.Timestamp;

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

    public String getMetodoPago() { return metodoPago; }
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
}
