import re


class SesionUsuario:
    _instancia = None

    def __new__(cls):
        if cls._instancia is None:
            cls._instancia = super().__new__(cls)
            cls._instancia.usuario_id = None
            cls._instancia.nombre_usuario = None
            cls._instancia.rol = None
            cls._instancia.permisos = None
        return cls._instancia

    def iniciar_sesion(self, id, nombre, rol, permisos):
        self.usuario_id = id
        self.nombre_usuario = nombre
        self.rol = rol
        self.permisos = permisos

    def cerrar_sesion(self):
        self.usuario_id = None
        self.nombre_usuario = None
        self.rol = None
        self.permisos = None

    def _normalizar(self, s):
        if not s:
            return ""
        s = s.upper()
        replacements = {"Á": "A", "É": "E", "Í": "I", "Ó": "O", "Ú": "U"}
        for k, v in replacements.items():
            s = s.replace(k, v)
        s = re.sub(r"[^A-Z0-9:,]", "", s)
        return s

    def tiene_permiso(self, modulo):
        p = self._normalizar(self.permisos)
        m = self._normalizar(modulo)
        idx = p.find(m + ":")
        if idx != -1:
            end = p.find(",", idx)
            sub = p[idx:] if end == -1 else p[idx:end]
            actions = sub[sub.index(":") + 1:]
            return bool(actions)
        if self.rol and self.rol.lower() in ("admin", "administrador"):
            return True
        return m in p

    def tiene_permiso_accion(self, modulo, accion):
        if self.permisos is None and not (self.rol and self.rol.lower() in ("admin", "administrador")):
            return False
        p = self._normalizar(self.permisos)
        m = self._normalizar(modulo)
        a_map = {"VER": None, "CREAR": "C", "EDITAR": "E", "ELIMINAR": "D", "EXPORTAR": "X"}
        a = a_map.get(accion.upper(), accion.upper()[:1])
        if a is None:
            return self.tiene_permiso(modulo)
        idx = p.find(m + ":")
        if idx != -1:
            end = p.find(",", idx)
            sub = p[idx:] if end == -1 else p[idx:end]
            actions = sub[sub.index(":") + 1:]
            return a in actions
        if self.rol and self.rol.lower() in ("admin", "administrador"):
            return True
        return m in p
