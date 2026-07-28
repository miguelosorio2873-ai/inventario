from db.conexion import conectar
from models.producto import Producto
from utils.aes_util import encriptar, desencriptar


class ProductoDAO:

    def _mapear(self, row):
        p = Producto()
        p.id = row[0]
        p.categoria_id = row[1]
        p.sku = desencriptar(row[2]) if row[2] else ""
        p.nombre = desencriptar(row[3]) if row[3] else ""
        p.descripcion = desencriptar(row[4]) if row[4] else ""
        p.precio_venta = row[5] or 0.0
        p.costo_promedio = row[6] or 0.0
        p.stock_minimo = row[7] or 0.0
        p.stock_actual = row[8] or 0.0
        p.state = bool(row[9])
        p.imagen = row[10]
        if len(row) > 11 and row[11]:
            p.categoria_nombre = desencriptar(row[11])
        return p

    def listar_todos(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM producto ORDER BY id DESC")
            rows = cur.fetchall()
            return [self._mapear(r) for r in rows]
        finally:
            conn.close()

    def listar_todos_con_categoria(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute(
                "SELECT p.*, c.nombre as categoria_nombre FROM producto p "
                "LEFT JOIN categorias c ON p.categoria_id = c.id ORDER BY p.id DESC"
            )
            rows = cur.fetchall()
            return [self._mapear(r) for r in rows]
        finally:
            conn.close()

    def buscar(self, texto):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            enc = encriptar(texto)
            cur.execute(
                "SELECT * FROM producto WHERE nombre LIKE %s OR sku LIKE %s OR nombre = %s OR sku = %s ORDER BY id DESC",
                (f"%{texto}%", f"%{texto}%", enc, enc),
            )
            rows = cur.fetchall()
            return [self._mapear(r) for r in rows]
        finally:
            conn.close()

    def listar_stock_bajo(self):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM producto WHERE stock_actual <= stock_minimo AND state = 1 ORDER BY id DESC")
            rows = cur.fetchall()
            return [self._mapear(r) for r in rows]
        finally:
            conn.close()

    def obtener_por_id(self, id):
        conn = conectar()
        if not conn:
            return None
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM producto WHERE id = %s", (id,))
            row = cur.fetchone()
            return self._mapear(row) if row else None
        finally:
            conn.close()

    def insertar(self, p):
        conn = conectar()
        if not conn:
            return False
        try:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO producto (categoria_id, sku, nombre, descripcion, precio_venta, costo_promedio, stock_minimo, stock_actual, state, imagen) "
                "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                (p.categoria_id, encriptar(p.sku) if p.sku else None, encriptar(p.nombre),
                 encriptar(p.descripcion) if p.descripcion else None,
                 p.precio_venta, p.costo_promedio, p.stock_minimo, p.stock_actual, 1 if p.state else 0, p.imagen),
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
                "UPDATE producto SET categoria_id=%s, sku=%s, nombre=%s, descripcion=%s, precio_venta=%s, "
                "costo_promedio=%s, stock_minimo=%s, state=%s, imagen=%s WHERE id=%s",
                (p.categoria_id, encriptar(p.sku) if p.sku else None, encriptar(p.nombre),
                 encriptar(p.descripcion) if p.descripcion else None,
                 p.precio_venta, p.costo_promedio, p.stock_minimo, 1 if p.state else 0, p.imagen, p.id),
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
            cur.execute("DELETE FROM producto WHERE id = %s", (id,))
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()

    def contar_total(self):
        conn = conectar()
        if not conn:
            return 0
        try:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) FROM producto")
            return cur.fetchone()[0]
        finally:
            conn.close()

    def contar_stock_bajo(self):
        conn = conectar()
        if not conn:
            return 0
        try:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) FROM producto WHERE stock_actual <= stock_minimo AND state = 1")
            return cur.fetchone()[0]
        finally:
            conn.close()

    def listar_top_productos_por_stock(self, limite=10):
        conn = conectar()
        if not conn:
            return []
        try:
            cur = conn.cursor()
            cur.execute("SELECT * FROM producto ORDER BY stock_actual DESC LIMIT %s", (limite,))
            return [self._mapear(r) for r in cur.fetchall()]
        finally:
            conn.close()
