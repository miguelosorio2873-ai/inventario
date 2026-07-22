import customtkinter as ctk
from config import *
from datetime import datetime
from core.sesion_usuario import SesionUsuario
from dao.producto_dao import ProductoDAO
from dao.cliente_dao import ClienteDAO
from dao.factura_dao import FacturaDAO
from dao.inventario_dao import InventarioDAO
import tkinter as tk


class PanelInicio(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)

        ctk.CTkLabel(self, text="Inicio", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=25, pady=(25, 15))

        sesion = SesionUsuario()
        nombre = sesion.nombre_usuario or "Admin"
        rol = sesion.rol or ""
        hora = datetime.now().hour
        saludo = "Buenos días" if hora < 12 else ("Buenas tardes" if hora < 18 else "Buenas noches")

        # Welcome card
        welcome_card = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        welcome_card.pack(fill="x", padx=25, pady=(0, 15))
        ctk.CTkLabel(welcome_card, text=f"{saludo}, {nombre}", font=("Segoe UI", 20, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=20, pady=(15, 5))
        ctk.CTkLabel(welcome_card, text=f"Rol: {rol}  |  Fecha: {datetime.now().strftime('%d/%m/%Y')}", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20, pady=(0, 10))
        ctk.CTkLabel(welcome_card, text="Sistema de Inventario Pro - Gestión integral de productos, inventario y facturación", font=("Segoe UI", 12), text_color=COLOR_TEXT_DIM).pack(anchor="w", padx=20, pady=(0, 15))

        # Stats cards
        stats_frame = ctk.CTkFrame(self, fg_color="transparent")
        stats_frame.pack(fill="x", padx=25, pady=10)

        try:
            pdao = ProductoDAO()
            cdao = ClienteDAO()
            fdao = FacturaDAO()
            idao = InventarioDAO()
            total_prod = pdao.contar_total()
            stock_bajo_count = pdao.contar_stock_bajo()
            total_clientes = cdao.contar_todos()
            facturas = fdao.listar_todas()
            ventas = idao.ventas_del_mes()
        except Exception:
            total_prod = total_clientes = stock_bajo_count = 0
            facturas = []
            ventas = 0

        self._stat_card(stats_frame, "Productos", total_prod, COLOR_GREEN)
        self._stat_card(stats_frame, "Stock Bajo", stock_bajo_count, COLOR_RED)
        self._stat_card(stats_frame, "Clientes", total_clientes, COLOR_BLUE)
        self._stat_card(stats_frame, "Facturas", len(facturas), COLOR_PURPLE)
        self._stat_card(stats_frame, "Ventas del Mes", f"${ventas:.2f}", COLOR_YELLOW)

        # Stock bajo table
        ctk.CTkLabel(self, text="Productos con Stock Bajo", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=25, pady=(25, 10))
        try:
            stock_bajo = pdao.listar_stock_bajo()
        except Exception:
            stock_bajo = []

        stock_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        stock_frame.pack(fill="x", padx=25, pady=(0, 15))
        if stock_bajo:
            header = ctk.CTkFrame(stock_frame, fg_color="#3f3f46", height=32, corner_radius=0)
            header.pack(fill="x")
            header.pack_propagate(False)
            for col in ["Producto", "SKU", "Stock Actual", "Stock Mín"]:
                ctk.CTkLabel(header, text=col, font=("Segoe UI", 12, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=15, pady=6)
            for p in stock_bajo:
                row = ctk.CTkFrame(stock_frame, fg_color="#2d2d2d", corner_radius=4)
                row.pack(fill="x", pady=1)
                ctk.CTkLabel(row, text=p.nombre or "", font=("Segoe UI", 12), text_color=COLOR_TEXT).pack(side="left", padx=15, pady=5)
                ctk.CTkLabel(row, text=p.sku or "", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(side="left", padx=15, pady=5)
                ctk.CTkLabel(row, text=f"{p.stock_actual:.0f}", font=("Segoe UI", 12, "bold"), text_color=COLOR_RED).pack(side="left", padx=15, pady=5)
                ctk.CTkLabel(row, text=f"{p.stock_minimo:.0f}", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(side="left", padx=15, pady=5)
        else:
            ctk.CTkLabel(stock_frame, text="  No hay productos con stock bajo ✓", font=("Segoe UI", 13), text_color=COLOR_GREEN).pack(anchor="w", padx=15, pady=15)

        # Bar chart - Top productos por stock
        ctk.CTkLabel(self, text="Top 10 Productos por Stock", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=25, pady=(15, 10))
        try:
            top_prod = pdao.listar_top_productos_por_stock(10)
        except Exception:
            top_prod = []

        chart_frame = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        chart_frame.pack(fill="x", padx=25, pady=(0, 25))
        if top_prod:
            max_stock = max(p.stock_actual for p in top_prod) or 1
            for p in top_prod:
                bar_row = ctk.CTkFrame(chart_frame, fg_color="transparent")
                bar_row.pack(fill="x", padx=15, pady=3)
                ctk.CTkLabel(bar_row, text=(p.nombre or "")[:20], font=("Segoe UI", 11), text_color=COLOR_TEXT_MUTED, width=120).pack(side="left", padx=(0, 10))
                bar_width = int((p.stock_actual / max_stock) * 300) if max_stock > 0 else 1
                bar = ctk.CTkFrame(bar_row, fg_color=COLOR_GREEN, corner_radius=3, width=max(bar_width, 2))
                bar.pack(side="left", padx=2)
                bar.pack_propagate(False)
                ctk.CTkLabel(bar_row, text=f"{p.stock_actual:.0f}", font=("Segoe UI", 11, "bold"), text_color=COLOR_TEXT).pack(side="left", padx=10)
        else:
            ctk.CTkLabel(chart_frame, text="  No hay datos disponibles", font=("Segoe UI", 13), text_color=COLOR_TEXT_DIM).pack(anchor="w", padx=15, pady=15)

    def _stat_card(self, parent, titulo, valor, color):
        card = ctk.CTkFrame(parent, fg_color=COLOR_CARD, corner_radius=8, border_color=color, border_width=2)
        card.pack(side="left", fill="both", expand=True, padx=5)
        ctk.CTkLabel(card, text=str(titulo), font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(pady=(15, 0))
        ctk.CTkLabel(card, text=str(valor), font=("Segoe UI", 24, "bold"), text_color=color).pack(pady=(5, 15))
