import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from dao.factura_dao import FacturaDAO
from dao.cliente_dao import ClienteDAO
from models.factura import Factura
from db.conexion import error_manager
from tkinter import messagebox
from datetime import datetime
from utils.icon_util import icon_file_invoice, icon_plus, icon_check_circle, icon_x, icon_trash, icon_search, icon_clear, icon_save


class PanelFacturas(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.dao = FacturaDAO()
        self.cli_dao = ClienteDAO()

        ctk.CTkLabel(self, text="Facturas", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_file_invoice(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        toolbar = ctk.CTkFrame(self, fg_color="transparent")
        toolbar.pack(fill="x", padx=25, pady=(0, 10))

        self.search_entry = ctk.CTkEntry(toolbar, width=250, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT, placeholder_text="Buscar por N° factura...")
        self.search_entry.pack(side="left", padx=(0, 10))
        ctk.CTkButton(toolbar, text="", width=40, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_search(16, COLOR_TEXT_MUTED), command=self.buscar).pack(side="left", padx=2)

        sesion = SesionUsuario()
        if sesion.tiene_permiso_accion("Facturas", "CREAR"):
            ctk.CTkButton(toolbar, text="Nueva", width=100, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_plus(16, "#ffffff"), compound="left", command=self.nuevo).pack(side="right", padx=2)

        self.search_entry.bind("<Return>", lambda e: self.buscar())
        ctk.CTkButton(toolbar, text="", width=30, fg_color=COLOR_CARD, hover_color=COLOR_BORDER, image=icon_clear(16, COLOR_TEXT_MUTED), command=self.limpiar_busqueda).pack(side="left", padx=2)

        self.tabla_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        self.tabla_frame.pack(fill="both", expand=True, padx=25, pady=(0, 25))
        self._crear_tabla()
        self.cargar_datos()

    def _crear_tabla(self):
        header = ctk.CTkFrame(self.tabla_frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header.pack(fill="x")
        header.pack_propagate(False)
        for col in ["ID", "N. Factura", "Cliente", "Fecha", "Método Pago", "Subtotal", "Impuestos", "Total", "Estado"]:
            ctk.CTkLabel(header, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)
        self.scroll = ctk.CTkScrollableFrame(self.tabla_frame, fg_color=COLOR_CARD, corner_radius=0)
        self.scroll.pack(fill="both", expand=True)

    def cargar_datos(self):
        for w in self.scroll.winfo_children():
            w.destroy()
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Facturas", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Facturas", "ELIMINAR")
        for f in self.dao.listar_todas_con_cliente():
            estado = f.estado or ""
            estado_color = COLOR_GREEN if estado == "Pagada" else (COLOR_RED if estado == "Anulada" else COLOR_YELLOW)
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            fecha_str = f.fecha_emision.strftime("%Y-%m-%d") if f.fecha_emision else ""
            vals = [f.id, f.numero_factura, f.cliente_nombre, fecha_str, f.metodo_pago, f"${f.subtotal:.2f}", f"${f.impuestos:.2f}", f"${f.total:.2f}", estado]
            for i, v in enumerate(vals):
                fg = estado_color if i == 8 else COLOR_TEXT
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12, "bold") if i == 8 else ("Segoe UI", 12), text_color=fg).pack(side="left", padx=10, pady=6)
            if puede_editar and estado != "Anulada":
                ctk.CTkButton(row, text="Pagada", width=60, fg_color="transparent", hover_color="#3d3d3d", text_color=COLOR_GREEN, font=("Segoe UI", 10), image=icon_check_circle(14, COLOR_GREEN), compound="left", command=lambda fid=f.id: self.marcar_pagada(fid)).pack(side="right", padx=2)
                ctk.CTkButton(row, text="Anular", width=60, fg_color="transparent", hover_color="#3d3d3d", text_color=COLOR_RED, font=("Segoe UI", 10), image=icon_x(14, COLOR_RED), compound="left", command=lambda fid=f.id: self.anular(fid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda fid=f.id: self.eliminar(fid)).pack(side="right", padx=2)

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
        puede_editar = sesion.tiene_permiso_accion("Facturas", "EDITAR")
        puede_eliminar = sesion.tiene_permiso_accion("Facturas", "ELIMINAR")
        for f in self.dao.buscar(texto):
            estado = f.estado or ""
            estado_color = COLOR_GREEN if estado == "Pagada" else (COLOR_RED if estado == "Anulada" else COLOR_YELLOW)
            row = ctk.CTkFrame(self.scroll, fg_color="#2d2d2d", corner_radius=4)
            row.pack(fill="x", pady=1)
            fecha_str = f.fecha_emision.strftime("%Y-%m-%d") if f.fecha_emision else ""
            vals = [f.id, f.numero_factura, f.cliente_nombre or "", fecha_str, f.metodo_pago, f"${f.subtotal:.2f}", f"${f.impuestos:.2f}", f"${f.total:.2f}", estado]
            for i, v in enumerate(vals):
                fg = estado_color if i == 8 else COLOR_TEXT
                ctk.CTkLabel(row, text=str(v), font=("Segoe UI", 12, "bold") if i == 8 else ("Segoe UI", 12), text_color=fg).pack(side="left", padx=10, pady=6)
            if puede_editar and estado != "Anulada":
                ctk.CTkButton(row, text="Pagada", width=60, fg_color="transparent", hover_color="#3d3d3d", text_color=COLOR_GREEN, font=("Segoe UI", 10), image=icon_check_circle(14, COLOR_GREEN), compound="left", command=lambda fid=f.id: self.marcar_pagada(fid)).pack(side="right", padx=2)
                ctk.CTkButton(row, text="Anular", width=60, fg_color="transparent", hover_color="#3d3d3d", text_color=COLOR_RED, font=("Segoe UI", 10), image=icon_x(14, COLOR_RED), compound="left", command=lambda fid=f.id: self.anular(fid)).pack(side="right", padx=2)
            if puede_eliminar:
                ctk.CTkButton(row, text="", width=30, fg_color="transparent", hover_color="#3d3d3d", image=icon_trash(16, COLOR_RED), command=lambda fid=f.id: self.eliminar(fid)).pack(side="right", padx=2)

    def nuevo(self):
        win = ctk.CTkToplevel(self)
        win.title("Nueva Factura")
        win.geometry("400x500")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text="Nueva Factura", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(pady=15)

        clientes = self.cli_dao.listar_todos()
        num_fact = self.dao.generar_numero()

        ctk.CTkLabel(win, text="N. Factura", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_num = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_num.pack(pady=(2, 10), padx=30)
        entry_num.insert(0, num_fact)

        ctk.CTkLabel(win, text="Cliente", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        cli_combo = ctk.CTkComboBox(win, values=["(Sin cliente)"] + [f"{c.id} - {c.nombre}" for c in clientes], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        cli_combo.pack(pady=(2, 10), padx=30)
        cli_combo.set("(Sin cliente)")

        ctk.CTkLabel(win, text="Método de Pago", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        pago_combo = ctk.CTkComboBox(win, values=["Efectivo", "Tarjeta", "Transferencia"], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        pago_combo.pack(pady=(2, 10), padx=30)
        pago_combo.set("Efectivo")

        ctk.CTkLabel(win, text="Subtotal", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_sub = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_sub.pack(pady=(2, 10), padx=30)
        entry_sub.insert(0, "0.00")

        ctk.CTkLabel(win, text="Impuestos", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        entry_imp = ctk.CTkEntry(win, width=340, fg_color=COLOR_CARD, border_color=COLOR_BORDER, text_color=COLOR_TEXT)
        entry_imp.pack(pady=(2, 10), padx=30)
        entry_imp.insert(0, "0.00")

        ctk.CTkLabel(win, text="Total (auto)", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        lbl_total = ctk.CTkLabel(win, text="$0.00", font=("Segoe UI", 16, "bold"), text_color=COLOR_GREEN)
        lbl_total.pack(anchor="w", padx=30, pady=(2, 10))

        def calc_total(*args):
            try:
                sub = float(entry_sub.get() or 0)
                imp = float(entry_imp.get() or 0)
                lbl_total.configure(text=f"${sub + imp:.2f}")
            except Exception:
                lbl_total.configure(text="$0.00")

        entry_sub.bind("<KeyRelease>", calc_total)
        entry_imp.bind("<KeyRelease>", calc_total)

        ctk.CTkLabel(win, text="Estado", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=30)
        estado_combo = ctk.CTkComboBox(win, values=["Pendiente", "Pagada", "Anulada"], width=340, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        estado_combo.pack(pady=(2, 10), padx=30)
        estado_combo.set("Pendiente")

        def guardar():
            try:
                f = Factura()
                f.numero_factura = entry_num.get().strip()
                cli_sel = cli_combo.get()
                if cli_sel and cli_sel != "(Sin cliente)":
                    f.cliente_id = int(cli_sel.split(" - ")[0])
                f.metodo_pago = pago_combo.get()
                f.subtotal = float(entry_sub.get() or 0)
                f.impuestos = float(entry_imp.get() or 0)
                f.total = f.subtotal + f.impuestos
                f.estado = estado_combo.get()
                f.fecha_emision = datetime.now()
                if not f.numero_factura:
                    messagebox.showwarning("Error", "El número de factura es obligatorio")
                    return
                self.dao.insertar(f)
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))

        ctk.CTkButton(win, text="Guardar", width=200, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 14, "bold"), image=icon_save(16, "#ffffff"), compound="left", command=guardar).pack(pady=20)

    def marcar_pagada(self, id):
        if messagebox.askyesno("Confirmar", "¿Marcar esta factura como PAGADA?"):
            try:
                self.dao.actualizar_estado(id, "Pagada")
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", str(ex))

    def anular(self, id):
        if messagebox.askyesno("Confirmar", "¿Anular esta factura? Esta acción no se puede deshacer."):
            try:
                self.dao.actualizar_estado(id, "Anulada")
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", str(ex))

    def cambiar_estado(self, id):
        win = ctk.CTkToplevel(self)
        win.title("Cambiar Estado")
        win.geometry("300x200")
        win.configure(fg_color=COLOR_BG)
        win.transient(self)
        win.grab_set()

        ctk.CTkLabel(win, text="Nuevo Estado", font=("Segoe UI", 16, "bold"), text_color=COLOR_TEXT).pack(pady=20)
        combo = ctk.CTkComboBox(win, values=["Pendiente", "Pagada", "Anulada"], width=200, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT)
        combo.pack(pady=10)
        combo.set("Pendiente")

        def aplicar():
            try:
                self.dao.actualizar_estado(id, combo.get())
                win.destroy()
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", str(ex))

        ctk.CTkButton(win, text="Aplicar", width=150, fg_color=COLOR_GREEN, hover_color="#059669", command=aplicar).pack(pady=20)

    def eliminar(self, id):
        if messagebox.askyesno("Confirmar", "¿Eliminar esta factura?"):
            try:
                self.dao.eliminar(id)
                self.cargar_datos()
            except Exception as ex:
                messagebox.showerror("Error", error_manager(ex) if hasattr(ex, 'errno') else str(ex))
