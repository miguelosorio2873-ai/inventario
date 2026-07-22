import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from dao.categoria_dao import CategoriaDAO
from models.categoria import Categoria
from db.conexion import error_manager
from tkinter import messagebox
from utils.icon_util import icon_tags, icon_plus, icon_edit, icon_trash, icon_save


class PanelCategorias(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.dao = CategoriaDAO()
        self.editando = None

        ctk.CTkLabel(self, text="Categorías", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_tags(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        toolbar = ctk.CTkFrame(self, fg_color="transparent")
        toolbar.pack(fill="x", padx=25, pady=(0, 10))

        sesion = SesionUsuario()
        if sesion.tiene_permiso_accion("Categorias", "CREAR"):
            ctk.CTkButton(toolbar, text="Nueva", width=100, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_plus(16, "#ffffff"), compound="left", command=self.nuevo).pack(side="right")

        self.tabla_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        self.tabla_frame.pack(fill="both", expand=True, padx=25, pady=(0, 25))
        self._crear_tabla()
        self.cargar_datos()

    def _crear_tabla(self):
        header = ctk.CTkFrame(self.tabla_frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header.pack(fill="x")
        header.pack_propagate(False)
        for col in ["ID", "Nombre", "Descripción"]:
            ctk.CTkLabel(header, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)
        self.scroll = ctk.CTkScrollableFrame(self.tabla_frame, fg_color=COLOR_CARD, corner_radius=0)
        self.scroll.pack(fill="both", expand=True)

    def cargar_datos(self):
        for w in self.scroll.winfo_children():
            w.destroy()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Categorias", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Categorias", "ELIMINAR")
        for c in self.dao.listar_todas():
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            for v in [c.id, c.nombre, c.descripcion]:
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)
            if puede_editar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_edit(16, COLOR_TEXT_MUTED), command=lambda cid=c.id: self.editar(cid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda cid=c.id: self.eliminar(cid)).pack(side="right", padx=2)

    def nuevo(self):
        self.editando = None
        self._mostrar_formulario()

    def editar(self, id):
        cats = self.dao.listar_todas()
        c = next((x for x in cats if x.id == id), None)
        if not c:
            return
        self.editando = c
        self._mostrar_formulario()

    def _mostrar_formulario(self):
        win = ctk.CTkToplevel(self)
        win.title("Categoría")
        win.geometry("400x300")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text="Formulario de Categoría", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(pady=15)

        ctk.CTkLabel(win, text="Nombre", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_nombre = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_nombre.pack(pady=(2, 10), padx=30)

        ctk.CTkLabel(win, text="Descripción", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_desc = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_desc.pack(pady=(2, 10), padx=30)

        if self.editando:
            entry_nombre.insert(0, self.editando.nombre or "")
            entry_desc.insert(0, self.editando.descripcion or "")

        def guardar():
            try:
                c = self.editando or Categoria()
                c.nombre = entry_nombre.get().strip()
                c.descripcion = entry_desc.get().strip()
                if self.editando:
                    self.dao.actualizar(c)
                else:
                    self.dao.insertar(c)
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))

        ctk.CTkButton(win, text="Guardar", width=200, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), image=icon_save(16, "#ffffff"), compound="left", command=guardar).pack(pady=20)

    def eliminar(self, id):
        if messagebox.askyesno("Confirmar", "¿Eliminar esta categoría?"):
            try:
                self.dao.eliminar(id)
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))
