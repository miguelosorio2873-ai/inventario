from db.conexion import conectar
from models.factura import Factura
from utils.aes_util import encriptar, desencriptar


class FacturaDAO:

    def _mapear(self, row):
        f = Factura()
        f.id = row[0]
        f.movimiento_id = row[1]
        f.cliente_id = row[2]
        f.numero_factura = desencriptar(row[3]) if row[3] else ""
        f.fecha_emision = row[4]
        f.metodo_pago = desencriptar(row[5]) if row[5] else ""
        f.estado = desencriptar(row[6]) if row[6] else ""
        f.subtotal = row[7] or 0.0
        f.impuestos = row[8] or 0.0
        f.total = row[9] or 0.0
        if len(row) > 10 and row[10]:
            f.cliente_nombre = desencriptar(row[10])
        return f

    def listar_todas(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM factura ORDER BY id DESC")
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def listar_todas_con_cliente(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute(
                "SELECT f.*, c.nombre as cliente_nombre FROM factura f "
                "LEFT JOIN cliente c ON f.cliente_id = c.id ORDER BY f.id DESC"
            )
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
            cur.execute("SELECT * FROM factura WHERE numero_factura LIKE %s OR numero_factura = %s ORDER BY id DESC", (f"%{texto}%", enc))
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def generar_numero(self):
        conn = conectar()
        if not conn:
            return "FAC-001"
        try:
            cur = conn.cursor()
            cur.execute("SELECT numero_factura FROM factura ORDER BY id DESC LIMIT 1")
            row = cur.fetchone()
            if row and row[0]:
                num = desencriptar(row[0])
                try:
                    n = int(num.split("-")[1]) + 1
                    return f"FAC-{n:03d}"
                except Exception:
                    return "FAC-001"
            return "FAC-001"
        finally:
            conn.close()

    def insertar(self, f):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO factura (movimiento_id, cliente_id, numero_factura, fecha_emision, metodo_pago, estado, subtotal, impuestos, total) "
                "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                (f.movimiento_id, f.cliente_id, encriptar(f.numero_factura) if f.numero_factura else None,
                 f.fecha_emision, encriptar(f.metodo_pago) if f.metodo_pago else None,
                 encriptar(f.estado) if f.estado else None, f.subtotal, f.impuestos, f.total),
            )
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def actualizar_estado(self, id, estado):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute("UPDATE factura SET estado=%s WHERE id=%s", (encriptar(estado), id))
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
            cur.execute("DELETE FROM factura WHERE id = %s", (id,))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()
