Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelFacturas
        Inherits UserControl

        Private dao As New FacturaDAO()
        Private grid As DataGridView
        Private txtBuscar As TextBox

        Public Sub New()
            Me.BackColor = Color.FromArgb(24, 24, 27)
            Me.Dock = DockStyle.Fill

            Dim lblTitulo = New Label()
            lblTitulo.Text = "Facturas"
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

            txtBuscar = New TextBox With {.Location = New Point(0, 7), .Width = 250, .BackColor = Color.FromArgb(45, 45, 45), .ForeColor = Color.White, .BorderStyle = BorderStyle.FixedSingle}
            toolbar.Controls.Add(txtBuscar)
            Dim btnBuscar As New Button With {.Text = "Buscar", .Location = New Point(260, 5), .Width = 80, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(55, 65, 81), .ForeColor = Color.White}
            btnBuscar.FlatAppearance.BorderSize = 0
            AddHandler btnBuscar.Click, AddressOf Buscar_Click
            toolbar.Controls.Add(btnBuscar)

            If SesionUsuario.TienePermisoAccion("Facturas", "CREAR") Then
                Dim btnNuevo = New Button()
                btnNuevo.Text = "+ Nueva"
                btnNuevo.Location = New Point(toolbar.Width - 100, 5)
                btnNuevo.Anchor = AnchorStyles.Top Or AnchorStyles.Right
                btnNuevo.Width = 90
                btnNuevo.FlatStyle = FlatStyle.Flat
                btnNuevo.BackColor = Color.FromArgb(16, 185, 129)
                btnNuevo.ForeColor = Color.White
                btnNuevo.FlatAppearance.BorderSize = 0
                AddHandler btnNuevo.Click, AddressOf Nuevo_Click
                toolbar.Controls.Add(btnNuevo)
            End If
            If SesionUsuario.TienePermisoAccion("Facturas", "EDITAR") Then
                Dim btnPagada As New Button With {.Text = "Pagada", .Location = New Point(toolbar.Width - 195, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(16, 185, 129), .ForeColor = Color.White}
                btnPagada.FlatAppearance.BorderSize = 0
                AddHandler btnPagada.Click, Sub(s, ev) CambiarEstado("Pagada")
                toolbar.Controls.Add(btnPagada)
                Dim btnAnular As New Button With {.Text = "Anular", .Location = New Point(toolbar.Width - 290, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(239, 68, 68), .ForeColor = Color.White}
                btnAnular.FlatAppearance.BorderSize = 0
                AddHandler btnAnular.Click, Sub(s, ev) CambiarEstado("Anulada")
                toolbar.Controls.Add(btnAnular)
            End If

            grid = New DataGridView()
            grid.Location = New Point(25, 120)
            grid.Size = New Size(Me.Width - 50, Me.Height - 155)
            grid.Anchor = AnchorStyles.Top Or AnchorStyles.Bottom Or AnchorStyles.Left Or AnchorStyles.Right
            AplicarEstiloGrid(grid)
            Me.Controls.Add(grid)
            CargarDatos()
        End Sub

        Private Sub Nuevo_Click(sender As Object, e As EventArgs)
            Using editor As New FacturaEditorForm()
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub CambiarEstado(estado As String)
            If grid.SelectedRows.Count = 0 Then Return
            dao.ActualizarEstado(Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value), estado)
            CargarDatos()
        End Sub

        Private Sub Buscar_Click(sender As Object, e As EventArgs)
            Dim texto As String = txtBuscar.Text.Trim().ToLower()
            CargarGrid(dao.ListarTodas().Where(Function(f) f.NumeroFactura.ToLower().Contains(texto) OrElse f.ClienteNombre.ToLower().Contains(texto) OrElse f.Estado.ToLower().Contains(texto)).ToList())
        End Sub

        Private Sub CargarDatos()
            CargarGrid(dao.ListarTodas())
        End Sub

        Private Sub CargarGrid(lista As List(Of Factura))
            grid.DataSource = Nothing
            Dim dt = New DataTable()
            dt.Columns.Add("ID")
            dt.Columns.Add("N. Factura")
            dt.Columns.Add("Cliente")
            dt.Columns.Add("Fecha")
            dt.Columns.Add("Método Pago")
            dt.Columns.Add("Subtotal")
            dt.Columns.Add("Impuestos")
            dt.Columns.Add("Total")
            dt.Columns.Add("Estado")
            For Each f In lista
                dt.Rows.Add(f.Id, f.NumeroFactura, f.ClienteNombre, f.FechaEmision.ToString("yyyy-MM-dd"), f.MetodoPago, f.Subtotal.ToString("C2"), f.Impuestos.ToString("C2"), f.Total.ToString("C2"), f.Estado)
            Next
            grid.DataSource = dt
        End Sub

    End Class

End Namespace



