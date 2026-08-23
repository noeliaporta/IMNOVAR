package com.miEmpresa.service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ManejadorFallos {

    private static final String LOG_FALLOS = "fallos_sistema.log";
    private static final String LOG_DECISIONES = "decisiones_sistema.log";

    public static void registrarFallo(String agente, String causa) {
        String mensaje = String.format("[%s] 🚨 FALLO EN [%s]: %s\n", LocalDateTime.now(), agente, causa);
        System.err.print(mensaje);
        escribirLog(LOG_FALLOS, mensaje);
        registrarDecision(agente, "RECHAZADO", causa);
    }

    public static void registrarDecision(String agente, String estado, String motivo) {
        String mensaje = String.format("[%s] [%s] ESTADO: %s | MOTIVO: %s\n", LocalDateTime.now(), agente, estado, motivo);
        escribirLog(LOG_DECISIONES, mensaje);
    }

    private static void escribirLog(String archivo, String mensaje) {
        try (FileWriter writer = new FileWriter(archivo, true)) {
            writer.write(mensaje);
        } catch (IOException e) {
            System.err.println("Error escribiendo en log (" + archivo + "): " + e.getMessage());
        }
    }
}