package com.unapec.entity;

public class Empresa {

    private Long id;
    private String nombre;
    private Long RNC;
    private String cuentaBancaria;
    private String formaPago;

    public Empresa() {
    }

    public Empresa(Long id, String nombre, Long RNC, String cuentaBancaria, String formaPago) {
        this.id = id;
        this.nombre = nombre;
        this.RNC = RNC;
        this.cuentaBancaria = cuentaBancaria;
        this.formaPago = formaPago;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getRNC() {
        return RNC;
    }

    public void setRNC(Long RNC) {
        this.RNC = RNC;
    }

    public String getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(String cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
