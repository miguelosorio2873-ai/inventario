import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import customtkinter as ctk
from dao.usuario_dao import UsuarioDAO
from core.sesion_usuario import SesionUsuario
from ui.dashboard import Dashboard
from utils.argon2_util import es_segura, get_requisitos_mensaje
from utils.icon_util import icon_box, icon_users, icon_key
from tkinter import messagebox, simpledialog
import random
from config import *


class Login(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Inventario Pro - Login")
        self.geometry("900x520")
        self.resizable(False, False)
        self.configure(fg_color=COLOR_BG)
        self.protocol("WM_DELETE_WINDOW", lambda: sys_exit())

        left = ctk.CTkFrame(self, fg_color=COLOR_GREEN, corner_radius=0)
        left.pack(side="left", fill="both", expand=True)

        ctk.CTkLabel(left, text="", image=icon_box(72, "#ffffff")).pack(pady=(120, 5))
        ctk.CTkLabel(left, text="Inventario Pro", font=("Segoe UI", 32, "bold"), text_color="#ffffff").pack(pady=(5, 5))
        ctk.CTkLabel(left, text="Sistema Integral de Gestión\nde Inventario", font=("Segoe UI", 16), text_color="#e0e0e0", justify="center").pack(pady=5)
        ctk.CTkLabel(left, text="v1.0.0", font=("Segoe UI", 12), text_color="#a0a0a0").pack(pady=(40, 0))

        right = ctk.CTkFrame(self, fg_color="#1e1e1e", corner_radius=0)
        right.pack(side="right", fill="both", expand=True, padx=(0, 0))

        ctk.CTkLabel(right, text="Iniciar Sesión", font=("Segoe UI", 26, "bold"), text_color=COLOR_TEXT).pack(pady=(60, 5), padx=50, anchor="w")
        ctk.CTkLabel(right, text="Ingrese sus credenciales para continuar", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(pady=(0, 30), padx=50, anchor="w")

        ctk.CTkLabel(right, text="Correo electrónico", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(padx=50, anchor="w")
        email_frame = ctk.CTkFrame(right, fg_color="#2d2d2d", corner_radius=6, border_width=1, border_color="#3c3c3c")
        email_frame.pack(padx=50, pady=(2, 15), anchor="w")
        ctk.CTkLabel(email_frame, text="", image=icon_users(16, "#969696")).pack(side="left", padx=(12, 0))
        self.email_entry = ctk.CTkEntry(email_frame, width=260, height=38, fg_color="transparent", border_width=0, text_color=COLOR_TEXT)
        self.email_entry.pack(side="left", padx=(8, 12), pady=0)

        ctk.CTkLabel(right, text="Contraseña", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(padx=50, anchor="w")
        pass_frame = ctk.CTkFrame(right, fg_color="#2d2d2d", corner_radius=6, border_width=1, border_color="#3c3c3c")
        pass_frame.pack(padx=50, pady=(2, 25), anchor="w")
        ctk.CTkLabel(pass_frame, text="", image=icon_key(16, "#969696")).pack(side="left", padx=(12, 0))
        self.pass_entry = ctk.CTkEntry(pass_frame, width=260, height=38, fg_color="transparent", border_width=0, text_color=COLOR_TEXT, show="*")
        self.pass_entry.pack(side="left", padx=(8, 12), pady=0)

        ctk.CTkButton(right, text="INGRESAR", width=300, height=42, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), command=self.hacer_login).pack(padx=50, pady=5, anchor="w")

        ctk.CTkButton(right, text="¿Olvidaste tu contraseña?", width=300, fg_color="transparent", hover_color="#2d2d2d", text_color=COLOR_GREEN, font=("Segoe UI", 12), command=self.recuperar_password).pack(padx=50, pady=(10, 0), anchor="w")

        self.bind("<Return>", lambda e: self.hacer_login())

    def hacer_login(self):
        email = self.email_entry.get().strip()
        password = self.pass_entry.get().strip()
        if not email or not password:
            return
        try:
            dao = UsuarioDAO()
            user = dao.login(email, password)
            if user:
                SesionUsuario().iniciar_sesion(user.id, user.nombre, user.rol, user.permisos)
                self.destroy()
                Dashboard().mainloop()
        except Exception as ex:
            messagebox.showwarning("Atención", str(ex))

    def recuperar_password(self):
        email = simpledialog.askstring("Recuperación de Contraseña", "Ingrese su correo registrado:")
        if not email or not email.strip():
            return
        try:
            dao = UsuarioDAO()
            user = dao.buscar_por_email(email.strip())
            if not user:
                messagebox.showerror("Error", "El correo no existe en el sistema.")
                return
            pnum = random.randint(1, 4)
            preguntas = {1: (user.pregunta_1, user.respuesta_1), 2: (user.pregunta_2, user.respuesta_2), 3: (user.pregunta_3, user.respuesta_3), 4: (user.pregunta_4, user.respuesta_4)}
            preg, resp_correcta = preguntas[pnum]
            if not preg:
                messagebox.showwarning("Atención", "El usuario no cuenta con preguntas de seguridad configuradas.")
                return
            respuesta = simpledialog.askstring("Pregunta de Seguridad", preg)
            if respuesta and respuesta.strip().lower() == (resp_correcta or "").lower():
                while True:
                    new_pass = simpledialog.askstring("Reset de Clave", "Ingrese su nueva contraseña:", show="*")
                    if not new_pass:
                        break
                    confirm = simpledialog.askstring("Confirmar", "Confirmar contraseña:", show="*")
                    if new_pass != confirm:
                        messagebox.showerror("Error", "Las contraseñas no coinciden.")
                        continue
                    if not es_segura(new_pass):
                        messagebox.showwarning("Contraseña Débil", get_requisitos_mensaje())
                        continue
                    dao.cambiar_password(user.id, new_pass)
                    messagebox.showinfo("Éxito", "Contraseña actualizada correctamente.")
                    break
            elif respuesta:
                messagebox.showerror("Error de Validación", "Respuesta incorrecta.")
        except Exception as ex:
            messagebox.showerror("Error de Sistema", str(ex))


def sys_exit():
    import sys
    sys.exit(0)


if __name__ == "__main__":
    ctk.set_appearance_mode("dark")
    ctk.set_default_color_theme("green")
    Login().mainloop()
