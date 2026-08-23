package com.miEmpresa.service;

import com.miEmpresa.model.Empleado;
import java.util.ArrayList;
import java.util.List;

public class AuditorAgente {

    private double limiteMaximoPorEmpleado;
    private double presupuestoTotalDisponible;

    public AuditorAgente(double limiteMaximoPorEmpleado, double presupuestoTotalDisponible) {
        this.limiteMaximoPorEmpleado = limiteMaximoPorEmpleado;
        this.presupuestoTotalDisponible = presupuestoTotalDisponible;
    }

    /**
     * Evalúa un empleado y retorna true si pasa todas las reglas de auditoría.
     */
    public boolean auditarEmpleado(Empleado empleado) {
        double neto = empleado.calcularSueldoNeto();
        String wallet = empleado.getDireccionWallet();

        System.out.println("🤖 [Agente Auditor] Analizando a " + empleado.getNombre() + " (" + empleado.getLegajo() + ")...");

        // Regla 1: Validar formato de Wallet EVM (0x + 40 caracteres hex)
        if (wallet == null || !wallet.matches("^0x[a-fA-F0-9]{40}$")) {
            System.err.println("   ❌ RECHAZADO: Dirección de Wallet inválida (" + wallet + ")");
            return false;
        }

        // Regla 2: Validar que el monto neto sea mayor a cero
        if (neto <= 0) {
            System.err.println("   ❌ RECHAZADO: El monto neto debe ser mayor a 0 USDT.");
            return false;
        }

        // Regla 3: Alerta de monto anómalo / límite de seguridad
        if (neto > limiteMaximoPorEmpleado) {
            System.err.println("   ⚠️ ALERTA: El monto de " + neto + " USDT supera el límite máximo permitido por empleado (" + limiteMaximoPorEmpleado + " USDT).");
            return false;
        }

        System.out.println("   ✅ APROBADO: Parámetros y wallet verificados.");
        return true;
    }

    /**
     * Audita la nómina completa y valida el presupuesto global.
     */
    public List<Empleado> auditarNominaCompleta(List<Empleado> nomina) {
        List<Empleado> empleadosAprobados = new ArrayList<>();
        double totalNomina = 0;

        System.out.println("===================================================");
        System.out.println("🤖 AGENTE AUDITOR DE IA: INICIANDO REVISIÓN DE LOTE");
        System.out.println("===================================================");

        for (Empleado emp : nomina) {
            if (auditarEmpleado(emp)) {
                empleadosAprobados.add(emp);
                totalNomina += emp.calcularSueldoNeto();
            }
        }

        System.out.println("\n📊 Resumen de Auditoría:");
        System.out.println("   - Total Aprobados: " + empleadosAprobados.size() + " / " + nomina.size());
        System.out.println("   - Total a Liquidar: " + totalNomina + " USDT");
        System.out.println("   - Presupuesto Disponible: " + presupuestoTotalDisponible + " USDT");

        if (totalNomina > presupuestoTotalDisponible) {
            System.err.println("🚨 BLOQUEO CRÍTICO: La nómina supera el presupuesto disponible en tesorería.");
            return new ArrayList<>(); // Retorna lista vacía para frenar la ejecución
        }

        System.out.println("🎉 LOTE DE LIQUIDACIÓN AUTORIZADO PARA TRANSMISIÓN WDK\n");
        return empleadosAprobados;
    }
}