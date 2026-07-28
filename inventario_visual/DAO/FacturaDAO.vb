Imports MySql.Data.MySqlClient
Imports System.Data

Namespace inventario_visual
    Public Class FacturaDAO
        Public Function ListarTodas() As List(Of Factura)
            Dim lista As New List(Of Factura)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "SELECT f.*, c.nombre AS cliente_nombre FROM factura f LEFT JOIN cliente c ON f.cliente_id=c.id ORDER BY f.id DESC"
                Using cmd As New MySqlCommand(sql, conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Function Buscar(texto As String) As List(Of Factura)
            Dim lista As New List(Of Factura)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim enc As String = AESUtil.Encriptar(texto)
                Dim sql As String = "SELECT f.*, c.nombre AS cliente_nombre FROM factura f LEFT JOIN cliente c ON f.cliente_id=c.id WHERE f.numero_factura LIKE @t OR f.numero_factura = @enc ORDER BY f.id DESC"
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

        Public Function GenerarNumero() As String
            Dim ultimoNumero As String = ""
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT numero_factura FROM factura ORDER BY id DESC LIMIT 1", conn)
                    Dim result = cmd.ExecuteScalar()
                    If result IsNot Nothing AndAlso Not IsDBNull(result) Then
                        ultimoNumero = AESUtil.Desencriptar(result.ToString())
                    End If
                End Using
            End Using
            Dim nextNum As Integer = 1
            If Not String.IsNullOrEmpty(ultimoNumero) AndAlso ultimoNumero.StartsWith("FAC-") Then
                Dim parts() As String = ultimoNumero.Split("-"c)
                If parts.Length >= 2 AndAlso Integer.TryParse(parts(parts.Length - 1), nextNum) Then
                    nextNum += 1
                End If
            End If
            Return String.Format("FAC-{0:D3}", nextNum)
        End Function

        Public Sub Insertar(f As Factura)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim sql As String = "INSERT INTO factura (movimiento_id,cliente_id,numero_factura,metodo_pago,estado,subtotal,impuestos,total) VALUES (@mov,@cli,@num,@metodo,@estado,@subtotal,@impuestos,@total)"
                Using cmd As New MySqlCommand(sql, conn)
                    cmd.Parameters.AddWithValue("@mov", If(f.MovimientoId.HasValue, CObj(f.MovimientoId.Value), DBNull.Value))
                    cmd.Parameters.AddWithValue("@cli", If(f.ClienteId.HasValue, CObj(f.ClienteId.Value), DBNull.Value))
                    cmd.Parameters.AddWithValue("@num", AESUtil.Encriptar(f.NumeroFactura))
                    cmd.Parameters.AddWithValue("@metodo", AESUtil.Encriptar(f.MetodoPago))
                    cmd.Parameters.AddWithValue("@estado", AESUtil.Encriptar(f.Estado))
                    cmd.Parameters.AddWithValue("@subtotal", f.Subtotal)
                    cmd.Parameters.AddWithValue("@impuestos", f.Impuestos)
                    cmd.Parameters.AddWithValue("@total", f.Total)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub ActualizarEstado(id As Long, estado As String)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("UPDATE factura SET estado=@estado WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@estado", AESUtil.Encriptar(estado))
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Eliminar(id As Long)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("DELETE FROM factura WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Private Shared Function Map(reader As MySqlDataReader) As Factura
            Return New Factura With {.Id = Convert.ToInt64(reader("id")), .MovimientoId = If(IsDBNull(reader("movimiento_id")), CType(Nothing, Long?), Convert.ToInt64(reader("movimiento_id"))), .ClienteId = If(IsDBNull(reader("cliente_id")), CType(Nothing, Long?), Convert.ToInt64(reader("cliente_id"))), .NumeroFactura = AESUtil.Desencriptar(reader("numero_factura").ToString()), .FechaEmision = Convert.ToDateTime(reader("fecha_emision")), .MetodoPago = AESUtil.Desencriptar(If(IsDBNull(reader("metodo_pago")), "", reader("metodo_pago").ToString())), .Estado = AESUtil.Desencriptar(If(IsDBNull(reader("estado")), "", reader("estado").ToString())), .Subtotal = Convert.ToDouble(reader("subtotal")), .Impuestos = Convert.ToDouble(reader("impuestos")), .Total = Convert.ToDouble(reader("total")), .ClienteNombre = AESUtil.Desencriptar(If(IsDBNull(reader("cliente_nombre")), "", reader("cliente_nombre").ToString()))}
        End Function
    End Class
End Namespace
