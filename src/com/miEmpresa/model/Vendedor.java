package com.miEmpresa.model;

public class Vendedor extends Empleado {
    private double totalVentasMes;
    private double porcentajeComision; // Ejemplo: 0.05 para un 5%

    public Vendedor(String nombre, String legajo, double sueldoBasico, double totalVentasMes, double porcentajeComision) {
        super(nombre, legajo, sueldoBasico, 0.0);
        this.totalVentasMes = totalVentasMes;
        this.porcentajeComision = porcentajeComision;
    }

    @Override
    public double calcularSueldoBruto() {
        // Sueldo básico + (total vendido * porcentaje de comisión)
        return getSueldoBasico() + (this.totalVentasMes * this.porcentajeComision);
    }

    // Getters y Setters específicos
    public double getTotalVentasMes() { return totalVentasMes; }
    public void setTotalVentasMes(double totalVentasMes) { this.totalVentasMes = totalVentasMes; }

    public double getPorcentajeComision() { return porcentajeComision; }
    public void setPorcentajeComision(double porcentajeComision) { this.porcentajeComision = porcentajeComision; }
}