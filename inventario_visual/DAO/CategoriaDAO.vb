Imports MySql.Data.MySqlClient
Imports System.Data

Namespace inventario_visual
    Public Class CategoriaDAO
        Public Function ListarTodas() As List(Of Categoria)
            Dim lista As New List(Of Categoria)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT * FROM categorias ORDER BY id DESC", conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Sub Insertar(c As Categoria)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("INSERT INTO categorias (nombre, descripcion) VALUES (@nombre, @desc)", conn)
                    cmd.Parameters.AddWithValue("@nombre", AESUtil.Encriptar(c.Nombre))
                    cmd.Parameters.AddWithValue("@desc", AESUtil.Encriptar(c.Descripcion))
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Actualizar(c As Categoria)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("UPDATE categorias SET nombre=@nombre, descripcion=@desc WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@nombre", AESUtil.Encriptar(c.Nombre))
                    cmd.Parameters.AddWithValue("@desc", AESUtil.Encriptar(c.Descripcion))
                    cmd.Parameters.AddWithValue("@id", c.Id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Eliminar(id As Long)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("DELETE FROM categorias WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Private Shared Function Map(reader As MySqlDataReader) As Categoria
            Return New Categoria With {
                .Id = Convert.ToInt64(reader("id")),
                .Nombre = AESUtil.Desencriptar(reader("nombre").ToString()),
                .Descripcion = AESUtil.Desencriptar(If(IsDBNull(reader("descripcion")), "", reader("descripcion").ToString()))
            }
        End Function
    End Class
End Namespace
