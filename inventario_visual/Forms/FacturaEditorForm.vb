Imports System.Collections.Generic
Imports System.Drawing
Imports System.Globalization
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class FacturaEditorForm
        Inherits Form

        Private clientes As List(Of Cliente)
        Private cboCliente As ComboBox
        Private cboMetodo As ComboBox
        Private txtSubtotal As TextBox
        Private txtImpuestos As TextBox

        Public Sub New()
            AplicarEstiloEditor(Me, "Nueva factura", 430, 390)
            clientes = New ClienteDAO().ListarTodos()
            Controls.Add(CrearLabel("Cliente", 30, 30))
            cboCliente = CrearCombo(30, 52, 350)
            cboCliente.Items.Add("(Sin cliente)")
            For Each cliente In clientes
                cboCliente.Items.Add(cliente.Id & " - " & cliente.Nombre)
            Next
            cboCliente.SelectedIndex = 0
            Controls.Add(cboCliente)
            Controls.Add(CrearLabel("Método de pago", 30, 100))
            cboMetodo = CrearCombo(30, 122, 350)
            cboMetodo.Items.AddRange(New String() {"Efectivo", "Transferencia", "Tarjeta", "Pago móvil"})
            cboMetodo.SelectedIndex = 0
            Controls.Add(cboMetodo)
            txtSubtotal = Campo("Subtotal", 170, "0")
            txtImpuestos = Campo("Impuestos", 225, "0")
            Dim guardar = CrearBotonGuardar(240, 285, 140)
            guardar.Text = "Emitir factura"
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Function Campo(etiqueta As String, y As Integer, valor As String) As TextBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim entrada = CrearTexto(valor, 30, y + 22, 350)
            Controls.Add(entrada)
            Return entrada
        End Function

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            Dim subtotal, impuestos As Double
            If Not Double.TryParse(txtSubtotal.Text, NumberStyles.Any, CultureInfo.InvariantCulture, subtotal) OrElse Not Double.TryParse(txtImpuestos.Text, NumberStyles.Any, CultureInfo.InvariantCulture, impuestos) OrElse subtotal < 0 OrElse impuestos < 0 Then
                MessageBox.Show("Ingrese importes válidos.", "Facturas", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            Dim dao As New FacturaDAO()
            Dim factura As New Factura With {.NumeroFactura = dao.GenerarNumero(), .MetodoPago = cboMetodo.Text, .Estado = "Pendiente", .Subtotal = subtotal, .Impuestos = impuestos, .Total = subtotal + impuestos}
            If cboCliente.SelectedIndex > 0 Then factura.ClienteId = clientes(cboCliente.SelectedIndex - 1).Id
            dao.Insertar(factura)
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
