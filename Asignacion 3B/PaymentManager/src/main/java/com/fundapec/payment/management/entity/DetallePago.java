package com.fundapec.payment.management.entity;

import java.time.LocalDate;

public class DetallePago {

    private Long id;
    private String codigoPago;
    private Double montoAprobado;
    private String moneda;
    private LocalDate fechaAprobacion;
    private String matricula; // FK

    public DetallePago() {}

    public DetallePago(String codigoPago, Double montoAprobado, String moneda, LocalDate fechaAprobacion, String matricula) {
        this.codigoPago = codigoPago;
        this.montoAprobado = montoAprobado;
        this.moneda = moneda;
        this.fechaAprobacion = fechaAprobacion;
        this.matricula = matricula;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoPago() { return codigoPago; }
    public void setCodigoPago(String codigoPago) { this.codigoPago = codigoPago; }

    public Double getMontoAprobado() { return montoAprobado; }
    public void setMontoAprobado(Double montoAprobado) { this.montoAprobado = montoAprobado; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public LocalDate getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDate fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
}
