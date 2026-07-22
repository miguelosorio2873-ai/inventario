import base64
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding as sym_padding
from config import AES_KEY


def encriptar(texto_plano):
    if not texto_plano:
        return texto_plano
    try:
        padder = sym_padding.PKCS7(128).padder()
        padded = padder.update(texto_plano.encode("utf-8")) + padder.finalize()
        cipher = Cipher(algorithms.AES(AES_KEY), modes.ECB())
        encryptor = cipher.encryptor()
        encrypted = encryptor.update(padded) + encryptor.finalize()
        return base64.b64encode(encrypted).decode("utf-8")
    except Exception as e:
        print(f"Error al encriptar: {e}")
        return texto_plano


def desencriptar(texto_encriptado):
    if not texto_encriptado:
        return texto_encriptado
    try:
        raw = base64.b64decode(texto_encriptado)
        cipher = Cipher(algorithms.AES(AES_KEY), modes.ECB())
        decryptor = cipher.decryptor()
        decrypted_padded = decryptor.update(raw) + decryptor.finalize()
        unpadder = sym_padding.PKCS7(128).unpadder()
        decrypted = unpadder.update(decrypted_padded) + unpadder.finalize()
        return decrypted.decode("utf-8")
    except Exception:
        return texto_encriptado
