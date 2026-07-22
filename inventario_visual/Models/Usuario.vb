Namespace inventario_visual
    Public Class Usuario
        Public Property Id As Long
        Public Property Nombre As String
        Public Property Email As String
        Public Property PasswordHash As String
        Public Property Rol As String
        Public Property Permisos As String
        Public Property Pregunta1 As String
        Public Property Respuesta1 As String
        Public Property Pregunta2 As String
        Public Property Respuesta2 As String
        Public Property Pregunta3 As String
        Public Property Respuesta3 As String
        Public Property Pregunta4 As String
        Public Property Respuesta4 As String
        Public Property IntentosFallidos As Integer
        Public Property BloqueadoHasta As DateTime?
        Public Property UltimoLogin As DateTime?
    End Class
End Namespace
