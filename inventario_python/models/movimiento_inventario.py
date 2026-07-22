class MovimientoInventario:
    def __init__(self):
        self.id = 0
        self.producto_id = 0
        self.proveedor_id = None
        self.precio = 0.0
        self.precio_balance = 0.0
        self.cantidad = 0.0
        self.tipo_movimiento = ""
        self.fecha_movimiento = None
        self.motivo = ""
        self.producto_nombre = ""
        self.proveedor_nombre = ""
