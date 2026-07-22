import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from dao.proveedor_dao import ProveedorDAO
from models.proveedor import Proveedor
from db.conexion import error_manager
from tkinter import messagebox
from utils.icon_util import icon_truck, icon_plus, icon_edit, icon_trash, icon_search, icon_clear, icon_save


class PanelProveedores(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.dao = ProveedorDAO()
        self.editando = None

        ctk.CTkLabel(self, text="Proveedores", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_truck(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        toolbar = ctk.CTkFrame(self, fg_color="transparent")
        toolbar.pack(fill="x", padx=25, pady=(0, 10))

        self.search_entry = ctk.CTkEntry(toolbar, width=250, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT, placeholder_text="Buscar...")
        self.search_entry.pack(side="left", padx=(0, 10))
        self.search_entry.bind("<Return>", lambda e: self.buscar())
        ctk.CTkButton(toolbar, text="", width=40, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_search(16, COLOR_TEXT_MUTED), command=self.buscar).pack(side="left", padx=2)
        ctk.CTkButton(toolbar, text="", width=30, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_clear(16, COLOR_TEXT_MUTED), command=self.limpiar_busqueda).pack(side="left", padx=2)

        sesion = SesionUsuario()
        if sesion.tiene_permiso_accion("Proveedores", "CREAR"):
            ctk.CTkButton(toolbar, text="Nuevo", width=100, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_plus(16, "#ffffff"), compound="left", command=self.nuevo).pack(side="right", padx=2)

        self.tabla_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        self.tabla_frame.pack(fill="both", expand=True, padx=25, pady=(0, 25))
        self._crear_tabla()
        self.cargar_datos()

    def _crear_tabla(self):
        header = ctk.CTkFrame(self.tabla_frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header.pack(fill="x")
        header.pack_propagate(False)
        for col in ["ID", "Empresa", "NIT/Cédula", "Teléfono", "Correo", "Contacto"]:
            ctk.CTkLabel(header, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)
        self.scroll = ctk.CTkScrollableFrame(self.tabla_frame, fg_color=COLOR_CARD, corner_radius=0)
        self.scroll.pack(fill="both", expand=True)

    def cargar_datos(self):
        for w in self.scroll.winfo_children():
            w.destroy()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Proveedores", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Proveedores", "ELIMINAR")
        for p in self.dao.listar_todos():
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            for v in [p.id, p.nombre_empresa, p.nit_cedula, p.telefono, p.correo, p.nombre_contacto]:
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)
            if puede_editar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_edit(16, COLOR_TEXT_MUTED), command=lambda pid=p.id: self.editar(pid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda pid=p.id: self.eliminar(pid)).pack(side="right", padx=2)

    def limpiar_busqueda(self):
        self.search_entry.delete(0, "end")
        self.cargar_datos()

    def buscar(self):
        texto = self.search_entry.get().strip()
        if not texto:
            self.cargar_datos()
            return
        for w in self.scroll.winfo_children():
            w.destroy()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Proveedores", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Proveedores", "ELIMINAR")
        for p in self.dao.buscar(texto):
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            for v in [p.id, p.nombre_empresa, p.nit_cedula, p.telefono, p.correo, p.nombre_contacto]:
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)
            if puede_editar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_edit(16, COLOR_TEXT_MUTED), command=lambda pid=p.id: self.editar(pid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda pid=p.id: self.eliminar(pid)).pack(side="right", padx=2)

    def nuevo(self):
        self.editando = None
        self._mostrar_formulario()

    def editar(self, id):
        provs = self.dao.listar_todos()
        p = next((x for x in provs if x.id == id), None)
        if not p:
            return
        self.editando = p
        self._mostrar_formulario()

    def _mostrar_formulario(self):
        win = ctk.CTkToplevel(self)
        win.title("Proveedor")
        win.geometry("400x500")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text="Formulario de Proveedor", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(pady=15)

        campos = {}
        for label in ["Nombre Empresa", "NIT/Cédula", "Teléfono", "Dirección", "Correo", "Contacto"]:
            ctk.CTkLabel(win, text=label, font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
            entry = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
            entry.pack(pady=(2, 10), padx=30)
            campos[label] = entry

        if self.editando:
            campos["Nombre Empresa"].insert(0, self.editando.nombre_empresa or "")
            campos["NIT/Cédula"].insert(0, self.editando.nit_cedula or "")
            campos["Teléfono"].insert(0, self.editando.telefono or "")
            campos["Dirección"].insert(0, self.editando.direccion or "")
            campos["Correo"].insert(0, self.editando.correo or "")
            campos["Contacto"].insert(0, self.editando.nombre_contacto or "")

        def guardar():
            try:
                p = self.editando or Proveedor()
                p.nombre_empresa = campos["Nombre Empresa"].get().strip()
                p.nit_cedula = campos["NIT/Cédula"].get().strip()
                p.telefono = campos["Teléfono"].get().strip()
                p.direccion = campos["Dirección"].get().strip()
                p.correo = campos["Correo"].get().strip()
                p.nombre_contacto = campos["Contacto"].get().strip()
                if self.editando:
                    self.dao.actualizar(p)
                else:
                    self.dao.insertar(p)
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))

        ctk.CTkButton(win, text="Guardar", width=200, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), image=icon_save(16, "#ffffff"), compound="left", command=guardar).pack(pady=20)

    def eliminar(self, id):
        if messagebox.askyesno("Confirmar", "¿Eliminar este proveedor?"):
            try:
                self.dao.eliminar(id)
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))
