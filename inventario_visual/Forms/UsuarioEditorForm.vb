Imports System.Drawing
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual
    Public Class UsuarioEditorForm
        Inherits Form

        Private ReadOnly usuario As Usuario
        Private ReadOnly esNuevo As Boolean
        Private txtNombre As TextBox
        Private txtEmail As TextBox
        Private txtPassword As TextBox
        Private cboRol As ComboBox
        Private txtPermisos As TextBox
        Private preguntas As TextBox()
        Private respuestas As TextBox()

        Public Sub New(Optional usuarioExistente As Usuario = Nothing)
            usuario = If(usuarioExistente, New Usuario())
            esNuevo = usuarioExistente Is Nothing
            AplicarEstiloEditor(Me, If(esNuevo, "Nuevo usuario", "Editar usuario"), 560, 700)

            Dim y As Integer = 20
            txtNombre = Campo("Nombre", y)
            txtNombre.Text = usuario.Nombre
            y += 60
            txtEmail = Campo("Email", y)
            txtEmail.Text = usuario.Email
            y += 60
            txtPassword = Campo("Contraseña" & If(esNuevo, "", " (dejar en blanco para no cambiar)"), y)
            y += 60
            Controls.Add(CrearLabel("Rol", 30, y))
            cboRol = CrearCombo(30, y + 22, 490)
            cboRol.Items.AddRange(New String() {"Admin", "Usuario"})
            cboRol.SelectedItem = If(String.IsNullOrEmpty(usuario.Rol), "Usuario", usuario.Rol)
            Controls.Add(cboRol)
            y += 60
            Controls.Add(CrearLabel("Permisos (Ej: Productos:VCEDX;Clientes:VC)", 30, y))
            txtPermisos = CrearTexto(usuario.Permisos, 30, y + 22, 490)
            Controls.Add(txtPermisos)
            y += 60
            Dim btnAdmin = New Button With {.Text = "Permisos de administrador", .Location = New Point(30, y), .Size = New Size(180, 30), .FlatStyle = FlatStyle.Flat, .BackColor = Color.FromArgb(59, 130, 246), .ForeColor = Color.White}
            btnAdmin.FlatAppearance.BorderSize = 0
            AddHandler btnAdmin.Click, AddressOf Admin_Click
            Controls.Add(btnAdmin)
            y += 55
            ReDim preguntas(3)
            ReDim respuestas(3)
            For i As Integer = 0 To 3
                Controls.Add(CrearLabel("Pregunta " & (i + 1), 30, y))
                preguntas(i) = CrearTexto("", 30, y + 22, 490)
                Controls.Add(preguntas(i))
                y += 55
                Controls.Add(CrearLabel("Respuesta " & (i + 1), 30, y))
                respuestas(i) = CrearTexto("", 30, y + 22, 490)
                Controls.Add(respuestas(i))
                y += 55
            Next
            CargarPreguntasRespuestas()
            Dim guardar = CrearBotonGuardar(400, y + 10)
            AddHandler guardar.Click, AddressOf Guardar_Click
            Controls.Add(guardar)
        End Sub

        Private Function Campo(etiqueta As String, y As Integer) As TextBox
            Controls.Add(CrearLabel(etiqueta, 30, y))
            Dim entrada = CrearTexto("", 30, y + 22, 490)
            Controls.Add(entrada)
            Return entrada
        End Function

        Private Sub CargarPreguntasRespuestas()
            If Not String.IsNullOrEmpty(usuario.Pregunta1) Then preguntas(0).Text = usuario.Pregunta1
            If Not String.IsNullOrEmpty(usuario.Respuesta1) Then respuestas(0).Text = usuario.Respuesta1
            If Not String.IsNullOrEmpty(usuario.Pregunta2) Then preguntas(1).Text = usuario.Pregunta2
            If Not String.IsNullOrEmpty(usuario.Respuesta2) Then respuestas(1).Text = usuario.Respuesta2
            If Not String.IsNullOrEmpty(usuario.Pregunta3) Then preguntas(2).Text = usuario.Pregunta3
            If Not String.IsNullOrEmpty(usuario.Respuesta3) Then respuestas(2).Text = usuario.Respuesta3
            If Not String.IsNullOrEmpty(usuario.Pregunta4) Then preguntas(3).Text = usuario.Pregunta4
            If Not String.IsNullOrEmpty(usuario.Respuesta4) Then respuestas(3).Text = usuario.Respuesta4
        End Sub

        Private Sub Admin_Click(sender As Object, e As EventArgs)
            txtPermisos.Text = "Productos:VCEDX;Categorias:VCEDX;Clientes:VCEDX;Proveedores:VCEDX;Inventario:VCEDX;Facturas:VCEDX;Usuarios:VCEDX;Reportes:VCEDX;Configuracion:VCEDX"
        End Sub

        Private Sub Guardar_Click(sender As Object, e As EventArgs)
            If String.IsNullOrWhiteSpace(txtNombre.Text) OrElse String.IsNullOrWhiteSpace(txtEmail.Text) Then
                MessageBox.Show("Nombre y email son obligatorios.", "Usuario", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            If esNuevo AndAlso String.IsNullOrWhiteSpace(txtPassword.Text) Then
                MessageBox.Show("La contraseña es obligatoria para un nuevo usuario.", "Usuario", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            If Not String.IsNullOrWhiteSpace(txtPassword.Text) AndAlso Not Argon2Util.EsSegura(txtPassword.Text) Then
                MessageBox.Show(Argon2Util.RequisitosMensaje(), "Contraseña insegura", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If
            usuario.Nombre = txtNombre.Text.Trim()
            usuario.Email = txtEmail.Text.Trim()
            usuario.Rol = cboRol.SelectedItem.ToString()
            usuario.Permisos = txtPermisos.Text.Trim()
            If Not String.IsNullOrWhiteSpace(txtPassword.Text) Then usuario.PasswordHash = txtPassword.Text
            usuario.Pregunta1 = preguntas(0).Text.Trim()
            usuario.Respuesta1 = respuestas(0).Text.Trim()
            usuario.Pregunta2 = preguntas(1).Text.Trim()
            usuario.Respuesta2 = respuestas(1).Text.Trim()
            usuario.Pregunta3 = preguntas(2).Text.Trim()
            usuario.Respuesta3 = respuestas(2).Text.Trim()
            usuario.Pregunta4 = preguntas(3).Text.Trim()
            usuario.Respuesta4 = respuestas(3).Text.Trim()
            Dim dao As New UsuarioDAO()
            If esNuevo Then
                dao.Insertar(usuario)
            Else
                If Not String.IsNullOrWhiteSpace(txtPassword.Text) Then
                    dao.CambiarPassword(usuario.Id, txtPassword.Text)
                End If
                dao.Actualizar(usuario)
            End If
            DialogResult = DialogResult.OK
            Close()
        End Sub
    End Class
End Namespace
