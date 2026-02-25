package com.unapec.entity;

import java.time.LocalDate;

public class Pago {

    private Long id;
    private LocalDate fecha;
    private double montoPago;

    public Pago(){}

    public Pago(Long id, double montoPago, LocalDate fecha) {
        this.id = id;
        this.montoPago = montoPago;
        this.fecha = fecha;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getMontoPago() {
        return montoPago;
    }

    public void setMontoPago(double montoPago) {
        this.montoPago = montoPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {}
}
