package com.procode.factura_register;

public class Factura {
    private String noFactura;
    private String condiciones;
    private String idCliente;
    private String fechaFactura;
    private Double monto;
    private String estado;

    public Factura(String noFactura, String condiciones, String idCliente, String fechaFactura, Double monto, String estado) {
        this.noFactura = noFactura;
        this.condiciones = condiciones;
        this.idCliente = idCliente;
        this.fechaFactura = fechaFactura;
        this.monto = monto;
        this.estado = estado;
    }

    public String getNoFactura() {
        return noFactura;
    }

    public void setNoFactura(String noFactura) {
        this.noFactura = noFactura;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(String condiciones) {
        this.condiciones = condiciones;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(String fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
