Imports System.Drawing
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class ClienteEditorForm
        Inherits Form

        Private ReadOnly cliente As Cliente
        Private ReadOnly esNuevo As Boolean
        Private txtCedula As TextBox
        Private txtNombre As TextBox
        Private txtCorreo As TextBox
        Private txtTelefono As TextBox

        Public Sub New(Optional clienteExistente As Cliente = Nothing)
            cliente = If(clienteExistente, New Cliente())
            esNuevo = clienteExistente Is Nothing
            AplicarEstiloEditor(Me, If(esNuevo, "Nuevo cliente", "Editar cliente"), 420, 350)

            txtCedula = AgregarCampo("Cédula", 30)
            txtNombre = AgregarCampo("Nombre", 85)
            txtCorreo = AgregarCampo("Correo", 140)
            txtTelefono = AgregarCampo("Teléfono", 195)
            txtCedula.Text = cliente.Cedula
            txtNombre.Text = cliente.Nombre
            txtCorreo.Text = cliente.Correo
            txtTelefono.Text = cliente.Telefono

            Dim guardar = CrearBotonGuardar(245, 260)
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Function AgregarCampo(etiqueta As String, y As Integer) As TextBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim campo = CrearTexto("", 30, y + 22, 335)
            Controls.Add(campo)
            Return campo
        End Function

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            If String.IsNullOrWhiteSpace(txtNombre.Text) Then
                MessageBox.Show("El nombre es obligatorio.", "Cliente", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            cliente.Cedula = txtCedula.Text.Trim()
            cliente.Nombre = txtNombre.Text.Trim()
            cliente.Correo = txtCorreo.Text.Trim()
            cliente.Telefono = txtTelefono.Text.Trim()
            Dim dao As New ClienteDAO()
            If esNuevo Then
                dao.Insertar(cliente)
            Else
                dao.Actualizar(cliente)
            End If
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
