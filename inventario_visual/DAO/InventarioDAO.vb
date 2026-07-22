Imports MySql.Data.MySqlClient
Imports System.Data
Imports System.Linq

Namespace inventario_visual
    Public Class InventarioDAO
        Public Function ListarMovimientos() As List(Of MovimientoInventario)
            Dim lista As New List(Of MovimientoInventario)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "SELECT i.*, p.nombre AS producto_nombre, pr.nombre_empresa AS proveedor_nombre FROM inventario i INNER JOIN producto p ON i.producto_id=p.id LEFT JOIN proveedor pr ON i.proveedor_id=pr.id ORDER BY i.id DESC"
                Using cmd As New MySqlCommand(sql, conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Function BuscarPorProducto(texto As String) As List(Of MovimientoInventario)
            Return ListarMovimientos().Where(Function(m) m.ProductoNombre.ToLower().Contains(texto.ToLower())).ToList()
        End Function

        Public Function StockActual(productoId As Long) As Decimal
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT stock_actual FROM producto WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@id", productoId)
                    Dim result = cmd.ExecuteScalar()
                    Return If(result Is Nothing OrElse IsDBNull(result), 0D, Convert.ToDecimal(result))
                End Using
            End Using
        End Function

        Public Sub RegistrarMovimiento(m As MovimientoInventario)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using transaction = conn.BeginTransaction()
                    Try
                        Dim sql As String = "INSERT INTO inventario (producto_id, proveedor_id, precio, precio_balance, cantidad, tipo_movimiento, fecha_movimiento, motivo) VALUES (@producto,@proveedor,@precio,@balance,@cantidad,@tipo,NOW(),@motivo)"
                        Using cmd As New MySqlCommand(sql, conn, transaction)
                            cmd.Parameters.AddWithValue("@producto", m.ProductoId)
                            cmd.Parameters.AddWithValue("@proveedor", If(m.ProveedorId.HasValue, CObj(m.ProveedorId.Value), DBNull.Value))
                            cmd.Parameters.AddWithValue("@precio", m.Precio)
                            cmd.Parameters.AddWithValue("@balance", m.PrecioBalance)
                            cmd.Parameters.AddWithValue("@cantidad", m.Cantidad)
                            cmd.Parameters.AddWithValue("@tipo", AESUtil.Encriptar(m.TipoMovimiento))
                            cmd.Parameters.AddWithValue("@motivo", AESUtil.Encriptar(m.Motivo))
                            cmd.ExecuteNonQuery()
                        End Using
                        Dim ajuste As Double = If(m.TipoMovimiento = "Entrada", m.Cantidad, If(m.TipoMovimiento = "Salida", -m.Cantidad, 0D))
                        Dim updateSql As String = If(m.TipoMovimiento = "Ajuste", "UPDATE producto SET stock_actual=@cantidad WHERE id=@id", "UPDATE producto SET stock_actual=stock_actual+@cantidad WHERE id=@id")
                        Using cmd As New MySqlCommand(updateSql, conn, transaction)
                            cmd.Parameters.AddWithValue("@cantidad", If(m.TipoMovimiento = "Ajuste", m.Cantidad, ajuste))
                            cmd.Parameters.AddWithValue("@id", m.ProductoId)
                            cmd.ExecuteNonQuery()
                        End Using
                        transaction.Commit()
                    Catch
                        transaction.Rollback()
                        Throw
                    End Try
                End Using
            End Using
        End Sub

        Private Shared Function Map(reader As MySqlDataReader) As MovimientoInventario
            Return New MovimientoInventario With {.Id = Convert.ToInt64(reader("id")), .ProductoId = Convert.ToInt64(reader("producto_id")), .ProveedorId = If(IsDBNull(reader("proveedor_id")), CType(Nothing, Long?), Convert.ToInt64(reader("proveedor_id"))), .Precio = Convert.ToDouble(reader("precio")), .PrecioBalance = Convert.ToDouble(reader("precio_balance")), .Cantidad = Convert.ToDouble(reader("cantidad")), .TipoMovimiento = AESUtil.Desencriptar(If(IsDBNull(reader("tipo_movimiento")), "", reader("tipo_movimiento").ToString())), .FechaMovimiento = Convert.ToDateTime(reader("fecha_movimiento")), .Motivo = AESUtil.Desencriptar(If(IsDBNull(reader("motivo")), "", reader("motivo").ToString())), .ProductoNombre = AESUtil.Desencriptar(reader("producto_nombre").ToString()), .ProveedorNombre = AESUtil.Desencriptar(If(IsDBNull(reader("proveedor_nombre")), "", reader("proveedor_nombre").ToString()))}
        End Function
    End Class
End Namespace
