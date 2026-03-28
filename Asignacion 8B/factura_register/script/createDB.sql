CREATE DATABASE SISTEMA_CONTABLE;
USE SISTEMA_CONTABLE;

CREATE TABLE facturas (
                          no_factura VARCHAR(20) PRIMARY KEY,
                          condiciones VARCHAR(100),
                          id_cliente VARCHAR(20),
                          fecha_factura VARCHAR(20),
                          monto DOUBLE,
                          estado VARCHAR(20)
);