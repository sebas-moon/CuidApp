package com.santo_tomas.cuidapp;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Evita que el contenido quede debajo de la barra de estado y la de navegación.
 *
 * Desde Android 15 (targetSdk 35+) las apps se dibujan "de borde a borde" (edge-to-edge).
 * Esta utilidad lee el tamaño de las barras del sistema y lo aplica como padding
 * al contenedor raíz de la pantalla.
 *
 * Un problema que sale de tu targetSdk = 36
 * Desde Android 15, las apps con targetSdk 35 o superior se dibujan debajo de la barra de estado
 * y de la barra de navegación. En Android 16 ya no se puede desactivar.
 * En tu app el título del Dashboard quedaría tapado por la barra de estado,
 * y el BottomNavigationView por los botones del sistema.
 * La solución es una clase pequeña que aplica el espacio de las barras como padding.
 */
public final class InsetsUtil {

    private InsetsUtil() { } // Clase de utilidades: no se instancia

    /** Llamar justo después de setContentView() en cada Activity. */
    public static void aplicar(Activity activity) {
        View raiz = activity.findViewById(android.R.id.content);
        if (raiz == null) return; // validación de nulo

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}