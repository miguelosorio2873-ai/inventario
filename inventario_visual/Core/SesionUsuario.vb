Namespace inventario_visual

    Public Module SesionUsuario

        Public Property UsuarioActual As Usuario

        Public Function TienePermiso(modulo As String) As Boolean
            If UsuarioActual Is Nothing Then Return False
            If EsAdministrador() Then Return True
            Dim permisos As String = Normalizar(UsuarioActual.Permisos)
            Dim moduloNormalizado As String = Normalizar(modulo)
            Dim inicio As Integer = permisos.IndexOf(moduloNormalizado & ":", StringComparison.Ordinal)
            If inicio >= 0 Then
                Dim fin As Integer = permisos.IndexOf(",", inicio, StringComparison.Ordinal)
                Dim acciones As String = If(fin < 0, permisos.Substring(inicio + moduloNormalizado.Length + 1), permisos.Substring(inicio + moduloNormalizado.Length + 1, fin - inicio - moduloNormalizado.Length - 1))
                Return acciones.Length > 0
            End If
            Return permisos.Contains(moduloNormalizado)
        End Function

        Public Function TienePermisoAccion(modulo As String, accion As String) As Boolean
            If UsuarioActual Is Nothing Then Return False
            If EsAdministrador() Then Return True
            Dim permisos As String = Normalizar(UsuarioActual.Permisos)
            Dim moduloNormalizado As String = Normalizar(modulo)
            Dim inicio As Integer = permisos.IndexOf(moduloNormalizado & ":", StringComparison.Ordinal)
            If inicio >= 0 Then
                Dim fin As Integer = permisos.IndexOf(",", inicio, StringComparison.Ordinal)
                Dim acciones As String = If(fin < 0, permisos.Substring(inicio + moduloNormalizado.Length + 1), permisos.Substring(inicio + moduloNormalizado.Length + 1, fin - inicio - moduloNormalizado.Length - 1))
                Dim codigo As String = If(accion.ToUpper() = "VER", "V", If(accion.ToUpper() = "CREAR", "C", If(accion.ToUpper() = "EDITAR", "E", If(accion.ToUpper() = "ELIMINAR", "D", If(accion.ToUpper() = "EXPORTAR", "X", accion.Substring(0, 1).ToUpper())))))
                Return acciones.Contains(codigo)
            End If
            Return permisos.Contains(String.Format("[{0}:{1}]", moduloNormalizado, accion.ToUpper())) OrElse permisos.Contains(moduloNormalizado)
        End Function

        Private Function EsAdministrador() As Boolean
            Return UsuarioActual IsNot Nothing AndAlso (String.Equals(UsuarioActual.Rol, "Admin", StringComparison.OrdinalIgnoreCase) OrElse String.Equals(UsuarioActual.Rol, "Administrador", StringComparison.OrdinalIgnoreCase))
        End Function

        Private Function Normalizar(valor As String) As String
            If String.IsNullOrEmpty(valor) Then Return ""
            Return valor.ToUpper().Replace("Á", "A").Replace("É", "E").Replace("Í", "I").Replace("Ó", "O").Replace("Ú", "U").Replace(" ", "")
        End Function

        Public Sub CerrarSesion()
            UsuarioActual = Nothing
        End Sub

    End Module

End Namespace

