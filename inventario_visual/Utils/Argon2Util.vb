Imports Isopoh.Cryptography.Argon2
Imports System.Security.Cryptography
Imports System.Text
Imports System.Linq

Namespace inventario_visual
    Public Module Argon2Util
        Public Function HashPassword(password As String) As String
            If String.IsNullOrEmpty(password) Then Return password
            Return Argon2.Hash(password, 65536, 10, 1, Argon2Type.HybridAddressing, 32, Nothing)
        End Function

        Public Function EsSegura(password As String) As Boolean
            If String.IsNullOrEmpty(password) OrElse password.Length < 8 Then Return False
            Dim mayuscula As Boolean = password.Any(Function(c) Char.IsUpper(c))
            Dim minuscula As Boolean = password.Any(Function(c) Char.IsLower(c))
            Dim numero As Boolean = password.Any(Function(c) Char.IsDigit(c))
            Dim especial As Boolean = password.Any(Function(c) "@#$%^&+=!._-".Contains(c))
            Return mayuscula AndAlso minuscula AndAlso numero AndAlso especial
        End Function

        Public Function RequisitosMensaje() As String
            Return "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=!._-)."
        End Function

        Public Function VerificarPassword(password As String, hashStr As String) As Boolean
            If String.IsNullOrEmpty(password) OrElse String.IsNullOrEmpty(hashStr) Then Return False
            If hashStr.StartsWith("$argon2") Then
                Try
                    Return Argon2.Verify(hashStr, password, 1, Nothing)
                Catch ex As Exception
                    Return False
                End Try
            End If
            Using sha As SHA256 = SHA256.Create()
                Dim hashBytes As Byte() = sha.ComputeHash(Encoding.UTF8.GetBytes(password))
                Dim hashSha256 As String = Convert.ToHexString(hashBytes).ToLowerInvariant()
                Return String.Equals(hashSha256, hashStr, StringComparison.OrdinalIgnoreCase)
            End Using
        End Function
    End Module
End Namespace
