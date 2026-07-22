Imports System.IO
Imports System.Security.Cryptography
Imports System.Text

Namespace inventario_visual

    Public Module AESUtil

        Private ReadOnly SECRET_KEY As String = "AntigravityKey26"
        Private ReadOnly ALGORITHM As String = "AES"

        Public Function Encriptar(textoPlano As String) As String
            If String.IsNullOrEmpty(textoPlano) Then Return textoPlano
            Try
                Dim keyBytes As Byte() = Encoding.UTF8.GetBytes(SECRET_KEY)
                Using aes As Aes = Aes.Create()
                    aes.Key = keyBytes
                    aes.Mode = CipherMode.ECB
                    aes.Padding = PaddingMode.PKCS7
                    Using encryptor As ICryptoTransform = aes.CreateEncryptor()
                        Dim plainBytes As Byte() = Encoding.UTF8.GetBytes(textoPlano)
                        Dim encrypted As Byte() = encryptor.TransformFinalBlock(plainBytes, 0, plainBytes.Length)
                        Return Convert.ToBase64String(encrypted)
                    End Using
                End Using
            Catch ex As Exception
                Return textoPlano
            End Try
        End Function

        Public Function Desencriptar(textoEncriptado As String) As String
            If String.IsNullOrEmpty(textoEncriptado) Then Return textoEncriptado
            Try
                Dim keyBytes As Byte() = Encoding.UTF8.GetBytes(SECRET_KEY)
                Using aes As Aes = Aes.Create()
                    aes.Key = keyBytes
                    aes.Mode = CipherMode.ECB
                    aes.Padding = PaddingMode.PKCS7
                    Using decryptor As ICryptoTransform = aes.CreateDecryptor()
                        Dim encryptedBytes As Byte() = Convert.FromBase64String(textoEncriptado)
                        Dim decrypted As Byte() = decryptor.TransformFinalBlock(encryptedBytes, 0, encryptedBytes.Length)
                        Return Encoding.UTF8.GetString(decrypted)
                    End Using
                End Using
            Catch ex As Exception
                Return textoEncriptado
            End Try
        End Function

    End Module

End Namespace
