Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelProveedores
        Inherits UserControl

        Private dao As New ProveedorDAO()
        Private grid As DataGridView
        Private txtBuscar As TextBox

        Public Sub New()
            Me.BackColor = Color.FromArgb(24, 24, 27)
            Me.Dock = DockStyle.Fill

            Dim lblTitulo = New Label()
            lblTitulo.Text = "Proveedores"
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

            If SesionUsuario.TienePermisoAccion("Proveedores", "CREAR") Then
                Dim btnNuevo = New Button()
                btnNuevo.Text = "+ Nuevo"
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
            If SesionUsuario.TienePermisoAccion("Proveedores", "EDITAR") Then
                Dim btnEditar As New Button With {.Text = "Editar", .Location = New Point(toolbar.Width - 195, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(59, 130, 246), .ForeColor = Color.White}
                btnEditar.FlatAppearance.BorderSize = 0
                AddHandler btnEditar.Click, AddressOf Editar_Click
                toolbar.Controls.Add(btnEditar)
            End If
            If SesionUsuario.TienePermisoAccion("Proveedores", "ELIMINAR") Then
                Dim btnEliminar As New Button With {.Text = "Eliminar", .Location = New Point(toolbar.Width - 290, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(239, 68, 68), .ForeColor = Color.White}
                btnEliminar.FlatAppearance.BorderSize = 0
                AddHandler btnEliminar.Click, AddressOf Eliminar_Click
                toolbar.Controls.Add(btnEliminar)
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
            Using editor As New ProveedorEditorForm()
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub Editar_Click(sender As Object, e As EventArgs)
            If grid.SelectedRows.Count = 0 Then Return
            Dim id As Long = Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value)
            Dim proveedor As Proveedor = dao.ListarTodos().FirstOrDefault(Function(p) p.Id = id)
            If proveedor Is Nothing Then Return
            Using editor As New ProveedorEditorForm(proveedor)
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub Eliminar_Click(sender As Object, e As EventArgs)
            If grid.SelectedRows.Count = 0 Then Return
            If MessageBox.Show("¿Eliminar el proveedor seleccionado?", "Proveedores", MessageBoxButtons.YesNo, MessageBoxIcon.Warning) = DialogResult.Yes Then
                dao.Eliminar(Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value))
                CargarDatos()
            End If
        End Sub

        Private Sub Buscar_Click(sender As Object, e As EventArgs)
            Dim texto As String = txtBuscar.Text.Trim().ToLower()
            Dim lista = dao.ListarTodos().Where(Function(p) p.NombreEmpresa.ToLower().Contains(texto) OrElse p.NitCedula.ToLower().Contains(texto) OrElse p.Correo.ToLower().Contains(texto)).ToList()
            CargarGrid(lista)
        End Sub

        Private Sub CargarDatos()
            CargarGrid(dao.ListarTodos())
        End Sub

        Private Sub CargarGrid(lista As List(Of Proveedor))
            grid.DataSource = Nothing
            Dim dt = New DataTable()
            dt.Columns.Add("ID")
            dt.Columns.Add("Empresa")
            dt.Columns.Add("NIT/Cédula")
            dt.Columns.Add("Teléfono")
            dt.Columns.Add("Correo")
            dt.Columns.Add("Contacto")
            For Each p In lista
                dt.Rows.Add(p.Id, p.NombreEmpresa, p.NitCedula, p.Telefono, p.Correo, p.NombreContacto)
            Next
            grid.DataSource = dt
        End Sub

    End Class

End Namespace



