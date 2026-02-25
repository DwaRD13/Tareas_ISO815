package com.unapec.nominagenerator;

public class DetalleNomina {
    private String cedula;
    private String tipoCuenta;
    private String cuentaDestino;
    private double sueldoBrutoPeriodo;
    private double descuentoTSS;
    private double sueldoNeto;

    public DetalleNomina(String cedula, String tipoCuenta, String cuentaDestino, double sueldoBrutoPeriodo, double descuentoTSS, double sueldoNeto) {
        this.cedula = cedula;
        this.tipoCuenta = tipoCuenta;
        this.cuentaDestino = cuentaDestino;
        this.sueldoBrutoPeriodo = sueldoBrutoPeriodo;
        this.descuentoTSS = descuentoTSS;
        this.sueldoNeto = sueldoNeto;
    }

    public String getCedula() { return cedula; }
    public String getTipoCuenta() { return tipoCuenta; }
    public String getCuentaDestino() { return cuentaDestino; }
    public double getSueldoBrutoPeriodo() { return sueldoBrutoPeriodo; }
    public double getDescuentoTSS() { return descuentoTSS; }
    public double getSueldoNeto() { return sueldoNeto; }
}
