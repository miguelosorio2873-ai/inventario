class Producto:
    def __init__(self):
        self.id = 0
        self.categoria_id = None
        self.sku = ""
        self.nombre = ""
        self.descripcion = ""
        self.precio_venta = 0.0
        self.costo_promedio = 0.0
        self.stock_minimo = 0.0
        self.stock_actual = 0.0
        self.state = True
        self.imagen = None
        self.categoria_nombre = ""

    def __str__(self):
        return self.nombre
