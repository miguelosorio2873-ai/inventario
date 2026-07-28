from db.conexion import conectar
from models.proveedor import Proveedor
from utils.aes_util import encriptar, desencriptar


class ProveedorDAO:

    def _mapear(self, row):
        p = Proveedor()
        p.id = row[0]
        p.nombre_empresa = desencriptar(row[1]) if row[1] else ""
        p.nit_cedula = desencriptar(row[2]) if row[2] else ""
        p.telefono = desencriptar(row[3]) if row[3] else ""
        p.direccion = desencriptar(row[4]) if row[4] else ""
        p.correo = desencriptar(row[5]) if row[5] else ""
        p.nombre_contacto = desencriptar(row[6]) if row[6] else ""
        return p

    def listar_todos(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM proveedor ORDER BY id DESC")
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
            cur.execute("SELECT * FROM proveedor WHERE nombre_empresa LIKE %s OR nit_cedula LIKE %s OR nombre_empresa = %s OR nit_cedula = %s ORDER BY id DESC", (f"%{texto}%", f"%{texto}%", enc, enc))
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def insertar(self, p):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO proveedor (nombre_empresa, nit_cedula, telefono, direccion, correo, nombre_contacto) "
                "VALUES (%s,%s,%s,%s,%s,%s)",
                (encriptar(p.nombre_empresa), encriptar(p.nit_cedula) if p.nit_cedula else None,
                 encriptar(p.telefono) if p.telefono else None, encriptar(p.direccion) if p.direccion else None,
                 encriptar(p.correo) if p.correo else None, encriptar(p.nombre_contacto) if p.nombre_contacto else None),
            )
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def actualizar(self, p):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute(
                "UPDATE proveedor SET nombre_empresa=%s, nit_cedula=%s, telefono=%s, direccion=%s, correo=%s, nombre_contacto=%s WHERE id=%s",
                (encriptar(p.nombre_empresa), encriptar(p.nit_cedula) if p.nit_cedula else None,
                 encriptar(p.telefono) if p.telefono else None, encriptar(p.direccion) if p.direccion else None,
                 encriptar(p.correo) if p.correo else None, encriptar(p.nombre_contacto) if p.nombre_contacto else None, p.id),
            )
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
            cur.execute("DELETE FROM proveedor WHERE id = %s", (id,))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()
