package com.miEmpresa.model;

public class Empleado {
    private String nombre;
    private String legajo;
    private double sueldoBasico;
    private double bonosOAdicionales;
    private String direccionWallet;

    private static final double PORC_JUBILACION = 0.11;     // 11%
    private static final double PORC_OBRA_SOCIAL = 0.03;    // 3%
    private static final double PORC_LEY_19032 = 0.03;      // 3%

    public Empleado (String nombre, String legajo, double sueldoBasico, double bonosOAdicionales){
        this.nombre = nombre;
        this.legajo = legajo;
        this.sueldoBasico = sueldoBasico;
        this.bonosOAdicionales = bonosOAdicionales;
    }

    public double calcularSueldoBruto(){
        return this.sueldoBasico + this.bonosOAdicionales;
    }

    public double calcularDescuentos(){
        double bruto = calcularSueldoBruto();
        double jubilacion = bruto * PORC_JUBILACION;
        double obraSocial = bruto * PORC_OBRA_SOCIAL;
        double ley19032 = bruto * PORC_LEY_19032;

        return jubilacion + obraSocial + ley19032;
    }

    public double calcularSueldoNeto(){
        return calcularSueldoBruto() - calcularDescuentos();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public double getSueldoBasico() {
        return sueldoBasico;
    }

    public void setSueldoBasico(double sueldoBasico) {
        this.sueldoBasico = sueldoBasico;
    }

    public double getBonosOAdicionales() {
        return bonosOAdicionales;
    }

    public void setBonosOAdicionales(double bonosOAdicionales) {
        this.bonosOAdicionales = bonosOAdicionales;
    }

    public String getDireccionWallet() {
        return direccionWallet;
    }

    public void setDireccionWallet(String direccionWallet) {
        this.direccionWallet = direccionWallet;
    }

}


/*
Esta es la parte técnica clave. En lugar de hacer una llamada a una API web común con una librería HTTP, tenés que hacer que Java execute un comando en la terminal de tu computadora.
ProcessBuilder: Es la herramienta nativa de Java para abrir la terminal, escribir un comando (como wdk send o wdk balance) y presionar Enter desde el código.
Captura de salida: Cuando la CLI (Command Line Interface) de Tether responde, imprime un texto en formato JSON. Tu programa en Java tiene que "leer" esa respuesta para saber si la transacción se hizo bien o falló.

 Una clase base Empleado (o abstracta) y sus derivadas (ej. Operario, Vendedor, Administrativo).
Métodos para calcular el Sueldo Bruto, aplicar Deducciones (descuentos) y obtener el Sueldo Neto final.
La diferencia es que la liquidación se calcula o cotiza en USD₮ (Tether).
También tenés que poder guardar/exportar esos lotes de liquidación (por ejemplo en archivos de texto, CSV o JSON).

Cuando se confirma un pago crypto en la red, la herramienta de Tether devuelve un código identificador único llamado txHash (el hash de la transacción).
Tu programa debe capturar ese txHash que devolvió la consola.
Tenés que asociar ese código al empleado correspondiente y guardarlo en el archivo o registro de recibos como comprobante del pago enviado.
 */