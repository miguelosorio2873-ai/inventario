Imports System.Collections.Generic
Imports System.Drawing
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class ProveedorEditorForm
        Inherits Form

        Private ReadOnly proveedor As Proveedor
        Private ReadOnly esNuevo As Boolean
        Private campos As New Dictionary(Of String, TextBox)()

        Public Sub New(Optional proveedorExistente As Proveedor = Nothing)
            proveedor = If(proveedorExistente, New Proveedor())
            esNuevo = proveedorExistente Is Nothing
            AplicarEstiloEditor(Me, If(esNuevo, "Nuevo proveedor", "Editar proveedor"), 440, 510)
            CrearCampo("Empresa", 20, proveedor.NombreEmpresa)
            CrearCampo("NIT/Cédula", 75, proveedor.NitCedula)
            CrearCampo("Teléfono", 130, proveedor.Telefono)
            CrearCampo("Dirección", 185, proveedor.Direccion)
            CrearCampo("Correo", 240, proveedor.Correo)
            CrearCampo("Contacto", 295, proveedor.NombreContacto)
            Dim guardar = CrearBotonGuardar(265, 390)
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Sub CrearCampo(etiqueta As String, y As Integer, valor As String)
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim campo = CrearTexto(valor, 30, y + 22, 355)
            campos.Add(etiqueta, campo)
            Controls.Add(campo)
        End Sub

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            If String.IsNullOrWhiteSpace(campos("Empresa").Text) Then
                MessageBox.Show("La empresa es obligatoria.", "Proveedores", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            proveedor.NombreEmpresa = campos("Empresa").Text.Trim()
            proveedor.NitCedula = campos("NIT/Cédula").Text.Trim()
            proveedor.Telefono = campos("Teléfono").Text.Trim()
            proveedor.Direccion = campos("Dirección").Text.Trim()
            proveedor.Correo = campos("Correo").Text.Trim()
            proveedor.NombreContacto = campos("Contacto").Text.Trim()
            Dim dao As New ProveedorDAO()
            If esNuevo Then
                dao.Insertar(proveedor)
            Else
                dao.Actualizar(proveedor)
            End If
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
