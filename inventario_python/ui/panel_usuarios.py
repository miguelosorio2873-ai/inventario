import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from dao.usuario_dao import UsuarioDAO
from models.usuario import Usuario
from utils.argon2_util import es_segura, get_requisitos_mensaje, hash_password
from db.conexion import error_manager
from tkinter import messagebox
import random
import tkinter as tk
from utils.icon_util import icon_users, icon_plus, icon_edit, icon_trash, icon_key, icon_search, icon_save


PREGUNTAS = [
    "¿Cuál es el nombre de tu primera mascota?",
    "¿Cuál es tu comida favorita?",
    "¿En qué ciudad naciste?",
    "¿Cuál es el segundo nombre de tu madre?",
]


class PanelUsuarios(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.dao = UsuarioDAO()
        self.editando = None

        ctk.CTkLabel(self, text="Usuarios", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_users(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        toolbar = ctk.CTkFrame(self, fg_color="transparent")
        toolbar.pack(fill="x", padx=25, pady=(0, 10))

        sesion = SesionUsuario()
        if sesion.tiene_permiso_accion("Usuarios", "CREAR"):
            ctk.CTkButton(toolbar, text="Nuevo", width=100, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_plus(16, "#ffffff"), compound="left", command=self.nuevo).pack(side="right", padx=2)

        self.search_entry = ctk.CTkEntry(toolbar, width=250, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT, placeholder_text="Buscar...")
        self.search_entry.pack(side="left", padx=(0, 10))
        self.search_entry.bind("<Return>", lambda e: self.buscar())
        ctk.CTkButton(toolbar, text="", width=40, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_search(16, COLOR_TEXT_MUTED), command=self.buscar).pack(side="left", padx=2)

        self.tabla_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        self.tabla_frame.pack(fill="both", expand=True, padx=25, pady=(0, 25))
        self._crear_tabla()
        self.cargar_datos()

    def _crear_tabla(self):
        header = ctk.CTkFrame(self.tabla_frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header.pack(fill="x")
        header.pack_propagate(False)
        for col in ["ID", "Nombre", "Email", "Rol", "Permisos", "Último Login"]:
            ctk.CTkLabel(header, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)
        self.scroll = ctk.CTkScrollableFrame(self.tabla_frame, fg_color=COLOR_CARD, corner_radius=0)
        self.scroll.pack(fill="both", expand=True)

    def cargar_datos(self):
        for w in self.scroll.winfo_children():
            w.destroy()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Usuarios", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Usuarios", "ELIMINAR")
        for u in self.dao.listar_todos():
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            perm_corto = (u.permisos[:40] + "...") if u.permisos and len(u.permisos) > 40 else (u.permisos or "")
            ultimo = u.ultimo_login.strftime("%Y-%m-%d %H:%M") if hasattr(u, 'ultimo_login') and u.ultimo_login else "Nunca"
            vals = [u.id, u.nombre, u.email, u.rol, perm_corto, ultimo]
            for v in vals:
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)
            if puede_editar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_key(16, COLOR_YELLOW), command=lambda uid=u.id: self.cambiar_password(uid)).pack(side="right", padx=2)
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_edit(16, COLOR_TEXT_MUTED), command=lambda uid=u.id: self.editar(uid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda uid=u.id: self.eliminar(uid)).pack(side="right", padx=2)

    def buscar(self):
        texto = self.search_entry.get().strip().lower()
        if not texto:
            self.cargar_datos()
            return
        for w in self.scroll.winfo_children():
            w.destroy()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Usuarios", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Usuarios", "ELIMINAR")
        for u in self.dao.listar_todos():
            if texto not in (u.nombre or "").lower() and texto not in (u.email or "").lower() and texto not in (u.rol or "").lower():
                continue
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            perm_corto = (u.permisos[:40] + "...") if u.permisos and len(u.permisos) > 40 else (u.permisos or "")
            ultimo = u.ultimo_login.strftime("%Y-%m-%d %H:%M") if hasattr(u, 'ultimo_login') and u.ultimo_login else "Nunca"
            vals = [u.id, u.nombre, u.email, u.rol, perm_corto, ultimo]
            for v in vals:
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)
            if puede_editar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_key(16, COLOR_YELLOW), command=lambda uid=u.id: self.cambiar_password(uid)).pack(side="right", padx=2)
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_edit(16, COLOR_TEXT_MUTED), command=lambda uid=u.id: self.editar(uid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda uid=u.id: self.eliminar(uid)).pack(side="right", padx=2)

    def nuevo(self):
        self.editando = None
        self._mostrar_formulario()

    def editar(self, id):
        users = self.dao.listar_todos()
        u = next((x for x in users if x.id == id), None)
        if not u:
            return
        self.editando = u
        self._mostrar_formulario()

    def _mostrar_formulario(self):
        win = ctk.CTkToplevel(self)
        win.title("Usuario")
        win.geometry("450x600")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text="Formulario de Usuario", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(pady=15)

        ctk.CTkLabel(win, text="Nombre", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_nombre = ctk.CTkEntry(win, width=380, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_nombre.pack(pady=(2, 10), padx=30)

        ctk.CTkLabel(win, text="Email", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_email = ctk.CTkEntry(win, width=380, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_email.pack(pady=(2, 10), padx=30)

        ctk.CTkLabel(win, text="Contraseña" + ("" if self.editando else " (obligatoria)"), font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_pass = ctk.CTkEntry(win, width=380, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT, show="*")
        entry_pass.pack(pady=(2, 10), padx=30)
        if self.editando:
            ctk.CTkLabel(win, text="(Dejar en blanco para mantener actual)", font=("Segoe UI", 10), text_color=COLOR_TEXT_DIM).pack(anchor="w", padx=30)

        ctk.CTkLabel(win, text="Rol", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        rol_combo = ctk.CTkComboBox(win, values=["Admin", "Usuario"], width=380, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        rol_combo.pack(pady=(2, 10), padx=30)
        rol_combo.set("Usuario")

        # Matriz de permisos
        ctk.CTkLabel(win, text="Permisos por Módulo", font=("Segoe UI", 14, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=30, pady=(10, 5))

        MODULOS = ["Productos", "Categorias", "Clientes", "Proveedores", "Inventario", "Facturas", "Usuarios", "Reportes", "Configuracion"]
        ACCIONES = ["VER", "CREAR", "EDITAR", "ELIMINAR", "EXPORTAR"]
        ACC_CODES = {"VER": "V", "CREAR": "C", "EDITAR": "E", "ELIMINAR": "D", "EXPORTAR": "X"}

        perm_frame = ctk.CTkScrollableFrame(win, fg_color=COLOR_CARD, height=200)
        perm_frame.pack(fill="x", padx=30, pady=(2, 10))

        perm_checks = {}
        perm_vars = {}
        for mod in MODULOS:
            mod_frame = ctk.CTkFrame(perm_frame, fg_color="transparent")
            mod_frame.pack(fill="x", pady=2)
            ctk.CTkLabel(mod_frame, text=mod, font=("Segoe UI", 12, "bold"), text_color=COLOR_TEXT, width=100).pack(side="left", padx=5)
            perm_checks[mod] = {}
            perm_vars[mod] = {}
            for acc in ACCIONES:
                var = tk.StringVar(value="off")
                chk = ctk.CTkCheckBox(mod_frame, text=acc[:3], font=("Segoe UI", 10), text_color=COLOR_TEXT_MUTED, fg_color=COLOR_GREEN, hover_color="#059669", width=50, variable=var, onvalue="on", offvalue="off")
                chk.pack(side="left", padx=3)
                perm_checks[mod][acc] = chk
                perm_vars[mod][acc] = var

        def generar_permisos_str():
            partes = []
            for mod in MODULOS:
                codigos = "".join(ACC_CODES[acc] for acc in ACCIONES if perm_vars[mod][acc].get() == "on")
                if codigos:
                    partes.append(f"{mod}:{codigos}")
            return ",".join(partes)

        def cargar_permisos_str(perm_str):
            if not perm_str:
                return
            for parte in perm_str.split(","):
                if ":" not in parte:
                    continue
                mod, cods = parte.split(":", 1)
                mod = mod.strip()
                cods = cods.strip().upper()
                if mod in perm_checks:
                    for acc in ACCIONES:
                        if ACC_CODES[acc] in cods:
                            perm_vars[mod][acc].set("on")
                        else:
                            perm_vars[mod][acc].set("off")

        # Preguntas de seguridad
        ctk.CTkLabel(win, text="Preguntas de Seguridad", font=("Segoe UI", 14, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=30, pady=(10, 5))
        preg_entries = []
        resp_entries = []
        for i, preg in enumerate(PREGUNTAS):
            ctk.CTkLabel(win, text=preg, font=("Segoe UI", 11), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
            re = ctk.CTkEntry(win, width=380, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
            re.pack(pady=(2, 5), padx=30)
            resp_entries.append(re)
            preg_entries.append(preg)

        if self.editando:
            u = self.editando
            entry_nombre.insert(0, u.nombre or "")
            entry_email.insert(0, u.email or "")
            rol_combo.set(u.rol or "Usuario")
            cargar_permisos_str(u.permisos or "")
            for i, re in enumerate(resp_entries):
                val = getattr(u, f"respuesta_{i+1}", "")
                re.insert(0, val or "")

        def guardar():
            try:
                u = self.editando or Usuario()
                u.nombre = entry_nombre.get().strip()
                u.email = entry_email.get().strip()
                u.rol = rol_combo.get()
                u.permisos = generar_permisos_str()
                pass_val = entry_pass.get().strip()
                if pass_val:
                    if not es_segura(pass_val):
                        messagebox.showwarning("Contraseña Débil", get_requisitos_mensaje())
                        return
                if not self.editando:
                    if not pass_val:
                        messagebox.showwarning("Error", "La contraseña es obligatoria")
                        return
                    u.password = pass_val
                    u.pregunta_1 = PREGUNTAS[0]
                    u.pregunta_2 = PREGUNTAS[1]
                    u.pregunta_3 = PREGUNTAS[2]
                    u.pregunta_4 = PREGUNTAS[3]
                    u.respuesta_1 = resp_entries[0].get().strip()
                    u.respuesta_2 = resp_entries[1].get().strip()
                    u.respuesta_3 = resp_entries[2].get().strip()
                    u.respuesta_4 = resp_entries[3].get().strip()
                    self.dao.insertar(u)
                else:
                    self.dao.actualizar(u)
                    if pass_val:
                        self.dao.cambiar_password(u.id, pass_val)
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))

        ctk.CTkButton(win, text="Guardar", width=200, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), image=icon_save(16, "#ffffff"), compound="left", command=guardar).pack(pady=15)

    def cambiar_password(self, id):
        users = self.dao.listar_todos()
        u = next((x for x in users if x.id == id), None)
        if not u:
            return
        pnum = random.randint(1, 4)
        preguntas = {1: (u.pregunta_1, u.respuesta_1), 2: (u.pregunta_2, u.respuesta_2), 3: (u.pregunta_3, u.respuesta_3), 4: (u.pregunta_4, u.respuesta_4)}
        preg, resp_correcta = preguntas[pnum]
        if not preg:
            messagebox.showwarning("Atención", "El usuario no tiene preguntas de seguridad configuradas.")
            return
        from tkinter import simpledialog
        respuesta = simpledialog.askstring("Verificación de Seguridad", f"{preg}:")
        if respuesta is None:
            return
        if respuesta.strip().lower() != (resp_correcta or "").lower():
            messagebox.showerror("Error", "Respuesta incorrecta.")
            return
        while True:
            new_pass = simpledialog.askstring("Nueva Contraseña", "Ingrese la nueva contraseña:", show="*")
            if not new_pass:
                break
            confirm = simpledialog.askstring("Confirmar", "Confirmar contraseña:", show="*")
            if new_pass != confirm:
                messagebox.showerror("Error", "Las contraseñas no coinciden.")
                continue
            if not es_segura(new_pass):
                messagebox.showwarning("Contraseña Débil", get_requisitos_mensaje())
                continue
            self.dao.cambiar_password(id, new_pass)
            messagebox.showinfo("Éxito", "Contraseña actualizada correctamente.")
            break

    def eliminar(self, id):
        if messagebox.askyesno("Confirmar", "¿Eliminar este usuario?"):
            try:
                self.dao.eliminar(id)
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))
