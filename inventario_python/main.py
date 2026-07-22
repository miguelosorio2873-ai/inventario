import customtkinter as ctk
from ui.login import Login


def main():
    ctk.set_appearance_mode("dark")
    ctk.set_default_color_theme("green")
    Login().mainloop()


if __name__ == "__main__":
    main()
