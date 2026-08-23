# 💼 — WDK Automated Payroll & AI Auditor

Sistema multiagente de liquidación de sueldos empresariales en **USD₮**, con
verificación, scoring de riesgo y bloqueo automático antes de cada transacción,
sobre **Tether WDK (Wallet Development Kit)**.

Construido para el hackathon — **Track 1: dale una wallet a un agente de IA**.
En vez de un único agente ejecutando pagos, el sistema usa 4 agentes con
responsabilidad única que colaboran para decidir si un pago se ejecuta,
bajo topes de gasto, validación cruzada y aprobación manual cuando el riesgo
supera un umbral.

---

## 📌 Permalinks a la integración con WDK

>https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L104
>https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L105
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L107
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L108
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L112
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L114
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L115
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L117
> https://github.com/noeliaporta/IMNOVAR/blob/62e8c3e356e3ebe7bb628219448c8f0670ffacde/src/com/miEmpresa/service/WdkCliService.java#L129

* **Ejecución de comandos WDK CLI (`unlock` / `send` / `lock` vía `ProcessBuilder`):**
  `WdkCliService.java` — métodos `ejecutarComandoSistema` y `crearComandoBase`
* **Desbloqueo/bloqueo de wallet (passphrase por variable de entorno):**
  `WdkCliService.java` — métodos `desbloquearWallet` / `bloquearWallet`
* **Orquestación del flujo completo (unlock → validaciones → send → lock):**
  `SupervisorAgente.java` — método `procesarNominaColaborativa`
* **Scoring de riesgo y validación cruzada de wallet destino:**
  `ControlTransaccionesAgente.java` — métodos `calcularScoringRiesgo` y
  `validarPostTransaccion`
* **Generación de recibos con `txHash` real:**
  `ReciboAgente.java` — método `generarReciboArea`

---

## 🛠️ Paquetes de WDK utilizados

* `@tetherto/wdk-cli` — versión **v1.0.0-beta.3**
* Dependencias principales resueltas por el CLI:
  `@tetherto/wdk-wallet-evm v1.0.0-beta.11`,
  `@tetherto/wdk-wallet-evm-erc-4337 v1.0.0-beta.6`,
  `@tetherto/wdk-utils v1.0.0-beta.11`
* Instalación global: `npm install -g @tetherto/wdk-cli`

---

## 🌐 Red y token

* **Red:** Sepolia Testnet
* **Activo liquidado:** USD₮ — ticker `usdt`, ya registrado de forma nativa
  (built-in) en el CLI de WDK para Sepolia, sin necesidad de `wdk token add`
* **Decimales:** 6
* **Contrato USD₮ en Sepolia:** `0xd077A400968890Eacc75cdc901F0356c943e4fDb`
* **Faucet de USD₮ testnet usado:** Pimlico / Candide (Sepolia) — mismos
  que recomienda la documentación oficial de WDK para fondear wallets de
  prueba

---

## 🤖 Arquitectura: 4 agentes, una responsabilidad cada uno

1. **`AuditorAgente`** — valida formato de wallet EVM, que el monto neto sea
   coherente y mayor a cero, y que la nómina completa no supere el
   presupuesto de tesorería antes de aprobar cualquier lote.
2. **`ControlTransaccionesAgente`** — calcula un **score de riesgo (0-100)**
   por transacción (monto atípico respecto al básico, patrones de wallet,
   proporción de bonos). Si el score cae debajo del umbral, el pago **no se
   ejecuta automáticamente** y queda pendiente de aprobación manual. También
   bloquea wallets duplicadas y doble pago sobre el mismo legajo, y valida
   que la wallet destino confirmada por WDK coincida con la esperada antes
   de generar el recibo.
3. **`SupervisorAgente`** — coordina el flujo completo: desbloquea la wallet
   una sola vez por lote (`wallet unlock`), procesa cada empleado a través
   de los demás agentes, corta el procesamiento restante si hay fallas
   consecutivas (**circuit breaker**), y garantiza el bloqueo de la wallet
   (`wallet lock`) al final, incluso si algo falla a mitad de camino.
4. **`ReciboAgente`** — genera el comprobante `.txt` por área
   (`Empleados/{Area}/recibo_LEGAJO.txt`), con el `txHash` y la comisión de
   red reales devueltos por WDK, sin sobrescribir recibos existentes.

Todo el registro de decisiones (aprobado, rechazado, pendiente de aprobación
manual) y de fallos queda persistido en `decisiones_sistema.log` y
`fallos_sistema.log` — trazabilidad auditable de por qué el sistema tomó
cada decisión, no solo cuándo falló.

---

## 🔐 Modelo de seguridad

* La passphrase de la wallet **nunca se pasa como argumento de línea de
  comandos ni queda hardcodeada** — se lee de la variable de entorno
  `WDK_PASSPHRASE` y se inyecta solo en el entorno del proceso hijo que
  ejecuta el comando de desbloqueo.
* Los comandos a WDK se arman con `ProcessBuilder` y una lista de argumentos
  (sin concatenar un string a un shell), eliminando el riesgo de inyección
  de comandos.
* TTL de desbloqueo corto (10 minutos) — la wallet no queda abierta más
  tiempo del necesario para procesar el lote.
* Timeout de 10 segundos en cada comando, con `destroyForcibly` si no
  responde, para evitar que un colgado del CLI trabe el sistema.
* Scoring de riesgo con umbral de bloqueo automático, cross-check del
  destino real contra el esperado, y presupuesto global de tesorería
  verificado antes de autorizar cualquier lote.

---

## 🔧 Configuración desde un clon limpio

### Requisitos previos

* Java SDK 17+ (o 21+)
* Node.js 18+
* WDK CLI instalado globalmente: `npm install -g @tetherto/wdk-cli`

### 1. Clonar el repositorio

```bash
git clone https://github.com/noeliaporta/wdk-java-payroll.git
cd wdk-java-payroll
```

### 2. Crear y fondear la wallet de tesorería

```bash
wdk wallet create --name treasury3
```

Guardá la passphrase que ingreses — la vas a necesitar en el paso 3.
Fondeá la dirección resultante con ETH (gas) y USD₮ de Sepolia usando los
faucets de Pimlico o Candide mencionados arriba.

```bash
wdk wallet unlock --name treasury3 --ttl 10
wdk get address --network sepolia --wallet treasury3
```

### 3. Configurar variables de entorno

```bash
cp .env.example .env
```

Completar en `.env`:

```
WDK_PASSPHRASE=<la passphrase que definiste en el paso 2>
```

> Esta es la única variable que el sistema lee en tiempo de ejecución
> (`WdkCliService.desbloquearWallet`). La red y el token están fijados en
> el código (`WdkCliService.RED_BLOCKCHAIN` y `TOKEN_SIMBOLO`) para
> mantener la integración mínima, tal como pide la consigna.

### 4. Compilar y ejecutar

```bash
javac -d out $(find src -name "*.java")
java -cp out com.miEmpresa.Main
```

Por defecto corre contra WDK real (`modoSimulacion = false` en `Main.java`).
Para probar sin gastar fondos de testnet, cambiar ese flag a `true`.

> Los montos de la nómina de prueba en `Main.java` están seteados en `0.01`
> USD₮ por empleado para poder demostrar el flujo completo con fondos
> mínimos de faucet.

---

## 🎥 Demo

<video src="assets/video-demo-proyecto-hackathon.mp4" controls></video>
---

## 🚧 Alcance actual y próximos pasos

* La integración es vía CLI (`ProcessBuilder`), no vía `wdk-mcp` — queda
  como siguiente paso natural para exponer estas mismas operaciones a un
  cliente MCP (Claude Desktop, Claude Code) en vez de invocarlas solo desde
  Java.
* No hay todavía un paso de `--dry-run` previo al envío real (previsualizar
  fee y destino antes de transmitir) — se ejecuta el envío directo tras las
  validaciones de los agentes.
* Los límites de presupuesto (`AuditorAgente`) están fijados en el código
  (`Main.java`), no configurados por variable de entorno todavía.
