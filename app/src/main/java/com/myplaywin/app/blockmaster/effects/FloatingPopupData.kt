package com.myplaywin.app.blockmaster.effects

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class FloatingPopupData(
    val id: Long = System.currentTimeMillis() + Random.nextLong(1000000),
    val text: String,
    val subText: String? = null,
    val color: Color = Color(0xFF00E5FF),
    val creationTime: Long = System.currentTimeMillis()
)
