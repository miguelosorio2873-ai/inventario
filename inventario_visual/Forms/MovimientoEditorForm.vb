Imports System.Collections.Generic
Imports System.Drawing
Imports System.Globalization
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class MovimientoEditorForm
        Inherits Form

        Private ReadOnly tipoInicial As String
        Private productos As List(Of Producto)
        Private proveedores As List(Of Proveedor)
        Private cboProducto As ComboBox
        Private cboProveedor As ComboBox
        Private cboTipo As ComboBox
        Private txtPrecio As TextBox
        Private txtCantidad As TextBox
        Private txtMotivo As TextBox

        Public Sub New(tipo As String)
            tipoInicial = tipo
            AplicarEstiloEditor(Me, "Registrar " & tipo, 440, 520)
            productos = New ProductoDAO().ListarTodos()
            proveedores = New ProveedorDAO().ListarTodos()
            cboProducto = Combo("Producto", 20, productos.Select(Function(p) p.Id & " - " & p.Nombre & " (Stock: " & p.StockActual & ")").ToArray())
            cboProveedor = Combo("Proveedor", 85, (New String() {"(Sin proveedor)"}).Concat(proveedores.Select(Function(p) p.Id & " - " & p.NombreEmpresa)).ToArray())
            cboTipo = Combo("Tipo", 150, New String() {"Entrada", "Salida", "Ajuste"})
            cboTipo.SelectedItem = tipoInicial
            cboTipo.Enabled = String.IsNullOrEmpty(tipoInicial)
            txtPrecio = Campo("Precio unitario", 215, "0")
            txtCantidad = Campo("Cantidad", 270, "")
            txtMotivo = Campo("Motivo", 325, "")
            Dim guardar = CrearBotonGuardar(275, 410)
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Function Combo(etiqueta As String, y As Integer, valores As String()) As ComboBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim selector = CrearCombo(30, y + 22, 365)
            selector.Items.AddRange(valores)
            If selector.Items.Count > 0 Then selector.SelectedIndex = 0
            Controls.Add(selector)
            Return selector
        End Function

        Private Function Campo(etiqueta As String, y As Integer, valor As String) As TextBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim entrada = CrearTexto(valor, 30, y + 22, 365)
            Controls.Add(entrada)
            Return entrada
        End Function

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            Dim precio, cantidad As Double
            If cboProducto.SelectedIndex < 0 OrElse Not Double.TryParse(txtPrecio.Text, NumberStyles.Any, CultureInfo.InvariantCulture, precio) OrElse Not Double.TryParse(txtCantidad.Text, NumberStyles.Any, CultureInfo.InvariantCulture, cantidad) OrElse cantidad <= 0 Then
                MessageBox.Show("Seleccione un producto e indique precio y cantidad válidos.", "Inventario", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            Dim producto As Producto = productos(cboProducto.SelectedIndex)
            Dim cantidadFinal As Double = If(cboTipo.Text = "Salida", -cantidad, cantidad)
            If cantidadFinal < 0 AndAlso Math.Abs(cantidadFinal) > producto.StockActual Then
                MessageBox.Show("Stock insuficiente. Disponible: " & producto.StockActual, "Inventario", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            Dim movimiento As New MovimientoInventario With {.ProductoId = producto.Id, .TipoMovimiento = cboTipo.Text, .Precio = precio, .PrecioBalance = precio * cantidad, .Cantidad = If(cboTipo.Text = "Salida", -cantidad, cantidad), .Motivo = txtMotivo.Text.Trim()}
            If cboProveedor.SelectedIndex > 0 Then movimiento.ProveedorId = proveedores(cboProveedor.SelectedIndex - 1).Id
            Dim dao As New InventarioDAO()
            dao.RegistrarMovimiento(movimiento)
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
