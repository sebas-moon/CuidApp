package com.santo_tomas.cuidapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class DashboardActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private MaterialButton btnShareReport;
    private MaterialButton btnBackToMenu;
    private MaterialButton actionWorkouts;
    private MaterialButton actionStayFit;
    private BottomNavigationView bottomNavigation;

    private BroadcastReceiver taquicardiaReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        inicializarVistas();
        verificarPermisos();
        configurarListeners();
        configurarReceptorSensor();

        // Iniciar servicio en segundo plano
        Intent serviceIntent = new Intent(this, ServicioMonitoreo.class);
        startService(serviceIntent);
    }

    private void inicializarVistas() {
        btnShareReport = findViewById(R.id.btn_share_report);
        btnBackToMenu = findViewById(R.id.btn_back_to_menu);
        actionWorkouts = findViewById(R.id.action_workouts_container);
        actionStayFit = findViewById(R.id.action_stay_fit_container);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (btnShareReport == null || bottomNavigation == null) {
            Toast.makeText(this, "Error de inicialización de UI.", Toast.LENGTH_LONG).show();
        }
    }

    private void configurarListeners() {
        // Lambdas para un código más limpio y legible
        btnBackToMenu.setOnClickListener(v -> {
            Intent intentMenu = new Intent(this, MenuPrincipalActivity.class);
            intentMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentMenu);
            finish();
        });

        btnShareReport.setOnClickListener(v -> compartirReporteNativo());

        actionWorkouts.setOnClickListener(v -> mostrarMensaje("Módulo Workouts (Próximamente)"));
        actionStayFit.setOnClickListener(v -> mostrarMensaje("Módulo Stay Fit (Próximamente)"));

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_health) {
                mostrarMensaje("Ya estás en Health");
                return true;
            } else if (itemId == R.id.nav_exercise || itemId == R.id.nav_discover || itemId == R.id.nav_devices) {
                mostrarMensaje("Vista en desarrollo");
                return true;
            }
            return false;
        });
    }

    private void compartirReporteNativo() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_report_text));
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartir reporte de salud");
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        } else {
            mostrarMensaje("No hay aplicaciones disponibles para compartir.");
        }
    }

    private void configurarReceptorSensor() {
        taquicardiaReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ServicioMonitoreo.ACTION_TAQUICARDIA.equals(intent.getAction())) {
                    int bpm = intent.getIntExtra(ServicioMonitoreo.EXTRA_BPM, 0);
                    mostrarAlertaTaquicardia(bpm);
                }
            }
        };
    }

    private void mostrarAlertaTaquicardia(int bpm) {
        // Aseguramos que el diálogo no intente abrirse si la Activity está finalizando
        if (!isFinishing()) {
            new AlertDialog.Builder(this, R.style.Theme_Material3_DayNight_Dialog_Alert)
                    .setTitle("⚠ Alerta de Salud")
                    .setMessage("Se ha detectado un ritmo cardíaco inusualmente alto: " + bpm + " BPM.")
                    .setPositiveButton("Entendido", null)
                    .show();
        }
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Modern Android Security: Declaramos que el receiver no se exporta a otras apps
        IntentFilter filter = new IntentFilter(ServicioMonitoreo.ACTION_TAQUICARDIA);
        ContextCompat.registerReceiver(this, taquicardiaReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (taquicardiaReceiver != null) {
            unregisterReceiver(taquicardiaReceiver);
        }
    }

    private void verificarPermisos() {
        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
        boolean necesitaPermisos = false;
        for (String p : permisos) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                necesitaPermisos = true;
                break;
            }
        }
        if (necesitaPermisos) {
            ActivityCompat.requestPermissions(this, permisos, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarMensaje("Permisos concedidos.");
            } else {
                mostrarMensaje("Funcionalidades limitadas por falta de permisos.");
            }
        }
    }
}