package com.example.examencierre.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Aplica los insets de las barras del sistema para soportar el modo edge-to-edge.
 */
fun View.applySystemBarInsets(
    applyTop: Boolean = true,
    applyBottom: Boolean = true
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(
            left = bars.left,
            top = if (applyTop) bars.top else view.paddingTop,
            right = bars.right,
            bottom = if (applyBottom) bars.bottom else view.paddingBottom
        )
        insets
    }
}
