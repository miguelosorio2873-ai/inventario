Imports System.Drawing
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class CategoriaEditorForm
        Inherits Form

        Private ReadOnly categoria As Categoria
        Private ReadOnly esNueva As Boolean
        Private txtNombre As TextBox
        Private txtDescripcion As TextBox

        Public Sub New(Optional categoriaExistente As Categoria = Nothing)
            categoria = If(categoriaExistente, New Categoria())
            esNueva = categoriaExistente Is Nothing
            AplicarEstiloEditor(Me, If(esNueva, "Nueva categoría", "Editar categoría"), 420, 290)
            txtNombre = Campo("Nombre", 30)
            txtDescripcion = Campo("Descripción", 85)
            txtNombre.Text = categoria.Nombre
            txtDescripcion.Text = categoria.Descripcion
            Dim guardar = CrearBotonGuardar(245, 190)
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Function Campo(etiqueta As String, y As Integer) As TextBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim entrada = CrearTexto("", 30, y + 22, 335)
            Controls.Add(entrada)
            Return entrada
        End Function

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            If String.IsNullOrWhiteSpace(txtNombre.Text) Then
                MessageBox.Show("El nombre es obligatorio.", "Categorías", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            categoria.Nombre = txtNombre.Text.Trim()
            categoria.Descripcion = txtDescripcion.Text.Trim()
            Dim dao As New CategoriaDAO()
            If esNueva Then
                dao.Insertar(categoria)
            Else
                dao.Actualizar(categoria)
            End If
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
