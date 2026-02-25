package com.unapec.entity;

import com.unapec.nominagenerator.DetalleNomina;

import java.util.List;

public class NominaDTO {

    private Empresa empresa;
    private List<Empleado> empleadoList;
    private Pago pago;

    private String fechaPago;
    private double totalPagado;
    private int cantidadRegistros;
    private List<DetalleNomina> detalles;

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public List<Empleado> getEmpleadoList() {
        return empleadoList;
    }

    public void setEmpleadoList(List<Empleado> empleadoList) {
        this.empleadoList = empleadoList;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public int getCantidadRegistros() {
        return cantidadRegistros;
    }

    public void setCantidadRegistros(int cantidadRegistros) {
        this.cantidadRegistros = cantidadRegistros;
    }

    public List<DetalleNomina> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleNomina> detalles) {
        this.detalles = detalles;
    }
}
