package com.santo_tomas.cuidapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
 * Ficha del paciente.
 *
 * INTENTS EN ESTA CLASE:
 *  - #2 EXPLÍCITO: btnVolverFicha  -> retorno global al Menú Principal
 *  - #4 IMPLÍCITO: llamarEmergencia() -> ACTION_DIAL
 *  - #5 IMPLÍCITO: enviarCorreo()     -> ACTION_SENDTO (mailto:)
 *  - #6 IMPLÍCITO: abrirCamara()      -> ACTION_IMAGE_CAPTURE
 *
 * PERMISO EN TIEMPO DE EJECUCIÓN: CAMERA.
 * Importante: si el Manifest declara CAMERA y la app NO lo tiene concedido, lanzar
 * ACTION_IMAGE_CAPTURE provoca SecurityException. Por eso se verifica antes.
 */
public class FichaPacienteActivity extends AppCompatActivity {

    private static final String TAG = "FichaPaciente";

    /** Respuesta del diálogo de permiso de cámara: si lo aceptó, abre la cámara de inmediato. */
    private final ActivityResultLauncher<String> solicitarPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    abrirCamara();
                } else {
                    mostrarMensaje(R.string.msg_camara_denegada);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_paciente);

        setContentView(R.layout.activity_menu_principal);
        InsetsUtil.aplicar(this); // evita que el contenido quede bajo las barras del sistema

        configurarBoton(R.id.btnLlamar, v -> llamarEmergencia());
        configurarBoton(R.id.btnCorreo, v -> enviarCorreo());
        configurarBoton(R.id.btnFoto, v -> tomarFoto());
        configurarBoton(R.id.btnVolverFicha, v -> volverAlMenu());
    }

    private void configurarBoton(@IdRes int id, View.OnClickListener accion) {
        View boton = findViewById(id);
        if (boton == null) {
            Log.e(TAG, "No se encontró el botón: " + getResources().getResourceEntryName(id));
            return;
        }
        boton.setOnClickListener(accion);
    }

    // =====================================================================
    // INTENT #4 (IMPLÍCITO): Llamada.
    // ACTION_DIAL solo abre el marcador con el número escrito: no requiere permiso CALL_PHONE.
    // =====================================================================
    private void llamarEmergencia() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL,
                Uri.parse(getString(R.string.intent_tel_emergencia)));
        iniciarSeguro(callIntent);
    }

    // =====================================================================
    // INTENT #5 (IMPLÍCITO): Correo.
    // ACTION_SENDTO + mailto: filtra para que solo respondan clientes de correo reales.
    // Si no hay ninguno instalado, iniciarSeguro() muestra un aviso en vez de crashear.
    // =====================================================================
    private void enviarCorreo() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse(getString(R.string.intent_mailto_cuidador)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        iniciarSeguro(emailIntent);
    }

    // =====================================================================
    // INTENT #6 (IMPLÍCITO): Cámara.  Primero se valida hardware y permiso.
    // =====================================================================
    private void tomarFoto() {
        // Validación 1: ¿el equipo tiene cámara? (el Manifest la declara como no obligatoria)
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            mostrarMensaje(R.string.err_sin_camara);
            return;
        }
        // Validación 2: ¿tenemos el permiso en tiempo de ejecución?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            solicitarPermisoCamara.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        iniciarSeguro(cameraIntent);
    }

    // =====================================================================
    // INTENT #2 (EXPLÍCITO): Retorno global al Menú Principal.
    // CLEAR_TOP: si el Menú ya está en la pila, cierra todo lo que esté encima de él
    // y lo reutiliza, evitando duplicados. SINGLE_TOP evita recrearlo.
    // =====================================================================
    private void volverAlMenu() {
        Intent intentMenu = new Intent(this, MenuPrincipalActivity.class);
        intentMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentMenu);
        finish();
    }

    // ------------------------- Utilidades -------------------------

    /** Lanza un intent implícito sin crashear si no hay app compatible. */
    private void iniciarSeguro(Intent intent) {
        if (intent == null) return; // validación de nulo
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            mostrarMensaje(R.string.err_no_app);
        }
    }

    private void mostrarMensaje(@StringRes int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}