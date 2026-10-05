package com.santo_tomas.cuidapp;

import android.os.Bundle;

/**
 * Expedientes médicos.
 *
 * INTENTS EN ESTA CLASE:
 *  - #2 EXPLÍCITO: btnVolverExpedientes -> volverAlMenu() heredado de BaseActivity
 *
 * La pantalla muestra una lista de documentos de ejemplo (definidos en strings.xml).
 */
public class MisExpedientesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_expedientes);
        InsetsUtil.aplicar(this); // evita que el contenido quede bajo las barras del sistema

        configurarBoton(R.id.btnVolverExpedientes, v -> volverAlMenu()); // Intent #2 (BaseActivity)
    }
}