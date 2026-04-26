-- =============================================================
-- BASE DE DATOS: Sistema de Inventario
-- =============================================================

DROP DATABASE IF EXISTS inventario_db;
CREATE DATABASE inventario_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE inventario_db;

-- =============================================================
-- TABLA: usuario
-- =============================================================
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) DEFAULT 'Estándar',
    pregunta_1 VARCHAR(255),
    pregunta_2 VARCHAR(255),
    pregunta_3 VARCHAR(255),
    pregunta_4 VARCHAR(255),
    respuesta_1 VARCHAR(255),
    respuesta_2 VARCHAR(255),
    respuesta_3 VARCHAR(255),
    respuesta_4 VARCHAR(255),
    permisos TEXT NULL
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: categorias
-- =============================================================
CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT NULL
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: producto
-- =============================================================
CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria_id BIGINT,
    sku VARCHAR(255) UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT NULL,
    precio_venta FLOAT NOT NULL DEFAULT 0,
    costo_promedio FLOAT NULL DEFAULT 0,
    stock_minimo FLOAT NOT NULL DEFAULT 0,
    state BOOLEAN NOT NULL DEFAULT TRUE,
    imagen VARCHAR(255),
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: categoria_producto (relación muchos a muchos)
-- =============================================================
CREATE TABLE categoria_producto (
    producto_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (producto_id, categoria_id),
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: cliente
-- =============================================================
CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(255) UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255),
    telefono VARCHAR(255)
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: proveedor
-- =============================================================
CREATE TABLE proveedor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa VARCHAR(255) NOT NULL,
    nit_cedula VARCHAR(255),
    telefono VARCHAR(255),
    direccion VARCHAR(255),
    correo VARCHAR(255),
    nombre_contacto VARCHAR(255)
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: inventario (movimientos)
-- =============================================================
CREATE TABLE inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    proveedor_id BIGINT NULL,
    precio FLOAT NOT NULL DEFAULT 0,
    precio_balance FLOAT NOT NULL DEFAULT 0,
    cantidad FLOAT NOT NULL DEFAULT 0,
    tipo_movimiento ENUM('entrada', 'salida', 'ajuste') NOT NULL,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT NULL,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
    FOREIGN KEY (proveedor_id) REFERENCES proveedor(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- TABLA: factura
-- =============================================================
CREATE TABLE factura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id BIGINT,
    cliente_id BIGINT,
    numero_factura VARCHAR(255) UNIQUE,
    fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago ENUM('Efectivo', 'Tarjeta', 'Transferencia') NOT NULL DEFAULT 'Efectivo',
    estado ENUM('Pagada', 'Pendiente', 'Anulada') NOT NULL DEFAULT 'Pendiente',
    subtotal FLOAT NOT NULL DEFAULT 0,
    impuestos FLOAT NOT NULL DEFAULT 0,
    total FLOAT NOT NULL DEFAULT 0,
    FOREIGN KEY (movimiento_id) REFERENCES inventario(id) ON DELETE SET NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- VISTA: stock actual por producto
-- =============================================================
CREATE VIEW vista_stock AS
SELECT 
    p.id,
    p.sku,
    p.nombre,
    p.precio_venta,
    p.costo_promedio,
    p.stock_minimo,
    p.state,
    c.nombre AS categoria,
    COALESCE(SUM(CASE 
        WHEN i.tipo_movimiento = 'entrada' THEN i.cantidad
        WHEN i.tipo_movimiento = 'salida' THEN -i.cantidad
        WHEN i.tipo_movimiento = 'ajuste' THEN i.cantidad
        ELSE 0
    END), 0) AS stock_actual
FROM producto p
LEFT JOIN categorias c ON p.categoria_id = c.id
LEFT JOIN inventario i ON p.id = i.producto_id
GROUP BY p.id;

-- =============================================================
-- DATOS INICIALES
-- =============================================================

-- Usuario admin (password: admin123 en SHA-256)
-- (Asegúrate de cambiar las contraseñas en un entorno de producción, hasheadas)
INSERT INTO usuario (nombre, email, password, rol) VALUES 
('Admin', 'admin@sig.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'Admin');

-- Categorías de ejemplo
INSERT INTO categorias (nombre, descripcion) VALUES 
('Electrónica', 'Dispositivos electrónicos y accesorios'),
('Alimentos', 'Productos alimenticios y bebidas'),
('Ropa', 'Prendas de vestir y accesorios de moda'),
('Hogar', 'Artículos para el hogar y decoración'),
('Oficina', 'Suministros y equipos de oficina');

-- Proveedores de ejemplo
INSERT INTO proveedor (nombre_empresa, nit_cedula, telefono, direccion, correo, nombre_contacto) VALUES
('TechSupply S.A.', 'J-12345678-9', '0212-5551234', 'Av. Principal, Caracas', 'ventas@techsupply.com', 'Carlos Méndez'),
('Distribuidora Central', 'J-98765432-1', '0212-5559876', 'Calle 10, Valencia', 'info@distcentral.com', 'María López');

-- Clientes de ejemplo
INSERT INTO cliente (cedula, nombre, correo, telefono) VALUES
('V-12345678', 'Juan Pérez', 'juan@email.com', '0414-1234567'),
('V-87654321', 'Ana García', 'ana@email.com', '0424-7654321');

-- Productos de ejemplo
INSERT INTO producto (categoria_id, sku, nombre, descripcion, precio_venta, costo_promedio, stock_minimo, state) VALUES
(1, 'ELEC-001', 'Teclado Mecánico RGB', 'Teclado mecánico con retroiluminación RGB', 45.99, 28.00, 10, TRUE),
(1, 'ELEC-002', 'Mouse Inalámbrico', 'Mouse ergonómico inalámbrico 2.4GHz', 18.50, 10.00, 15, TRUE),
(1, 'ELEC-003', 'Monitor 24" Full HD', 'Monitor LED 24 pulgadas 1080p', 189.99, 120.00, 5, TRUE),
(5, 'OFIC-001', 'Resma de Papel A4', 'Resma de 500 hojas tamaño carta', 4.50, 2.80, 50, TRUE),
(5, 'OFIC-002', 'Bolígrafos Pack x12', 'Pack de 12 bolígrafos tinta azul', 3.25, 1.50, 30, TRUE),
(4, 'HOGR-001', 'Lámpara LED Escritorio', 'Lámpara de escritorio LED regulable', 22.00, 12.00, 8, TRUE);

-- Movimientos de inventario iniciales (entradas)
INSERT INTO inventario (producto_id, proveedor_id, precio, precio_balance, cantidad, tipo_movimiento, motivo) VALUES
(1, 1, 28.00, 28.00, 25, 'entrada', 'Stock inicial'),
(2, 1, 10.00, 10.00, 40, 'entrada', 'Stock inicial'),
(3, 1, 120.00, 120.00, 10, 'entrada', 'Stock inicial'),
(4, 2, 2.80, 2.80, 100, 'entrada', 'Stock inicial'),
(5, 2, 1.50, 1.50, 60, 'entrada', 'Stock inicial'),
(6, 2, 12.00, 12.00, 20, 'entrada', 'Stock inicial');
