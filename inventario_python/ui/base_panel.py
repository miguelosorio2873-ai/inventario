import customtkinter as ctk
from config import *


class BasePanel(ctk.CTkFrame):
    def __init__(self, parent, titulo=""):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.parent_dashboard = parent.winfo_toplevel()

        # Header
        header = ctk.CTkFrame(self, fg_color="transparent")
        header.pack(fill="x", padx=25, pady=(25, 15))

        ctk.CTkLabel(header, text=titulo, font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT).pack(side="left")

        # Content area
        self.content = ctk.CTkFrame(self, fg_color="transparent")
        self.content.pack(fill="both", expand=True, padx=25, pady=(0, 25))

    def crear_boton(self, texto, color, comando=None):
        btn = ctk.CTkButton(self, text=texto, fg_color=color, hover_color=self._darken(color),
                            font=("Segoe UI", 13, "bold"), text_color="#ffffff",
                            height=36, corner_radius=6, command=comando)
        return btn

    def _darken(self, hex_color, factor=0.85):
        hex_color = hex_color.lstrip("#")
        r, g, b = int(hex_color[:2], 16), int(hex_color[2:4], 16), int(hex_color[4:6], 16)
        r, g, b = int(r * factor), int(g * factor), int(b * factor)
        return f"#{r:02x}{g:02x}{b:02x}"

    def crear_tabla(self, columnas):
        frame = ctk.CTkFrame(self.content, fg_color=COLOR_CARD, corner_radius=8)
        frame.pack(fill="both", expand=True)

        # Header row
        header_frame = ctk.CTkFrame(frame, fg_color="#3f3f46", height=36, corner_radius=0)
        header_frame.pack(fill="x")
        header_frame.pack_propagate(False)

        for i, col in enumerate(columnas):
            ctk.CTkLabel(header_frame, text=col, font=("Segoe UI", 13, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=8)

        # Scrollable body
        scroll = ctk.CTkScrollableFrame(frame, fg_color=COLOR_CARD, corner_radius=0)
        scroll.pack(fill="both", expand=True)

        return scroll

    def agregar_fila(self, scroll, valores, on_edit=None, on_delete=None):
        row = ctk.CTkFrame(scroll, fg_color="#2d2d2d", corner_radius=4)
        row.pack(fill="x", pady=1)

        for val in valores:
            ctk.CTkLabel(row, text=str(val), font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=10, pady=6)

        if on_edit:
            ctk.CTkButton(row, text="✏️", width=30, fg_color="transparent", hover_color="#3d3d3d", command=on_edit).pack(side="right", padx=2)
        if on_delete:
            ctk.CTkButton(row, text="🗑️", width=30, fg_color="transparent", hover_color="#3d3d3d", command=on_delete).pack(side="right", padx=2)

        return row
