from db.conexion import conectar
from models.cliente import Cliente
from utils.aes_util import encriptar, desencriptar


class ClienteDAO:

    def _mapear(self, row):
        c = Cliente()
        c.id = row[0]
        c.cedula = desencriptar(row[1]) if row[1] else ""
        c.nombre = desencriptar(row[2]) if row[2] else ""
        c.correo = desencriptar(row[3]) if row[3] else ""
        c.telefono = desencriptar(row[4]) if row[4] else ""
        return c

    def listar_todos(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM cliente ORDER BY id DESC")
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def buscar(self, texto):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            enc = encriptar(texto)
            cur.execute("SELECT * FROM cliente WHERE nombre LIKE %s OR cedula LIKE %s OR nombre = %s OR cedula = %s ORDER BY id DESC", (f"%{texto}%", f"%{texto}%", enc, enc))
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def insertar(self, c):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("INSERT INTO cliente (cedula, nombre, correo, telefono) VALUES (%s,%s,%s,%s)",
                        (encriptar(c.cedula) if c.cedula else None, encriptar(c.nombre),
                         encriptar(c.correo) if c.correo else None, encriptar(c.telefono) if c.telefono else None))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def actualizar(self, c):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("UPDATE cliente SET cedula=%s, nombre=%s, correo=%s, telefono=%s WHERE id=%s",
                        (encriptar(c.cedula) if c.cedula else None, encriptar(c.nombre),
                         encriptar(c.correo) if c.correo else None, encriptar(c.telefono) if c.telefono else None, c.id))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def eliminar(self, id):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("DELETE FROM cliente WHERE id = %s", (id,))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def contar_todos(self):
        conn = conectar()
        if not conn:
            return 0
        try:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) FROM cliente")
            return cur.fetchone()[0]
        finally:
            conn.close()
