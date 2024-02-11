package io.xrystalll.playdesu.data

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color

data class GameModel(
    val id: String,
    val displayName: String,
    val color: String,
    val description: String,
    val backdrop: String,
    val poster: String,
    val file: String,
    val studio: String,
    val gameSystem: String,
    val releaseYear: String,
    val genre: String,
    val price: Int,
    val downloads: Int,
    val rating: Int,
    val size: Int,
    val screenshots: ArrayList<String>,
) {
    fun getColor(): Color {
        val intColor = parseColor(color)
        return Color(intColor)
    }
}
