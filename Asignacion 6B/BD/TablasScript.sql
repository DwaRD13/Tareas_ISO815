CREATE DATABASE TERMOPAC;

-- ==========================================
-- APLICACIÓN DE INVENTARIOS
-- ==========================================
USE TERMOPAC;

CREATE TABLE inventario (
    id_articulo INT AUTO_INCREMENT PRIMARY KEY,
    codigo_producto VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    existencia INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_existencia_positiva CHECK (existencia >= 0) 
);

-- ==========================================
-- APLICACIÓN DE FACTURACIÓN
-- ==========================================

CREATE TABLE factura (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(20) UNIQUE NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cliente VARCHAR(100) NOT NULL
);

CREATE TABLE detalle_factura (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    id_articulo INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    
    FOREIGN KEY (id_factura) REFERENCES factura(id) ON DELETE CASCADE,
    FOREIGN KEY (id_articulo) REFERENCES inventario(id_articulo),
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0)
);