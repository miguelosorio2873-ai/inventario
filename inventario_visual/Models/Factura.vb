Namespace inventario_visual
    Public Class Factura
        Public Property Id As Long
        Public Property MovimientoId As Long?
        Public Property ClienteId As Long?
        Public Property NumeroFactura As String
        Public Property FechaEmision As DateTime
        Public Property MetodoPago As String
        Public Property Estado As String
        Public Property Subtotal As Double
        Public Property Impuestos As Double
        Public Property Total As Double
        Public Property ClienteNombre As String
    End Class
End Namespace
