Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelInventario
        Inherits UserControl

        Private ReadOnly dao As New InventarioDAO()
        Private grid As DataGridView
        Private txtBuscar As TextBox

        Public Sub New()
            Me.BackColor = Color.FromArgb(24, 24, 27)
            Me.Dock = DockStyle.Fill

            Dim header = New Panel With {.Dock = DockStyle.Top, .Height = 65, .BackColor = Color.FromArgb(24, 24, 27), .Padding = New Padding(25, 20, 25, 10)}
            Dim lblTitulo = New Label With {.Text = "Control de Inventario", .Font = New Font("Segoe UI", 20, FontStyle.Bold), .ForeColor = Color.White, .AutoSize = True, .Location = New Point(0, 5)}
            header.Controls.Add(lblTitulo)

            Dim toolbar = New Panel With {.Dock = DockStyle.Top, .Height = 50, .BackColor = Color.FromArgb(24, 24, 27), .Padding = New Padding(25, 5, 25, 5)}

            txtBuscar = New TextBox With {.Width = 260, .Height = 26, .BackColor = Color.FromArgb(45, 45, 45), .ForeColor = Color.White, .BorderStyle = BorderStyle.FixedSingle, .Font = New Font("Segoe UI", 11)}
            AddHandler txtBuscar.TextChanged, AddressOf CargarDatos
            toolbar.Controls.Add(txtBuscar)

            If SesionUsuario.TienePermisoAccion("Inventario", "CREAR") Then
                Dim btnEntrada = CrearBoton("Entrada", Color.FromArgb(16, 185, 129))
                AddHandler btnEntrada.Click, Sub(s, ev) RegistrarMovimiento("Entrada")
                toolbar.Controls.Add(btnEntrada)

                Dim btnSalida = CrearBoton("Salida", Color.FromArgb(245, 158, 11))
                AddHandler btnSalida.Click, Sub(s, ev) RegistrarMovimiento("Salida")
                toolbar.Controls.Add(btnSalida)

                Dim btnAjuste = CrearBoton("Ajuste", Color.FromArgb(99, 102, 241))
                AddHandler btnAjuste.Click, Sub(s, ev) RegistrarMovimiento("Ajuste")
                toolbar.Controls.Add(btnAjuste)
            End If

            ' Alinear botones a la derecha
            AddHandler toolbar.Resize, Sub(s, ev)
                                           Dim x = toolbar.Width - 25
                                           For Each ctrl As Control In toolbar.Controls.OfType(Of Button)()
                                               x -= ctrl.Width + 10
                                               ctrl.Location = New Point(x, 5)
                                           Next
                                       End Sub
            toolbar.PerformLayout()

            grid = New DataGridView With {.Dock = DockStyle.Fill, .BackgroundColor = Color.FromArgb(24, 24, 27), .BorderStyle = BorderStyle.None, .ReadOnly = True, .AllowUserToAddRows = False, .RowHeadersVisible = False, .AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill, .SelectionMode = DataGridViewSelectionMode.FullRowSelect, .ColumnHeadersHeight = 32}
            AplicarEstiloGrid(grid)

            Me.Controls.Add(grid)
            Me.Controls.Add(toolbar)
            Me.Controls.Add(header)
            CargarDatos()
        End Sub

        Private Function CrearBoton(texto As String, color As Color) As Button
            Dim btn = New Button With {.Text = texto, .Width = 90, .Height = 32, .FlatStyle = FlatStyle.Flat, .BackColor = color, .ForeColor = Color.White, .Font = New Font("Segoe UI", 10, FontStyle.Bold)}
            btn.FlatAppearance.BorderSize = 0
            Return btn
        End Function

        Private Sub RegistrarMovimiento(tipo As String)
            Using editor As New MovimientoEditorForm(tipo)
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub CargarDatos(sender As Object, e As EventArgs)
            CargarDatos()
        End Sub

        Private Sub CargarDatos()
            Dim texto = txtBuscar.Text.Trim().ToLower()
            grid.DataSource = Nothing
            Dim dt = New DataTable()
            dt.Columns.Add("ID")
            dt.Columns.Add("Producto")
            dt.Columns.Add("Proveedor")
            dt.Columns.Add("Tipo")
            dt.Columns.Add("Cantidad")
            dt.Columns.Add("Precio")
            dt.Columns.Add("Balance")
            dt.Columns.Add("Fecha")
            dt.Columns.Add("Motivo")
            Dim movimientos = dao.ListarMovimientos()
            If Not String.IsNullOrEmpty(texto) Then
                movimientos = movimientos.Where(Function(m) (m.ProductoNombre IsNot Nothing AndAlso m.ProductoNombre.ToLower().Contains(texto)) OrElse (m.ProveedorNombre IsNot Nothing AndAlso m.ProveedorNombre.ToLower().Contains(texto)) OrElse (m.TipoMovimiento IsNot Nothing AndAlso m.TipoMovimiento.ToLower().Contains(texto))).ToList()
            End If
            For Each m In movimientos
                dt.Rows.Add(m.Id, If(m.ProductoNombre, "N/A"), If(m.ProveedorNombre, "—"), m.TipoMovimiento, m.Cantidad, "$" & m.Precio.ToString("F2"), "$" & m.PrecioBalance.ToString("F2"), m.FechaMovimiento.ToString("dd/MM/yyyy HH:mm"), m.Motivo)
            Next
            grid.DataSource = dt
        End Sub

    End Class

End Namespace



