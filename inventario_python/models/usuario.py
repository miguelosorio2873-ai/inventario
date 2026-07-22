class Usuario:
    def __init__(self):
        self.id = 0
        self.nombre = ""
        self.email = ""
        self.password = None
        self.rol = ""
        self.pregunta_1 = ""
        self.pregunta_2 = ""
        self.pregunta_3 = ""
        self.pregunta_4 = ""
        self.respuesta_1 = ""
        self.respuesta_2 = ""
        self.respuesta_3 = ""
        self.respuesta_4 = ""
        self.intentos_fallidos = 0
        self.bloqueado_hasta = None
        self.ultimo_login = None
        self.permisos = ""
