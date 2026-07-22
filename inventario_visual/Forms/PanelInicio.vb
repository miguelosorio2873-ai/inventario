Imports System.Drawing
Imports System.Data
Imports System.Linq
Imports System.Windows.Forms

Namespace inventario_visual

    Public Class PanelInicio
        Inherits UserControl

        Private ReadOnly COLOR_CARD As Color = Color.FromArgb(39, 39, 42)
        Private ReadOnly COLOR_BG As Color = Color.FromArgb(24, 24, 27)
        Private topProductos As List(Of Producto)

        Public Sub New()
            Me.BackColor = COLOR_BG
            Me.Dock = DockStyle.Fill
            Me.AutoScroll = True
            CargarDatos()

            Dim container As New Panel()
            container.Dock = DockStyle.Fill
            container.AutoScroll = True
            container.BackColor = COLOR_BG
            container.Padding = New Padding(25)

            ' Header
            Dim user = SesionUsuario.UsuarioActual
            Dim nombre = If(user IsNot Nothing, user.Nombre, "Usuario")
            Dim hora = DateTime.Now.Hour
            Dim saludo = If(hora < 12, "Buenos dias", If(hora < 18, "Buenas tardes", "Buenas noches"))

            Dim lblTitulo = New Label With {.Text = "Inicio", .Font = New Font("Segoe UI", 26, FontStyle.Bold), .ForeColor = Color.White, .AutoSize = True, .Location = New Point(0, 0)}
            container.Controls.Add(lblTitulo)

            ' Welcome card
            Dim welcomeCard = CrearCard(0, 45, 500, 110)
            AddHandler welcomeCard.Paint, Sub(s, e)
                                              Using b As New SolidBrush(COLOR_CARD), p As New Pen(Color.FromArgb(16, 185, 129), 2)
                                                  e.Graphics.SmoothingMode = Drawing2D.SmoothingMode.AntiAlias
                                                  e.Graphics.FillRoundedRectangle(b, 0, 0, welcomeCard.Width - 1, welcomeCard.Height - 1, 16)
                                                  e.Graphics.DrawRoundedRectangle(p, 0, 0, welcomeCard.Width - 1, welcomeCard.Height - 1, 16)
                                              End Using
                                          End Sub
            Dim lblWelcome = New Label With {.Text = saludo & ", " & nombre, .Font = New Font("Segoe UI", 18, FontStyle.Bold), .ForeColor = Color.FromArgb(16, 185, 129), .AutoSize = True, .Location = New Point(20, 15), .BackColor = Color.Transparent}
            welcomeCard.Controls.Add(lblWelcome)
            Dim lblInfo = New Label With {.Text = "Rol: " & If(user IsNot Nothing, user.Rol, "Admin") & "  |  Fecha: " & DateTime.Now.ToString("dd/MM/yyyy"), .Font = New Font("Segoe UI", 12), .ForeColor = Color.FromArgb(180, 180, 180), .AutoSize = True, .Location = New Point(20, 48), .BackColor = Color.Transparent}
            welcomeCard.Controls.Add(lblInfo)
            Dim lblDesc = New Label With {.Text = "Sistema de Inventario Pro - Gestion integral de productos, inventario y facturacion", .Font = New Font("Segoe UI", 11), .ForeColor = Color.FromArgb(120, 120, 120), .AutoSize = True, .Location = New Point(20, 75), .BackColor = Color.Transparent}
            welcomeCard.Controls.Add(lblDesc)
            container.Controls.Add(welcomeCard)

            ' Stats
            Dim statsPanel = New FlowLayoutPanel With {.Location = New Point(0, 170), .Size = New Size(1000, 120), .BackColor = Color.Transparent, .FlowDirection = FlowDirection.LeftToRight, .WrapContents = True, .AutoScroll = False, .AutoSize = True}
            Dim pDao = New ProductoDAO()
            Dim cDao = New ClienteDAO()
            Dim fDao = New FacturaDAO()
            Dim iDao = New InventarioDAO()
            Dim totalProd = 0
            Dim stockBajo = 0
            Dim totalClientes = 0
            Dim ventasMes = 0.0
            Try
                Dim productos = pDao.ListarTodos()
                totalProd = productos.Count
                stockBajo = productos.Where(Function(p) p.StockActual < p.StockMinimo).Count()
                totalClientes = cDao.ListarTodos().Count
                Dim facturas = fDao.ListarTodas()
                Dim mesActual = DateTime.Now.Month
                ventasMes = facturas.Where(Function(f) f.FechaEmision.Month = mesActual).Sum(Function(f) f.Total)
            Catch
            End Try
            statsPanel.Controls.Add(CrearStatCard("Productos", totalProd.ToString(), Color.FromArgb(59, 130, 246)))
            statsPanel.Controls.Add(CrearStatCard("Stock Bajo", stockBajo.ToString(), Color.FromArgb(245, 158, 11)))
            statsPanel.Controls.Add(CrearStatCard("Clientes", totalClientes.ToString(), Color.FromArgb(139, 92, 246)))
            statsPanel.Controls.Add(CrearStatCard("Ventas del Mes", "$" & ventasMes.ToString("N2"), Color.FromArgb(16, 185, 129)))
            container.Controls.Add(statsPanel)

            ' Bottom section
            Dim bottomPanel = New TableLayoutPanel With {.Location = New Point(0, 305), .Size = New Size(1000, 300), .ColumnCount = 2, .RowCount = 2, .BackColor = Color.Transparent}
            bottomPanel.ColumnStyles.Add(New ColumnStyle(SizeType.Percent, 50.0F))
            bottomPanel.ColumnStyles.Add(New ColumnStyle(SizeType.Percent, 50.0F))
            bottomPanel.RowStyles.Add(New RowStyle(SizeType.AutoSize))
            bottomPanel.RowStyles.Add(New RowStyle(SizeType.Percent, 100.0F))

            Dim lblStock = New Label With {.Text = "Productos con Stock Bajo", .Font = New Font("Segoe UI", 16, FontStyle.Bold), .ForeColor = Color.White, .AutoSize = True, .Anchor = AnchorStyles.Left}
            bottomPanel.Controls.Add(lblStock, 0, 0)

            Dim lblChart = New Label With {.Text = "Top 10 Productos por Stock", .Font = New Font("Segoe UI", 16, FontStyle.Bold), .ForeColor = Color.White, .AutoSize = True, .Anchor = AnchorStyles.Left}
            bottomPanel.Controls.Add(lblChart, 1, 0)

            Dim stockCard = CrearCard(0, 0, 100, 100)
            stockCard.Dock = DockStyle.Fill
            AddHandler stockCard.Paint, Sub(s, e)
                                            Using b As New SolidBrush(COLOR_CARD)
                                                e.Graphics.SmoothingMode = Drawing2D.SmoothingMode.AntiAlias
                                                e.Graphics.FillRoundedRectangle(b, 0, 0, stockCard.Width - 1, stockCard.Height - 1, 16)
                                            End Using
                                        End Sub
            Dim gridStock = CrearGridStockBajo()
            stockCard.Controls.Add(gridStock)
            bottomPanel.Controls.Add(stockCard, 0, 1)

            Dim chartCard = CrearCard(0, 0, 100, 100)
            chartCard.Dock = DockStyle.Fill
            AddHandler chartCard.Paint, AddressOf ChartCard_Paint
            bottomPanel.Controls.Add(chartCard, 1, 1)

            container.Controls.Add(bottomPanel)

            Me.Controls.Add(container)
            AddHandler container.Resize, Sub(s, e)
                                             welcomeCard.Width = container.Width - 50
                                             statsPanel.Width = container.Width - 50
                                             bottomPanel.Width = container.Width - 50
                                             bottomPanel.Height = Math.Max(250, container.Height - statsPanel.Bottom - 60)
                                             bottomPanel.Top = statsPanel.Bottom + 25
                                         End Sub
        End Sub

        Private Sub CargarDatos()
            Try
                topProductos = New ProductoDAO().ListarTodos().OrderByDescending(Function(p) p.StockActual).Take(10).ToList()
            Catch
                topProductos = New List(Of Producto)()
            End Try
        End Sub

        Private Function CrearCard(x As Integer, y As Integer, w As Integer, h As Integer) As Panel
            Return New Panel With {.Location = New Point(x, y), .Size = New Size(w, h), .BackColor = Color.Transparent}
        End Function

        Private Function CrearStatCard(titulo As String, valor As String, color As Color) As Panel
            Dim card = New Panel With {.Size = New Size(185, 100), .BackColor = Color.Transparent, .Margin = New Padding(8)}
            AddHandler card.Paint, Sub(s, e)
                                       Using b As New SolidBrush(COLOR_CARD), p As New Pen(color, 2)
                                           e.Graphics.SmoothingMode = Drawing2D.SmoothingMode.AntiAlias
                                           e.Graphics.FillRoundedRectangle(b, 0, 0, card.Width - 1, card.Height - 1, 12)
                                           e.Graphics.DrawLine(p, 0, 10, 0, card.Height - 10)
                                       End Using
                                   End Sub
            Dim lblTitulo = New Label With {.Text = titulo, .Font = New Font("Segoe UI", 11), .ForeColor = Color.FromArgb(140, 140, 140), .AutoSize = True, .Location = New Point(15, 15), .BackColor = Color.Transparent}
            Dim lblValor = New Label With {.Text = valor, .Font = New Font("Segoe UI", 22, FontStyle.Bold), .ForeColor = color, .AutoSize = True, .Location = New Point(15, 42), .BackColor = Color.Transparent}
            card.Controls.Add(lblTitulo)
            card.Controls.Add(lblValor)
            Return card
        End Function

        Private Function CrearGridStockBajo() As DataGridView
            Dim grid = New DataGridView With {.Dock = DockStyle.Fill, .BackgroundColor = COLOR_CARD, .BorderStyle = BorderStyle.None, .ReadOnly = True, .AllowUserToAddRows = False, .RowHeadersVisible = False, .AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill, .SelectionMode = DataGridViewSelectionMode.FullRowSelect, .ColumnHeadersHeight = 32}
            grid.DefaultCellStyle.BackColor = COLOR_CARD
            grid.DefaultCellStyle.ForeColor = Color.White
            grid.DefaultCellStyle.SelectionBackColor = Color.FromArgb(16, 185, 129, 50)
            grid.DefaultCellStyle.SelectionForeColor = Color.White
            grid.DefaultCellStyle.Font = New Font("Segoe UI", 11)
            grid.ColumnHeadersDefaultCellStyle.BackColor = Color.FromArgb(30, 30, 30)
            grid.ColumnHeadersDefaultCellStyle.ForeColor = Color.FromArgb(180, 180, 180)
            grid.ColumnHeadersDefaultCellStyle.Font = New Font("Segoe UI", 11, FontStyle.Bold)
            grid.EnableHeadersVisualStyles = False
            grid.RowTemplate.Height = 30
            grid.Columns.Add("Producto", "Producto")
            grid.Columns.Add("SKU", "SKU")
            grid.Columns.Add("Stock", "Stock Actual")
            grid.Columns.Add("Minimo", "Stock Mín")
            Try
                Dim bajos = New ProductoDAO().ListarTodos().Where(Function(p) p.StockActual < p.StockMinimo).ToList()
                For Each p In bajos
                    grid.Rows.Add(p.Nombre, p.Sku, p.StockActual.ToString("F0"), p.StockMinimo.ToString("F0"))
                Next
            Catch
            End Try
            Return grid
        End Function

        Private Sub ChartCard_Paint(sender As Object, e As PaintEventArgs)
            Dim panel = CType(sender, Panel)
            e.Graphics.SmoothingMode = Drawing2D.SmoothingMode.AntiAlias
            Using b As New SolidBrush(COLOR_CARD)
                e.Graphics.FillRoundedRectangle(b, 0, 0, Math.Max(1, panel.Width - 1), Math.Max(1, panel.Height - 1), 16)
            End Using
            If topProductos Is Nothing OrElse topProductos.Count = 0 Then Return
            If panel.Width < 80 OrElse panel.Height < 80 Then Return
            Dim g = e.Graphics
            Dim limit = Math.Min(10, topProductos.Count)
            Dim maxStock = topProductos.Take(limit).Max(Function(p) p.StockActual)
            If maxStock = 0 Then maxStock = 1
            Dim areaW = Math.Max(40, panel.Width - 40)
            Dim areaH = Math.Max(60, panel.Height - 60)
            Dim barW = Math.Max(20, (areaW - (limit + 1) * 8) / limit)
            For i As Integer = 0 To limit - 1
                Dim p = topProductos(i)
                Dim rawBarH = CInt((p.StockActual / maxStock) * (areaH - 20))
                Dim barH = Math.Max(5, rawBarH)
                Dim x = 20 + i * (barW + 8)
                Dim y = panel.Height - 40 - barH
                If y < 0 Then y = 0
                If y + barH <= y Then Continue For
                Try
                    Using barBrush As New Drawing2D.LinearGradientBrush(New Point(CInt(x), y), New Point(CInt(x), y + barH), Color.FromArgb(59, 130, 246), Color.FromArgb(29, 78, 216))
                        g.FillRoundedRectangle(barBrush, x, y, CInt(barW), barH, 6)
                    End Using
                Catch
                    Using fallback As New SolidBrush(Color.FromArgb(59, 130, 246))
                        g.FillRoundedRectangle(fallback, x, y, CInt(barW), barH, 6)
                    End Using
                End Try
                g.DrawString(p.StockActual.ToString("F0"), New Font("Segoe UI", 9, FontStyle.Bold), Brushes.White, CSng(x + (barW - g.MeasureString(p.StockActual.ToString("F0"), New Font("Segoe UI", 9, FontStyle.Bold)).Width) / 2), CSng(y - 15))
                Dim nombre = If(p.Nombre.Length > 8, p.Nombre.Substring(0, 8) & "..", p.Nombre)
                g.DrawString(nombre, New Font("Segoe UI", 9), Brushes.Gray, CSng(x + (barW - g.MeasureString(nombre, New Font("Segoe UI", 9)).Width) / 2), CSng(panel.Height - 30))
            Next
        End Sub
    End Class

    Public Module GraphicsExtensions
        <System.Runtime.CompilerServices.Extension()>
        Public Sub FillRoundedRectangle(g As Graphics, brush As Brush, x As Single, y As Single, w As Single, h As Single, radius As Single)
            Using path As New Drawing2D.GraphicsPath()
                path.AddArc(x + w - 2 * radius, y, 2 * radius, 2 * radius, 270, 90)
                path.AddArc(x + w - 2 * radius, y + h - 2 * radius, 2 * radius, 2 * radius, 0, 90)
                path.AddArc(x, y + h - 2 * radius, 2 * radius, 2 * radius, 90, 90)
                path.AddArc(x, y, 2 * radius, 2 * radius, 180, 90)
                path.CloseFigure()
                g.FillPath(brush, path)
            End Using
        End Sub

        <System.Runtime.CompilerServices.Extension()>
        Public Sub DrawRoundedRectangle(g As Graphics, pen As Pen, x As Single, y As Single, w As Single, h As Single, radius As Single)
            Using path As New Drawing2D.GraphicsPath()
                path.AddArc(x + w - 2 * radius, y, 2 * radius, 2 * radius, 270, 90)
                path.AddArc(x + w - 2 * radius, y + h - 2 * radius, 2 * radius, 2 * radius, 0, 90)
                path.AddArc(x, y + h - 2 * radius, 2 * radius, 2 * radius, 90, 90)
                path.AddArc(x, y, 2 * radius, 2 * radius, 180, 90)
                path.CloseFigure()
                g.DrawPath(pen, path)
            End Using
        End Sub
    End Module

End Namespace



