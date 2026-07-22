from db.conexion import conectar
from models.usuario import Usuario
from utils.aes_util import encriptar, desencriptar
from utils.argon2_util import hash_password, verify_password
from datetime import datetime


class UsuarioDAO:

    def _mapear(self, row):
        u = Usuario()
        u.id = row[0]
        u.nombre = desencriptar(row[1]) if row[1] else ""
        u.email = desencriptar(row[2]) if row[2] else ""
        u.password = row[3]
        u.rol = desencriptar(row[4]) if row[4] else ""
        u.pregunta_1 = desencriptar(row[5]) if row[5] else ""
        u.pregunta_2 = desencriptar(row[6]) if row[6] else ""
        u.pregunta_3 = desencriptar(row[7]) if row[7] else ""
        u.pregunta_4 = desencriptar(row[8]) if row[8] else ""
        u.respuesta_1 = desencriptar(row[9]) if row[9] else ""
        u.respuesta_2 = desencriptar(row[10]) if row[10] else ""
        u.respuesta_3 = desencriptar(row[11]) if row[11] else ""
        u.respuesta_4 = desencriptar(row[12]) if row[12] else ""
        u.intentos_fallidos = row[13] or 0
        u.bloqueado_hasta = row[14]
        u.ultimo_login = row[15]
        u.permisos = row[16] if row[16] else ""
        return u

    def login(self, email, password):
        conn = conectar()
        if not conn:
            return None
        try:
            cur = conn.cursor()
            enc_email = encriptar(email)
            cur.execute("SELECT * FROM usuario WHERE email = %s", (enc_email,))
            row = cur.fetchone()
            if not row:
                raise Exception("Usuario no encontrado")
            u = self._mapear(row)
            if u.bloqueado_hasta and isinstance(u.bloqueado_hasta, datetime):
                if u.bloqueado_hasta > datetime.now():
                    raise Exception("Cuenta bloqueada. Intente más tarde.")
            if u.password and verify_password(password, u.password):
                cur.execute("UPDATE usuario SET intentos_fallidos=0, ultimo_login=NOW() WHERE id=%s", (u.id,))
                conn.commit()
                return u
            else:
                intentos = (u.intentos_fallidos or 0) + 1
                if intentos >= 5:
                    cur.execute("UPDATE usuario SET intentos_fallidos=%s, bloqueado_hasta=DATE_ADD(NOW(), INTERVAL 15 MINUTE) WHERE id=%s", (intentos, u.id))
                else:
                    cur.execute("UPDATE usuario SET intentos_fallidos=%s WHERE id=%s", (intentos, u.id))
                conn.commit()
                raise Exception("Contraseña incorrecta")
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def buscar_por_email(self, email):
        conn = conectar()
        if not conn:
            return None
        try:
            cur = conn.cursor()
            enc_email = encriptar(email)
            cur.execute("SELECT * FROM usuario WHERE email = %s", (enc_email,))
            row = cur.fetchone()
            return self._mapear(row) if row else None
        finally:
            conn.close()

    def insertar(self, u):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO usuario (nombre, email, password, rol, pregunta_1, pregunta_2, pregunta_3, pregunta_4, "
                "respuesta_1, respuesta_2, respuesta_3, respuesta_4, permisos) "
                "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                (encriptar(u.nombre), encriptar(u.email), hash_password(u.password) if u.password else None,
                 encriptar(u.rol) if u.rol else None,
                 encriptar(u.pregunta_1) if u.pregunta_1 else None,
                 encriptar(u.pregunta_2) if u.pregunta_2 else None,
                 encriptar(u.pregunta_3) if u.pregunta_3 else None,
                 encriptar(u.pregunta_4) if u.pregunta_4 else None,
                 encriptar(u.respuesta_1) if u.respuesta_1 else None,
                 encriptar(u.respuesta_2) if u.respuesta_2 else None,
                 encriptar(u.respuesta_3) if u.respuesta_3 else None,
                 encriptar(u.respuesta_4) if u.respuesta_4 else None,
                 u.permisos),
            )
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def actualizar(self, u):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute(
                "UPDATE usuario SET nombre=%s, email=%s, rol=%s, permisos=%s WHERE id=%s",
                (encriptar(u.nombre), encriptar(u.email), encriptar(u.rol) if u.rol else None, u.permisos, u.id),
            )
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def cambiar_password(self, id, new_password):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("UPDATE usuario SET password=%s WHERE id=%s", (hash_password(new_password), id))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def listar_todos(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM usuario ORDER BY id")
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def eliminar(self, id):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("DELETE FROM usuario WHERE id = %s", (id,))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()
