Imports System.Drawing
Imports System.Windows.Forms

Namespace inventario_visual
    Public Module UIHelper

        Public ReadOnly COLOR_BG As Color = Color.FromArgb(24, 24, 27)
        Public ReadOnly COLOR_CARD As Color = Color.FromArgb(39, 39, 42)
        Public ReadOnly COLOR_HEADER As Color = Color.FromArgb(30, 30, 30)
        Public ReadOnly COLOR_TEXT As Color = Color.White
        Public ReadOnly COLOR_TEXT_MUTED As Color = Color.FromArgb(180, 180, 180)
        Public ReadOnly COLOR_GREEN As Color = Color.FromArgb(16, 185, 129)
        Public ReadOnly COLOR_BLUE As Color = Color.FromArgb(59, 130, 246)
        Public ReadOnly COLOR_RED As Color = Color.FromArgb(239, 68, 68)
        Public ReadOnly COLOR_YELLOW As Color = Color.FromArgb(245, 158, 11)
        Public ReadOnly COLOR_PURPLE As Color = Color.FromArgb(139, 92, 246)
        Public ReadOnly COLOR_BORDER As Color = Color.FromArgb(63, 63, 70)

        Public Sub AplicarEstiloGrid(grid As DataGridView)
            grid.BackgroundColor = COLOR_BG
            grid.BorderStyle = BorderStyle.None
            grid.ReadOnly = True
            grid.AllowUserToAddRows = False
            grid.RowHeadersVisible = False
            grid.SelectionMode = DataGridViewSelectionMode.FullRowSelect
            grid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill
            grid.ColumnHeadersHeight = 34
            grid.RowTemplate.Height = 32
            grid.CellBorderStyle = DataGridViewCellBorderStyle.SingleHorizontal
            grid.GridColor = Color.FromArgb(55, 55, 55)
            grid.DefaultCellStyle.BackColor = COLOR_CARD
            grid.DefaultCellStyle.ForeColor = COLOR_TEXT
            grid.DefaultCellStyle.SelectionBackColor = Color.FromArgb(16, 185, 129, 50)
            grid.DefaultCellStyle.SelectionForeColor = Color.White
            grid.DefaultCellStyle.Font = New Font("Segoe UI", 12)
            grid.ColumnHeadersDefaultCellStyle.BackColor = COLOR_HEADER
            grid.ColumnHeadersDefaultCellStyle.ForeColor = COLOR_TEXT_MUTED
            grid.ColumnHeadersDefaultCellStyle.Font = New Font("Segoe UI", 12, FontStyle.Bold)
            grid.ColumnHeadersBorderStyle = DataGridViewHeaderBorderStyle.None
            grid.EnableHeadersVisualStyles = False
            grid.AdvancedCellBorderStyle.Left = DataGridViewAdvancedCellBorderStyle.None
            grid.AdvancedCellBorderStyle.Right = DataGridViewAdvancedCellBorderStyle.None
            grid.AdvancedCellBorderStyle.Top = DataGridViewAdvancedCellBorderStyle.None
            grid.AdvancedCellBorderStyle.Bottom = DataGridViewAdvancedCellBorderStyle.Single
        End Sub

        Public Function CrearTextoBuscar() As TextBox
            Dim txt = New TextBox With {.Width = 250, .Height = 32, .BackColor = COLOR_CARD, .ForeColor = COLOR_TEXT, .BorderStyle = BorderStyle.FixedSingle, .Font = New Font("Segoe UI", 11)}
            Return txt
        End Function

        Public Function CrearBotonToolbar(texto As String, color As Color, Optional ancho As Integer = 90) As Button
            Dim btn = New Button With {.Text = texto, .Width = ancho, .Height = 32, .FlatStyle = FlatStyle.Flat, .BackColor = color, .ForeColor = Color.White, .Font = New Font("Segoe UI", 9, FontStyle.Bold)}
            btn.FlatAppearance.BorderSize = 0
            btn.Cursor = Cursors.Hand
            Return btn
        End Function

        Public Function CrearBotonSecundarioToolbar(texto As String, Optional ancho As Integer = 80) As Button
            Dim btn = New Button With {.Text = texto, .Width = ancho, .Height = 32, .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(55, 65, 81), .ForeColor = Color.White, .Font = New Font("Segoe UI", 9)}
            btn.FlatAppearance.BorderSize = 0
            btn.Cursor = Cursors.Hand
            Return btn
        End Function

        Public Function CrearLabel(etiqueta As String, x As Integer, y As Integer) As Label
            Return New Label With {.Text = etiqueta, .Location = New Point(x, y), .AutoSize = True, .ForeColor = Color.FromArgb(180, 180, 180), .Font = New Font("Segoe UI", 10)}
        End Function

        Public Function CrearTexto(valor As String, x As Integer, y As Integer, Optional ancho As Integer = 380) As TextBox
            Return New TextBox With {.Location = New Point(x, y), .Size = New Size(ancho, 28), .BackColor = Color.FromArgb(45, 45, 45), .ForeColor = Color.White, .BorderStyle = BorderStyle.FixedSingle, .Text = valor, .Font = New Font("Segoe UI", 11)}
        End Function

        Public Function CrearCombo(x As Integer, y As Integer, Optional ancho As Integer = 380) As ComboBox
            Return New ComboBox With {.Location = New Point(x, y), .Size = New Size(ancho, 28), .BackColor = Color.FromArgb(45, 45, 45), .ForeColor = Color.White, .FlatStyle = FlatStyle.Flat, .DropDownStyle = ComboBoxStyle.DropDownList, .Font = New Font("Segoe UI", 11)}
        End Function

        Public Function CrearBotonGuardar(x As Integer, y As Integer, Optional ancho As Integer = 120) As Button
            Dim btn = New Button With {.Text = "Guardar", .Location = New Point(x, y), .Size = New Size(ancho, 36), .BackColor = Color.FromArgb(16, 185, 129), .ForeColor = Color.White, .FlatStyle = FlatStyle.Flat, .Font = New Font("Segoe UI", 10, FontStyle.Bold)}
            btn.FlatAppearance.BorderSize = 0
            btn.Cursor = Cursors.Hand
            Return btn
        End Function

        Public Sub AplicarEstiloEditor(form As Form, titulo As String, ancho As Integer, alto As Integer)
            form.Text = titulo
            form.Size = New Size(ancho, alto)
            form.StartPosition = FormStartPosition.CenterParent
            form.BackColor = Color.FromArgb(30, 30, 30)
            form.FormBorderStyle = FormBorderStyle.FixedDialog
            form.MaximizeBox = False
        End Sub

    End Module
End Namespace
