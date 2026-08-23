package com.miEmpresa.service;

import com.miEmpresa.model.Empleado;
import java.util.HashSet;
import java.util.Set;

public class ControlTransaccionesAgente {

    private Set<String> walletsProcesadas = new HashSet<>();
    private Set<String> legajosPagados = new HashSet<>();

    public int calcularScoringRiesgo(Empleado emp, double monto) {
        int score = 100;
        if (monto > emp.getSueldoBasico() * 1.5) score -= 25;

        String wallet = emp.getDireccionWallet().toLowerCase();
        if (wallet.matches("^0x(.)\\1+$") || wallet.endsWith("0000")) score -= 30;
        if (emp.getBonosOAdicionales() > emp.calcularSueldoBruto() * 0.4) score -= 20;

        return Math.max(0, score);
    }

    public boolean validarPreTransaccion(Empleado emp, double monto) {
        if (emp == null) {
            ManejadorFallos.registrarFallo("ControlTransaccionesAgente", "Empleado es nulo.");
            return false;
        }

        if (monto <= 0 || Math.abs(monto - emp.calcularSueldoNeto()) > 0.001) {
            ManejadorFallos.registrarFallo("ControlTransaccionesAgente", "Incoherencia de monto neto para legajo " + emp.getLegajo());
            return false;
        }

        if (walletsProcesadas.contains(emp.getDireccionWallet())) {
            ManejadorFallos.registrarFallo("ControlTransaccionesAgente", "Wallet duplicada detectada: " + emp.getDireccionWallet());
            return false;
        }

        if (legajosPagados.contains(emp.getLegajo())) {
            ManejadorFallos.registrarFallo("ControlTransaccionesAgente", "Doble pago bloqueado para legajo: " + emp.getLegajo());
            return false;
        }

        walletsProcesadas.add(emp.getDireccionWallet());
        ManejadorFallos.registrarDecision("ControlTransaccionesAgente", "APROBADO_PRE", "Monto y wallet unicos verificados para " + emp.getLegajo());
        return true;
    }

    public boolean validarPostTransaccion(Empleado emp, String respuestaJsonWdk) {
        if (respuestaJsonWdk == null || respuestaJsonWdk.contains("error") || !respuestaJsonWdk.contains("txHash")) {
            ManejadorFallos.registrarFallo("ControlTransaccionesAgente", "Respuesta WDK invalida o fallida para legajo " + emp.getLegajo());
            return false;
        }

        // Validación cruzada de coherencia entre la wallet del empleado y el campo 'to' de la respuesta
        String walletEjecutada = extraerValorJson(respuestaJsonWdk, "to");
        if (!emp.getDireccionWallet().equalsIgnoreCase(walletEjecutada)) {
            ManejadorFallos.registrarFallo("ControlTransaccionesAgente",
                    "DESVIACIÓN CRÍTICA DETECTADA: Se envio a " + walletEjecutada + " pero se esperaba " + emp.getDireccionWallet());
            return false;
        }

        legajosPagados.add(emp.getLegajo());
        ManejadorFallos.registrarDecision("ControlTransaccionesAgente", "APROBADO_POST", "Transaccion WDK verificada y coherente con wallet de destino.");
        return true;
    }

    private String extraerValorJson(String json, String clave) {
        try {
            int idxClave = json.indexOf("\"" + clave + "\":");
            if (idxClave == -1) return "";
            int inicio = json.indexOf("\"", idxClave + clave.length() + 3) + 1;
            int fin = json.indexOf("\"", inicio);
            return json.substring(inicio, fin);
        } catch (Exception e) {
            return "";
        }
    }
}