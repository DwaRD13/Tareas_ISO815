package com.industria.termopac.facturacion.entity;

public class ProductoCombo {
    private final int id;
    private final String nombre;
    private final int existencia;

    public ProductoCombo(int id, String nombre, int existencia) {
        this.id = id; this.nombre = nombre; this.existencia = existencia;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getExistencia() { return existencia; }

    @Override
    public String toString() { return nombre + " (Stock: " + existencia + ")"; }
}
