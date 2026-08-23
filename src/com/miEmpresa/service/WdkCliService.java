package com.miEmpresa.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WdkCliService {

    private static final String RED_BLOCKCHAIN = "sepolia";
    private static final String WALLET_NAME = "treasury3";
    private static final String TOKEN_SIMBOLO = "usdt";
    private static final String TTL_MINUTOS = "10"; // TTL ajustado a minutos por seguridad
    private boolean modoSimulacion = true;

    public WdkCliService(boolean modoSimulacion) {
        this.modoSimulacion = modoSimulacion;
    }

    private List<String> crearComandoBase() {
        boolean esWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String userHome = System.getProperty("user.home");
        String wdkExecutable = esWindows ? userHome + "\\AppData\\Roaming\\npm\\wdk.cmd" : "wdk";

        List<String> comando = new ArrayList<>();
        comando.add(wdkExecutable);
        return comando;
    }

    public boolean desbloquearWallet() {
        if (modoSimulacion) {
            ManejadorFallos.registrarDecision("WdkCliService", "MOCK_UNLOCK", "Wallet desbloqueada en modo simulacion");
            return true;
        }

        String passphrase = System.getenv("WDK_PASSPHRASE");
        if (passphrase == null || passphrase.trim().isEmpty()) {
            ManejadorFallos.registrarFallo("WdkCliService", "ERROR CRÍTICO: La variable WDK_PASSPHRASE no está configurada en el entorno.");
            return false;
        }

        List<String> comando = crearComandoBase();
        comando.add("wallet");
        comando.add("unlock");
        comando.add("--name");
        comando.add(WALLET_NAME);
        comando.add("--ttl");
        comando.add(TTL_MINUTOS);
        comando.add("--json");

        String respuesta = ejecutarComandoSistema(comando, "UNLOCK", passphrase);
        System.out.println("🔍 DEBUG WDK UNLOCK RESPONSE: " + respuesta);
        return !respuesta.isEmpty() && !respuesta.toLowerCase().contains("error");
    }

    public boolean bloquearWallet() {
        if (modoSimulacion) {
            ManejadorFallos.registrarDecision("WdkCliService", "MOCK_LOCK", "Wallet bloqueada en modo simulacion");
            return true;
        }

        List<String> comando = crearComandoBase();
        comando.add("wallet");
        comando.add("lock");
        comando.add("--name");
        comando.add(WALLET_NAME);
        comando.add("--json");

        String respuesta = ejecutarComandoSistema(comando, "LOCK", null);
        return !respuesta.isEmpty() && !respuesta.toLowerCase().contains("error");
    }

    public String ejecutarPagoUsdt(String direccionWallet, double montoUSDT) {
        if (modoSimulacion) {
            String fakeTxHash = "0x" + Long.toHexString(Double.doubleToLongBits(Math.random())) + Long.toHexString(System.currentTimeMillis());
            return String.format(java.util.Locale.US,
                    "{\"status\":\"success\",\"network\":\"%s\",\"to\":\"%s\",\"amount\":%.2f,\"feeFormatted\":\"0.0015 ETH\",\"txHash\":\"%s\"}",
                    RED_BLOCKCHAIN, direccionWallet, montoUSDT, fakeTxHash);
        }

        List<String> comando = crearComandoBase();
        comando.add("send");
        comando.add("--wallet");
        comando.add(WALLET_NAME);
        comando.add("--token");
        comando.add(TOKEN_SIMBOLO);
        comando.add("--to");
        comando.add(direccionWallet);
        comando.add("--amount");
        comando.add(String.format(java.util.Locale.US, "%.2f", montoUSDT));
        comando.add("--network");
        comando.add(RED_BLOCKCHAIN);
        comando.add("--json");

        String respuesta = ejecutarComandoSistema(comando, "SEND", null);
        System.out.println("🔍 DEBUG WDK SEND RESPONSE: " + respuesta); // <--- AGREGAR AQUÍ
        return respuesta;
    }

    private String ejecutarComandoSistema(List<String> comando, String operacion, String passphrase) {
        ProcessBuilder processBuilder = new ProcessBuilder(comando);
        processBuilder.redirectErrorStream(true);

        if (passphrase != null && !passphrase.isEmpty()) {
            Map<String, String> env = processBuilder.environment();
            env.put("WDK_PASSPHRASE", passphrase);
        }

        StringBuilder resultadoOutput = new StringBuilder();
        try {
            Process proceso = processBuilder.start();
            BufferedReader lector = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = lector.readLine()) != null) {
                resultadoOutput.append(linea).append("\n");
            }

            boolean finalizado = proceso.waitFor(10, TimeUnit.SECONDS);
            if (!finalizado) {
                proceso.destroyForcibly();
                ManejadorFallos.registrarFallo("WdkCliService", "TIMEOUT en operacion: " + operacion);
                return "{\"error\": \"TIMEOUT\"}";
            }

        } catch (IOException | InterruptedException e) {
            ManejadorFallos.registrarFallo("WdkCliService", "Error de ejecucion ProcessBuilder: " + e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }

        return resultadoOutput.toString().trim();
    }
}