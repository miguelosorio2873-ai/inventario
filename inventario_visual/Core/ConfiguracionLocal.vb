Imports System.Collections.Generic
Imports System.IO
Imports System.Linq

Namespace inventario_visual
    Public Module ConfiguracionLocal
        Private ReadOnly Ruta As String = Path.Combine(AppContext.BaseDirectory, "configuracion.txt")

        Public Function Cargar() As Dictionary(Of String, String)
            Dim valores As New Dictionary(Of String, String)(StringComparer.OrdinalIgnoreCase)
            If Not File.Exists(Ruta) Then Return valores
            For Each linea As String In File.ReadAllLines(Ruta)
                Dim partes As String() = linea.Split(New Char() {"="c}, 2)
                If partes.Length = 2 Then valores(partes(0)) = partes(1)
            Next
            Return valores
        End Function

        Public Sub Guardar(valores As Dictionary(Of String, String))
            File.WriteAllLines(Ruta, valores.Select(Function(par) par.Key & "=" & par.Value))
        End Sub
    End Module
End Namespace
