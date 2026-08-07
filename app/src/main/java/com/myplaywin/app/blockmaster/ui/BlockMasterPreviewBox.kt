package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.blockmaster.blocks.TetrominoBlock

@Composable
fun BlockMasterPreviewBox(
    title: String,
    piece: TetrominoBlock?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF140D26))
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                color = Color(0xFF00E5FF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (piece != null) {
                    Canvas(modifier = Modifier.fillMaxSize(0.9f)) {
                        val matrix = piece.matrix
                        val matrixSize = matrix.size
                        val availableW = size.width
                        val availableH = size.height

                        val cellSize = minOf(availableW / 4f, availableH / 4f)
                        val gap = cellSize * 0.1f
                        val innerSize = cellSize - gap
                        val cornerRadius = cellSize * 0.2f

                        val contentW = matrixSize * cellSize
                        val contentH = matrixSize * cellSize

                        val startX = (availableW - contentW) / 2f
                        val startY = (availableH - contentH) / 2f

                        for (r in 0 until matrixSize) {
                            for (c in 0 until matrixSize) {
                                if (matrix[r][c]) {
                                    val x = startX + c * cellSize + gap / 2f
                                    val y = startY + r * cellSize + gap / 2f

                                    drawRoundRect(
                                        color = piece.color,
                                        topLeft = Offset(x, y),
                                        size = Size(innerSize, innerSize),
                                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                                    )

                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.6f),
                                        topLeft = Offset(x, y),
                                        size = Size(innerSize, innerSize),
                                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
