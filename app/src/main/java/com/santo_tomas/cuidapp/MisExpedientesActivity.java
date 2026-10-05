package com.santo_tomas.cuidapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Expedientes médicos.
 *
 * INTENTS EN ESTA CLASE:
 *  - #2 EXPLÍCITO: btnVolverExpedientes -> retorno global al Menú Principal
 *  - EXTRA (NO cuenta entre los 8 de la rúbrica): selector de archivos PDF con ACTION_GET_CONTENT
 */
public class MisExpedientesActivity extends AppCompatActivity {

    private static final String TAG = "MisExpedientes";

    private TextView tvDocumento;

    /** Recibe el resultado del selector de archivos (el PDF elegido por el usuario). */
    private final ActivityResultLauncher<Intent> selectorDocumento =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                // Si canceló o no hay datos, no se hace nada
                if (resultado.getResultCode() != RESULT_OK || resultado.getData() == null) return;

                Uri uri = resultado.getData().getData();
                String nombre = (uri != null) ? obtenerNombreArchivo(uri) : null;

                if (nombre == null) {
                    mostrarMensaje(R.string.expedientes_sin_archivo);
                } else if (tvDocumento != null) {
                    tvDocumento.setText(getString(R.string.expedientes_seleccionado, nombre));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_expedientes);

        setContentView(R.layout.activity_menu_principal);
        InsetsUtil.aplicar(this); // evita que el contenido quede bajo las barras del sistema

        tvDocumento = findViewById(R.id.tv_documento_seleccionado);

        configurarBoton(R.id.btnSubirDoc, v -> abrirSelectorPdf());
        configurarBoton(R.id.btnVolverExpedientes, v -> volverAlMenu());
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
    // EXTRA (IMPLÍCITO, fuera de la rúbrica): explorador de archivos.
    // CATEGORY_OPENABLE garantiza que el archivo elegido se pueda abrir/leer.
    // =====================================================================
    private void abrirSelectorPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            selectorDocumento.launch(intent);
        } catch (ActivityNotFoundException e) {
            mostrarMensaje(R.string.err_no_app);
        }
    }

    /** Obtiene el nombre visible del archivo desde su Uri (o null si no se puede leer). */
    private String obtenerNombreArchivo(Uri uri) {
        String nombre = null;
        try (Cursor cursor = getContentResolver().query(
                uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) nombre = cursor.getString(idx);
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.w(TAG, "No se pudo leer el nombre del archivo", e);
        }
        return (nombre != null) ? nombre : uri.getLastPathSegment();
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

    private void mostrarMensaje(@StringRes int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}