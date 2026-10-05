package com.tuempresa.appsalud;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;

public class FichaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha);

        // Llamada (Intent Implícito)
        findViewById(R.id.btnLlamar).setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:131"));
            startActivity(callIntent);
        });

        // Correo (Intent Implícito)
        findViewById(R.id.btnCorreo).setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:cuidador@correo.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "🚨 Reporte de Paciente");
            startActivity(emailIntent);
        });

        // Cámara (Intent Implícito)
        findViewById(R.id.btnFoto).setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(cameraIntent);
        });

        // Volver (Intent Explícito)
        findViewById(R.id.btnVolverFicha).setOnClickListener(v -> finish());
    }
}