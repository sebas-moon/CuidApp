package com.santo_tomas.cuidapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase base de las 4 pantallas. Reúne el código que antes estaba repetido en cada Activity:
 *  - configurarBoton(): asigna un click validando que la vista exista (evita NullPointerException)
 *  - iniciarSeguro():   lanza un intent implícito sin crashear si no hay app compatible
 *  - mostrarMensaje():  Toast a partir de un recurso de strings.xml
 *  - volverAlMenu():    INTENT #2 (EXPLÍCITO)
 *
 * La arquitectura sigue siendo plana: es solo herencia simple, sin MVC ni MVVM.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Asigna el listener solo si el botón existe en el layout; si no, lo registra en el Log. */
    protected void configurarBoton(@IdRes int id, View.OnClickListener accion) {
        View boton = findViewById(id);
        if (boton == null) {
            Log.e(getClass().getSimpleName(),
                    "No se encontró el botón: " + getResources().getResourceEntryName(id));
            return;
        }
        boton.setOnClickListener(accion);
    }

    /** Lanza un intent implícito. Si no existe app que lo atienda, avisa en vez de cerrar la app. */
    protected void iniciarSeguro(Intent intent) {
        if (intent == null) return; // validación de nulo
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            mostrarMensaje(R.string.err_no_app);
        }
    }

    protected void mostrarMensaje(@StringRes int mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    // =====================================================================
    // INTENT #2 (EXPLÍCITO): Retorno global al Menú Principal.
    // Lo heredan Ficha, Expedientes y Dashboard. "Explícito" = se indica la clase destino.
    // CLEAR_TOP: si el Menú ya está en la pila, cierra lo que esté encima y lo reutiliza.
    // SINGLE_TOP: evita crear una segunda copia del Menú.
    // =====================================================================
    protected void volverAlMenu() {
        Intent intentMenu = new Intent(this, MenuPrincipalActivity.class);
        intentMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentMenu);
        finish();
    }
}