Imports System.Windows.Forms
Namespace inventario_visual

    Friend Module Program

        <STAThread()>
        Sub Main()
            Application.EnableVisualStyles()
            Application.SetCompatibleTextRenderingDefault(False)
            Application.Run(New LoginForm())
        End Sub

    End Module

End Namespace


