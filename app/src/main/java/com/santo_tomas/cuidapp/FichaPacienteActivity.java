package com.santo_tomas.cuidapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

/**
 * Ficha del paciente.
 *
 * INTENTS EN ESTA CLASE:
 *  - #2 EXPLÍCITO: btnVolverFicha     -> volverAlMenu() heredado de BaseActivity
 *  - #4 IMPLÍCITO: llamarEmergencia() -> ACTION_DIAL
 *  - #5 IMPLÍCITO: enviarCorreo()     -> ACTION_SENDTO (mailto:)
 *  - #6 IMPLÍCITO: abrirCamara()      -> ACTION_IMAGE_CAPTURE (la foto vuelve y se muestra en pantalla)
 *
 * PERMISO EN TIEMPO DE EJECUCIÓN: CAMERA.
 * Si el Manifest declara CAMERA y la app NO lo tiene concedido, lanzar ACTION_IMAGE_CAPTURE
 * provoca SecurityException. Por eso se verifica antes de abrir la cámara.
 */
public class FichaPacienteActivity extends BaseActivity {

    private ImageView ivFoto;

    /** Respuesta del diálogo de permiso de cámara: si lo aceptó, abre la cámara de inmediato. */
    private final ActivityResultLauncher<String> solicitarPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    abrirCamara();
                } else {
                    mostrarMensaje(R.string.msg_camara_denegada);
                }
            });

    /** Resultado de la cámara: llega la miniatura de la foto en el extra "data". */
    private final ActivityResultLauncher<Intent> lanzadorCamara =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                // Validación 1: el usuario pudo cancelar la captura
                if (resultado.getResultCode() != RESULT_OK) {
                    mostrarMensaje(R.string.msg_foto_cancelada);
                    return;
                }
                Bitmap miniatura = extraerMiniatura(resultado.getData());

                // Validación 2: la cámara puede devolver el resultado sin imagen
                if (miniatura == null || ivFoto == null) {
                    mostrarMensaje(R.string.err_foto);
                    return;
                }
                ivFoto.setImageBitmap(miniatura);
                ivFoto.setVisibility(View.VISIBLE);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_paciente);
        InsetsUtil.aplicar(this); // evita que el contenido quede bajo las barras del sistema

        ivFoto = findViewById(R.id.iv_foto_perfil);

        configurarBoton(R.id.btnLlamar, v -> llamarEmergencia());
        configurarBoton(R.id.btnCorreo, v -> enviarCorreo());
        configurarBoton(R.id.btnFoto, v -> tomarFoto());
        configurarBoton(R.id.btnVolverFicha, v -> volverAlMenu()); // Intent #2 (BaseActivity)
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
    // INTENT #6 (IMPLÍCITO): Cámara. Primero se valida hardware y permiso.
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

    /** Lanza la cámara esperando resultado. Si no hay app de cámara, avisa en vez de crashear. */
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            lanzadorCamara.launch(cameraIntent);
        } catch (ActivityNotFoundException e) {
            mostrarMensaje(R.string.err_no_app);
        }
    }

    /**
     * Saca la miniatura (Bitmap) del resultado de la cámara, o null si no viene.
     * MEJORA: Se evita el uso exclusivo del método deprecated, bifurcando
     * según la versión del sistema operativo (API 33+).
     */
    @SuppressWarnings("deprecation")
    private Bitmap extraerMiniatura(Intent datos) {
        if (datos == null || datos.getExtras() == null) return null;

        Bitmap miniatura = null;

        // A partir de Android 13 (Tiramisu, API 33), se debe usar el método con clase explícita
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            miniatura = datos.getExtras().getParcelable("data", Bitmap.class);
        } else {
            // Para versiones anteriores, mantenemos la lógica clásica
            Object dato = datos.getExtras().get("data");
            if (dato instanceof Bitmap) {
                miniatura = (Bitmap) dato;
            }
        }

        return miniatura;
    }
}