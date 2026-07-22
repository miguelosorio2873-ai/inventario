Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelCategorias
        Inherits UserControl

        Private dao As New CategoriaDAO()
        Private grid As DataGridView

        Public Sub New()
            Me.BackColor = Color.FromArgb(24, 24, 27)
            Me.Dock = DockStyle.Fill

            Dim lblTitulo = New Label()
            lblTitulo.Text = "CategorÃ­as"
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

            If SesionUsuario.TienePermisoAccion("Categorias", "CREAR") Then
                Dim btnNuevo = New Button()
                btnNuevo.Text = "+ Nueva"
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

            If SesionUsuario.TienePermisoAccion("Categorias", "EDITAR") Then
                Dim btnEditar As New Button With {.Text = "Editar", .Location = New Point(toolbar.Width - 195, 5), .Anchor = AnchorStyles.Top Or AnchorStyles.Right, .Width = 85, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(59, 130, 246), .ForeColor = Color.White}
                btnEditar.FlatAppearance.BorderSize = 0
                AddHandler btnEditar.Click, AddressOf Editar_Click
                toolbar.Controls.Add(btnEditar)
            End If
            If SesionUsuario.TienePermisoAccion("Categorias", "ELIMINAR") Then
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
            Using editor As New CategoriaEditorForm()
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub Editar_Click(sender As Object, e As EventArgs)
            If grid.SelectedRows.Count = 0 Then Return
            Dim id As Long = Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value)
            Dim categoria As Categoria = dao.ListarTodas().FirstOrDefault(Function(c) c.Id = id)
            If categoria Is Nothing Then Return
            Using editor As New CategoriaEditorForm(categoria)
                If editor.ShowDialog() = DialogResult.OK Then CargarDatos()
            End Using
        End Sub

        Private Sub Eliminar_Click(sender As Object, e As EventArgs)
            If grid.SelectedRows.Count = 0 Then Return
            If MessageBox.Show("¿Eliminar la categoría seleccionada?", "Categorías", MessageBoxButtons.YesNo, MessageBoxIcon.Warning) = DialogResult.Yes Then
                dao.Eliminar(Convert.ToInt64(grid.SelectedRows(0).Cells("ID").Value))
                CargarDatos()
            End If
        End Sub

        Private Sub CargarDatos()
            Dim lista = dao.ListarTodas()
            Dim dt = New DataTable()
            dt.Columns.Add("ID")
            dt.Columns.Add("Nombre")
            dt.Columns.Add("DescripciÃ³n")
            For Each c In lista
                dt.Rows.Add(c.Id, c.Nombre, c.Descripcion)
            Next
            grid.DataSource = dt
        End Sub

    End Class

End Namespace



