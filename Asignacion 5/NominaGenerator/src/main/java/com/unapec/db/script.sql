CREATE DATABASE NOMINA;

USE NOMINA;
CREATE TABLE EMPRESA(
                        ID INT AUTO_INCREMENT PRIMARY KEY,
                        NOMBRE varchar(100),
                        RNC numeric(9),
                        CUENTA_BANCARIA VARCHAR(20),
                        FORMA_PAGO VARCHAR(20)
);

USE NOMINA;
CREATE TABLE EMPLEADO(
                         ID INT AUTO_INCREMENT PRIMARY KEY,
                         CEDULA numeric(11),
                         CUENTA_BANCARIA numeric(20),
                         TIPO_CUENTA VARCHAR(2),  -- DE, CR, CO
                         SALARIO DECIMAL(10,2),
                         EMPRESA_ID INT,
                         FOREIGN KEY (EMPRESA_ID) REFERENCES EMPRESA(ID)
);

USE NOMINA;
CREATE TABLE Pago(
                     ID INT AUTO_INCREMENT PRIMARY KEY,
                     FECHA_PAGO DATE,
                     MONTO_PAGO DECIMAL (12,0),
                     EMPRESA_ID INT,
                     EMPLEADO_ID INT,
                     FOREIGN KEY  (EMPRESA_ID) REFERENCES EMPRESA(ID) ON DELETE SET NULL,
                     FOREIGN KEY  (EMPLEADO_ID) REFERENCES EMPLEADO(ID) ON DELETE SET NULL
);