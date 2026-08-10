package com.myplaywin.app.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.ludo.data.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LudoBoardCanvas(
    gameState: LudoGameState,
    currentUserId: String,
    onTokenClick: (tokenIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPlayer = gameState.currentTurnPlayer
    val isMyTurn = currentPlayer != null && currentPlayer.uid == currentUserId
    val movableIndices = if (isMyTurn && gameState.hasRolled) gameState.movableTokenIndices else emptyList()

    // Pulse animation for movable tokens
    val infiniteTransition = rememberInfiniteTransition(label = "TokenPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2C2218),
                        Color(0xFF1E160E),
                        Color(0xFF120C07)
                    )
                )
            )
            .border(
                width = 5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD700),
                        Color(0xFFB8860B),
                        Color(0xFF8B6508),
                        Color(0xFFFFD700)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(6.dp)
    ) {
        val boardSizeDp = maxWidth - 12.dp
        val cellSizeDp = boardSizeDp / 15f
        val tokenSizeDp = cellSizeDp * 0.74f

        // 1. Draw Master Ludo Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size.width
            val cellSize = boardSize / 15f
            // Background Canvas Base Surface (Ivory/Cream)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFAF8F0), Color(0xFFEFECE2)),
                    center = Offset(boardSize / 2f, boardSize / 2f),
                    radius = boardSize * 0.8f
                ),
                size = Size(boardSize, boardSize)
            )

            // Inner Metallic Frame Border around 15x15 Grid
            drawRect(
                color = Color(0xFF2C2843),
                size = Size(boardSize, boardSize),
                style = Stroke(width = 3f)
            )

            // A. Draw 4 Corner Home Yards (6x6 cells each)
            drawHomeYard(
                ludoColor = LudoColor.RED,
                topLeftCell = Pair(0f, 0f),
                cellSize = cellSize,
                innerCoords = LudoBoardLayout.redYardCoords
            )
            drawHomeYard(
                ludoColor = LudoColor.GREEN,
                topLeftCell = Pair(9f, 0f),
                cellSize = cellSize,
                innerCoords = LudoBoardLayout.greenYardCoords
            )
            drawHomeYard(
                ludoColor = LudoColor.YELLOW,
                topLeftCell = Pair(9f, 9f),
                cellSize = cellSize,
                innerCoords = LudoBoardLayout.yellowYardCoords
            )
            drawHomeYard(
                ludoColor = LudoColor.BLUE,
                topLeftCell = Pair(0f, 9f),
                cellSize = cellSize,
                innerCoords = LudoBoardLayout.blueYardCoords
            )

            // B. Draw Common Track Grid Cells (52 cells)
            for (i in 0..51) {
                val coord = LudoBoardLayout.commonTrackCoords[i]
                val offset = Offset(coord.col * cellSize, coord.row * cellSize)
                val size = Size(cellSize, cellSize)

                // Fill Start Cells or Default Track Cells
                val startColor = when (i) {
                    0 -> LudoColor.RED
                    13 -> LudoColor.GREEN
                    26 -> LudoColor.YELLOW
                    39 -> LudoColor.BLUE
                    else -> null
                }

                if (startColor != null) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(startColor.displayColor, startColor.darkColor)
                        ),
                        topLeft = offset,
                        size = size
                    )
                } else {
                    drawRect(
                        color = Color(0xFFFAF8F0),
                        topLeft = offset,
                        size = size
                    )
                }

                // Grid border line
                drawRect(
                    color = Color(0xFF38334A),
                    topLeft = offset,
                    size = size,
                    style = Stroke(width = 1.2f)
                )
            }

            // C. Draw Player Colored Home Paths (5 cells each)
            drawColoredHomePath(LudoColor.RED, LudoBoardLayout.redHomePath, cellSize)
            drawColoredHomePath(LudoColor.GREEN, LudoBoardLayout.greenHomePath, cellSize)
            drawColoredHomePath(LudoColor.YELLOW, LudoBoardLayout.yellowHomePath, cellSize)
            drawColoredHomePath(LudoColor.BLUE, LudoBoardLayout.blueHomePath, cellSize)

            // D. Draw Safe Stars on track
            for (safeIdx in LudoBoardLayout.safeStarIndices) {
                val coord = LudoBoardLayout.commonTrackCoords[safeIdx]
                val center = Offset(coord.col * cellSize + cellSize / 2f, coord.row * cellSize + cellSize / 2f)
                val starPath = createStarPath(center, outerRadius = cellSize * 0.38f, innerRadius = cellSize * 0.16f)

                // Star shadow
                drawPath(
                    path = starPath,
                    color = Color.Black.copy(alpha = 0.25f)
                )
                // Star body
                drawPath(
                    path = starPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF176), Color(0xFFFFB300), Color(0xFFF57F17)),
                        center = center,
                        radius = cellSize * 0.4f
                    )
                )
                // Star outline
                drawPath(
                    path = starPath,
                    color = Color(0xFF5D4037),
                    style = Stroke(width = 1.8f)
                )
            }

            // E. Draw Home Entry Arrows
            drawHomeEntryArrows(cellSize)

            // F. Draw Center Victory Triangles (3x3 grid at 6..8, 6..8)
            drawCenterVictoryArea(cellSize)
        }

        // 2. Render Tokens
        val allTokens = mutableListOf<TokenRenderData>()
        for (player in gameState.players) {
            for (tokenIdx in player.tokens.indices) {
                val step = player.tokens[tokenIdx]
                val coord = LudoBoardLayout.getTokenCoordinate(player.ludoColor, tokenIdx, step)
                allTokens.add(
                    TokenRenderData(
                        playerUid = player.uid,
                        color = player.ludoColor,
                        tokenIndex = tokenIdx,
                        stepCount = step,
                        gridCoord = coord
                    )
                )
            }
        }

        // Group tokens on same cell to apply stack offsets
        val tokensByCell = allTokens.groupBy { "${it.gridCoord.row}_${it.gridCoord.col}" }

        for ((_, cellTokens) in tokensByCell) {
            val count = cellTokens.size
            for (idx in cellTokens.indices) {
                val tokenData = cellTokens[idx]
                val baseCoord = tokenData.gridCoord

                // Center coordinate in grid units
                val centerCol = if (tokenData.stepCount <= 0) baseCoord.col else baseCoord.col + 0.5f
                val centerRow = if (tokenData.stepCount <= 0) baseCoord.row else baseCoord.row + 0.5f

                // Offset calculation for multiple stacked tokens on same cell
                val (offsetXDp, offsetYDp) = if (count > 1 && tokenData.stepCount in 1..56) {
                    val shift = cellSizeDp * 0.15f
                    when (idx % 4) {
                        0 -> Pair(-shift, -shift)
                        1 -> Pair(shift, shift)
                        2 -> Pair(shift, -shift)
                        else -> Pair(-shift, shift)
                    }
                } else {
                    Pair(0.dp, 0.dp)
                }

                val topXDp = (cellSizeDp * centerCol) - (tokenSizeDp / 2f) + offsetXDp
                val topYDp = (cellSizeDp * centerRow) - (tokenSizeDp / 2f) + offsetYDp

                val currentTurnP = gameState.currentTurnPlayer
                val isTurnOwner = tokenData.playerUid == gameState.currentTurnUid
                val isHumanPlayer = currentTurnP != null && !currentTurnP.isBot
                val isLocalOrMyTurn = isTurnOwner && isHumanPlayer && (gameState.gameMode != LudoGameMode.PRIVATE_ROOM.name || tokenData.playerUid == currentUserId)

                val isMovable = isLocalOrMyTurn &&
                        gameState.hasRolled &&
                        movableIndices.contains(tokenData.tokenIndex)

                val tokenScale = if (isMovable) pulseScale else 1.0f

                Box(
                    modifier = Modifier
                        .offset(x = topXDp, y = topYDp)
                        .size(tokenSizeDp)
                        .scale(tokenScale)
                        .clickable(enabled = isMovable) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onTokenClick(tokenData.tokenIndex)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Premium 3D Game Piece Token
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(if (isMovable) 10.dp else 4.dp, CircleShape, spotColor = tokenData.color.displayColor)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        tokenData.color.lightColor,
                                        tokenData.color.displayColor,
                                        tokenData.color.darkColor
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(10f, 10f)
                                )
                            )
                            .border(
                                width = if (isMovable) 2.5.dp else 1.8.dp,
                                color = if (isMovable) Color(0xFFFFD700) else Color(0xFFFFFFFF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner ring accent
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.65f)
                                .clip(CircleShape)
                                .border(1.2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${tokenData.tokenIndex + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = (tokenSizeDp.value * 0.42f).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawHomeYard(
    ludoColor: LudoColor,
    topLeftCell: Pair<Float, Float>,
    cellSize: Float,
    innerCoords: List<GridCoord>
) {
    val yardOffset = Offset(topLeftCell.first * cellSize, topLeftCell.second * cellSize)
    val yardSize = Size(cellSize * 6f, cellSize * 6f)

    // Outer Yard Fill Gradient
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(ludoColor.displayColor, ludoColor.darkColor)
        ),
        topLeft = yardOffset,
        size = yardSize
    )

    // Outer Yard Border
    drawRect(
        color = Color(0xFF2C2843),
        topLeft = yardOffset,
        size = yardSize,
        style = Stroke(width = 2.5f)
    )

    // Inner Cream Box (4x4 cells at offset + 1 cell)
    val innerBoxOffset = Offset(yardOffset.x + cellSize, yardOffset.y + cellSize)
    val innerBoxSize = Size(cellSize * 4f, cellSize * 4f)

    drawRoundRect(
        color = Color(0xFFFAF8F0),
        topLeft = innerBoxOffset,
        size = innerBoxSize,
        cornerRadius = CornerRadius(18f, 18f)
    )
    drawRoundRect(
        color = ludoColor.darkColor,
        topLeft = innerBoxOffset,
        size = innerBoxSize,
        cornerRadius = CornerRadius(18f, 18f),
        style = Stroke(width = 2.5f)
    )

    // 4 Token Slots
    val circleRadius = cellSize * 0.68f
    for (c in innerCoords) {
        val centerPx = Offset(c.col * cellSize, c.row * cellSize)

        // Slot inset shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.15f),
            radius = circleRadius + 2f,
            center = centerPx
        )
        // Slot background radial tint
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, ludoColor.lightColor, ludoColor.displayColor.copy(alpha = 0.35f)),
                center = centerPx,
                radius = circleRadius
            ),
            radius = circleRadius,
            center = centerPx
        )
        // Slot border
        drawCircle(
            color = ludoColor.displayColor,
            radius = circleRadius,
            center = centerPx,
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawColoredHomePath(
    ludoColor: LudoColor,
    pathCoords: List<GridCoord>,
    cellSize: Float
) {
    for (coord in pathCoords) {
        val offset = Offset(coord.col * cellSize, coord.row * cellSize)
        val size = Size(cellSize, cellSize)

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(ludoColor.displayColor, ludoColor.darkColor)
            ),
            topLeft = offset,
            size = size
        )
        drawRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = offset,
            size = size,
            style = Stroke(width = 1.2f)
        )
    }
}

private fun DrawScope.drawHomeEntryArrows(cellSize: Float) {
    // Red entry arrow at (7, 0) pointing Right
    val redEntry = Offset(0.5f * cellSize, 7.5f * cellSize)
    drawArrow(redEntry, cellSize * 0.3f, 0f, LudoColor.RED.displayColor)

    // Green entry arrow at (0, 7) pointing Down
    val greenEntry = Offset(7.5f * cellSize, 0.5f * cellSize)
    drawArrow(greenEntry, cellSize * 0.3f, 90f, LudoColor.GREEN.displayColor)

    // Yellow entry arrow at (7, 14) pointing Left
    val yellowEntry = Offset(14.5f * cellSize, 7.5f * cellSize)
    drawArrow(yellowEntry, cellSize * 0.3f, 180f, LudoColor.YELLOW.displayColor)

    // Blue entry arrow at (14, 7) pointing Up
    val blueEntry = Offset(7.5f * cellSize, 14.5f * cellSize)
    drawArrow(blueEntry, cellSize * 0.3f, 270f, LudoColor.BLUE.displayColor)
}

private fun DrawScope.drawArrow(center: Offset, size: Float, angleDegrees: Float, color: Color) {
    val path = Path()
    val rad = angleDegrees * (PI / 180f).toFloat()

    fun transform(x: Float, y: Float): Offset {
        val rx = x * cos(rad) - y * sin(rad)
        val ry = x * sin(rad) + y * cos(rad)
        return Offset(center.x + rx, center.y + ry)
    }

    val p1 = transform(size, 0f)
    val p2 = transform(-size * 0.6f, -size * 0.6f)
    val p3 = transform(-size * 0.2f, 0f)
    val p4 = transform(-size * 0.6f, size * 0.6f)

    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.lineTo(p4.x, p4.y)
    path.close()

    drawPath(path = path, color = color)
    drawPath(path = path, color = Color.White, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawCenterVictoryArea(cellSize: Float) {
    val centerLeft = cellSize * 6f
    val centerTop = cellSize * 6f
    val centerRight = cellSize * 9f
    val centerBottom = cellSize * 9f
    val centerMid = Offset(cellSize * 7.5f, cellSize * 7.5f)

    // Red Left Triangle
    val redTriangle = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerLeft, centerBottom)
        close()
    }
    drawPath(
        path = redTriangle,
        brush = Brush.radialGradient(
            colors = listOf(LudoColor.RED.displayColor, LudoColor.RED.darkColor),
            center = centerMid,
            radius = cellSize * 1.5f
        )
    )

    // Green Top Triangle
    val greenTriangle = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerRight, centerTop)
        close()
    }
    drawPath(
        path = greenTriangle,
        brush = Brush.radialGradient(
            colors = listOf(LudoColor.GREEN.displayColor, LudoColor.GREEN.darkColor),
            center = centerMid,
            radius = cellSize * 1.5f
        )
    )

    // Yellow Right Triangle
    val yellowTriangle = Path().apply {
        moveTo(centerRight, centerTop)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerRight, centerBottom)
        close()
    }
    drawPath(
        path = yellowTriangle,
        brush = Brush.radialGradient(
            colors = listOf(LudoColor.YELLOW.displayColor, LudoColor.YELLOW.darkColor),
            center = centerMid,
            radius = cellSize * 1.5f
        )
    )

    // Blue Bottom Triangle
    val blueTriangle = Path().apply {
        moveTo(centerLeft, centerBottom)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerRight, centerBottom)
        close()
    }
    drawPath(
        path = blueTriangle,
        brush = Brush.radialGradient(
            colors = listOf(LudoColor.BLUE.displayColor, LudoColor.BLUE.darkColor),
            center = centerMid,
            radius = cellSize * 1.5f
        )
    )

    // Inner Dividers between triangles
    val strokeColor = Color.White.copy(alpha = 0.9f)
    val strokeWidth = 2f
    drawLine(strokeColor, Offset(centerLeft, centerTop), centerMid, strokeWidth)
    drawLine(strokeColor, Offset(centerRight, centerTop), centerMid, strokeWidth)
    drawLine(strokeColor, Offset(centerLeft, centerBottom), centerMid, strokeWidth)
    drawLine(strokeColor, Offset(centerRight, centerBottom), centerMid, strokeWidth)

    // Outer Center Square Frame
    drawRect(
        color = Color(0xFFFFD700),
        topLeft = Offset(centerLeft, centerTop),
        size = Size(cellSize * 3f, cellSize * 3f),
        style = Stroke(width = 3f)
    )

    // Center Gold Star / Crown Victory Emblem
    val centerStar = createStarPath(centerMid, outerRadius = cellSize * 0.45f, innerRadius = cellSize * 0.2f)
    drawPath(centerStar, color = Color.Black.copy(alpha = 0.3f))
    drawPath(
        centerStar,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFFF9800))
        )
    )
    drawPath(centerStar, color = Color.White, style = Stroke(width = 1.5f))
}

private fun createStarPath(center: Offset, outerRadius: Float, innerRadius: Float): Path {
    val path = Path()
    val doublePi = Math.PI * 2
    val step = doublePi / 10
    var angle = -Math.PI / 2

    path.moveTo(
        (center.x + outerRadius * cos(angle)).toFloat(),
        (center.y + outerRadius * sin(angle)).toFloat()
    )

    for (i in 1 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        angle += step
        path.lineTo(
            (center.x + radius * cos(angle)).toFloat(),
            (center.y + radius * sin(angle)).toFloat()
        )
    }
    path.close()
    return path
}

private data class TokenRenderData(
    val playerUid: String,
    val color: LudoColor,
    val tokenIndex: Int,
    val stepCount: Int,
    val gridCoord: GridCoord
)

