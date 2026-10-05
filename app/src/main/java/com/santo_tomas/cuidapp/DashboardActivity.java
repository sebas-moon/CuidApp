package com.santo_tomas.cuidapp;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dashboard de salud.
 *
 * INTENTS EN ESTA CLASE:
 *  - #2 EXPLÍCITO: btnBackToMenu            -> retorno global al Menú Principal
 *  - #3 EXPLÍCITO: iniciarServicioMonitoreo -> lanza ServicioMonitoreo (que corre un Thread)
 *  - EXTRA (NO cuenta entre los 8): compartirReporteNativo() -> ACTION_SEND
 *
 * COMUNICACIÓN CON EL SERVICIO: el Service (hilo secundario) envía Broadcasts; esta Activity
 * los recibe con un BroadcastReceiver (se ejecuta en el hilo principal, así que puede tocar la UI).
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private MaterialButton btnShareReport;
    private MaterialButton btnBackToMenu;
    private BottomNavigationView bottomNavigation;
    private TextView tvBpm;

    private Intent servicioIntent;              // Intent explícito #3 (se reutiliza para detener el servicio)
    private BroadcastReceiver sensorReceiver;
    private boolean receptorRegistrado = false;
    private AlertDialog dialogoAlerta;          // Referencia para no apilar diálogos repetidos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setContentView(R.layout.activity_menu_principal);
        InsetsUtil.aplicar(this); // evita que el contenido quede bajo las barras del sistema

        inicializarVistas();
        configurarListeners();
        configurarReceptorSensor();
        iniciarServicioMonitoreo();
    }

    private void inicializarVistas() {
        btnShareReport = findViewById(R.id.btn_share_report);
        btnBackToMenu = findViewById(R.id.btn_back_to_menu);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        tvBpm = findViewById(R.id.tv_bpm);

        // Validación de nulos: evita NullPointerException si un ID no existe en el layout
        if (btnShareReport == null || btnBackToMenu == null
                || bottomNavigation == null || tvBpm == null) {
            mostrarMensaje(R.string.err_ui_init);
        }
        if (bottomNavigation != null) {
            // Se marca "Salud" ANTES de asignar el listener para que no se dispare al iniciar
            bottomNavigation.setSelectedItemId(R.id.nav_health);
        }
    }

    private void configurarListeners() {
        if (btnBackToMenu != null) {
            btnBackToMenu.setOnClickListener(v -> volverAlMenu());
        }
        if (btnShareReport != null) {
            btnShareReport.setOnClickListener(v -> compartirReporteNativo());
        }
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_health) {
                    return true; // Ya estamos en Salud
                }
                // Las demás pestañas aún no existen: se avisa y NO se cambia la selección
                mostrarMensaje(R.string.msg_vista_desarrollo);
                return false;
            });
        }
    }

    // =====================================================================
    // INTENT #3 (EXPLÍCITO): lanza el servicio de monitoreo cardíaco.
    // Se guarda en un campo para poder detener el mismo servicio en onDestroy().
    // =====================================================================
    private void iniciarServicioMonitoreo() {
        servicioIntent = new Intent(this, ServicioMonitoreo.class);
        try {
            startService(servicioIntent);
        } catch (IllegalStateException e) {
            // Android 8+ puede rechazar servicios si la app no está en primer plano
            Log.e(TAG, "No se pudo iniciar el servicio de monitoreo", e);
        }
    }

    // =====================================================================
    // INTENT #2 (EXPLÍCITO): Retorno global al Menú Principal.
    // =====================================================================
    private void volverAlMenu() {
        Intent intentMenu = new Intent(this, MenuPrincipalActivity.class);
        intentMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentMenu);
        finish();
    }

    // =====================================================================
    // EXTRA (IMPLÍCITO, fuera de la rúbrica): compartir reporte con ACTION_SEND.
    // =====================================================================
    private void compartirReporteNativo() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_report_text));
        try {
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
        } catch (ActivityNotFoundException e) {
            mostrarMensaje(R.string.err_no_app);
        }
    }

    // ---------------------- Recepción de alertas del servicio ----------------------

    private void configurarReceptorSensor() {
        sensorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Validaciones de nulos: intent y acción pueden venir vacíos
                if (intent == null || intent.getAction() == null) return;

                int bpm = intent.getIntExtra(ServicioMonitoreo.EXTRA_BPM, -1);
                if (bpm < 0) return; // dato inválido

                if (ServicioMonitoreo.ACTION_LECTURA.equals(intent.getAction())) {
                    actualizarBpm(bpm);
                } else if (ServicioMonitoreo.ACTION_TAQUICARDIA.equals(intent.getAction())) {
                    mostrarAlertaTaquicardia(bpm);
                }
            }
        };
    }

    /** Muestra el BPM actual; en rojo si supera el umbral de taquicardia. */
    private void actualizarBpm(int bpm) {
        if (tvBpm == null) return;
        tvBpm.setText(getString(R.string.bpm_lectura, bpm));
        int color = (bpm > ServicioMonitoreo.UMBRAL_TAQUICARDIA)
                ? R.color.accent_move : R.color.accent_exercise;
        tvBpm.setTextColor(ContextCompat.getColor(this, color));
    }

    /** Si ya hay un diálogo abierto, solo se actualiza su texto (evita apilar diálogos cada 3 s). */
    private void mostrarAlertaTaquicardia(int bpm) {
        if (isFinishing() || isDestroyed()) return;

        String mensaje = getString(R.string.alerta_taquicardia_mensaje, bpm);
        if (dialogoAlerta != null && dialogoAlerta.isShowing()) {
            dialogoAlerta.setMessage(mensaje);
            return;
        }
        dialogoAlerta = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.alerta_titulo)
                .setMessage(mensaje)
                .setPositiveButton(R.string.alerta_btn_entendido, null)
                .show();
    }

    // ---------------------- Ciclo de vida ----------------------

    @Override
    protected void onResume() {
        super.onResume();
        if (!receptorRegistrado && sensorReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ServicioMonitoreo.ACTION_LECTURA);
            filter.addAction(ServicioMonitoreo.ACTION_TAQUICARDIA);
            // RECEIVER_NOT_EXPORTED: solo acepta broadcasts de esta misma app (obligatorio en Android 13+).
            // ContextCompat ya resuelve la diferencia entre versiones, no hace falta if por SDK.
            ContextCompat.registerReceiver(this, sensorReceiver, filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED);
            receptorRegistrado = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receptorRegistrado) {
            unregisterReceiver(sensorReceiver);
            receptorRegistrado = false;
        }
        // Evita "window leaked" si la pantalla se cierra con el diálogo abierto
        if (dialogoAlerta != null && dialogoAlerta.isShowing()) {
            dialogoAlerta.dismiss();
        }
        dialogoAlerta = null;
    }

    @Override
    protected void onDestroy() {
        // Solo se detiene el servicio si el usuario realmente sale (no en una rotación de pantalla)
        if (isFinishing() && servicioIntent != null) {
            stopService(servicioIntent);
        }
        super.onDestroy();
    }

    private void mostrarMensaje(@StringRes int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}