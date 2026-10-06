package com.santo_tomas.cuidapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

/**
 * Pantalla de Expedientes Médicos.
 * Permite seleccionar un documento PDF mediante ActivityResultContracts.GetContent().
 */
public class MisExpedientesActivity extends BaseActivity {

    private TextView tvDocumento;

    // Selector moderno para abrir archivos PDF
    private final ActivityResultLauncher<String> selectorPdf =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    tvDocumento.setText(obtenerNombre(uri));
                } else {
                    Toast.makeText(this, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_expedientes);
        InsetsUtil.aplicar(this);

        tvDocumento = findViewById(R.id.tv_documento_seleccionado);

        // Configuración de botones
        configurarBoton(R.id.btnSubirDoc, v -> selectorPdf.launch("application/pdf"));
        configurarBoton(R.id.btnVolverExpedientes, v -> volverAlMenu());
    }

    // Obtiene el nombre real del archivo seleccionado
    private String obtenerNombre(Uri uri) {
        String nombre = uri.getLastPathSegment();
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) nombre = c.getString(i);
            }
        } catch (Exception ignored) {}
        return nombre;
    }
}