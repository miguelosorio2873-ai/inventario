Imports MySql.Data.MySqlClient
Imports System.Data

Namespace inventario_visual
    Public Class ProductoDAO
        Public Function ListarTodos() As List(Of Producto)
            Dim lista As New List(Of Producto)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categorias c ON p.categoria_id = c.id WHERE p.state = 1 ORDER BY p.id DESC"
                Using cmd As New MySqlCommand(sql, conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Function Buscar(texto As String) As List(Of Producto)
            Dim lista As New List(Of Producto)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim enc As String = AESUtil.Encriptar(texto)
                Dim sql As String = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categorias c ON p.categoria_id = c.id WHERE p.state = 1 AND (p.nombre LIKE @t OR p.sku LIKE @t OR p.nombre = @enc OR p.sku = @enc) ORDER BY p.id DESC"
                Using cmd As New MySqlCommand(sql, conn)
                    cmd.Parameters.AddWithValue("@t", "%" & texto & "%")
                    cmd.Parameters.AddWithValue("@enc", enc)
                    Using reader = cmd.ExecuteReader()
                        While reader.Read()
                            lista.Add(Map(reader))
                        End While
                    End Using
                End Using
            End Using
            Return lista
        End Function

        Public Function ObtenerPorId(id As Long) As Producto
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categorias c ON p.categoria_id = c.id WHERE p.id = @id"
                Using cmd As New MySqlCommand(sql, conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    Using reader = cmd.ExecuteReader()
                        If reader.Read() Then Return Map(reader)
                    End Using
                End Using
            End Using
            Return Nothing
        End Function

        Public Sub Insertar(p As Producto)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "INSERT INTO producto (categoria_id, sku, nombre, descripcion, precio_venta, costo_promedio, stock_minimo, stock_actual, state, imagen) VALUES (@cat, @sku, @nombre, @desc, @pv, @pc, @min, @stock, @state, @img)"
                Using cmd As New MySqlCommand(sql, conn)
                    AddParams(cmd, p)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Actualizar(p As Producto)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "UPDATE producto SET categoria_id=@cat, sku=@sku, nombre=@nombre, descripcion=@desc, precio_venta=@pv, costo_promedio=@pc, stock_minimo=@min, stock_actual=@stock, state=@state, imagen=@img WHERE id=@id"
                Using cmd As New MySqlCommand(sql, conn)
                    AddParams(cmd, p)
                    cmd.Parameters.AddWithValue("@id", p.Id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Eliminar(id As Long)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("DELETE FROM producto WHERE id = @id", conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Function ContarTotal() As Integer
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT COUNT(*) FROM producto WHERE state = 1", conn)
                    Return Convert.ToInt32(cmd.ExecuteScalar())
                End Using
            End Using
        End Function

        Public Function ListarStockBajo() As List(Of Producto)
            Dim lista As New List(Of Producto)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categorias c ON p.categoria_id = c.id WHERE p.state = 1"
                Using cmd As New MySqlCommand(sql, conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        Dim p = Map(reader)
                        If p.StockActual <= p.StockMinimo Then lista.Add(p)
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Function ContarStockBajo() As Integer
            Return ListarStockBajo().Count
        End Function

        Public Function ListarTopProductosPorStock(limite As Integer) As List(Of Producto)
            Dim lista As New List(Of Producto)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "SELECT p.*, c.nombre AS categoria_nombre FROM producto p LEFT JOIN categorias c ON p.categoria_id = c.id WHERE p.state = 1 ORDER BY p.stock_actual DESC LIMIT @lim"
                Using cmd As New MySqlCommand(sql, conn)
                    cmd.Parameters.AddWithValue("@lim", limite)
                    Using reader = cmd.ExecuteReader()
                        While reader.Read()
                            lista.Add(Map(reader))
                        End While
                    End Using
                End Using
            End Using
            Return lista
        End Function

        Private Shared Sub AddParams(cmd As MySqlCommand, p As Producto)
            cmd.Parameters.AddWithValue("@sku", AESUtil.Encriptar(p.Sku))
            cmd.Parameters.AddWithValue("@nombre", AESUtil.Encriptar(p.Nombre))
            cmd.Parameters.AddWithValue("@desc", AESUtil.Encriptar(p.Descripcion))
            cmd.Parameters.AddWithValue("@cat", If(p.CategoriaId.HasValue, CObj(p.CategoriaId.Value), DBNull.Value))
            cmd.Parameters.AddWithValue("@pv", p.PrecioVenta)
            cmd.Parameters.AddWithValue("@pc", p.PrecioCompra)
            cmd.Parameters.AddWithValue("@stock", p.StockActual)
            cmd.Parameters.AddWithValue("@min", p.StockMinimo)
            cmd.Parameters.AddWithValue("@state", If(p.State, 1, 0))
            cmd.Parameters.AddWithValue("@img", If(String.IsNullOrEmpty(p.Imagen), CObj(DBNull.Value), p.Imagen))
        End Sub

        Private Shared Function Map(reader As MySqlDataReader) As Producto
            Return New Producto With {
                .Id = Convert.ToInt64(reader("id")),
                .Sku = AESUtil.Desencriptar(reader("sku").ToString()),
                .Nombre = AESUtil.Desencriptar(reader("nombre").ToString()),
                .Descripcion = AESUtil.Desencriptar(If(IsDBNull(reader("descripcion")), "", reader("descripcion").ToString())),
                .CategoriaId = If(IsDBNull(reader("categoria_id")), CType(Nothing, Long?), Convert.ToInt64(reader("categoria_id"))),
                .PrecioVenta = Convert.ToDouble(reader("precio_venta")),
                .PrecioCompra = Convert.ToDouble(reader("costo_promedio")),
                .StockActual = Convert.ToDouble(reader("stock_actual")),
                .StockMinimo = Convert.ToDouble(reader("stock_minimo")),
                .State = Convert.ToBoolean(reader("state")),
                .Imagen = If(IsDBNull(reader("imagen")), "", reader("imagen").ToString()),
                .CategoriaNombre = AESUtil.Desencriptar(If(IsDBNull(reader("categoria_nombre")), "", reader("categoria_nombre").ToString()))
            }
        End Function
    End Class
End Namespace
