import os
import json

DB_HOST = os.environ.get("INV_DB_HOST", "localhost")
DB_PORT = int(os.environ.get("INV_DB_PORT", "3306"))
DB_NAME = os.environ.get("INV_DB_NAME", "inventario_db")
DB_USER = os.environ.get("INV_DB_USER", "root")
DB_PASS = os.environ.get("INV_DB_PASS", "")

AES_KEY = b"AntigravityKey26"

CONFIG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app_config.json")


def _load_config():
    try:
        with open(CONFIG_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception:
        return {}


def _save_config(data):
    try:
        with open(CONFIG_FILE, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
    except Exception as e:
        print(f"Error guardando config: {e}")


def get_tasa_ves():
    return _load_config().get("tasa_ves", 45.0)


def set_tasa_ves(tasa):
    cfg = _load_config()
    cfg["tasa_ves"] = tasa
    _save_config(cfg)


def get_empresa_data():
    return _load_config().get("empresa", {"nombre": "", "nit": "", "telefono": "", "direccion": ""})


def set_empresa_data(data):
    cfg = _load_config()
    cfg["empresa"] = data
    _save_config(cfg)


def get_tema():
    return _load_config().get("tema", "dark")


def set_tema(tema):
    cfg = _load_config()
    cfg["tema"] = tema
    _save_config(cfg)

COLOR_BG = "#18181b"
COLOR_SIDEBAR = "#111827"
COLOR_SIDEBAR_HOVER = "#1f2937"
COLOR_ACTIVE = "#10b981"
COLOR_CARD = "#272729"
COLOR_BORDER = "#3f3f46"
COLOR_TEXT = "#ffffff"
COLOR_TEXT_MUTED = "#b4b4b4"
COLOR_TEXT_DIM = "#8c8c8c"
COLOR_GREEN = "#10b981"
COLOR_BLUE = "#3b82f6"
COLOR_RED = "#ef4444"
COLOR_YELLOW = "#facc15"
COLOR_PURPLE = "#a855f7"
