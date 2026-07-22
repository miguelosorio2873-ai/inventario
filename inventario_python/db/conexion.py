import mysql.connector
from config import DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS


def conectar():
    try:
        conn = mysql.connector.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASS,
        )
        return conn
    except mysql.connector.Error as e:
        print(f"Conexión fallida: {e}")
        return None


def error_manager(e):
    code = getattr(e, "errno", 0)
    msg = str(e)
    if code == 1452:
        return "Registro referenciado no encontrado"
    elif code == 1062:
        return "El registro ya existe (duplicado)"
    elif code == 1048:
        return "Campo obligatorio vacío"
    else:
        return f"Error BD [{code}]: {msg}"
