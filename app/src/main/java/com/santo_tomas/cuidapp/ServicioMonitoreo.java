package com.santo_tomas.cuidapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * Servicio en segundo plano que SIMULA un sensor de ritmo cardíaco.
 * Es lanzado por DashboardActivity (Intent explícito #3).
 *
 * Un Service corre en el hilo principal, por eso el trabajo repetitivo (espera de 3 s)
 * va en un Thread aparte: si no, congelaría la interfaz (ANR).
 *
 * Cada 3 segundos:
 *  - genera un BPM aleatorio entre 60 y 130,
 *  - envía un Broadcast ACTION_LECTURA (para mostrar el valor en pantalla),
 *  - si el BPM supera 100 (taquicardia), envía además ACTION_TAQUICARDIA (para la alerta).
 */
public class ServicioMonitoreo extends Service {

    private static final String TAG = "ServicioMonitoreo";

    // Acciones y extras de los Broadcasts (constantes públicas para usarlas desde la Activity)
    public static final String ACTION_LECTURA = "com.santo_tomas.cuidapp.LECTURA";
    public static final String ACTION_TAQUICARDIA = "com.santo_tomas.cuidapp.TAQUICARDIA";
    public static final String EXTRA_BPM = "BPM";

    public static final int UMBRAL_TAQUICARDIA = 100; // BPM sobre este valor = alerta
    private static final long INTERVALO_MS = 3000;    // 3 segundos entre lecturas

    // volatile: el valor lo escribe el hilo principal y lo lee el hilo secundario;
    // sin volatile el hilo podría no "ver" el cambio nunca.
    private volatile boolean isRunning = false;
    private Thread hiloMonitoreo;
    private final Random random = new Random();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Se puede llamar varias veces (ej. rotación de pantalla): solo se crea un hilo
        if (!isRunning) {
            iniciarMonitoreoCardiaco();
        }
        // START_NOT_STICKY: si el sistema mata el servicio, no se reinicia solo
        // (la simulación solo tiene sentido mientras el Dashboard está abierto).
        return START_NOT_STICKY;
    }

    private void iniciarMonitoreoCardiaco() {
        isRunning = true;
        Log.d(TAG, "Iniciando servicio de monitoreo con Thread...");

        // THREAD de la rúbrica: bucle con pausa que simula las lecturas del sensor
        hiloMonitoreo = new Thread(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(INTERVALO_MS);
                } catch (InterruptedException e) {
                    // onDestroy() nos interrumpió: restauramos la bandera y SALIMOS del bucle
                    // (sin este break, el hilo giraría sin pausa consumiendo CPU)
                    Thread.currentThread().interrupt();
                    break;
                }

                int bpm = 60 + random.nextInt(71); // 60..130
                Log.d(TAG, "Lectura del sensor: " + bpm + " BPM");

                enviarBroadcast(ACTION_LECTURA, bpm);
                if (bpm > UMBRAL_TAQUICARDIA) {
                    enviarBroadcast(ACTION_TAQUICARDIA, bpm);
                }
            }
            Log.d(TAG, "Hilo de monitoreo finalizado");
        }, "hilo-monitoreo-cardiaco");

        hiloMonitoreo.start();
    }

    /** Envía un broadcast restringido a esta app (setPackage) para que ninguna otra app lo reciba. */
    private void enviarBroadcast(String accion, int bpm) {
        Intent broadcastIntent = new Intent(accion);
        broadcastIntent.putExtra(EXTRA_BPM, bpm);
        broadcastIntent.setPackage(getPackageName());
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Deteniendo servicio de monitoreo...");
        isRunning = false;
        if (hiloMonitoreo != null) {
            hiloMonitoreo.interrupt(); // despierta el sleep para que el hilo termine al instante
            hiloMonitoreo = null;
        }
        super.onDestroy();
    }

    /** Servicio "iniciado" (startService), no enlazado: no se usa bind. */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}