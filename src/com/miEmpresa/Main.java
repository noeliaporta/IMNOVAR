package com.miEmpresa;

import com.miEmpresa.model.*;
import com.miEmpresa.service.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Empleado> nomina = new ArrayList<>();

        Operario emp1 = new Operario("Carlos Gómez", "OP-101", 0.01, 0, 0.0);
        emp1.setDireccionWallet("0x71C7656EC7ab88b098defB751B7401B5f6d8976F");

        Vendedor emp2 = new Vendedor("Ana Martínez", "VEN-202", 0.01, 0.0, 0.0);
        emp2.setDireccionWallet("0xFd9cCA3918b0bFf32D10CCeF6CFD561B7CE277D5");

        Marketing emp3 = new Marketing("Lucía Ruiz", "MKT-303", 0.01, 0, 0.0);
        emp3.setDireccionWallet("0x1111111111111111111111111111111111111111");

        nomina.add(emp1);
        nomina.add(emp2);
        nomina.add(emp3);

        AuditorAgente a1 = new AuditorAgente(1500.0, 5000.0);
        ControlTransaccionesAgente a2 = new ControlTransaccionesAgente();
        ReciboAgente a3 = new ReciboAgente();
        WdkCliService wdkService = new WdkCliService(false);

        SupervisorAgente supervisor = new SupervisorAgente(a1, a2, a3, wdkService);
        supervisor.procesarNominaColaborativa(nomina);
    }
}