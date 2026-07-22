class Cliente:
    def __init__(self):
        self.id = 0
        self.cedula = ""
        self.nombre = ""
        self.correo = ""
        self.telefono = ""

    def __str__(self):
        return self.nombre
