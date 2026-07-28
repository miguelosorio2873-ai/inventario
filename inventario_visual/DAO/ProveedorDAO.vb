Imports MySql.Data.MySqlClient
Imports System.Data
Imports System.Linq

Namespace inventario_visual
    Public Class ProveedorDAO
        Public Function ListarTodos() As List(Of Proveedor)
            Dim lista As New List(Of Proveedor)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT * FROM proveedor ORDER BY id DESC", conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Function Buscar(texto As String) As List(Of Proveedor)
            Dim lista As New List(Of Proveedor)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Dim enc As String = AESUtil.Encriptar(texto)
                Dim sql As String = "SELECT * FROM proveedor WHERE nombre_empresa LIKE @t OR nit_cedula LIKE @t OR nombre_empresa = @enc OR nit_cedula = @enc ORDER BY id DESC"
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

        Public Sub Insertar(p As Proveedor)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("INSERT INTO proveedor (nombre_empresa, nit_cedula, telefono, direccion, correo, nombre_contacto) VALUES (@nom, @nit, @tel, @dir, @corr, @contacto)", conn)
                    AddParams(cmd, p)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Actualizar(p As Proveedor)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("UPDATE proveedor SET nombre_empresa=@nom, nit_cedula=@nit, telefono=@tel, direccion=@dir, correo=@corr, nombre_contacto=@contacto WHERE id=@id", conn)
                    AddParams(cmd, p)
                    cmd.Parameters.AddWithValue("@id", p.Id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Eliminar(id As Long)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("DELETE FROM proveedor WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Private Shared Sub AddParams(cmd As MySqlCommand, p As Proveedor)
            cmd.Parameters.AddWithValue("@nom", AESUtil.Encriptar(p.NombreEmpresa))
            cmd.Parameters.AddWithValue("@nit", AESUtil.Encriptar(p.NitCedula))
            cmd.Parameters.AddWithValue("@tel", AESUtil.Encriptar(p.Telefono))
            cmd.Parameters.AddWithValue("@dir", AESUtil.Encriptar(p.Direccion))
            cmd.Parameters.AddWithValue("@corr", AESUtil.Encriptar(p.Correo))
            cmd.Parameters.AddWithValue("@contacto", AESUtil.Encriptar(p.NombreContacto))
        End Sub

        Private Shared Function Map(reader As MySqlDataReader) As Proveedor
            Return New Proveedor With {
                .Id = Convert.ToInt64(reader("id")),
                .NombreEmpresa = AESUtil.Desencriptar(reader("nombre_empresa").ToString()),
                .NitCedula = AESUtil.Desencriptar(If(IsDBNull(reader("nit_cedula")), "", reader("nit_cedula").ToString())),
                .Telefono = AESUtil.Desencriptar(If(IsDBNull(reader("telefono")), "", reader("telefono").ToString())),
                .Direccion = AESUtil.Desencriptar(If(IsDBNull(reader("direccion")), "", reader("direccion").ToString())),
                .Correo = AESUtil.Desencriptar(If(IsDBNull(reader("correo")), "", reader("correo").ToString())),
                .NombreContacto = AESUtil.Desencriptar(If(IsDBNull(reader("nombre_contacto")), "", reader("nombre_contacto").ToString()))
            }
        End Function
    End Class
End Namespace
