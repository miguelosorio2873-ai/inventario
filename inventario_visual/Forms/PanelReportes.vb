Imports System.Drawing
Imports System.IO
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms
Imports System.Diagnostics

Namespace inventario_visual

    Public Class PanelReportes
        Inherits UserControl

        Private ReadOnly COLOR_BG As Color = Color.FromArgb(24, 24, 27)
        Private ReadOnly COLOR_CARD As Color = Color.FromArgb(39, 39, 42)
        Private ReadOnly COLOR_GREEN As Color = Color.FromArgb(16, 185, 129)
        Private ReadOnly COLOR_BLUE As Color = Color.FromArgb(59, 130, 246)
        Private ReadOnly COLOR_TEXT As Color = Color.FromArgb(180, 180, 180)

        Private comboTipo As ComboBox
        Private panelColumnas As Panel
        Private panelFiltroEspecifico As Panel
        Private panelFecha As Panel
        Private comboFiltroEspecifico As ComboBox
        Private chkFecha As CheckBox
        Private txtFechaIni As TextBox
        Private txtFechaFin As TextBox
        Private gridPreview As DataGridView
        Private lblRegistros As Label
        Private txtBuscar As TextBox

        Private columnasPorTipo As Dictionary(Of String, String())
        Private filtrosPorTipo As Dictionary(Of String, String())
        Private tiposConFecha As List(Of String)
        Private columnasActuales As List(Of String)
        Private datosCompletos As List(Of Object())
        Private datosActuales As List(Of Object())

        Public Sub New()
            Me.BackColor = COLOR_BG
            Me.Dock = DockStyle.Fill
            InicializarDiccionarios()

            Dim mainLayout As New TableLayoutPanel()
            mainLayout.Dock = DockStyle.Fill
            mainLayout.ColumnCount = 2
            mainLayout.RowCount = 1
            mainLayout.ColumnStyles.Add(New ColumnStyle(SizeType.Absolute, 320.0F))
            mainLayout.ColumnStyles.Add(New ColumnStyle(SizeType.Percent, 100.0F))
            mainLayout.Padding = New Padding(25)
            mainLayout.BackColor = COLOR_BG

            ' --- Panel izquierdo de configuracion ---
            Dim configPanel As New Panel()
            configPanel.Dock = DockStyle.Fill
            configPanel.BackColor = COLOR_CARD
            configPanel.Padding = New Padding(15)

            Dim lblTitulo = New Label With {.Text = "Generador de Reportes", .Font = New Font("Segoe UI", 20, FontStyle.Bold), .ForeColor = Color.White, .AutoSize = True, .Location = New Point(15, 15)}
            configPanel.Controls.Add(lblTitulo)

            Dim y As Integer = 55

            Dim lblTipo = New Label With {.Text = "Tipo de Reporte", .Font = New Font("Segoe UI", 11, FontStyle.Bold), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(15, y)}
            configPanel.Controls.Add(lblTipo)
            y += 25

            comboTipo = New ComboBox With {.Location = New Point(15, y), .Size = New Size(280, 28), .Font = New Font("Segoe UI", 11), .BackColor = Color.FromArgb(55, 55, 55), .ForeColor = Color.White, .FlatStyle = FlatStyle.Flat, .DropDownStyle = ComboBoxStyle.DropDownList}
            comboTipo.Items.AddRange(columnasPorTipo.Keys.ToArray())
            comboTipo.SelectedIndex = 0
            AddHandler comboTipo.SelectedIndexChanged, AddressOf ActualizarColumnas
            configPanel.Controls.Add(comboTipo)
            y += 45

            Dim lblCols = New Label With {.Text = "Columnas a incluir", .Font = New Font("Segoe UI", 11, FontStyle.Bold), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(15, y)}
            configPanel.Controls.Add(lblCols)
            y += 25

            panelColumnas = New Panel With {.Location = New Point(15, y), .Size = New Size(280, 180), .BackColor = COLOR_CARD, .AutoScroll = True}
            configPanel.Controls.Add(panelColumnas)
            y += 195

            panelFiltroEspecifico = New Panel With {.Location = New Point(15, y), .Size = New Size(280, 70), .BackColor = COLOR_CARD}
            configPanel.Controls.Add(panelFiltroEspecifico)

            panelFecha = New Panel With {.Location = New Point(15, y), .Size = New Size(280, 140), .BackColor = COLOR_CARD, .Visible = False}
            configPanel.Controls.Add(panelFecha)

            Dim btnGenerar = CrearBoton("Generar Reporte", COLOR_GREEN, 280)
            btnGenerar.Location = New Point(15, 470)
            AddHandler btnGenerar.Click, AddressOf GenerarReporte
            configPanel.Controls.Add(btnGenerar)

            Dim btnExportar = CrearBoton("Exportar a Excel", COLOR_BLUE, 280)
            btnExportar.Location = New Point(15, 515)
            AddHandler btnExportar.Click, AddressOf ExportarExcel
            configPanel.Controls.Add(btnExportar)

            ' --- Panel derecho de preview ---
            Dim tablePanel As New Panel()
            tablePanel.Dock = DockStyle.Fill
            tablePanel.BackColor = COLOR_CARD
            tablePanel.Padding = New Padding(10)

            Dim topPanel = New Panel With {.Dock = DockStyle.Top, .Height = 45, .BackColor = COLOR_CARD}
            txtBuscar = New TextBox With {.Location = New Point(10, 8), .Size = New Size(300, 26), .Font = New Font("Segoe UI", 11), .BackColor = Color.FromArgb(55, 55, 55), .ForeColor = Color.White, .BorderStyle = BorderStyle.FixedSingle, .Text = "Filtrar resultados..."}
            AddHandler txtBuscar.TextChanged, AddressOf AplicarFiltroTexto
            AddHandler txtBuscar.GotFocus, Sub(s, e) If txtBuscar.Text = "Filtrar resultados..." Then txtBuscar.Text = ""
            AddHandler txtBuscar.LostFocus, Sub(s, e) If String.IsNullOrWhiteSpace(txtBuscar.Text) Then txtBuscar.Text = "Filtrar resultados..."
            topPanel.Controls.Add(txtBuscar)

            lblRegistros = New Label With {.Text = "0 registros", .Font = New Font("Segoe UI", 11), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(330, 12)}
            topPanel.Controls.Add(lblRegistros)
            tablePanel.Controls.Add(topPanel)

            gridPreview = New DataGridView With {.Dock = DockStyle.Fill, .BackgroundColor = COLOR_CARD, .BorderStyle = BorderStyle.None, .ReadOnly = True, .AllowUserToAddRows = False, .RowHeadersVisible = False, .AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill, .SelectionMode = DataGridViewSelectionMode.FullRowSelect, .ColumnHeadersHeight = 32}
            gridPreview.DefaultCellStyle.BackColor = COLOR_CARD
            gridPreview.DefaultCellStyle.ForeColor = Color.White
            gridPreview.DefaultCellStyle.SelectionBackColor = Color.FromArgb(16, 185, 129, 50)
            gridPreview.DefaultCellStyle.SelectionForeColor = Color.White
            gridPreview.DefaultCellStyle.Font = New Font("Segoe UI", 11)
            gridPreview.ColumnHeadersDefaultCellStyle.BackColor = Color.FromArgb(30, 30, 30)
            gridPreview.ColumnHeadersDefaultCellStyle.ForeColor = COLOR_TEXT
            gridPreview.ColumnHeadersDefaultCellStyle.Font = New Font("Segoe UI", 11, FontStyle.Bold)
            gridPreview.EnableHeadersVisualStyles = False
            gridPreview.RowTemplate.Height = 30
            tablePanel.Controls.Add(gridPreview)

            mainLayout.Controls.Add(configPanel, 0, 0)
            mainLayout.Controls.Add(tablePanel, 1, 0)
            Me.Controls.Add(mainLayout)

            ActualizarColumnas(Nothing, EventArgs.Empty)
        End Sub

        Private Sub InicializarDiccionarios()
            columnasPorTipo = New Dictionary(Of String, String())(StringComparer.OrdinalIgnoreCase) From {
                {"Productos", New String() {"ID", "SKU", "Nombre", "Descripcion", "Categoria", "Precio Venta", "Costo Promedio", "Stock Minimo", "Stock Actual", "Estado"}},
                {"Categorias", New String() {"ID", "Nombre", "Descripcion"}},
                {"Clientes", New String() {"ID", "Cedula", "Nombre", "Correo", "Telefono"}},
                {"Proveedores", New String() {"ID", "Nombre Empresa", "NIT/Cedula", "Telefono", "Direccion", "Correo", "Contacto"}},
                {"Movimientos de Inventario", New String() {"ID", "Producto", "Proveedor", "Precio", "Precio Balance", "Cantidad", "Tipo Movimiento", "Fecha", "Motivo"}},
                {"Facturas", New String() {"ID", "N. Factura", "Cliente", "Fecha", "Metodo Pago", "Subtotal", "Impuestos", "Total", "Estado"}},
                {"Usuarios", New String() {"ID", "Nombre", "Email", "Rol"}}
            }

            filtrosPorTipo = New Dictionary(Of String, String())(StringComparer.OrdinalIgnoreCase) From {
                {"Productos", New String() {"Todos", "Activo", "Inactivo"}},
                {"Movimientos de Inventario", New String() {"Todos", "Entrada", "Salida", "Ajuste"}},
                {"Facturas", New String() {"Todos", "Pagada", "Pendiente", "Anulada"}}
            }

            tiposConFecha = New List(Of String) From {"Movimientos de Inventario", "Facturas"}
        End Sub

        Private Function CrearBoton(texto As String, color As Color, ancho As Integer) As Button
            Dim btn = New Button With {.Text = texto, .Size = New Size(ancho, 36), .BackColor = color, .ForeColor = Color.White, .FlatStyle = FlatStyle.Flat, .Font = New Font("Segoe UI", 11, FontStyle.Bold), .Cursor = Cursors.Hand}
            btn.FlatAppearance.BorderSize = 0
            Return btn
        End Function

        Private Sub ActualizarColumnas(sender As Object, e As EventArgs)
            Dim tipo = comboTipo.SelectedItem.ToString()

            panelColumnas.Controls.Clear()
            Dim y As Integer = 0
            For Each col In columnasPorTipo(tipo)
                Dim chk = New CheckBox With {.Text = col, .ForeColor = Color.White, .BackColor = COLOR_CARD, .Font = New Font("Segoe UI", 11), .AutoSize = True, .Location = New Point(0, y), .Checked = True, .Tag = col}
                panelColumnas.Controls.Add(chk)
                y += 25
            Next

            panelFiltroEspecifico.Controls.Clear()
            If filtrosPorTipo.ContainsKey(tipo) Then
                Dim lbl = New Label With {.Text = "Filtrar por", .Font = New Font("Segoe UI", 11, FontStyle.Bold), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(0, 0)}
                panelFiltroEspecifico.Controls.Add(lbl)
                comboFiltroEspecifico = New ComboBox With {.Location = New Point(0, 22), .Size = New Size(260, 26), .Font = New Font("Segoe UI", 11), .BackColor = Color.FromArgb(55, 55, 55), .ForeColor = Color.White, .FlatStyle = FlatStyle.Flat, .DropDownStyle = ComboBoxStyle.DropDownList}
                comboFiltroEspecifico.Items.AddRange(filtrosPorTipo(tipo))
                comboFiltroEspecifico.SelectedIndex = 0
                panelFiltroEspecifico.Controls.Add(comboFiltroEspecifico)
                panelFiltroEspecifico.Visible = True
            Else
                panelFiltroEspecifico.Visible = False
                comboFiltroEspecifico = Nothing
            End If

            panelFecha.Controls.Clear()
            If tiposConFecha.Contains(tipo) Then
                Dim lbl = New Label With {.Text = "Filtrar por fecha", .Font = New Font("Segoe UI", 11, FontStyle.Bold), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(0, 0)}
                panelFecha.Controls.Add(lbl)
                chkFecha = New CheckBox With {.Text = "Habilitar filtro de rango", .ForeColor = Color.White, .BackColor = COLOR_CARD, .Font = New Font("Segoe UI", 11), .AutoSize = True, .Location = New Point(0, 22)}
                panelFecha.Controls.Add(chkFecha)

                Dim lblIni = New Label With {.Text = "Desde (YYYY-MM-DD):", .Font = New Font("Segoe UI", 10), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(0, 50)}
                panelFecha.Controls.Add(lblIni)
                txtFechaIni = New TextBox With {.Location = New Point(0, 70), .Size = New Size(260, 24), .Font = New Font("Segoe UI", 11), .BackColor = Color.FromArgb(55, 55, 55), .ForeColor = Color.White, .BorderStyle = BorderStyle.FixedSingle, .Enabled = False}
                panelFecha.Controls.Add(txtFechaIni)

                Dim lblFin = New Label With {.Text = "Hasta (YYYY-MM-DD):", .Font = New Font("Segoe UI", 10), .ForeColor = COLOR_TEXT, .AutoSize = True, .Location = New Point(0, 100)}
                panelFecha.Controls.Add(lblFin)
                txtFechaFin = New TextBox With {.Location = New Point(0, 120), .Size = New Size(260, 24), .Font = New Font("Segoe UI", 11), .BackColor = Color.FromArgb(55, 55, 55), .ForeColor = Color.White, .BorderStyle = BorderStyle.FixedSingle, .Enabled = False}
                panelFecha.Controls.Add(txtFechaFin)

                AddHandler chkFecha.CheckedChanged, Sub(s, ev)
                                                        txtFechaIni.Enabled = chkFecha.Checked
                                                        txtFechaFin.Enabled = chkFecha.Checked
                                                    End Sub
                panelFecha.Visible = True
            Else
                panelFecha.Visible = False
                chkFecha = Nothing
            End If
        End Sub

        Private Function ObtenerColumnasSeleccionadas() As List(Of String)
            Dim tipo = comboTipo.SelectedItem.ToString()
            Return columnasPorTipo(tipo).Where(Function(c) panelColumnas.Controls.OfType(Of CheckBox)().Any(Function(ch) ch.Tag IsNot Nothing AndAlso ch.Tag.ToString() = c AndAlso ch.Checked)).ToList()
        End Function

        Private Sub GenerarReporte(sender As Object, e As EventArgs)
            Dim tipo = comboTipo.SelectedItem.ToString()
            columnasActuales = ObtenerColumnasSeleccionadas()
            If columnasActuales.Count = 0 Then
                MessageBox.Show("Seleccione al menos una columna.", "Aviso", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If

            datosCompletos = New List(Of Object())
            datosActuales = New List(Of Object())

            Try
                Select Case tipo
                    Case "Productos"
                        CargarProductos()
                    Case "Categorias"
                        CargarCategorias()
                    Case "Clientes"
                        CargarClientes()
                    Case "Proveedores"
                        CargarProveedores()
                    Case "Movimientos de Inventario"
                        CargarMovimientos()
                    Case "Facturas"
                        CargarFacturas()
                    Case "Usuarios"
                        CargarUsuarios()
                End Select
            Catch ex As Exception
                MessageBox.Show("Error al cargar datos: " & ex.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error)
                Return
            End Try

            AplicarFiltroEspecifico()
            AplicarFiltroTexto(sender, e)
        End Sub

        Private Sub AplicarFiltroEspecifico()
            If comboFiltroEspecifico Is Nothing Then
                datosActuales = New List(Of Object())(datosCompletos)
                Return
            End If
            Dim valor = comboFiltroEspecifico.SelectedItem.ToString()
            If valor = "Todos" Then
                datosActuales = New List(Of Object())(datosCompletos)
            Else
                datosActuales = datosCompletos.Where(Function(fila) fila.Any(Function(v) v IsNot Nothing AndAlso v.ToString().Equals(valor, StringComparison.OrdinalIgnoreCase))).ToList()
            End If
        End Sub

        Private Sub AplicarFiltroTexto(sender As Object, e As EventArgs)
            Dim texto = If(txtBuscar.Text = "Filtrar resultados...", "", txtBuscar.Text.Trim().ToLower())
            Dim filtrados = datosActuales.Where(Function(fila) String.IsNullOrEmpty(texto) OrElse fila.Any(Function(v) v IsNot Nothing AndAlso v.ToString().ToLower().Contains(texto))).ToList()

            gridPreview.Columns.Clear()
            For Each col In columnasActuales
                gridPreview.Columns.Add(col, col)
            Next

            gridPreview.Rows.Clear()
            For Each fila In filtrados
                gridPreview.Rows.Add(fila)
            Next

            lblRegistros.Text = $"{filtrados.Count} de {datosActuales.Count} registro(s)"
        End Sub

        Private Sub CargarProductos()
            For Each p In New ProductoDAO().ListarTodos()
                datosCompletos.Add(New Object() {p.Id, p.Sku, p.Nombre, p.Descripcion, p.CategoriaNombre, "$" & p.PrecioVenta.ToString("F2"), "$" & p.PrecioCompra.ToString("F2"), p.StockMinimo, p.StockActual, If(p.State, "Activo", "Inactivo")})
            Next
        End Sub

        Private Sub CargarCategorias()
            For Each c In New CategoriaDAO().ListarTodas()
                datosCompletos.Add(New Object() {c.Id, c.Nombre, c.Descripcion})
            Next
        End Sub

        Private Sub CargarClientes()
            For Each c In New ClienteDAO().ListarTodos()
                datosCompletos.Add(New Object() {c.Id, c.Cedula, c.Nombre, c.Correo, c.Telefono})
            Next
        End Sub

        Private Sub CargarProveedores()
            For Each p In New ProveedorDAO().ListarTodos()
                datosCompletos.Add(New Object() {p.Id, p.NombreEmpresa, p.NitCedula, p.Telefono, p.Direccion, p.Correo, p.NombreContacto})
            Next
        End Sub

        Private Sub CargarMovimientos()
            Dim fechaIni = ObtenerFecha(txtFechaIni.Text)
            Dim fechaFin = ObtenerFecha(txtFechaFin.Text)
            For Each m In New InventarioDAO().ListarMovimientos()
                If fechaIni.HasValue AndAlso m.FechaMovimiento < fechaIni.Value Then Continue For
                If fechaFin.HasValue AndAlso m.FechaMovimiento > fechaFin.Value Then Continue For
                datosCompletos.Add(New Object() {m.Id, m.ProductoNombre, m.ProveedorNombre, "$" & m.Precio.ToString("F2"), "$" & m.PrecioBalance.ToString("F2"), m.Cantidad, m.TipoMovimiento, m.FechaMovimiento.ToString("yyyy-MM-dd"), m.Motivo})
            Next
        End Sub

        Private Sub CargarFacturas()
            Dim fechaIni = ObtenerFecha(txtFechaIni.Text)
            Dim fechaFin = ObtenerFecha(txtFechaFin.Text)
            For Each f In New FacturaDAO().ListarTodas()
                If fechaIni.HasValue AndAlso f.FechaEmision < fechaIni.Value Then Continue For
                If fechaFin.HasValue AndAlso f.FechaEmision > fechaFin.Value Then Continue For
                datosCompletos.Add(New Object() {f.Id, f.NumeroFactura, f.ClienteNombre, f.FechaEmision.ToString("yyyy-MM-dd"), f.MetodoPago, "$" & f.Subtotal.ToString("F2"), "$" & f.Impuestos.ToString("F2"), "$" & f.Total.ToString("F2"), f.Estado})
            Next
        End Sub

        Private Sub CargarUsuarios()
            For Each u In New UsuarioDAO().ListarTodos()
                datosCompletos.Add(New Object() {u.Id, u.Nombre, u.Email, u.Rol})
            Next
        End Sub

        Private Function ObtenerFecha(texto As String) As DateTime?
            Dim fecha As DateTime
            If DateTime.TryParseExact(texto.Trim(), "yyyy-MM-dd", Globalization.CultureInfo.InvariantCulture, Globalization.DateTimeStyles.None, fecha) Then
                Return fecha
            End If
            Return Nothing
        End Function

        Private Sub ExportarExcel(sender As Object, e As EventArgs)
            If gridPreview.Rows.Count = 0 Then
                MessageBox.Show("Genere un reporte primero.", "Aviso", MessageBoxButtons.OK, MessageBoxIcon.Warning)
                Return
            End If

            If Not SesionUsuario.TienePermisoAccion("Reportes", "EXPORTAR") Then
                MessageBox.Show("No tiene permiso para exportar reportes.")
                Return
            End If

            Dim tipo = comboTipo.SelectedItem.ToString().Replace(" ", "_")
            Using dialogo As New SaveFileDialog With {.Filter = "Excel CSV (*.csv)|*.csv", .FileName = $"Reporte_{tipo}.csv"}
                If dialogo.ShowDialog() <> DialogResult.OK Then Return

                Dim lineas As New List(Of String)()
                lineas.Add(String.Join(",", columnasActuales.Select(Function(c) Csv(c))))

                For Each row As DataGridViewRow In gridPreview.Rows
                    If row.IsNewRow Then Continue For
                    Dim valores = columnasActuales.Select(Function(c, i) Csv(If(row.Cells(i).Value, "").ToString())).ToArray()
                    lineas.Add(String.Join(",", valores))
                Next

                File.WriteAllLines(dialogo.FileName, lineas)

                Try
                    Dim libreOffice = BuscarLibreOffice()
                    If libreOffice IsNot Nothing Then
                        Process.Start(New ProcessStartInfo(libreOffice, dialogo.FileName) With {.UseShellExecute = False})
                    Else
                        Process.Start(New ProcessStartInfo(dialogo.FileName) With {.UseShellExecute = True})
                    End If
                Catch
                End Try
            End Using
        End Sub

        Private Function BuscarLibreOffice() As String
            Dim rutas() As String = {
                "C:\Program Files\LibreOffice\program\soffice.exe",
                "C:\Program Files (x86)\LibreOffice\program\soffice.exe"
            }
            For Each r In rutas
                If File.Exists(r) Then Return r
            Next
            Return Nothing
        End Function

        Private Function Csv(valor As String) As String
            Dim comilla As String = ChrW(34)
            Dim s = If(valor, "").Replace(comilla, comilla & comilla)
            If s.Contains(",") OrElse s.Contains(ChrW(10)) OrElse s.Contains(ChrW(13)) Then
                s = comilla & s & comilla
            End If
            Return s
        End Function

    End Class

End Namespace



