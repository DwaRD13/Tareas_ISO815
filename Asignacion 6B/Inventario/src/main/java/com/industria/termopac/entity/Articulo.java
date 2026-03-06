package com.industria.termopac.entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Articulo {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty codigo;
    private final SimpleStringProperty nombre;
    private final SimpleIntegerProperty existencia;

    public Articulo(int id, String codigo, String nombre, int existencia) {
        this.id = new SimpleIntegerProperty(id);
        this.codigo = new SimpleStringProperty(codigo);
        this.nombre = new SimpleStringProperty(nombre);
        this.existencia = new SimpleIntegerProperty(existencia);
    }

    public int getId() { return id.get(); }
    public String getCodigo() { return codigo.get(); }
    public String getNombre() { return nombre.get(); }
    public int getExistencia() { return existencia.get(); }
}
