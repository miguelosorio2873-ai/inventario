Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Partial Public Class DashboardForm
        Inherits Form

        Private sidePanel As Panel
        Private contentPanel As Panel
        Private topBar As Panel
        Private activeButton As Button
        Private currentControl As UserControl
        Private clockLabel As Label
        Private ReadOnly COLOR_SIDEBAR As Color = Color.FromArgb(17, 24, 39)
        Private ReadOnly COLOR_SIDEBAR_HOVER As Color = Color.FromArgb(31, 41, 55)
        Private ReadOnly COLOR_ACTIVE As Color = Color.FromArgb(16, 185, 129)
        Private ReadOnly COLOR_BG As Color = Color.FromArgb(24, 24, 27)

        Public Sub New()
            Me.Text = "Inventario Pro - Dashboard"
            Me.Size = New Size(1280, 780)
            Me.StartPosition = FormStartPosition.CenterScreen
            Me.BackColor = COLOR_BG
            Me.MinimumSize = New Size(1100, 700)

            ' Layout principal con TableLayoutPanel para evitar solapamientos
            Dim mainLayout = New TableLayoutPanel()
            mainLayout.Dock = DockStyle.Fill
            mainLayout.ColumnCount = 2
            mainLayout.RowCount = 2
            mainLayout.ColumnStyles.Add(New ColumnStyle(SizeType.Absolute, 220.0F))
            mainLayout.ColumnStyles.Add(New ColumnStyle(SizeType.Percent, 100.0F))
            mainLayout.RowStyles.Add(New RowStyle(SizeType.Absolute, 50.0F))
            mainLayout.RowStyles.Add(New RowStyle(SizeType.Percent, 100.0F))
            mainLayout.BackColor = COLOR_BG

            topBar = CreateTopBar()
            topBar.Dock = DockStyle.Fill
            mainLayout.Controls.Add(topBar, 0, 0)
            mainLayout.SetColumnSpan(topBar, 2)

            sidePanel = CreateSidebar()
            sidePanel.Dock = DockStyle.Fill
            sidePanel.BackColor = COLOR_SIDEBAR
            mainLayout.Controls.Add(sidePanel, 0, 1)

            contentPanel = New Panel()
            contentPanel.Dock = DockStyle.Fill
            contentPanel.BackColor = COLOR_BG
            mainLayout.Controls.Add(contentPanel, 1, 1)

            Me.Controls.Add(mainLayout)

            ' Drag support
            AddHandler topBar.MouseDown, AddressOf Form_MouseDown
            AddHandler topBar.MouseMove, AddressOf Form_MouseMove

            ShowPanel("Inicio", activeButton)
        End Sub

        Private dragPoint As Point
        Private Sub Form_MouseDown(sender As Object, e As MouseEventArgs)
            dragPoint = New Point(e.X, e.Y)
        End Sub

        Private Sub Form_MouseMove(sender As Object, e As MouseEventArgs)
            If e.Button = MouseButtons.Left Then
                Me.Location = New Point(Me.Location.X + e.X - dragPoint.X, Me.Location.Y + e.Y - dragPoint.Y)
            End If
        End Sub

        Private Function CreateTopBar() As Panel
            Dim bar As New Panel()
            bar.BackColor = COLOR_SIDEBAR

            Dim logo = New Label()
            logo.Text = "  Inventario Pro"
            logo.Font = New Font("Segoe UI", 15, FontStyle.Bold)
            logo.ForeColor = COLOR_ACTIVE
            logo.AutoSize = True
            logo.Location = New Point(20, 12)
            bar.Controls.Add(logo)

            Dim rightPanel = New Panel()
            rightPanel.Dock = DockStyle.Right
            rightPanel.Width = 400
            rightPanel.BackColor = Color.Transparent
            bar.Controls.Add(rightPanel)

            clockLabel = New Label()
            clockLabel.Font = New Font("Segoe UI", 12, FontStyle.Bold)
            clockLabel.ForeColor = COLOR_ACTIVE
            clockLabel.AutoSize = True
            clockLabel.Location = New Point(10, 14)
            rightPanel.Controls.Add(clockLabel)

            Dim timer As New Timer()
            timer.Interval = 1000
            AddHandler timer.Tick, AddressOf UpdateClock
            timer.Start()
            UpdateClock(Nothing, EventArgs.Empty)

            Dim user = SesionUsuario.UsuarioActual
            Dim userLabel = New Label()
            userLabel.Text = If(user IsNot Nothing, "  " & user.Nombre, "  Admin")
            userLabel.Font = New Font("Segoe UI", 12)
            userLabel.ForeColor = Color.FromArgb(180, 180, 180)
            userLabel.AutoSize = True
            userLabel.Location = New Point(210, 14)
            rightPanel.Controls.Add(userLabel)

            Dim btnMin = CreateControlButton("_", Color.FromArgb(250, 204, 21))
            btnMin.Location = New Point(320, 10)
            AddHandler btnMin.Click, Sub(s, e) Me.WindowState = FormWindowState.Minimized
            rightPanel.Controls.Add(btnMin)

            Dim btnMax = CreateControlButton("□", Color.FromArgb(52, 211, 153))
            btnMax.Location = New Point(355, 10)
            AddHandler btnMax.Click, AddressOf ToggleMaximize
            rightPanel.Controls.Add(btnMax)

            Dim btnClose = CreateControlButton("×", Color.FromArgb(239, 68, 68))
            btnClose.Location = New Point(390, 10)
            AddHandler btnClose.Click, AddressOf CloseButton_Click
            rightPanel.Controls.Add(btnClose)

            Return bar
        End Function

        Private Function CreateControlButton(texto As String, hoverColor As Color) As Button
            Dim btn = New Button()
            btn.Text = texto
            btn.Size = New Size(30, 30)
            btn.FlatStyle = FlatStyle.Flat
            btn.FlatAppearance.BorderSize = 0
            btn.Font = New Font("Segoe UI", 10, FontStyle.Bold)
            btn.ForeColor = Color.FromArgb(150, 150, 150)
            btn.BackColor = Color.Transparent
            btn.Cursor = Cursors.Hand
            AddHandler btn.MouseEnter, Sub(s, e) btn.ForeColor = hoverColor
            AddHandler btn.MouseLeave, Sub(s, e) btn.ForeColor = Color.FromArgb(150, 150, 150)
            Return btn
        End Function

        Private Sub ToggleMaximize(sender As Object, e As EventArgs)
            If Me.WindowState = FormWindowState.Maximized Then
                Me.WindowState = FormWindowState.Normal
            Else
                Me.WindowState = FormWindowState.Maximized
            End If
        End Sub

        Private Sub CloseButton_Click(sender As Object, e As EventArgs)
            If MessageBox.Show("¿Desea salir del sistema?", "Confirmar", MessageBoxButtons.YesNo, MessageBoxIcon.Question) = DialogResult.Yes Then
                Application.Exit()
            End If
        End Sub

        Private Sub UpdateClock(sender As Object, e As EventArgs)
            clockLabel.Text = DateTime.Now.ToString("dd/MM/yyyy HH:mm:ss")
        End Sub

        Private Function CreateSidebar() As Panel
            Dim panel As New Panel()
            panel.BackColor = COLOR_SIDEBAR
            panel.AutoScroll = True

            Dim lblMenu = New Label()
            lblMenu.Text = "  MENÚ PRINCIPAL"
            lblMenu.ForeColor = Color.FromArgb(100, 100, 100)
            lblMenu.Font = New Font("Segoe UI", 9, FontStyle.Bold)
            lblMenu.Dock = DockStyle.Top
            lblMenu.Height = 35
            lblMenu.TextAlign = ContentAlignment.MiddleLeft
            panel.Controls.Add(lblMenu)

            Dim items As String()() = {
                New String() {"Inicio", "🏠"},
                New String() {"Productos", "📦"},
                New String() {"Categorias", "🏷️"},
                New String() {"Clientes", "👥"},
                New String() {"Proveedores", "🚚"},
                New String() {"Inventario", "📊"},
                New String() {"Facturas", "🧾"},
                New String() {"Usuarios", "👤"},
                New String() {"Reportes", "📈"},
                New String() {"Configuracion", "⚙️"}
            }

            For i As Integer = 0 To items.Length - 1
                Dim panelName As String = items(i)(0)
                Dim icon As String = items(i)(1)
                Dim btn = CreateSidebarButton("  " & icon & "  " & panelName, panelName = "Cerrar Sesion")
                btn.Dock = DockStyle.Top
                AddHandler btn.Click, Sub(s, ev) ShowPanel(panelName, CType(s, Button))
                If panelName <> "Inicio" AndAlso Not SesionUsuario.TienePermiso(panelName) Then
                    btn.Enabled = False
                    btn.ForeColor = Color.FromArgb(80, 80, 80)
                End If
                panel.Controls.Add(btn)
                If panelName = "Inicio" Then activeButton = btn
            Next

            Dim sep As New Panel()
            sep.Height = 1
            sep.Dock = DockStyle.Top
            sep.BackColor = Color.FromArgb(55, 65, 81)
            sep.Margin = New Padding(15, 10, 15, 10)
            panel.Controls.Add(sep)

            Dim btnLogout = CreateSidebarButton("  🚪  Cerrar Sesión", True)
            btnLogout.Dock = DockStyle.Top
            AddHandler btnLogout.Click, AddressOf Logout_Click
            panel.Controls.Add(btnLogout)

            Return panel
        End Function

        Private Function CreateSidebarButton(text As String, Optional isLogout As Boolean = False) As Button
            Dim btn = New Button()
            btn.Text = text
            btn.Height = 42
            btn.FlatStyle = FlatStyle.Flat
            btn.FlatAppearance.BorderSize = 0
            btn.TextAlign = ContentAlignment.MiddleLeft
            btn.Font = New Font("Segoe UI", 12)
            If isLogout Then
                btn.ForeColor = Color.FromArgb(239, 68, 68)
            Else
                btn.ForeColor = Color.FromArgb(180, 180, 180)
            End If
            btn.BackColor = COLOR_SIDEBAR
            btn.Margin = Padding.Empty
            AddHandler btn.MouseEnter, Sub(s, ev)
                                           If btn IsNot activeButton Then
                                               btn.BackColor = COLOR_SIDEBAR_HOVER
                                               If isLogout Then btn.ForeColor = Color.FromArgb(239, 68, 68) Else btn.ForeColor = Color.White
                                           End If
                                       End Sub
            AddHandler btn.MouseLeave, Sub(s, ev)
                                           If btn IsNot activeButton Then
                                               btn.BackColor = COLOR_SIDEBAR
                                               If isLogout Then btn.ForeColor = Color.FromArgb(239, 68, 68) Else btn.ForeColor = Color.FromArgb(180, 180, 180)
                                           End If
                                       End Sub
            Return btn
        End Function

        Private Sub ShowPanel(name As String, btn As Button)
            If name <> "Inicio" AndAlso Not SesionUsuario.TienePermiso(name) Then Return
            If currentControl IsNot Nothing Then
                contentPanel.Controls.Remove(currentControl)
                currentControl.Dispose()
            End If

            If activeButton IsNot Nothing Then
                activeButton.BackColor = COLOR_SIDEBAR
                If activeButton.Text.Contains("Cerrar") Then activeButton.ForeColor = Color.FromArgb(239, 68, 68) Else activeButton.ForeColor = Color.FromArgb(180, 180, 180)
            End If
            activeButton = btn
            activeButton.BackColor = Color.FromArgb(16, 185, 129, 30)
            activeButton.ForeColor = COLOR_ACTIVE

            Select Case name
                Case "Inicio"
                    currentControl = New PanelInicio()
                Case "Productos"
                    currentControl = New PanelProductos()
                Case "Categorias"
                    currentControl = New PanelCategorias()
                Case "Clientes"
                    currentControl = New PanelClientes()
                Case "Proveedores"
                    currentControl = New PanelProveedores()
                Case "Inventario"
                    currentControl = New PanelInventario()
                Case "Facturas"
                    currentControl = New PanelFacturas()
                Case "Usuarios"
                    currentControl = New PanelUsuarios()
                Case "Reportes"
                    currentControl = New PanelReportes()
                Case "Configuracion"
                    currentControl = New PanelConfiguracion()
                Case Else
                    currentControl = New PanelInicio()
            End Select

            currentControl.Dock = DockStyle.Fill
            contentPanel.Controls.Add(currentControl)
        End Sub

        Private Sub Logout_Click(sender As Object, e As EventArgs)
            If MessageBox.Show("¿Cerrar sesión?", "Confirmar", MessageBoxButtons.YesNo, MessageBoxIcon.Question) = DialogResult.Yes Then
                SesionUsuario.CerrarSesion()
                Dim login = New LoginForm()
                login.Show()
                Me.Close()
            End If
        End Sub

    End Class

End Namespace



