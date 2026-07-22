Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelProductos
        Inherits UserControl

        Private dao As New ProductoDAO()
        Private grid As DataGridView
        Private txtBuscar As TextBox
        Private productoEditando As Producto

        Public Sub New()
            Me.BackColor = Color.FromArgb(24, 24, 27)
            Me.Dock = DockStyle.Fill

            Dim lblTitulo = New Label()
            lblTitulo.Text = "Productos"
            lblTitulo.Font = New Font("Segoe UI", 20, FontStyle.Bold)
            lblTitulo.ForeColor = Color.White
            lblTitulo.AutoSize = True
            lblTitulo.Location = New Point(25, 25)
            Me.Controls.Add(lblTitulo)

            Dim toolbar = New Panel()
            toolbar.Location = New Point(25, 70)
            toolbar.Size = New Size(Me.Width - 50, 40)
            toolbar.Anchor = AnchorStyles.Top Or AnchorStyles.Left Or AnchorStyles.Right
            toolbar.BackColor = Color.FromArgb(24, 24, 27)
            Me.Controls.Add(toolbar)

            txtBuscar = New TextBox()
            txtBuscar.Width = 250
            txtBuscar.Height = 25
            txtBuscar.Location = New Point(0, 7)
            txtBuscar.BackColor = Color.FromArgb(45, 45, 45)
            txtBuscar.ForeColor = Color.White
            txtBuscar.BorderStyle = BorderStyle.FixedSingle
            txtBuscar.Font = New Font("Segoe UI", 10)
            toolbar.Controls.Add(txtBuscar)

            Dim btnBuscar = New Button()
            btnBuscar.Text = "Buscar"
            btnBuscar.Location = New Point(260, 5)
            btnBuscar.Width = 80
            btnBuscar.FlatStyle = FlatStyle.Flat
            btnBuscar.BackColor = Color.FromArgb(55, 65, 81)
            btnBuscar.ForeColor = Color.White
            btnBuscar.FlatAppearance.BorderSize = 0
            AddHandler btnBuscar.Click, AddressOf Buscar_Click
            toolbar.Controls.Add(btnBuscar)

            Dim btnLimpiar = New Button()
            btnLimpiar.Text = "Limpiar"
            btnLimpiar.Location = New Point(350, 5)
            btnLimpiar.Width = 80
            btnLimpiar.FlatStyle = FlatStyle.Flat
            btnLimpiar.BackColor = Color.FromArgb(55, 65, 81)
            btnLimpiar.ForeColor = Color.White
            btnLimpiar.FlatAppearance.BorderSize = 0
            AddHandler btnLimpiar.Click, AddressOf Limpiar_Click
            toolbar.Controls.Add(btnLimpiar)

            If SesionUsuario.TienePermisoAccion("Productos", "ELIMINAR") Then
                Dim btnEliminar As New Button With {.Text = "Eliminar", .Location = New Point(toolbar.Width - 290, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(239, 68, 68), .ForeColor = Color.White}
                btnEliminar.FlatAppearance.BorderSize = 0
                AddHandler btnEliminar.Click, AddressOf Eliminar_Click
                toolbar.Controls.Add(btnEliminar)
            End If
            If SesionUsuario.TienePermisoAccion("Productos", "EDITAR") Then
                Dim btnEditar As New Button With {.Text = "Editar", .Location = New Point(toolbar.Width - 195, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(59, 130, 246), .ForeColor = Color.White}
                btnEditar.FlatAppearance.BorderSize = 0
                AddHandler btnEditar.Click, AddressOf Editar_Click
                toolbar.Controls.Add(btnEditar)
            End If
            If SesionUsuario.TienePermisoAccion("Productos", "CREAR") Then
                Dim btnNuevo = New Button()
                btnNuevo.Text = "+ Nuevo"
                btnNuevo.Location = New Point(toolbar.Width - 100, 5)
                btnNuevo.Anchor = AnchorStyles.Top Or AnchorStyles.Right
                btnNuevo.Width = 90
                btnNuevo.FlatStyle = FlatStyle.Flat
                btnNuevo.BackColor = Color.FromArgb(16, 185, 129)
                btnNuevo.ForeColor = Color.White
                btnNuevo.FlatAppearance.BorderSize = 0
                btnNuevo.Font = New Font("Segoe UI", 9, FontStyle.Bold)
                AddHandler btnNuevo.Click, AddressOf Nuevo_Click
                toolbar.Controls.Add(btnNuevo)
            End If

            grid = New DataGridView()
            grid.Location = New Point(25, 120)
            grid.Size = New Size(Me.Width - 50, Me.Height - 155)
            grid.Anchor = AnchorStyles.Top Or AnchorStyles.Bottom Or AnchorStyles.Left Or AnchorStyles.Right
            AplicarEstiloGrid(grid)
            Me.Controls.Add(grid)

            CargarDatos()
        End Sub

        Private Sub CargarDatos()
            grid.DataSource = Nothing
            Dim lista = dao.ListarTodos()
            Dim dt = New DataTable()
            dt.Columns.Add("ID")
            dt.Columns.Add("SKU")
            dt.Columns.Add("Nombre")
            dt.Columns.Add("Categoría")
            dt.Columns.Add("Precio")
            dt.Columns.Add("Stock")
            dt.Columns.Add("Stock Mín")
            dt.Columns.Add("Estado")
            For Each p In lista
                dt.Rows.Add(p.Id, p.Sku, p.Nombre, p.CategoriaNombre, p.PrecioVenta.ToString("C2"), p.StockActual, p.StockMinimo, If(p.State, "Activo", "Inactivo"))
            Next
            grid.DataSource = dt
        End Sub

        Private Sub Buscar_Click(sender As Object, e As EventArgs)
            Dim texto = txtBuscar.Text.Trim()
            grid.DataSource = Nothing
            Dim lista = If(String.IsNullOrEmpty(texto), dao.ListarTodos(), dao.Buscar(texto))
            Dim dt = New DataTable()
            dt.Columns.Add("ID")
            dt.Columns.Add("SKU")
            dt.Columns.Add("Nombre")
            dt.Columns.Add("Categoría")
            dt.Columns.Add("Precio")
            dt.Columns.Add("Stock")
            dt.Columns.Add("Stock Mín")
            dt.Columns.Add("Estado")
            For Each p In lista
                dt.Rows.Add(p.Id, p.Sku, p.Nombre, p.CategoriaNombre, p.PrecioVenta.ToString("C2"), p.StockActual, p.StockMinimo, If(p.State, "Activo", "Inactivo"))
            Next
            grid.DataSource = dt
        End Sub

        Private Sub Limpiar_Click(sender As Object, e As EventArgs)
            txtBuscar.Clear()
            CargarDatos()
        End Sub

        Private Sub Nuevo_Click(sender As Object, e As EventArgs)
            Using editor As New ProductoEditorForm()
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub Editar_Click(sender As Object, e As EventArgs)
            If grid.SelectedRows.Count = 0 Then Return
            Dim producto As Producto = dao.ObtenerPorId(Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value))
            If producto Is Nothing Then Return
            Using editor As New ProductoEditorForm(producto)
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub Eliminar_Click(sender As Object, e As EventArgs)
            If grid.SelectedRows.Count = 0 Then Return
            If MessageBox.Show("¿Eliminar el producto seleccionado?", "Productos", MessageBoxButtons.YesNo, MessageBoxIcon.Warning) = DialogResult.Yes Then
                dao.Eliminar(Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value))
                CargarDatos()
            End If
        End Sub

    End Class

End Namespace



