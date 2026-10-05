package com.santo_tomas.cuidapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServicioMonitoreo extends Service {

    private static final String TAG = "ServicioMonitoreo";
    public static final String ACTION_TAQUICARDIA = "com.santo_tomas.cuidapp.TAQUICARDIA";
    public static final String EXTRA_BPM = "BPM";

    private ScheduledExecutorService executorService;
    private final Random random = new Random();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (executorService == null || executorService.isShutdown()) {
            iniciarMonitoreoCardiaco();
        }
        return START_STICKY;
    }

    private void iniciarMonitoreoCardiaco() {
        Log.d(TAG, "Iniciando servicio de monitoreo...");
        // Usar ExecutorService es más seguro y eficiente que Thread.sleep()
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            int bpm = 60 + random.nextInt(71); // Genera entre 60 y 130
            Log.d(TAG, "Lectura del sensor: " + bpm + " BPM");

            if (bpm > 100) {
                enviarAlertaTaquicardia(bpm);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void enviarAlertaTaquicardia(int bpm) {
        // Broadcast nativo en lugar de LocalBroadcastManager (Deprecado)
        Intent broadcastIntent = new Intent(ACTION_TAQUICARDIA);
        broadcastIntent.putExtra(EXTRA_BPM, bpm);
        broadcastIntent.setPackage(getPackageName()); // Restringe el envío a esta app
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Deteniendo servicio de monitoreo...");
        if (executorService != null) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}