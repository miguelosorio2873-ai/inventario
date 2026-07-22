Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Partial Public Class LoginForm
        Inherits Form

        Private txtUsuario As TextBox
        Private txtPassword As TextBox
        Private btnLogin As Button
        Private lblError As Label
        Private brandPanel As Panel
        Private loginPanel As Panel

        Public Sub New()
            Me.Text = "Inventario Pro - Login"
            Me.Size = New Size(900, 520)
            Me.StartPosition = FormStartPosition.CenterScreen
            Me.FormBorderStyle = FormBorderStyle.Sizable
            Me.MinimumSize = New Size(800, 480)
            Me.MaximizeBox = False
            Me.BackColor = Color.FromArgb(30, 30, 30)

            ' Contenedor principal con 2 columnas
            Dim mainPanel As New TableLayoutPanel()
            mainPanel.Dock = DockStyle.Fill
            mainPanel.ColumnCount = 2
            mainPanel.RowCount = 1
            mainPanel.ColumnStyles.Add(New ColumnStyle(SizeType.Percent, 40.0F))
            mainPanel.ColumnStyles.Add(New ColumnStyle(SizeType.Percent, 60.0F))
            mainPanel.BackColor = Color.FromArgb(30, 30, 30)

            ' Panel izquierdo - Branding
            brandPanel = New Panel()
            brandPanel.Dock = DockStyle.Fill
            AddHandler brandPanel.Paint, AddressOf BrandPanel_Paint
            brandPanel.BackColor = Color.FromArgb(16, 185, 129)

            Dim brandLayout As New TableLayoutPanel()
            brandLayout.Dock = DockStyle.Fill
            brandLayout.ColumnCount = 1
            brandLayout.RowCount = 3
            brandLayout.RowStyles.Add(New RowStyle(SizeType.Percent, 35.0F))
            brandLayout.RowStyles.Add(New RowStyle(SizeType.AutoSize))
            brandLayout.RowStyles.Add(New RowStyle(SizeType.Percent, 65.0F))
            brandLayout.BackColor = Color.Transparent

            Dim title = New Label()
            title.Text = "Inventario Pro"
            title.Font = New Font("Segoe UI", 32, FontStyle.Bold)
            title.ForeColor = Color.White
            title.AutoSize = True
            title.Anchor = AnchorStyles.None
            title.TextAlign = ContentAlignment.MiddleCenter

            Dim subtitle = New Label()
            subtitle.Text = "Sistema Integral de Gestion de Inventario"
            subtitle.Font = New Font("Segoe UI", 14)
            subtitle.ForeColor = Color.FromArgb(255, 255, 255, 200)
            subtitle.AutoSize = True
            subtitle.Anchor = AnchorStyles.None
            subtitle.TextAlign = ContentAlignment.MiddleCenter

            brandLayout.Controls.Add(title, 0, 1)
            brandLayout.SetRow(title, 1)
            brandLayout.Controls.Add(subtitle, 0, 2)
            brandLayout.SetRow(subtitle, 2)
            brandPanel.Controls.Add(brandLayout)

            ' Panel derecho - Login con controles centrados
            loginPanel = New Panel()
            loginPanel.Dock = DockStyle.Fill
            loginPanel.BackColor = Color.FromArgb(30, 30, 30)

            ' Contenedor centrado para los controles
            Dim container As New Panel()
            container.Size = New Size(340, 420)
            container.BackColor = Color.FromArgb(30, 30, 30)
            AddHandler loginPanel.Resize, Sub(s, e)
                                              container.Left = (loginPanel.Width - container.Width) / 2
                                              container.Top = (loginPanel.Height - container.Height) / 2
                                          End Sub
            loginPanel.Controls.Add(container)

            Dim loginTitle = New Label()
            loginTitle.Text = "Iniciar Sesion"
            loginTitle.Font = New Font("Segoe UI", 26, FontStyle.Bold)
            loginTitle.ForeColor = Color.White
            loginTitle.AutoSize = True
            loginTitle.Location = New Point(0, 0)
            container.Controls.Add(loginTitle)

            Dim loginSub = New Label()
            loginSub.Text = "Ingrese sus credenciales para continuar"
            loginSub.Font = New Font("Segoe UI", 11)
            loginSub.ForeColor = Color.FromArgb(150, 150, 150)
            loginSub.AutoSize = True
            loginSub.Location = New Point(0, 45)
            container.Controls.Add(loginSub)

            Dim lblU = New Label()
            lblU.Text = "Correo electronico"
            lblU.ForeColor = Color.FromArgb(180, 180, 180)
            lblU.Font = New Font("Segoe UI", 10)
            lblU.AutoSize = True
            lblU.Location = New Point(0, 95)
            container.Controls.Add(lblU)

            txtUsuario = New TextBox()
            txtUsuario.Location = New Point(0, 120)
            txtUsuario.Size = New Size(340, 32)
            txtUsuario.Font = New Font("Segoe UI", 12)
            txtUsuario.BackColor = Color.FromArgb(50, 50, 50)
            txtUsuario.ForeColor = Color.White
            txtUsuario.BorderStyle = BorderStyle.FixedSingle
            container.Controls.Add(txtUsuario)

            Dim lblP = New Label()
            lblP.Text = "Contrasena"
            lblP.ForeColor = Color.FromArgb(180, 180, 180)
            lblP.Font = New Font("Segoe UI", 10)
            lblP.AutoSize = True
            lblP.Location = New Point(0, 175)
            container.Controls.Add(lblP)

            txtPassword = New TextBox()
            txtPassword.Location = New Point(0, 200)
            txtPassword.Size = New Size(340, 32)
            txtPassword.Font = New Font("Segoe UI", 12)
            txtPassword.BackColor = Color.FromArgb(50, 50, 50)
            txtPassword.ForeColor = Color.White
            txtPassword.BorderStyle = BorderStyle.FixedSingle
            txtPassword.UseSystemPasswordChar = True
            container.Controls.Add(txtPassword)

            btnLogin = New Button()
            btnLogin.Text = "INGRESAR"
            btnLogin.Location = New Point(0, 270)
            btnLogin.Size = New Size(340, 45)
            btnLogin.Font = New Font("Segoe UI", 13, FontStyle.Bold)
            btnLogin.BackColor = Color.FromArgb(16, 185, 129)
            btnLogin.ForeColor = Color.White
            btnLogin.FlatStyle = FlatStyle.Flat
            btnLogin.FlatAppearance.BorderSize = 0
            btnLogin.Cursor = Cursors.Hand
            AddHandler btnLogin.Click, AddressOf BtnLogin_Click
            AddHandler btnLogin.MouseEnter, Sub(s, e) btnLogin.BackColor = Color.FromArgb(20, 200, 140)
            AddHandler btnLogin.MouseLeave, Sub(s, e) btnLogin.BackColor = Color.FromArgb(16, 185, 129)
            container.Controls.Add(btnLogin)

            Dim btnRecuperar As New Button()
            btnRecuperar.Text = "Olvidaste tu contrasena?"
            btnRecuperar.Location = New Point(0, 330)
            btnRecuperar.Size = New Size(340, 30)
            btnRecuperar.FlatStyle = FlatStyle.Flat
            btnRecuperar.FlatAppearance.BorderSize = 0
            btnRecuperar.BackColor = Color.FromArgb(30, 30, 30)
            btnRecuperar.ForeColor = Color.FromArgb(16, 185, 129)
            btnRecuperar.Cursor = Cursors.Hand
            AddHandler btnRecuperar.Click, AddressOf RecuperarPassword_Click
            container.Controls.Add(btnRecuperar)

            lblError = New Label()
            lblError.Text = ""
            lblError.ForeColor = Color.FromArgb(239, 68, 68)
            lblError.Location = New Point(0, 370)
            lblError.Size = New Size(340, 30)
            lblError.TextAlign = ContentAlignment.MiddleCenter
            lblError.Font = New Font("Segoe UI", 10)
            container.Controls.Add(lblError)

            mainPanel.Controls.Add(brandPanel, 0, 0)
            mainPanel.Controls.Add(loginPanel, 1, 0)
            Me.Controls.Add(mainPanel)

            ' Forzar centrado inicial del contenedor
            AddHandler Me.Load, Sub(s, e)
                                    container.Left = (loginPanel.Width - container.Width) / 2
                                    container.Top = (loginPanel.Height - container.Height) / 2
                                End Sub
        End Sub

        Private Sub BrandPanel_Paint(sender As Object, e As PaintEventArgs)
            Dim g As Graphics = e.Graphics
            g.SmoothingMode = Drawing2D.SmoothingMode.AntiAlias
            Using brush As New Drawing2D.LinearGradientBrush(New Point(0, 0), New Point(brandPanel.Width, brandPanel.Height), Color.FromArgb(16, 185, 129), Color.FromArgb(5, 150, 105))
                g.FillRectangle(brush, 0, 0, brandPanel.Width, brandPanel.Height)
            End Using
            Using brush As New SolidBrush(Color.FromArgb(25, 255, 255, 255))
                g.FillEllipse(brush, -50, -50, 250, 250)
                g.FillEllipse(brush, brandPanel.Width - 150, brandPanel.Height - 150, 250, 250)
                g.FillEllipse(brush, CInt(brandPanel.Width / 2 - 60), CInt(brandPanel.Height / 2 + 50), 180, 180)
            End Using
        End Sub

        Private Sub RecuperarPassword_Click(sender As Object, e As EventArgs)
            Dim email As String = InputBox("Ingrese su correo registrado:", "Recuperar contraseña").Trim()
            If String.IsNullOrEmpty(email) Then Return
            Dim dao As New UsuarioDAO()
            Dim usuario As Usuario = dao.BuscarPorEmail(email)
            If usuario Is Nothing Then
                MessageBox.Show("El correo no existe en el sistema.", "Recuperar contraseña", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            Dim respuesta As String = InputBox(usuario.Pregunta1, "Pregunta de seguridad").Trim()
            If Not String.Equals(respuesta, usuario.Respuesta1, StringComparison.OrdinalIgnoreCase) Then
                MessageBox.Show("Respuesta incorrecta.", "Recuperar contraseña", MessageBoxButtons.OK, MessageBoxIcon.Error)
                Return
            End If
            Dim nueva As String = InputBox("Ingrese la nueva contraseña:", "Nueva contraseña")
            If Not Argon2Util.EsSegura(nueva) Then
                MessageBox.Show(Argon2Util.RequisitosMensaje(), "Contraseña débil", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            Dim confirmar As String = InputBox("Confirme la nueva contraseña:", "Nueva contraseña")
            If nueva <> confirmar Then
                MessageBox.Show("Las contraseñas no coinciden.", "Nueva contraseña", MessageBoxButtons.OK, MessageBoxIcon.Error)
                Return
            End If
            dao.CambiarPassword(usuario.Id, nueva)
            MessageBox.Show("Contraseña actualizada correctamente.", "Recuperar contraseña", MessageBoxButtons.OK, MessageBoxIcon.Information)
        End Sub

        Private Sub BtnLogin_Click(sender As Object, e As EventArgs)
            Dim dao As New UsuarioDAO()
            Dim u = dao.ValidarUsuario(txtUsuario.Text.Trim(), txtPassword.Text)
            If u IsNot Nothing Then
                SesionUsuario.UsuarioActual = u
                Dim dash = New DashboardForm()
                dash.Show()
                Me.Hide()
            Else
                lblError.Text = "Usuario o contraseÃ±a incorrectos"
            End If
        End Sub

    End Class

End Namespace



