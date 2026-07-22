Imports System.Collections.Generic
Imports System.Drawing
Imports System.Globalization
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class ProductoEditorForm
        Inherits Form

        Private ReadOnly producto As Producto
        Private ReadOnly esNuevo As Boolean
        Private txtSku As TextBox
        Private txtNombre As TextBox
        Private txtDescripcion As TextBox
        Private txtVenta As TextBox
        Private txtCompra As TextBox
        Private txtMinimo As TextBox
        Private txtStock As TextBox
        Private cboCategoria As ComboBox
        Private categorias As List(Of Categoria)

        Public Sub New(Optional productoExistente As Producto = Nothing)
            producto = If(productoExistente, New Producto With {.State = True})
            esNuevo = productoExistente Is Nothing
            AplicarEstiloEditor(Me, If(esNuevo, "Nuevo producto", "Editar producto"), 470, 620)
            txtSku = Campo("SKU", 20, producto.Sku)
            txtNombre = Campo("Nombre", 75, producto.Nombre)
            txtDescripcion = Campo("Descripción", 130, producto.Descripcion)
            cboCategoria = CrearCombo(30, 205)
            Controls.Add(CrearLabel("Categoría", 30, 185))
            Controls.Add(cboCategoria)
            categorias = New CategoriaDAO().ListarTodas()
            cboCategoria.Items.Add("(Sin categoría)")
            For Each c In categorias
                cboCategoria.Items.Add(c.Nombre)
            Next
            cboCategoria.SelectedIndex = 0
            If producto.CategoriaId.HasValue Then
                Dim indice As Integer = categorias.FindIndex(Function(c) c.Id = producto.CategoriaId.Value)
                If indice >= 0 Then cboCategoria.SelectedIndex = indice + 1
            End If
            txtVenta = Campo("Precio venta", 245, producto.PrecioVenta.ToString(CultureInfo.InvariantCulture))
            txtCompra = Campo("Costo promedio", 300, producto.PrecioCompra.ToString(CultureInfo.InvariantCulture))
            txtMinimo = Campo("Stock mínimo", 355, producto.StockMinimo.ToString(CultureInfo.InvariantCulture))
            txtStock = Campo("Stock actual", 410, producto.StockActual.ToString(CultureInfo.InvariantCulture))
            Dim guardar = CrearBotonGuardar(290, 490)
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Function Campo(etiqueta As String, y As Integer, valor As String) As TextBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim entrada = CrearTexto(valor, 30, y + 22)
            Controls.Add(entrada)
            Return entrada
        End Function

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            Dim venta, compra, minimo, stock As Double
            If String.IsNullOrWhiteSpace(txtSku.Text) OrElse String.IsNullOrWhiteSpace(txtNombre.Text) OrElse Not Double.TryParse(txtVenta.Text, NumberStyles.Any, CultureInfo.InvariantCulture, venta) OrElse Not Double.TryParse(txtCompra.Text, NumberStyles.Any, CultureInfo.InvariantCulture, compra) OrElse Not Double.TryParse(txtMinimo.Text, NumberStyles.Any, CultureInfo.InvariantCulture, minimo) OrElse Not Double.TryParse(txtStock.Text, NumberStyles.Any, CultureInfo.InvariantCulture, stock) Then
                MessageBox.Show("Complete los campos obligatorios con valores numéricos válidos.", "Productos", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            producto.Sku = txtSku.Text.Trim()
            producto.Nombre = txtNombre.Text.Trim()
            producto.Descripcion = txtDescripcion.Text.Trim()
            producto.CategoriaId = If(cboCategoria.SelectedIndex > 0, CType(categorias(cboCategoria.SelectedIndex - 1).Id, Long?), Nothing)
            producto.PrecioVenta = venta
            producto.PrecioCompra = compra
            producto.StockMinimo = minimo
            producto.StockActual = stock
            producto.State = True
            Dim dao As New ProductoDAO()
            If esNuevo Then
                dao.Insertar(producto)
            Else
                dao.Actualizar(producto)
            End If
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
