Imports MySql.Data.MySqlClient
Imports System.Data
Imports System.Linq

Namespace inventario_visual
    Public Class UsuarioDAO
        Public Function ValidarUsuario(email As String, password As String) As Usuario
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT * FROM usuario WHERE email=@email", conn)
                    cmd.Parameters.AddWithValue("@email", AESUtil.Encriptar(email))
                    Using reader = cmd.ExecuteReader()
                        If Not reader.Read() Then Return Nothing
                        Dim u As Usuario = Map(reader, True)
                        If u.BloqueadoHasta.HasValue AndAlso u.BloqueadoHasta.Value > DateTime.Now Then Return Nothing
                        If Not Argon2Util.VerificarPassword(password, u.PasswordHash) Then
                            reader.Close()
                            RegistrarFallo(conn, u.Id, u.IntentosFallidos + 1)
                            Return Nothing
                        End If
                        reader.Close()
                        Using updateCmd As New MySqlCommand("UPDATE usuario SET ultimo_login=NOW(), intentos_fallidos=0, bloqueado_hasta=NULL WHERE id=@id", conn)
                            updateCmd.Parameters.AddWithValue("@id", u.Id)
                            updateCmd.ExecuteNonQuery()
                        End Using
                        u.UltimoLogin = DateTime.Now
                        Return u
                    End Using
                End Using
            End Using
        End Function

        Public Function BuscarPorEmail(email As String) As Usuario
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT * FROM usuario WHERE email=@email", conn)
                    cmd.Parameters.AddWithValue("@email", AESUtil.Encriptar(email))
                    Using reader = cmd.ExecuteReader()
                        If reader.Read() Then Return Map(reader, False)
                    End Using
                End Using
            End Using
            Return Nothing
        End Function

        Private Shared Sub RegistrarFallo(conn As MySqlConnection, id As Long, intentos As Integer)
            Dim sql As String = If(intentos >= 5, "UPDATE usuario SET intentos_fallidos=@intentos, bloqueado_hasta=DATE_ADD(NOW(), INTERVAL 15 MINUTE) WHERE id=@id", "UPDATE usuario SET intentos_fallidos=@intentos WHERE id=@id")
            Using cmd As New MySqlCommand(sql, conn)
                cmd.Parameters.AddWithValue("@intentos", intentos)
                cmd.Parameters.AddWithValue("@id", id)
                cmd.ExecuteNonQuery()
            End Using
        End Sub

        Public Function ListarTodos() As List(Of Usuario)
            Dim lista As New List(Of Usuario)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("SELECT * FROM usuario ORDER BY id DESC", conn), reader = cmd.ExecuteReader()
                    While reader.Read()
                        lista.Add(Map(reader, True))
                    End While
                End Using
            End Using
            Return lista
        End Function

        Public Sub Insertar(u As Usuario)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("INSERT INTO usuario (nombre,email,password,rol,permisos,pregunta_1,respuesta_1,pregunta_2,respuesta_2,pregunta_3,respuesta_3,pregunta_4,respuesta_4) VALUES (@nombre,@email,@pass,@rol,@permisos,@p1,@r1,@p2,@r2,@p3,@r3,@p4,@r4)", conn)
                    AddParams(cmd, u, True)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Actualizar(u As Usuario)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("UPDATE usuario SET nombre=@nombre,email=@email,rol=@rol,permisos=@permisos,pregunta_1=@p1,respuesta_1=@r1,pregunta_2=@p2,respuesta_2=@r2,pregunta_3=@p3,respuesta_3=@r3,pregunta_4=@p4,respuesta_4=@r4 WHERE id=@id", conn)
                    AddParams(cmd, u, False)
                    cmd.Parameters.AddWithValue("@id", u.Id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub CambiarPassword(id As Long, nuevoPassword As String)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("UPDATE usuario SET password=@pass WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@pass", Argon2Util.HashPassword(nuevoPassword))
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Public Sub Eliminar(id As Long)
            Using conn = ConexionDB.GetConnection()
                conn.Open()
                Using cmd As New MySqlCommand("DELETE FROM usuario WHERE id=@id", conn)
                    cmd.Parameters.AddWithValue("@id", id)
                    cmd.ExecuteNonQuery()
                End Using
            End Using
        End Sub

        Private Shared Sub AddParams(cmd As MySqlCommand, u As Usuario, incluirPassword As Boolean)
            cmd.Parameters.AddWithValue("@nombre", AESUtil.Encriptar(u.Nombre))
            cmd.Parameters.AddWithValue("@email", AESUtil.Encriptar(u.Email))
            If incluirPassword Then cmd.Parameters.AddWithValue("@pass", Argon2Util.HashPassword(u.PasswordHash))
            cmd.Parameters.AddWithValue("@rol", AESUtil.Encriptar(u.Rol))
            cmd.Parameters.AddWithValue("@permisos", u.Permisos)
            cmd.Parameters.AddWithValue("@p1", AESUtil.Encriptar(u.Pregunta1))
            cmd.Parameters.AddWithValue("@r1", AESUtil.Encriptar(u.Respuesta1))
            cmd.Parameters.AddWithValue("@p2", AESUtil.Encriptar(u.Pregunta2))
            cmd.Parameters.AddWithValue("@r2", AESUtil.Encriptar(u.Respuesta2))
            cmd.Parameters.AddWithValue("@p3", AESUtil.Encriptar(u.Pregunta3))
            cmd.Parameters.AddWithValue("@r3", AESUtil.Encriptar(u.Respuesta3))
            cmd.Parameters.AddWithValue("@p4", AESUtil.Encriptar(u.Pregunta4))
            cmd.Parameters.AddWithValue("@r4", AESUtil.Encriptar(u.Respuesta4))
        End Sub

        Private Shared Function Map(reader As MySqlDataReader, incluirPassword As Boolean) As Usuario
            Dim u As New Usuario With {.Id = Convert.ToInt64(reader("id")), .Nombre = AESUtil.Desencriptar(reader("nombre").ToString()), .Email = AESUtil.Desencriptar(reader("email").ToString()), .Rol = AESUtil.Desencriptar(If(IsDBNull(reader("rol")), "", reader("rol").ToString())), .Permisos = If(IsDBNull(reader("permisos")), "", reader("permisos").ToString()), .IntentosFallidos = If(IsDBNull(reader("intentos_fallidos")), 0, Convert.ToInt32(reader("intentos_fallidos")))}
            If incluirPassword Then u.PasswordHash = If(IsDBNull(reader("password")), "", reader("password").ToString())
            u.Pregunta1 = AESUtil.Desencriptar(If(IsDBNull(reader("pregunta_1")), "", reader("pregunta_1").ToString()))
            u.Respuesta1 = AESUtil.Desencriptar(If(IsDBNull(reader("respuesta_1")), "", reader("respuesta_1").ToString()))
            u.Pregunta2 = AESUtil.Desencriptar(If(IsDBNull(reader("pregunta_2")), "", reader("pregunta_2").ToString()))
            u.Respuesta2 = AESUtil.Desencriptar(If(IsDBNull(reader("respuesta_2")), "", reader("respuesta_2").ToString()))
            u.Pregunta3 = AESUtil.Desencriptar(If(IsDBNull(reader("pregunta_3")), "", reader("pregunta_3").ToString()))
            u.Respuesta3 = AESUtil.Desencriptar(If(IsDBNull(reader("respuesta_3")), "", reader("respuesta_3").ToString()))
            u.Pregunta4 = AESUtil.Desencriptar(If(IsDBNull(reader("pregunta_4")), "", reader("pregunta_4").ToString()))
            u.Respuesta4 = AESUtil.Desencriptar(If(IsDBNull(reader("respuesta_4")), "", reader("respuesta_4").ToString()))
            If Not IsDBNull(reader("ultimo_login")) Then u.UltimoLogin = Convert.ToDateTime(reader("ultimo_login"))
            If Not IsDBNull(reader("bloqueado_hasta")) Then u.BloqueadoHasta = Convert.ToDateTime(reader("bloqueado_hasta"))
            Return u
        End Function
    End Class
End Namespace
