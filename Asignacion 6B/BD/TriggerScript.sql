USE TERMOPAC;

DELIMITER $$

CREATE TRIGGER tr_rebajar_inventario_facturacion
AFTER INSERT ON detalle_factura
FOR EACH ROW
BEGIN
    UPDATE inventario
    SET existencia = existencia - NEW.cantidad
    WHERE id_articulo = NEW.id_articulo;
END$$

DELIMITER ;
