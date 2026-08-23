package com.miEmpresa.service;

import com.miEmpresa.model.Empleado;
import java.util.List;

public class SupervisorAgente {

    private static final int MAX_FALLOS_CONSECUTIVOS = 2;
    private static final int UMBRAL_RIESGO_MINIMO = 60;

    private AuditorAgente agenteIntegridad;
    private ControlTransaccionesAgente agenteControl;
    private ReciboAgente agenteRecibos;
    private WdkCliService wdkService;

    public SupervisorAgente(AuditorAgente a1, ControlTransaccionesAgente a2, ReciboAgente a3, WdkCliService wdk) {
        this.agenteIntegridad = a1;
        this.agenteControl = a2;
        this.agenteRecibos = a3;
        this.wdkService = wdk;
    }

    public void procesarNominaColaborativa(List<Empleado> nomina) {
        System.out.println("🕵️‍♂️ [SupervisorAgente] Validando presupuesto global de tesorería...");

        List<Empleado> nominaAprobadaGlobal = agenteIntegridad.auditarNominaCompleta(nomina);
        if (nominaAprobadaGlobal == null || nominaAprobadaGlobal.isEmpty()) {
            ManejadorFallos.registrarFallo("SupervisorAgente", "PROCESO CANCELADO: Presupuesto global superado o nomina vacia.");
            return;
        }

        // Se invoca el desbloqueo leyendo directamente del entorno del proceso
        if (!wdkService.desbloquearWallet()) {
            ManejadorFallos.registrarFallo("SupervisorAgente", "ABORTADO: Fallo en el desbloqueo de la wallet WDK.");
            return;
        }

        int fallosConsecutivos = 0;

        try {
            for (Empleado emp : nominaAprobadaGlobal) {
                if (fallosConsecutivos >= MAX_FALLOS_CONSECUTIVOS) {
                    ManejadorFallos.registrarFallo("SupervisorAgente", "CIRCUIT BREAKER ACTIVADO: Se detiene la nomina restante por anomalia.");
                    break;
                }

                if (!agenteIntegridad.auditarEmpleado(emp)) {
                    fallosConsecutivos++;
                    continue;
                }

                double monto = emp.calcularSueldoNeto();
                if (!agenteControl.validarPreTransaccion(emp, monto)) {
                    fallosConsecutivos++;
                    continue;
                }

                int score = agenteControl.calcularScoringRiesgo(emp, monto);
                if (score < UMBRAL_RIESGO_MINIMO) {
                    ManejadorFallos.registrarDecision("SupervisorAgente", "PENDIENTE_APROBACION_MANUAL",
                            "Score de riesgo bajo (" + score + "/100) para legajo " + emp.getLegajo() + ". Pago detenido.");
                    continue;
                }

                String respuestaWdk = wdkService.ejecutarPagoUsdt(emp.getDireccionWallet(), monto);

                // Muestra de la respuesta WDK con txHash
                System.out.println("📄 [WDK Response] Legajo " + emp.getLegajo() + ": " + respuestaWdk);

                if (!agenteControl.validarPostTransaccion(emp, respuestaWdk)) {
                    fallosConsecutivos++;
                    continue;
                }

                if (!agenteRecibos.generarReciboArea(emp, monto, respuestaWdk, score)) {
                    fallosConsecutivos++;
                    continue;
                }

                fallosConsecutivos = 0;
            }
        } finally {
            wdkService.bloquearWallet();
        }
    }
}