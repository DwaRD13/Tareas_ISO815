package com.fundapec.payment.management.entity;

public class Estudiante {
    private String matricula;
    private String estado;
    private String periodoAcademico;
    private String descripcionPeriodo;

    public Estudiante() {}

    public Estudiante(String matricula, String estado, String periodoAcademico, String descripcionPeriodo) {
        this.matricula = matricula;
        this.estado = estado;
        this.periodoAcademico = periodoAcademico;
        this.descripcionPeriodo = descripcionPeriodo;
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPeriodoAcademico() { return periodoAcademico; }
    public void setPeriodoAcademico(String periodoAcademico) { this.periodoAcademico = periodoAcademico; }

    public String getDescripcionPeriodo() { return descripcionPeriodo; }
    public void setDescripcionPeriodo(String descripcionPeriodo) { this.descripcionPeriodo = descripcionPeriodo; }
}
