Namespace inventario_visual
    Public Class MovimientoInventario
        Public Property Id As Long
        Public Property ProductoId As Long
        Public Property ProveedorId As Long?
        Public Property Precio As Double
        Public Property PrecioBalance As Double
        Public Property Cantidad As Double
        Public Property TipoMovimiento As String
        Public Property FechaMovimiento As DateTime
        Public Property Motivo As String
        Public Property ProductoNombre As String
        Public Property ProveedorNombre As String
    End Class
End Namespace
