package io.xrystalll.playdesu.util

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SystemUIHelper(window: Window) {
    private val _window = window
    private val controller = WindowInsetsControllerCompat(window, window.decorView)

    fun hideStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(_window, false)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun showStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(_window, false)
        controller.show(WindowInsetsCompat.Type.statusBars())
    }

    fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(_window, false)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(_window, false)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
