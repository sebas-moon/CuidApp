package com.tuempresa.appsalud;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuPrincipalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Intents Explícitos (Navegación)
        findViewById(R.id.btnFicha).setOnClickListener(v -> {
            startActivity(new Intent(this, FichaActivity.class));
        });

        findViewById(R.id.btnExpedientes).setOnClickListener(v -> {
            startActivity(new Intent(this, ExpedientesActivity.class));
        });

        findViewById(R.id.btnMonitoreo).setOnClickListener(v -> {
            startActivity(new Intent(this, MonitoreoActivity.class));
        });

        // 2. Intents Implícitos (Alessy)
        findViewById(R.id.btnHospital).setOnClickListener(v -> {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=hospital"));
            startActivity(mapIntent);
        });

        findViewById(R.id.btnWeb).setOnClickListener(v -> {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.minsal.cl"));
            startActivity(webIntent);
        });
    }
}