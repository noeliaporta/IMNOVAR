package com.miEmpresa.model;
/*
Operario: Cobra un básico más horas extras (calculadas a un valor/hora determinado).
Vendedor: Cobra un básico más una comisión basada en sus ventas totales del mes.
 */

public class Operario extends Empleado {
    private int horasExtras;
    private double valorHoraExtra;

    public Operario(String nombre, String legajo, double sueldoBasico, int horasExtras, double valorHoraExtra) {
        super(nombre, legajo, sueldoBasico, 0.0);
        this.horasExtras = horasExtras;
        this.valorHoraExtra = valorHoraExtra;
    }

    public int getHorasExtras() {
        return horasExtras;
    }

    public void setHorasExtras(int horasExtras) {
        this.horasExtras = horasExtras;
    }

    public double getValorHoraExtra() {
        return valorHoraExtra;
    }

    public void setValorHoraExtra(double valorHoraExtra) {
        this.valorHoraExtra = valorHoraExtra;
    }

    @Override
    public double calcularSueldoBruto() {
        // Sueldo básico + (cantidad de horas extras * precio por hora)
        return getSueldoBasico() + (this.horasExtras * this.valorHoraExtra);
    }

}