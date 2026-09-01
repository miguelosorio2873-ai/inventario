package Modelo;

public class Producto {
    private long id;
    private long categoriaId;
    private String sku;
    private String nombre;
    private String descripcion;
    private double precioVenta;
    private boolean state;
    private String imagen;
    private String presentacion;
    private double unidadesPresentacion;
    private double costoPresentacion;
    // Campo calculado
    private String categoriaNombre;
    private double stockActual;

    public Producto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(long categoriaId) { this.categoriaId = categoriaId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    /** Costo por unidad: equivalente al costo calculado desde la presentación. */
    public double getCostoPromedio() { return getCostoPorUnidad(); }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public double getStockActual() { return stockActual; }
    public void setStockActual(double stockActual) { this.stockActual = stockActual; }

    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

    public double getUnidadesPresentacion() { return unidadesPresentacion; }
    public void setUnidadesPresentacion(double unidadesPresentacion) { this.unidadesPresentacion = unidadesPresentacion; }

    public double getCostoPresentacion() { return costoPresentacion; }
    public void setCostoPresentacion(double costoPresentacion) { this.costoPresentacion = costoPresentacion; }

    /** Costo por unidad = costo de la presentación ÷ unidades de la presentación. */
    public double getCostoPorUnidad() {
        double un = (unidadesPresentacion > 0) ? unidadesPresentacion : 1;
        return costoPresentacion / un;
    }

    /** Ganancia por unidad = precio de venta − costo por unidad. */
    public double getGananciaPorUnidad() {
        return precioVenta - getCostoPorUnidad();
    }
}
