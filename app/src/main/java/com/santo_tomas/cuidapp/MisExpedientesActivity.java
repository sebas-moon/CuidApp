package com.tuempresa.appsalud;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ExpedientesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expedientes);

        // Intent implícito extra: Abrir explorador de archivos
        findViewById(R.id.btnSubirDoc).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivity(intent);
        });

        // Volver
        findViewById(R.id.btnVolverExpedientes).setOnClickListener(v -> finish());
    }
}