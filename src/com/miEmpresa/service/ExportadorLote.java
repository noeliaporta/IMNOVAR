package com.miEmpresa.service;

import com.miEmpresa.model.Empleado;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportadorLote {

    public static void exportarAJson(List<Empleado> empleados, String rutaArchivo) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < empleados.size(); i++) {
            Empleado emp = empleados.get(i);
            json.append("  {\n")
                    .append("    \"legajo\": \"").append(emp.getLegajo()).append("\",\n")
                    .append("    \"nombre\": \"").append(emp.getNombre()).append("\",\n")
                    .append("    \"sueldoBruto\": ").append(emp.calcularSueldoBruto()).append(",\n")
                    .append("    \"descuentos\": ").append(emp.calcularDescuentos()).append(",\n")
                    .append("    \"sueldoNetoUSDT\": ").append(emp.calcularSueldoNeto()).append("\n")
                    .append("  }");

            if (i < empleados.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]");

        try (FileWriter file = new FileWriter(rutaArchivo)) {
            file.write(json.toString());
            System.out.println("Lote de liquidación exportado con éxito a: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar el lote JSON: " + e.getMessage());
        }
    }
}