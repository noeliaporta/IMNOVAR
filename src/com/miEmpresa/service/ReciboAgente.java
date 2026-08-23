package com.miEmpresa.service;

import com.miEmpresa.model.Empleado;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ReciboAgente {

    private String extraerCampoJson(String jsonRespuesta, String clave, String valorPorDefecto) {
        try {
            int idxClave = jsonRespuesta.indexOf("\"" + clave + "\":");
            if (idxClave == -1) return valorPorDefecto;
            int inicio = jsonRespuesta.indexOf("\"", idxClave + clave.length() + 3) + 1;
            int fin = jsonRespuesta.indexOf("\"", inicio);
            return jsonRespuesta.substring(inicio, fin);
        } catch (Exception e) {
            return valorPorDefecto;
        }
    }

    public boolean generarReciboArea(Empleado emp, double monto, String jsonWdk, int scoreRiesgo) {
        String area = emp.getClass().getSimpleName();
        File carpetaArea = new File("Empleados" + File.separator + area);

        if (!carpetaArea.exists() && !carpetaArea.mkdirs()) {
            ManejadorFallos.registrarFallo("ReciboAgente", "Fallo al crear directorio " + carpetaArea.getPath());
            return false;
        }

        File archivoRecibo = new File(carpetaArea, "recibo_" + emp.getLegajo() + ".txt");
        if (archivoRecibo.exists()) {
            ManejadorFallos.registrarFallo("ReciboAgente", "El recibo ya existe, operacion cancelada para " + emp.getLegajo());
            return false;
        }

        String txHash = extraerCampoJson(jsonWdk, "txHash", "N/A");
        String feeRed = extraerCampoJson(jsonWdk, "feeFormatted", "0.00 USDT");

        StringBuilder contenido = new StringBuilder();
        contenido.append("===================================================\n")
                .append("        COMPROBANTE DE LIQUIDACIÓN - ").append(area.toUpperCase()).append("\n")
                .append("===================================================\n")
                .append("Fecha: ").append(LocalDateTime.now()).append("\n")
                .append("Empleado: ").append(emp.getNombre()).append(" (").append(emp.getLegajo()).append(")\n")
                .append("Monto Pago USDT: $").append(monto).append("\n")
                .append("Comisión de Red: ").append(feeRed).append("\n")
                .append("Wallet Destino: ").append(emp.getDireccionWallet()).append("\n")
                .append("Scoring de Riesgo: ").append(scoreRiesgo).append("/100\n")
                .append("Hash TX WDK: ").append(txHash).append("\n")
                .append("===================================================\n");

        try (FileWriter writer = new FileWriter(archivoRecibo)) {
            writer.write(contenido.toString());
            ManejadorFallos.registrarDecision("ReciboAgente", "EXITO", "Recibo guardado en " + archivoRecibo.getPath());
            return true;
        } catch (IOException e) {
            ManejadorFallos.registrarFallo("ReciboAgente", "Error I/O guardando recibo para " + emp.getLegajo());
            return false;
        }
    }
}