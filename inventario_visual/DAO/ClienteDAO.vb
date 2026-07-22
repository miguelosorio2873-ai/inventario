Imports MySql.Data.MySqlClient
Imports System.Data
Imports System.Linq

Namespace inventario_visual
    Public Class ClienteDAO
        Public Function ListarTodos() As List(Of Cliente)
            Dim lista As New List(Of Cliente)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT * FROM cliente ORDER BY id DESC", conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Function Buscar(texto As String) As List(Of Cliente)
            Dim t = texto.ToLower()
            Return ListarTodos().Where(Function(c) c.Nombre.ToLower().Contains(t) OrElse c.Cedula.ToLower().Contains(t)).ToList()
        End Function

        Public Sub Insertar(c As Cliente)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("INSERT INTO cliente (cedula, nombre, correo, telefono) VALUES (@ced, @nom, @corr, @tel)", conn)
                    cmd.Parameters.AddWithValue("@ced", AESUtil.Encriptar(c.Cedula))
                    cmd.Parameters.AddWithValue("@nom", AESUtil.Encriptar(c.Nombre))
                    cmd.Parameters.AddWithValue("@corr", AESUtil.Encriptar(c.Correo))
                    cmd.Parameters.AddWithValue("@tel", AESUtil.Encriptar(c.Telefono))
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Actualizar(c As Cliente)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("UPDATE cliente SET cedula=@ced, nombre=@nom, correo=@corr, telefono=@tel WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@ced", AESUtil.Encriptar(c.Cedula))
                    cmd.Parameters.AddWithValue("@nom", AESUtil.Encriptar(c.Nombre))
                    cmd.Parameters.AddWithValue("@corr", AESUtil.Encriptar(c.Correo))
                    cmd.Parameters.AddWithValue("@tel", AESUtil.Encriptar(c.Telefono))
                    cmd.Parameters.AddWithValue("@id", c.Id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Eliminar(id As Long)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("DELETE FROM cliente WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Private Shared Function Map(reader As MySqlDataReader) As Cliente
            Return New Cliente With {
                .Id = Convert.ToInt64(reader("id")),
                .Cedula = AESUtil.Desencriptar(If(IsDBNull(reader("cedula")), "", reader("cedula").ToString())),
                .Nombre = AESUtil.Desencriptar(reader("nombre").ToString()),
                .Correo = AESUtil.Desencriptar(If(IsDBNull(reader("correo")), "", reader("correo").ToString())),
                .Telefono = AESUtil.Desencriptar(If(IsDBNull(reader("telefono")), "", reader("telefono").ToString()))
            }
        End Function
    End Class
End Namespace
