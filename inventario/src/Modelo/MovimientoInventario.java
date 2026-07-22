package Modelo;

import java.sql.Timestamp;

public class MovimientoInventario {
    private long id;
    private long productoId;
    private Long proveedorId;
    private double precio;
    private double precioBalance;
    private double cantidad;
    private String tipoMovimiento; // entrada, salida, ajuste
    private Timestamp fechaMovimiento;
    private String motivo;
    // Campos calculados
    private String productoNombre;
    private String proveedorNombre;

    public MovimientoInventario() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProductoId() { return productoId; }
    public void setProductoId(long productoId) { this.productoId = productoId; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public double getPrecioBalance() { return precioBalance; }
    public void setPrecioBalance(double precioBalance) { this.precioBalance = precioBalance; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public Timestamp getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(Timestamp fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
}
