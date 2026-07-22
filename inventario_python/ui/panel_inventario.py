import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from dao.inventario_dao import InventarioDAO
from dao.producto_dao import ProductoDAO
from dao.proveedor_dao import ProveedorDAO
from models.movimiento_inventario import MovimientoInventario
from db.conexion import error_manager
from tkinter import messagebox
from utils.icon_util import icon_chart_bar, icon_download, icon_upload, icon_wrench, icon_search, icon_clear, icon_save


class PanelInventario(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.dao = InventarioDAO()
        self.prod_dao = ProductoDAO()
        self.prov_dao = ProveedorDAO()

        ctk.CTkLabel(self, text="Inventario - Movimientos", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_chart_bar(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        toolbar = ctk.CTkFrame(self, fg_color="transparent")
        toolbar.pack(fill="x", padx=25, pady=(0, 10))

        self.search_entry = ctk.CTkEntry(toolbar, width=250, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT, placeholder_text="Buscar por producto...")
        self.search_entry.pack(side="left", padx=(0, 10))
        self.search_entry.bind("<Return>", lambda e: self.buscar())
        ctk.CTkButton(toolbar, text="", width=40, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_search(16, COLOR_TEXT_MUTED), command=self.buscar).pack(side="left", padx=2)
        ctk.CTkButton(toolbar, text="", width=30, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_clear(16, COLOR_TEXT_MUTED), command=self.limpiar_busqueda).pack(side="left", padx=2)

        sesion = SesionUsuario()
        puede_crear = sesion.tiene_permiso_accion("Inventario", "CREAR")
        if puede_crear:
            ctk.CTkButton(toolbar, text="Entrada", width=100, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_download(16, "#ffffff"), compound="left", command=lambda: self.nuevo("Entrada")).pack(side="right", padx=2)
            ctk.CTkButton(toolbar, text="Salida", width=100, fg_color=COLOR_RED, hover_color="#dc2626", font=("Segoe UI", 13, "bold"), image=icon_upload(16, "#ffffff"), compound="left", command=lambda: self.nuevo("Salida")).pack(side="right", padx=2)
            ctk.CTkButton(toolbar, text="Ajuste", width=100, fg_color=COLOR_YELLOW, hover_color="#eab308", font=("Segoe UI", 13, "bold"), text_color="#000", image=icon_wrench(16, "#000000"), compound="left", command=lambda: self.nuevo("Ajuste")).pack(side="right", padx=2)

        self.tabla_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        self.tabla_frame.pack(fill="both", expand=True, padx=25, pady=(0, 25))
        self._crear_tabla()
        self.cargar_datos()

    def _crear_tabla(self):
        header = ctk.CTkFrame(self.tabla_frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header.pack(fill="x")
        header.pack_propagate(False)
        for col in ["ID", "Producto", "Proveedor", "Precio", "Cantidad", "Tipo", "Fecha", "Motivo"]:
            ctk.CTkLabel(header, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)
        self.scroll = ctk.CTkScrollableFrame(self.tabla_frame, fg_color=COLOR_CARD, corner_radius=0)
        self.scroll.pack(fill="both", expand=True)

    def cargar_datos(self):
        for w in self.scroll.winfo_children():
            w.destroy()
        for m in self.dao.listar_movimientos_con_nombres():
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            fecha_str = m.fecha_movimiento.strftime("%Y-%m-%d %H:%M") if m.fecha_movimiento else ""
            tipo = m.tipo_movimiento or ""
            tipo_color = COLOR_GREEN if tipo == "Entrada" else (COLOR_RED if tipo == "Salida" else COLOR_YELLOW)
            vals = [m.id, m.producto_nombre, m.proveedor_nombre or "", f"${m.precio:.2f}", f"{m.cantidad:.0f}", tipo, fecha_str, m.motivo or ""]
            for i, v in enumerate(vals):
                fg = tipo_color if i == 5 else COLOR_TEXT
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12, "bold") if i == 5 else ("Segoe UI", 12), text_color=fg).pack(side="left", padx=10, pady=6)

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
        for m in self.dao.buscar_por_producto(texto):
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            fecha_str = m.fecha_movimiento.strftime("%Y-%m-%d %H:%M") if m.fecha_movimiento else ""
            tipo = m.tipo_movimiento or ""
            tipo_color = COLOR_GREEN if tipo == "Entrada" else (COLOR_RED if tipo == "Salida" else COLOR_YELLOW)
            vals = [m.id, m.producto_nombre, "", f"${m.precio:.2f}", f"{m.cantidad:.0f}", tipo, fecha_str, m.motivo or ""]
            for i, v in enumerate(vals):
                fg = tipo_color if i == 5 else COLOR_TEXT
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12, "bold") if i == 5 else ("Segoe UI", 12), text_color=fg).pack(side="left", padx=10, pady=6)

    def nuevo(self, tipo_prefijado=None):
        win = ctk.CTkToplevel(self)
        win.title(f"Movimiento de {tipo_prefijado}" if tipo_prefijado else "Nuevo Movimiento")
        win.geometry("400x520")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text=f"Registrar {tipo_prefijado or 'Movimiento'}", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(pady=15)

        productos = self.prod_dao.listar_todos()
        proveedores = self.prov_dao.listar_todos()

        ctk.CTkLabel(win, text="Producto", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        prod_combo = ctk.CTkComboBox(win, values=[f"{p.id} - {p.nombre} (Stock: {p.stock_actual:.0f})" for p in productos], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        prod_combo.pack(pady=(2, 10), padx=30)
        if productos:
            prod_combo.set(f"{productos[0].id} - {productos[0].nombre} (Stock: {productos[0].stock_actual:.0f})")

        ctk.CTkLabel(win, text="Proveedor (opcional)", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        prov_combo = ctk.CTkComboBox(win, values=["(Sin proveedor)"] + [f"{p.id} - {p.nombre_empresa}" for p in proveedores], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        prov_combo.pack(pady=(2, 10), padx=30)
        prov_combo.set("(Sin proveedor)")

        ctk.CTkLabel(win, text="Tipo de Movimiento", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        tipo_combo = ctk.CTkComboBox(win, values=["Entrada", "Salida", "Ajuste"], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        tipo_combo.pack(pady=(2, 10), padx=30)
        tipo_combo.set(tipo_prefijado or "Entrada")
        if tipo_prefijado:
            tipo_combo.configure(state="disabled")

        ctk.CTkLabel(win, text="Precio (unitario)", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_precio = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_precio.pack(pady=(2, 10), padx=30)
        entry_precio.insert(0, "0.00")

        ctk.CTkLabel(win, text="Cantidad", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_cant = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_cant.pack(pady=(2, 10), padx=30)

        ctk.CTkLabel(win, text="Motivo", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_motivo = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_motivo.pack(pady=(2, 10), padx=30)

        def guardar():
            try:
                m = MovimientoInventario()
                prod_sel = prod_combo.get()
                m.producto_id = int(prod_sel.split(" - ")[0])
                prov_sel = prov_combo.get()
                if prov_sel and prov_sel != "(Sin proveedor)":
                    m.proveedor_id = int(prov_sel.split(" - ")[0])
                m.tipo_movimiento = tipo_combo.get()
                m.cantidad = float(entry_cant.get() or 0)
                m.motivo = entry_motivo.get().strip()
                m.precio = float(entry_precio.get() or 0)
                m.precio_balance = m.precio * m.cantidad
                if m.tipo_movimiento == "Salida":
                    stock = self.dao.stock_actual(m.producto_id)
                    if m.cantidad > stock:
                        messagebox.showerror("Error", f"Stock insuficiente. Disponible: {stock:.0f}, Solicitado: {m.cantidad:.0f}")
                        return
                if m.cantidad <= 0:
                    messagebox.showerror("Error", "La cantidad debe ser mayor a 0.")
                    return
                self.dao.registrar_movimiento(m)
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))

        ctk.CTkButton(win, text="Guardar", width=200, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), image=icon_save(16, "#ffffff"), compound="left", command=guardar).pack(pady=20)
