package com.santo_tomas.cuidapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Pantalla principal (Launcher).
 *
 * INTENTS EN ESTA CLASE:
 *  - #1 EXPLÍCITO: navegarA()            -> navegación hacia Ficha, Expedientes y Dashboard
 *  - #7 IMPLÍCITO: abrirMapaHospitales() -> ACTION_VIEW con esquema geo: (app de mapas)
 *  - #8 IMPLÍCITO: abrirPortalMinsal()   -> ACTION_VIEW con https: (navegador web)
 *
 * PERMISO EN TIEMPO DE EJECUCIÓN: ubicación (se pide al tocar "Buscar Hospital Cercano").
 */
public class MenuPrincipalActivity extends AppCompatActivity {

    private static final String TAG = "MenuPrincipal";

    /**
     * Launcher moderno (Activity Result API) para pedir permisos.
     * Debe declararse como campo (o en onCreate) ANTES de que la Activity llegue a STARTED.
     * Cuando el usuario responde el diálogo del sistema, se ejecuta este callback.
     */
    private final ActivityResultLauncher<String[]> solicitarPermisosUbicacion =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                if (!tienePermisoUbicacion()) {
                    // Si lo rechazó, la app no se rompe: abre el mapa igual, pero sin usar su posición
                    mostrarMensaje(R.string.msg_ubicacion_denegada);
                }
                abrirMapaHospitales();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        setContentView(R.layout.activity_menu_principal);
        InsetsUtil.aplicar(this); // evita que el contenido quede bajo las barras del sistema

        // --- Navegación interna (Intent explícito #1, un único punto de código) ---
        configurarBoton(R.id.btnFicha, v -> navegarA(FichaPacienteActivity.class));
        configurarBoton(R.id.btnExpedientes, v -> navegarA(MisExpedientesActivity.class));
        configurarBoton(R.id.btnMonitoreo, v -> navegarA(DashboardActivity.class));

        // --- Servicios externos (Intents implícitos #7 y #8) ---
        configurarBoton(R.id.btnHospital, v -> buscarHospitalCercano());
        configurarBoton(R.id.btnWeb, v -> abrirPortalMinsal());
    }

    /**
     * Asigna un listener validando que la vista exista (evita NullPointerException
     * si el ID no está en el layout).
     */
    private void configurarBoton(@IdRes int id, View.OnClickListener accion) {
        View boton = findViewById(id);
        if (boton == null) {
            Log.e(TAG, "No se encontró el botón: " + getResources().getResourceEntryName(id));
            return;
        }
        boton.setOnClickListener(accion);
    }

    // =====================================================================
    // INTENT #1 (EXPLÍCITO): navegación entre pantallas de la app.
    // "Explícito" = se indica la clase destino exacta (Activity.class).
    // =====================================================================
    private void navegarA(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        startActivity(intent);
    }

    // =====================================================================
    // INTENT #7 (IMPLÍCITO): Mapas
    // Si hay permiso y ubicación conocida, centra la búsqueda ahí; si no, usa geo:0,0.
    // =====================================================================
    private void buscarHospitalCercano() {
        if (tienePermisoUbicacion()) {
            abrirMapaHospitales();
        } else {
            // Pide FINE y COARSE: el usuario puede conceder solo la aproximada (Android 12+)
            solicitarPermisosUbicacion.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void abrirMapaHospitales() {
        String consulta = Uri.encode(getString(R.string.intent_busqueda_hospital));
        Location ubicacion = obtenerUltimaUbicacion();

        String uri = (ubicacion != null)
                ? "geo:" + ubicacion.getLatitude() + "," + ubicacion.getLongitude() + "?q=" + consulta
                : "geo:0,0?q=" + consulta;

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        iniciarSeguro(mapIntent);
    }

    // =====================================================================
    // INTENT #8 (IMPLÍCITO): Navegador web
    // =====================================================================
    private void abrirPortalMinsal() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.intent_url_minsal)));
        iniciarSeguro(webIntent);
    }

    // ------------------------- Utilidades -------------------------

    /** Lanza un intent implícito sin que la app se caiga si no existe app que lo atienda. */
    private void iniciarSeguro(Intent intent) {
        if (intent == null) return; // validación de nulo
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            mostrarMensaje(R.string.err_no_app);
        }
    }

    /** true si el usuario concedió ubicación precisa O aproximada. */
    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Devuelve la mejor última ubicación conocida del equipo, o null si no hay.
     * @SuppressLint se justifica porque tienePermisoUbicacion() se verifica antes.
     */
    @SuppressLint("MissingPermission")
    private Location obtenerUltimaUbicacion() {
        if (!tienePermisoUbicacion()) return null;

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return null;

        Location mejor = null;
        for (String proveedor : lm.getProviders(true)) {
            try {
                Location l = lm.getLastKnownLocation(proveedor);
                if (l != null && (mejor == null || l.getAccuracy() < mejor.getAccuracy())) {
                    mejor = l;
                }
            } catch (SecurityException e) {
                // Ej.: solo se concedió ubicación aproximada y el proveedor GPS exige precisa
                Log.w(TAG, "Proveedor sin acceso: " + proveedor);
            }
        }
        return mejor;
    }

    private void mostrarMensaje(@StringRes int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}