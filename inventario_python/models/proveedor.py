class Proveedor:
    def __init__(self):
        self.id = 0
        self.nombre_empresa = ""
        self.nit_cedula = ""
        self.telefono = ""
        self.direccion = ""
        self.correo = ""
        self.nombre_contacto = ""

    def __str__(self):
        return self.nombre_empresa
