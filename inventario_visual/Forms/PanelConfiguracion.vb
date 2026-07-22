Imports System.Collections.Generic
Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelConfiguracion
        Inherits UserControl

        Public Sub New()
            Me.BackColor = Color.FromArgb(24, 24, 27)
            Me.Dock = DockStyle.Fill
            Dim valores = ConfiguracionLocal.Cargar()
            Dim titulo As New Label With {.Text = "Configuración", .Font = New Font("Segoe UI", 24, FontStyle.Bold), .ForeColor = Color.White, .AutoSize = True, .Location = New Point(25, 25)}
            Me.Controls.Add(titulo)
            Dim txtEmpresa = CrearCampo("Empresa", 85, If(valores.ContainsKey("Empresa"), valores("Empresa"), ""))
            Dim txtRif = CrearCampo("RIF / NIT", 150, If(valores.ContainsKey("Rif"), valores("Rif"), ""))
            Dim txtTasa = CrearCampo("Tasa USD a VES", 215, If(valores.ContainsKey("Tasa"), valores("Tasa"), "1"))
            Dim guardar = CrearBotonGuardar(285, 285)
            AddHandler guardar.Click, Sub(s, e) GuardarConfiguracion(txtEmpresa.Text, txtRif.Text, txtTasa.Text)
            Me.Controls.Add(guardar)
        End Sub

        Private Sub GuardarConfiguracion(empresa As String, rif As String, tasa As String)
            ConfiguracionLocal.Guardar(New Dictionary(Of String, String) From {{"Empresa", empresa.Trim()}, {"Rif", rif.Trim()}, {"Tasa", tasa.Trim()}})
            MessageBox.Show("Configuración guardada.")
        End Sub

        Private Function CrearCampo(etiqueta As String, y As Integer, valor As String) As TextBox
            Me.Controls.Add(CrearLabel(etiqueta, 25, y))
            Dim entrada = CrearTexto(valor, 25, y + 22, 380)
            Me.Controls.Add(entrada)
            Return entrada
        End Function

    End Class

End Namespace



