class Factura:
    def __init__(self):
        self.id = 0
        self.movimiento_id = None
        self.cliente_id = None
        self.numero_factura = ""
        self.fecha_emision = None
        self.metodo_pago = ""
        self.estado = ""
        self.subtotal = 0.0
        self.impuestos = 0.0
        self.total = 0.0
        self.cliente_nombre = ""
