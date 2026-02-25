package com.unapec.entity;

public class Empleado {

    private Long id;
    private String cedula;
    private String cuentaBancaria;
    private String tipoDeCuenta;
    private double salario;
    private Long empresaId;

    public Empleado(){}

    public Empleado(Long id, String cedula, String cuentaBancaria, String tipoDeCuenta, double salario) {
        this.id = id;
        this.cedula = cedula;
        this.cuentaBancaria = cuentaBancaria;
        this.tipoDeCuenta = tipoDeCuenta;
        this.salario = salario;
    }

    public Empleado(Long id, String cedula, String cuentaBancaria, String tipoDeCuenta, double salario, Long empresaId) {
        this.id = id;
        this.cedula = cedula;
        this.cuentaBancaria = cuentaBancaria;
        this.tipoDeCuenta = tipoDeCuenta;
        this.salario = salario;
        this.empresaId = empresaId;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(String cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public String getTipoDeCuenta() {
        return tipoDeCuenta;
    }

    public void setTipoDeCuenta(String tipoDeCuenta) {
        this.tipoDeCuenta = tipoDeCuenta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}
