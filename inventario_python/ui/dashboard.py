import customtkinter as ctk
from core.sesion_usuario import SesionUsuario
from config import *
from datetime import datetime
from utils.icon_util import icon_box, icon_users, icon_minus, icon_square, icon_close, icon_home, icon_tags, icon_truck, icon_chart_bar, icon_file_invoice, icon_chart_pie, icon_cog, icon_sign_out
import sys


class Dashboard(ctk.CTk):
    _instancia = None

    def __init__(self):
        super().__init__()
        Dashboard._instancia = self
        self.title("Inventario Pro - Dashboard")
        self.geometry("1280x780")
        self.minsize(1100, 700)
        self.configure(fg_color=COLOR_BG)

        self.btn_activo = None

        # Top bar
        topbar = ctk.CTkFrame(self, fg_color=COLOR_SIDEBAR, height=50, corner_radius=0)
        topbar.pack(side="top", fill="x")
        topbar.pack_propagate(False)

        logo_icon = icon_box(20, COLOR_GREEN)
        logo_label = ctk.CTkLabel(topbar, text="Inventario Pro", font=("Segoe UI", 18, "bold"), text_color=COLOR_GREEN, image=logo_icon, compound="left")
        logo_label.pack(side="left", padx=20)

        right = ctk.CTkFrame(topbar, fg_color="transparent")
        right.pack(side="right", padx=15)

        self.clock_label = ctk.CTkLabel(right, text="", font=("Segoe UI", 13, "bold"), text_color=COLOR_GREEN)
        self.clock_label.pack(side="left", padx=(0, 15))
        self._update_clock()

        sesion = SesionUsuario()
        nombre = sesion.nombre_usuario or "Admin"
        user_icon = icon_users(16, COLOR_TEXT_MUTED)
        ctk.CTkLabel(right, text=f" {nombre}", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED, image=user_icon, compound="left").pack(side="left", padx=(0, 15))

        ctk.CTkButton(right, text="", width=30, fg_color="transparent", hover_color=COLOR_SIDEBAR_HOVER, image=icon_minus(14, COLOR_YELLOW), command=lambda: self.iconify()).pack(side="left", padx=2)
        ctk.CTkButton(right, text="", width=30, fg_color="transparent", hover_color=COLOR_SIDEBAR_HOVER, image=icon_square(14, "#34d399"), command=self._toggle_max).pack(side="left", padx=2)
        ctk.CTkButton(right, text="", width=30, fg_color="transparent", hover_color=COLOR_SIDEBAR_HOVER, image=icon_close(14, COLOR_RED), command=self._cerrar).pack(side="left", padx=2)

        # Body
        body = ctk.CTkFrame(self, fg_color=COLOR_BG)
        body.pack(side="top", fill="both", expand=True)

        # Sidebar
        self.sidebar = ctk.CTkFrame(body, fg_color=COLOR_SIDEBAR, width=220, corner_radius=0)
        self.sidebar.pack(side="left", fill="y")
        self.sidebar.pack_propagate(False)

        # Content
        self.content = ctk.CTkFrame(body, fg_color=COLOR_BG, corner_radius=0)
        self.content.pack(side="left", fill="both", expand=True)

        self._crear_sidebar()
        self._mostrar_panel("Inicio")

    def _update_clock(self):
        self.clock_label.configure(text=datetime.now().strftime("%d/%m/%Y %H:%M:%S"))
        self.after(1000, self._update_clock)

    def _toggle_max(self):
        if self.state() == "zoomed":
            self.state("normal")
        else:
            self.state("zoomed")

    def _cerrar(self):
        from tkinter import messagebox
        if messagebox.askyesno("Confirmar", "¿Desea salir del sistema?"):
            sys.exit(0)

    def _crear_sidebar(self):
        for w in self.sidebar.winfo_children():
            w.destroy()

        ctk.CTkLabel(self.sidebar, text="  MENÚ PRINCIPAL", font=("Segoe UI", 11, "bold"), text_color="#646464").pack(anchor="w", padx=15, pady=(15, 10))

        items = [
            ("Inicio", icon_home(20, COLOR_TEXT_MUTED)),
            ("Productos", icon_box(20, COLOR_TEXT_MUTED)),
            ("Categorias", icon_tags(20, COLOR_TEXT_MUTED)),
            ("Clientes", icon_users(20, COLOR_TEXT_MUTED)),
            ("Proveedores", icon_truck(20, COLOR_TEXT_MUTED)),
            ("Inventario", icon_chart_bar(20, COLOR_TEXT_MUTED)),
            ("Facturas", icon_file_invoice(20, COLOR_TEXT_MUTED)),
            ("Usuarios", icon_users(20, COLOR_TEXT_MUTED)),
            ("Reportes", icon_chart_pie(20, COLOR_TEXT_MUTED)),
            ("Configuracion", icon_cog(20, COLOR_TEXT_MUTED)),
        ]

        sesion = SesionUsuario()
        for label, icon in items:
            tiene = sesion.tiene_permiso(label)
            btn = ctk.CTkButton(
                self.sidebar, text=f"  {label}", anchor="w",
                font=("Segoe UI", 14), text_color=COLOR_TEXT_MUTED,
                fg_color="transparent", hover_color=COLOR_SIDEBAR_HOVER,
                height=42, corner_radius=0,
                image=icon, compound="left",
                command=lambda l=label: self._mostrar_panel(l),
            )
            if label != "Inicio" and not tiene:
                btn.configure(state="disabled", text_color="#444444")
            btn.pack(fill="x")

        # Separador
        ctk.CTkFrame(self.sidebar, fg_color="#374151", height=1).pack(fill="x", padx=15, pady=15)

        btn_logout = ctk.CTkButton(
            self.sidebar, text="  Cerrar Sesión", anchor="w",
            font=("Segoe UI", 14), text_color=COLOR_RED,
            fg_color="transparent", hover_color=COLOR_SIDEBAR_HOVER,
            height=42, corner_radius=0,
            image=icon_sign_out(20, COLOR_RED), compound="left",
            command=self._logout,
        )
        btn_logout.pack(fill="x")

    def _set_btn_activo(self, btn):
        if self.btn_activo:
            self.btn_activo.configure(fg_color="transparent", text_color=COLOR_TEXT_MUTED)
        btn.configure(fg_color=COLOR_ACTIVE, text_color=COLOR_TEXT)
        self.btn_activo = btn

    def _mostrar_panel(self, nombre):
        for w in self.content.winfo_children():
            w.destroy()

        # Find the button and set active
        for w in self.sidebar.winfo_children():
            if hasattr(w, "cget") and w.cget("text") and nombre in w.cget("text"):
                self._set_btn_activo(w)
                break

        panel = None
        if nombre == "Inicio":
            from ui.panel_inicio import PanelInicio
            panel = PanelInicio(self.content)
        elif nombre == "Productos":
            from ui.panel_productos import PanelProductos
            panel = PanelProductos(self.content)
        elif nombre == "Categorias":
            from ui.panel_categorias import PanelCategorias
            panel = PanelCategorias(self.content)
        elif nombre == "Clientes":
            from ui.panel_clientes import PanelClientes
            panel = PanelClientes(self.content)
        elif nombre == "Proveedores":
            from ui.panel_proveedores import PanelProveedores
            panel = PanelProveedores(self.content)
        elif nombre == "Inventario":
            from ui.panel_inventario import PanelInventario
            panel = PanelInventario(self.content)
        elif nombre == "Facturas":
            from ui.panel_facturas import PanelFacturas
            panel = PanelFacturas(self.content)
        elif nombre == "Usuarios":
            from ui.panel_usuarios import PanelUsuarios
            panel = PanelUsuarios(self.content)
        elif nombre == "Reportes":
            from ui.panel_reportes import PanelReportes
            panel = PanelReportes(self.content)
        elif nombre == "Configuracion":
            from ui.panel_configuracion import PanelConfiguracion
            panel = PanelConfiguracion(self.content)

        if panel:
            panel.pack(fill="both", expand=True)

    def refrescar_menu(self):
        self._crear_sidebar()

    def _logout(self):
        from tkinter import messagebox
        if messagebox.askyesno("Confirmar", "¿Cerrar sesión?"):
            SesionUsuario().cerrar_sesion()
            self.destroy()
            from ui.login import Login
            Login().mainloop()
