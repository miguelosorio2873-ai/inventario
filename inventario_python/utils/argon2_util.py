from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError, InvalidHashError

_ph = PasswordHasher(memory_cost=65536, time_cost=10, parallelism=1)


def hash_password(password):
    return _ph.hash(password)


def verify_password(password, hash_str):
    try:
        return _ph.verify(hash_str, password)
    except (VerifyMismatchError, InvalidHashError):
        return False


def es_segura(password):
    if len(password) < 8:
        return False
    tiene_mayus = any(c.isupper() for c in password)
    tiene_minus = any(c.islower() for c in password)
    tiene_num = any(c.isdigit() for c in password)
    tiene_especial = any(not c.isalnum() for c in password)
    return tiene_mayus and tiene_minus and tiene_num and tiene_especial


def get_requisitos_mensaje():
    return ("La contraseña debe tener:\n"
            "- Mínimo 8 caracteres\n"
            "- Al menos una mayúscula\n"
            "- Al menos una minúscula\n"
            "- Al menos un número\n"
            "- Al menos un carácter especial")
