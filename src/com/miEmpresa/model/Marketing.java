package com.miEmpresa.model;

public class Marketing extends Empleado {
    private int campañasCumplidas;
    private double bonoPorCampaña;

    public Marketing(String nombre, String legajo, double sueldoBasico, int campañasCumplidas, double bonoPorCampaña) {
        super(nombre, legajo, sueldoBasico, 0.0);
        this.campañasCumplidas = campañasCumplidas;
        this.bonoPorCampaña = bonoPorCampaña;
    }

    @Override
    public double calcularSueldoBruto() {
        return getSueldoBasico() + (this.campañasCumplidas * this.bonoPorCampaña);
    }

    public int getCampañasCumplidas() {
        return campañasCumplidas;
    }

    public void setCampañasCumplidas(int campañasCumplidas) {
        this.campañasCumplidas = campañasCumplidas;
    }

    public double getBonoPorCampaña() {
        return bonoPorCampaña;
    }

    public void setBonoPorCampaña(double bonoPorCampaña) {
        this.bonoPorCampaña = bonoPorCampaña;
    }
}