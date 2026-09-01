package Modelo;

public class DetalleFactura {
    private long id;
    private long facturaId;
    private long productoId;
    private double cantidad;
    private double precioUnitario;
    private double subtotal;
    // Campos calculados
    private String productoNombre;
    private String productoSku;

    public DetalleFactura() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getFacturaId() { return facturaId; }
    public void setFacturaId(long facturaId) { this.facturaId = facturaId; }

    public long getProductoId() { return productoId; }
    public void setProductoId(long productoId) { this.productoId = productoId; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getProductoSku() { return productoSku; }
    public void setProductoSku(String productoSku) { this.productoSku = productoSku; }
}
