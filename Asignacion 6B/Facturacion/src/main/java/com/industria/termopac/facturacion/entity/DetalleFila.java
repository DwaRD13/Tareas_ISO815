package com.industria.termopac.facturacion.entity;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DetalleFila {

    private final int idArticulo;
    private final SimpleStringProperty nombre;
    private final SimpleIntegerProperty cantidad;
    private final SimpleDoubleProperty precio;
    private final SimpleDoubleProperty subtotal;

    public DetalleFila(int idArticulo, String nombre, int cantidad, double precio) {
        this.idArticulo = idArticulo;
        this.nombre = new SimpleStringProperty(nombre);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.precio = new SimpleDoubleProperty(precio);
        this.subtotal = new SimpleDoubleProperty(cantidad * precio);
    }

    public int getIdArticulo() { return idArticulo; }
    public int getCantidad() { return cantidad.get(); }
    public double getPrecio() { return precio.get(); }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleIntegerProperty cantidadProperty() { return cantidad; }
    public SimpleDoubleProperty precioProperty() { return precio; }
    public SimpleDoubleProperty subtotalProperty() { return subtotal; }
}
