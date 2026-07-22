import customtkinter as ctk
from config import *
from core.sesion_usuario import SesionUsuario
from tkinter import messagebox
from utils.icon_util import icon_cog, icon_save, icon_moon, icon_sun, icon_dollar


class PanelConfiguracion(ctk.CTkFrame):
    def __init__(self, parent):
        super().__init__(parent, fg_color=COLOR_BG, corner_radius=0)
        self.parent_window = parent

        ctk.CTkLabel(self, text="Configuración", font=("Segoe UI", 24, "bold"), text_color=COLOR_TEXT, image=icon_cog(24, COLOR_TEXT), compound="left").pack(anchor="w", padx=25, pady=(25, 15))

        # --- Sistema info ---
        card = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        card.pack(fill="x", padx=25, pady=10)

        ctk.CTkLabel(card, text="Información del Sistema", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=20, pady=(20, 10))
        ctk.CTkLabel(card, text="Inventario Pro v1.0.0 (Python)", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20, pady=5)
        ctk.CTkLabel(card, text=f"Base de datos: {DB_NAME} @ {DB_HOST}:{DB_PORT}", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20, pady=5)
        ctk.CTkLabel(card, text="Encriptación: AES-128 (ECB) + Argon2id", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20, pady=5)
        ctk.CTkLabel(card, text="UI: CustomTkinter", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20, pady=(5, 20))

        # --- Apariencia ---
        sesion = SesionUsuario()
        puede_editar = sesion.tiene_permiso_accion("Configuracion", "EDITAR")

        card2 = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        card2.pack(fill="x", padx=25, pady=10)

        ctk.CTkLabel(card2, text="Apariencia", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=20, pady=(20, 10))

        tema_frame = ctk.CTkFrame(card2, fg_color="transparent")
        tema_frame.pack(fill="x", padx=20, pady=(0, 20))
        ctk.CTkLabel(tema_frame, text="Tema:", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(side="left", padx=(0, 10))
        tema_actual = get_tema()
        self.tema_combo = ctk.CTkComboBox(tema_frame, values=["dark", "light"], width=120, fg_color=COLOR_CARD, button_color=COLOR_BORDER, text_color=COLOR_TEXT, state="normal" if puede_editar else "disabled")
        self.tema_combo.pack(side="left", padx=5)
        self.tema_combo.set(tema_actual)
        if puede_editar:
            ctk.CTkButton(tema_frame, text="Aplicar", width=80, fg_color=COLOR_GREEN, hover_color="#059669", command=self.aplicar_tema).pack(side="left", padx=10)

        # --- Datos de Empresa ---
        card3 = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        card3.pack(fill="x", padx=25, pady=10)

        ctk.CTkLabel(card3, text="Datos de Empresa", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT).pack(anchor="w", padx=20, pady=(20, 10))

        emp = get_empresa_data()
        self.emp_campos = {}
        for label in ["Nombre", "NIT", "Teléfono", "Dirección"]:
            ctk.CTkLabel(card3, text=label, font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20)
            entry = ctk.CTkEntry(card3, width=400, fg_color=COLOR_BG, border_color=COLOR_BORDER, text_color=COLOR_TEXT, state="normal" if puede_editar else "disabled")
            entry.pack(pady=(2, 8), padx=20)
            entry.insert(0, emp.get(label.lower(), ""))
            self.emp_campos[label] = entry

        if puede_editar:
            ctk.CTkButton(card3, text="Guardar Empresa", width=150, fg_color=COLOR_GREEN, hover_color="#059669", font=("Segoe UI", 13, "bold"), image=icon_save(16, "#ffffff"), compound="left", command=self.guardar_empresa).pack(pady=(5, 20), padx=20)

        # --- Tasa VES ---
        card4 = ctk.CTkFrame(self, fg_color=COLOR_CARD, corner_radius=8)
        card4.pack(fill="x", padx=25, pady=10)

        ctk.CTkLabel(card4, text="Tasa de Cambio USD → VES", font=("Segoe UI", 18, "bold"), text_color=COLOR_TEXT, image=icon_dollar(20, COLOR_TEXT), compound="left").pack(anchor="w", padx=20, pady=(20, 10))
        ctk.CTkLabel(card4, text="Tasa actual del dólar a Bolívares. Usada para mostrar precios en VES.", font=("Segoe UI", 12), text_color=COLOR_TEXT_MUTED).pack(anchor="w", padx=20, pady=(0, 10))

        tasa_frame = ctk.CTkFrame(card4, fg_color="transparent")
        tasa_frame.pack(fill="x", padx=20, pady=(0, 20))
        ctk.CTkLabel(tasa_frame, text="1 USD =", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(side="left", padx=(0, 5))
        self.entry_tasa = ctk.CTkEntry(tasa_frame, width=100, fg_color=COLOR_BG, border_color=COLOR_BORDER, text_color=COLOR_TEXT, state="normal" if puede_editar else "disabled")
        self.entry_tasa.pack(side="left", padx=5)
        self.entry_tasa.insert(0, str(get_tasa_ves()))
        ctk.CTkLabel(tasa_frame, text="VES", font=("Segoe UI", 13), text_color=COLOR_TEXT_MUTED).pack(side="left", padx=5)
        if puede_editar:
            ctk.CTkButton(tasa_frame, text="Guardar", width=80, fg_color=COLOR_GREEN, hover_color="#059669", image=icon_save(16, "#ffffff"), compound="left", command=self.guardar_tasa).pack(side="left", padx=10)

    def aplicar_tema(self):
        tema = self.tema_combo.get()
        set_tema(tema)
        ctk.set_appearance_mode(tema)
        messagebox.showinfo("Tema", f"Tema cambiado a: {tema}")

    def guardar_empresa(self):
        data = {label.lower(): entry.get().strip() for label, entry in self.emp_campos.items()}
        set_empresa_data(data)
        messagebox.showinfo("Éxito", "Datos de empresa guardados.")

    def guardar_tasa(self):
        try:
            tasa = float(self.entry_tasa.get().strip())
            if tasa <= 0:
                messagebox.showerror("Error", "La tasa debe ser mayor a 0.")
                return
            set_tasa_ves(tasa)
            messagebox.showinfo("Éxito", f"Tasa actualizada: 1 USD = {tasa} VES")
        except ValueError:
            messagebox.showerror("Error", "Ingrese un valor numérico válido.")
