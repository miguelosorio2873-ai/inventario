from db.conexion import conectar
from models.movimiento_inventario import MovimientoInventario
from utils.aes_util import encriptar, desencriptar


class InventarioDAO:

    def _mapear(self, row):
        m = MovimientoInventario()
        m.id = row[0]
        m.producto_id = row[1]
        m.proveedor_id = row[2]
        m.precio = row[3] or 0.0
        m.precio_balance = row[4] or 0.0
        m.cantidad = row[5] or 0.0
        m.tipo_movimiento = desencriptar(row[6]) if row[6] else ""
        m.fecha_movimiento = row[7]
        m.motivo = desencriptar(row[8]) if row[8] else ""
        if len(row) > 9 and row[9]:
            m.producto_nombre = desencriptar(row[9])
        if len(row) > 10 and row[10]:
            m.proveedor_nombre = desencriptar(row[10])
        return m

    def listar_movimientos(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM inventario ORDER BY id DESC")
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def listar_movimientos_con_nombres(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute(
                "SELECT i.*, p.nombre as producto_nombre, pr.nombre_empresa as proveedor_nombre "
                "FROM inventario i "
                "LEFT JOIN producto p ON i.producto_id = p.id "
                "LEFT JOIN proveedor pr ON i.proveedor_id = pr.id "
                "ORDER BY i.id DESC"
            )
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def buscar_por_producto(self, texto):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            enc = encriptar(texto)
            cur.execute(
                "SELECT i.* FROM inventario i JOIN producto p ON i.producto_id = p.id "
                "WHERE p.nombre LIKE %s OR p.nombre = %s ORDER BY i.id DESC",
                (f"%{texto}%", enc),
            )
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()

    def registrar_movimiento(self, m):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            tipo_enc = encriptar(m.tipo_movimiento) if m.tipo_movimiento else None
            motivo_enc = encriptar(m.motivo) if m.motivo else None
            cur.execute(
                "INSERT INTO inventario (producto_id, proveedor_id, precio, precio_balance, cantidad, tipo_movimiento, motivo) "
                "VALUES (%s,%s,%s,%s,%s,%s,%s)",
                (m.producto_id, m.proveedor_id, m.precio, m.precio_balance, m.cantidad, tipo_enc, motivo_enc),
            )
            if m.tipo_movimiento == "Entrada":
                cur.execute("UPDATE producto SET stock_actual = stock_actual + %s WHERE id = %s", (m.cantidad, m.producto_id))
            elif m.tipo_movimiento == "Salida":
                cur.execute("UPDATE producto SET stock_actual = stock_actual - %s WHERE id = %s", (m.cantidad, m.producto_id))
            elif m.tipo_movimiento == "Ajuste":
                cur.execute("UPDATE producto SET stock_actual = %s WHERE id = %s", (m.cantidad, m.producto_id))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def stock_actual(self, producto_id):
        conn = conectar()
        if not conn:
            return 0.0
        try:
            cur = conn.cursor()
            cur.execute("SELECT stock_actual FROM producto WHERE id = %s", (producto_id,))
            row = cur.fetchone()
            return row[0] if row else 0.0
        finally:
            conn.close()

    def ventas_del_mes(self):
        conn = conectar()
        if not conn:
            return 0.0
        try:
            cur = conn.cursor()
            anulada = encriptar("Anulada")
            cur.execute("SELECT COALESCE(SUM(total),0) FROM factura WHERE MONTH(fecha_emision) = MONTH(CURDATE()) AND YEAR(fecha_emision) = YEAR(CURDATE()) AND estado != %s", (anulada,))
            return cur.fetchone()[0]
        finally:
            conn.close()
