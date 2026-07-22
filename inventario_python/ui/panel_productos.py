import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from dao.producto_dao import ProductoDAO
from dao.categoria_dao import CategoriaDAO
from models.producto import Producto
from db.conexion import error_manager
from tkinter import messagebox
from utils.icon_util import icon_box, icon_plus, icon_edit, icon_trash, icon_search, icon_clear


class PanelProductos(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.dao = ProductoDAO()
        self.cat_dao = CategoriaDAO()
        self.editando = None

        ctk.CTkLabel(self, text="Productos", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_box(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        # Toolbar
        toolbar = ctk.CTkFrame(self, fg_color="transparent")
        toolbar.pack(fill="x", padx=25, pady=(0, 10))

        sesion = SesionUsuario()
        puede_crear = sesion.tiene_permiso_accion("Productos", "CREAR")
        puede_editar = sesion.tiene_permiso_accion("Productos", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Productos", "ELIMINAR")

        self.search_entry = ctk.CTkEntry(toolbar, width=250, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT, placeholder_text="Buscar...")
        self.search_entry.pack(side="left", padx=(0, 10))
        self.search_entry.bind("<Return>", lambda e: self.buscar())
        ctk.CTkButton(toolbar, text="", width=40, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_search(16, COLOR_TEXT_MUTED), command=self.buscar).pack(side="left", padx=2)
        ctk.CTkButton(toolbar, text="", width=30, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_clear(16, COLOR_TEXT_MUTED), command=self.limpiar_busqueda).pack(side="left", padx=2)

        if puede_crear:
            ctk.CTkButton(toolbar, text="Nuevo", width=100, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_plus(16, "#ffffff"), compound="left", command=self.nuevo).pack(side="right", padx=2)

        # Tabla
        self.tabla_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        self.tabla_frame.pack(fill="both", expand=True, padx=25, pady=(0, 25))
        self._crear_tabla()
        self.cargar_datos()

    def _crear_tabla(self):
        cols = ["ID", "SKU", "Nombre", "Categoría", "Precio Venta", "Precio VES", "Stock Actual", "Stock Mín", "Estado"]
        header = ctk.CTkFrame(self.tabla_frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header.pack(fill="x")
        header.pack_propagate(False)
        for col in cols:
            ctk.CTkLabel(header, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)

        self.scroll = ctk.CTkScrollableFrame(self.tabla_frame, fg_color=COLOR_CARD, corner_radius=0)
        self.scroll.pack(fill="both", expand=True)

    def cargar_datos(self):
        for w in self.scroll.winfo_children():
            w.destroy()
        try:
            lista = self.dao.listar_todos_con_categoria()
        except Exception:
            lista = self.dao.listar_todos()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Productos", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Productos", "ELIMINAR")
        tasa = get_tasa_ves()
        for p in lista:
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            precio_ves = p.precio_venta * tasa
            vals = [p.id, p.sku, p.nombre, getattr(p, 'categoria_nombre', ''), f"${p.precio_venta:.2f}", f"Bs {precio_ves:.2f}", f"{p.stock_actual:.0f}", f"{p.stock_minimo:.0f}", "Activo" if p.state else "Inactivo"]
            for v in vals:
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
        lista = self.dao.buscar(texto)
        tasa = get_tasa_ves()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Productos", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Productos", "ELIMINAR")
        for p in lista:
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            precio_ves = p.precio_venta * tasa
            vals = [p.id, p.sku, p.nombre, getattr(p, 'categoria_nombre', ''), f"${p.precio_venta:.2f}", f"Bs {precio_ves:.2f}", f"{p.stock_actual:.0f}", f"{p.stock_minimo:.0f}", "Activo" if p.state else "Inactivo"]
            for v in vals:
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)
            if puede_editar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_edit(16, COLOR_TEXT_MUTED), command=lambda pid=p.id: self.editar(pid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda pid=p.id: self.eliminar(pid)).pack(side="right", padx=2)

    def nuevo(self):
        self.editando = None
        self._mostrar_formulario()

    def editar(self, id):
        p = self.dao.obtener_por_id(id)
        if not p:
            return
        self.editando = p
        self._mostrar_formulario()

    def _mostrar_formulario(self):
        win = ctk.CTkToplevel(self)
        win.title("Producto")
        win.geometry("400x550")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text="Formulario de Producto", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(pady=15)

        campos = {}
        for label in ["SKU", "Nombre", "Descripción", "Precio Venta", "Costo Promedio", "Stock Mínimo"]:
            ctk.CTkLabel(win, text=label, font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
            entry = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
            entry.pack(pady=(2, 10), padx=30)
            campos[label] = entry

        # Categoría dropdown
        ctk.CTkLabel(win, text="Categoría", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        cats = self.cat_dao.listar_todas()
        cat_names = ["(Sin categoría)"] + [c.nombre for c in cats]
        cat_combo = ctk.CTkComboBox(win, values=cat_names, width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        cat_combo.pack(pady=(2, 10), padx=30)
        campos["Categoria"] = cat_combo

        # Estado
        ctk.CTkLabel(win, text="Estado", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        estado_combo = ctk.CTkComboBox(win, values=["Activo", "Inactivo"], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        estado_combo.pack(pady=(2, 10), padx=30)
        campos["Estado"] = estado_combo

        if self.editando:
            p = self.editando
            campos["SKU"].insert(0, p.sku or "")
            campos["Nombre"].insert(0, p.nombre or "")
            campos["Descripción"].insert(0, p.descripcion or "")
            campos["Precio Venta"].insert(0, str(p.precio_venta))
            campos["Costo Promedio"].insert(0, str(p.costo_promedio))
            campos["Stock Mínimo"].insert(0, str(p.stock_minimo))
            if hasattr(p, 'categoria_nombre') and p.categoria_nombre:
                cat_combo.set(p.categoria_nombre)
            else:
                cat_combo.set("(Sin categoría)")
            estado_combo.set("Activo" if p.state else "Inactivo")

        def guardar():
            try:
                p = self.editando or Producto()
                p.sku = campos["SKU"].get().strip()
                p.nombre = campos["Nombre"].get().strip()
                p.descripcion = campos["Descripción"].get().strip()
                p.precio_venta = float(campos["Precio Venta"].get() or 0)
                p.costo_promedio = float(campos["Costo Promedio"].get() or 0)
                p.stock_minimo = float(campos["Stock Mínimo"].get() or 0)
                cat_sel = campos["Categoria"].get()
                if cat_sel and cat_sel != "(Sin categoría)":
                    for c in cats:
                        if c.nombre == cat_sel:
                            p.categoria_id = c.id
                            break
                else:
                    p.categoria_id = None
                p.state = campos["Estado"].get() == "Activo"
                if self.editando:
                    self.dao.actualizar(p)
                else:
                    self.dao.insertar(p)
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))

        ctk.CTkButton(win, text="Guardar", width=200, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), command=guardar).pack(pady=20)

    def eliminar(self, id):
        if messagebox.askyesno("Confirmar", "¿Eliminar este producto?"):
            try:
                self.dao.eliminar(id)
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))
