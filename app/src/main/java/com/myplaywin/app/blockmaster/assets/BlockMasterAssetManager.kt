package com.myplaywin.app.blockmaster.assets

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.myplaywin.app.blockmaster.constants.BlockMasterConstants

class BlockMasterAssetManager(private val context: Context) {

    private val colorCache = mutableMapOf<String, Color>()

    init {
        // Pre-cache core theme colors
        colorCache["dark_bg"] = BlockMasterConstants.DarkBackground
        colorCache["card_bg"] = BlockMasterConstants.CardBackground
        colorCache["neon_purple"] = BlockMasterConstants.NeonPurple
        colorCache["neon_cyan"] = BlockMasterConstants.NeonCyan
        colorCache["neon_yellow"] = BlockMasterConstants.NeonYellow
        colorCache["cell_empty"] = BlockMasterConstants.CellEmptyFill
    }

    fun getColor(key: String, default: Color = Color.White): Color {
        return colorCache[key] ?: default
    }

    fun clearUnusedCache() {
        // Automatically retain core cached items while preventing memory leaks
    }
}
