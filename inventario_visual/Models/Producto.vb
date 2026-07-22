Namespace inventario_visual
    Public Class Producto
        Public Property Id As Long
        Public Property Sku As String
        Public Property Nombre As String
        Public Property Descripcion As String
        Public Property CategoriaId As Long?
        Public Property PrecioVenta As Double
        Public Property PrecioCompra As Double
        Public Property StockActual As Double
        Public Property StockMinimo As Double
        Public Property State As Boolean = True
        Public Property Imagen As String
        Public Property CategoriaNombre As String
    End Class
End Namespace
