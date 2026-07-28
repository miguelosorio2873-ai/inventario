from db.conexion import conectar
from models.categoria import Categoria
from utils.aes_util import encriptar, desencriptar


class CategoriaDAO:

    def _mapear(self, row):
        c = Categoria()
        c.id = row[0]
        c.nombre = desencriptar(row[1]) if row[1] else ""
        c.descripcion = desencriptar(row[2]) if row[2] else ""
        return c

    def listar_todas(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM categorias ORDER BY id DESC")
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def insertar(self, c):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("INSERT INTO categorias (nombre, descripcion) VALUES (%s, %s)",
                        (encriptar(c.nombre), encriptar(c.descripcion) if c.descripcion else None))
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
            cur.execute("UPDATE categorias SET nombre=%s, descripcion=%s WHERE id=%s",
                        (encriptar(c.nombre), encriptar(c.descripcion) if c.descripcion else None, c.id))
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
            cur.execute("DELETE FROM categorias WHERE id = %s", (id,))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()
