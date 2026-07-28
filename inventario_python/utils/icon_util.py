import os
import customtkinter as ctk

IMG_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "img")

_cache = {}


def _load_svg(filename, size=20, color="#ffffff"):
    """Carga un SVG, reemplaza currentColor por el color dado y lo convierte a CTkImage."""
    key = (filename, size, color)
    if key in _cache:
        return _cache[key]

    svg_path = os.path.join(IMG_DIR, filename)
    if not os.path.exists(svg_path):
        return None

    try:
        with open(svg_path, "r", encoding="utf-8") as f:
            svg_content = f.read()

        svg_content = svg_content.replace("currentColor", color)

        from resvg_py import svg_to_bytes
        from PIL import Image
        import io

        png_bytes = svg_to_bytes(svg_content)
        pil_image = Image.open(io.BytesIO(png_bytes))
        pil_image = pil_image.resize((size * 2, size * 2), Image.LANCZOS)

        img = ctk.CTkImage(light_image=pil_image, dark_image=pil_image, size=(size, size))
        _cache[key] = img
        return img
    except Exception as e:
        print(f"Error cargando icono {filename}: {e}")
        return None


def icon_home(size=20, color="#b4b4b4"):
    return _load_svg("home.svg", size, color)

def icon_box(size=20, color="#b4b4b4"):
    return _load_svg("box.svg", size, color)

def icon_tags(size=20, color="#b4b4b4"):
    return _load_svg("tags.svg", size, color)

def icon_users(size=20, color="#b4b4b4"):
    return _load_svg("users.svg", size, color)

def icon_truck(size=20, color="#b4b4b4"):
    return _load_svg("truck.svg", size, color)

def icon_chart_bar(size=20, color="#b4b4b4"):
    return _load_svg("chart-bar.svg", size, color)

def icon_file_invoice(size=20, color="#b4b4b4"):
    return _load_svg("file-invoice.svg", size, color)

def icon_chart_pie(size=20, color="#b4b4b4"):
    return _load_svg("chart-pie.svg", size, color)

def icon_cog(size=20, color="#b4b4b4"):
    return _load_svg("cog.svg", size, color)

def icon_sign_out(size=20, color="#ef4444"):
    return _load_svg("sign-out.svg", size, color)

def icon_plus(size=16, color="#ffffff"):
    return _load_svg("plus.svg", size, color)

def icon_edit(size=16, color="#ffffff"):
    return _load_svg("edit.svg", size, color)

def icon_trash(size=16, color="#ffffff"):
    return _load_svg("trash.svg", size, color)

def icon_search(size=16, color="#b4b4b4"):
    return _load_svg("search.svg", size, color)

def icon_clear(size=16, color="#b4b4b4"):
    return _load_svg("clear.svg", size, color)

def icon_check_circle(size=16, color="#ffffff"):
    return _load_svg("check-circle.svg", size, color)

def icon_x(size=16, color="#ffffff"):
    return _load_svg("x.svg", size, color)

def icon_key(size=16, color="#ffffff"):
    return _load_svg("key.svg", size, color)

def icon_save(size=16, color="#ffffff"):
    return _load_svg("save.svg", size, color)

def icon_download(size=16, color="#ffffff"):
    return _load_svg("download.svg", size, color)

def icon_upload(size=16, color="#ffffff"):
    return _load_svg("upload.svg", size, color)

def icon_wrench(size=16, color="#ffffff"):
    return _load_svg("wrench.svg", size, color)

def icon_minus(size=14, color="#facc15"):
    return _load_svg("minus.svg", size, color)

def icon_square(size=14, color="#34d399"):
    return _load_svg("square.svg", size, color)

def icon_close(size=14, color="#ef4444"):
    return _load_svg("x.svg", size, color)

def icon_moon(size=16, color="#ffffff"):
    return _load_svg("moon.svg", size, color)

def icon_sun(size=16, color="#ffffff"):
    return _load_svg("sun.svg", size, color)

def icon_dollar(size=16, color="#ffffff"):
    return _load_svg("dollar-sign.svg", size, color)

def icon_refresh(size=16, color="#ffffff"):
    return _load_svg("refresh.svg", size, color)

def icon_eye(size=16, color="#ffffff"):
    return _load_svg("eye.svg", size, color)

def icon_lock(size=16, color="#ffffff"):
    return _load_svg("lock.svg", size, color)

def icon_mail(size=16, color="#ffffff"):
    return _load_svg("mail.svg", size, color)
