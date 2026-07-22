Imports MySql.Data.MySqlClient
Imports System.Data

Namespace inventario_visual

    Public Module ConexionDB

        Public Function GetConnection() As MySqlConnection
            Dim host As String = Environment.GetEnvironmentVariable("DB_HOST")
            If String.IsNullOrEmpty(host) Then host = "localhost"

            Dim port As String = Environment.GetEnvironmentVariable("DB_PORT")
            If String.IsNullOrEmpty(port) Then port = "3306"

            Dim db As String = Environment.GetEnvironmentVariable("DB_NAME")
            If String.IsNullOrEmpty(db) Then db = "inventario_db"

            Dim user As String = Environment.GetEnvironmentVariable("DB_USER")
            If String.IsNullOrEmpty(user) Then user = "root"

            Dim pass As String = Environment.GetEnvironmentVariable("DB_PASS")
            If pass Is Nothing Then pass = ""

            Dim connStr As String = String.Format("Server={0};Port={1};Database={2};Uid={3};Pwd={4};SslMode=None;", host, port, db, user, pass)
            Return New MySqlConnection(connStr)
        End Function

        Public Function ErrorManager(ex As Exception) As String
            If TypeOf ex Is MySqlException Then
                Dim mysqlEx = CType(ex, MySqlException)
                Return String.Format("Error BD ({0}): {1}", mysqlEx.Number, mysqlEx.Message)
            End If
            Return ex.Message
        End Function

    End Module

End Namespace
