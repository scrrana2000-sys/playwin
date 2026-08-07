package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import com.myplaywin.app.blockmaster.grid.BlockGridCell
import com.myplaywin.app.blockmaster.grid.BlockGridState
import com.myplaywin.app.blockmaster.special.SpecialBlockRegistry
import com.myplaywin.app.blockmaster.special.SpecialBlockType
import com.myplaywin.app.blockmaster.world.BlockWorld
import com.myplaywin.app.blockmaster.world.WorldProgressionManager

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

class TouchRippleState(
    val x: Float,
    val y: Float,
    val color: Color
) {
    val alpha = androidx.compose.animation.core.Animatable(1.0f)
    val scale = androidx.compose.animation.core.Animatable(0.0f)

    suspend fun animate() {
        kotlinx.coroutines.coroutineScope {
            launch {
                scale.animateTo(1.0f, animationSpec = tween(350))
            }
            launch {
                alpha.animateTo(0.0f, animationSpec = tween(350))
            }
        }
    }
}

@Composable
fun BlockMasterGridBoard(
    gridState: BlockGridState,
    activePiece: TetrominoBlock?,
    ghostY: Int,
    world: BlockWorld = WorldProgressionManager.WORLDS.first(),
    onMoveLeft: () -> Unit = {},
    onMoveRight: () -> Unit = {},
    onRotate: () -> Unit = {},
    onSoftDrop: () -> Unit = {},
    onHardDrop: () -> Unit = {},
    onHold: () -> Unit = {},
    engineState: com.myplaywin.app.blockmaster.engine.GameEngineState = com.myplaywin.app.blockmaster.engine.GameEngineState.PLAYING,
    modifier: Modifier = Modifier
) {
    val animBoardBorder by animateColorAsState(
        targetValue = world.boardBorderColor,
        animationSpec = tween(durationMillis = 600),
        label = "boardBorderAnim"
    )

    val animAccentColor by animateColorAsState(
        targetValue = world.accentColor,
        animationSpec = tween(durationMillis = 600),
        label = "boardAccentAnim"
    )

    val density = LocalDensity.current.density
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val touchRipples = remember { mutableStateListOf<TouchRippleState>() }
    var lastTapTime by remember { mutableStateOf(0L) }

    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = animAccentColor)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161226).copy(alpha = 0.95f),
                        Color(0xFF0F0C1B).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        animBoardBorder.copy(alpha = 0.8f),
                        world.secondaryColor.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp)
            .pointerInput(engineState) {
                if (engineState != com.myplaywin.app.blockmaster.engine.GameEngineState.PLAYING) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downTime = System.currentTimeMillis()
                        val startPos = down.position
                        var lastMoveX = startPos.x
                        var lastMoveY = startPos.y
                        var isDragDetected = false
                        var hasHoldTriggered = false
                        var hasHardDropped = false

                        // Add beautiful touch-down ripple animation
                        val downRipple = TouchRippleState(startPos.x, startPos.y, world.accentColor)
                        touchRipples.add(downRipple)
                        scope.launch {
                            downRipple.animate()
                            touchRipples.remove(downRipple)
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) {
                                // Gesture released (Up event)
                                if (change != null) {
                                    val upPos = change.position
                                    val upTime = System.currentTimeMillis()
                                    val elapsed = upTime - downTime
                                    val totalDeltaX = upPos.x - startPos.x
                                    val totalDeltaY = upPos.y - startPos.y

                                    // Fast Swipe Down (Hard Drop / Slam) Detection
                                    if (!hasHardDropped && elapsed in 40..300 && totalDeltaY > 60f * density && totalDeltaY > kotlin.math.abs(totalDeltaX) * 1.5f) {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (_: Exception) {}
                                        onHardDrop()
                                        hasHardDropped = true
                                        
                                        // Visual confirmation splash ripple
                                        val splashRipple = TouchRippleState(upPos.x, upPos.y, Color.Red)
                                        touchRipples.add(splashRipple)
                                        scope.launch {
                                            splashRipple.animate()
                                            touchRipples.remove(splashRipple)
                                        }
                                    } else if (!isDragDetected && !hasHoldTriggered) {
                                        // Tap / Double Tap
                                        val now = System.currentTimeMillis()
                                        if (now - lastTapTime < 250) {
                                            // Double tap -> Rotate Clockwise twice (simulates responsive counter-clockwise)
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (_: Exception) {}
                                            onRotate()
                                            onRotate()
                                            lastTapTime = 0L
                                        } else {
                                            // Single tap -> Rotate Clockwise
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (_: Exception) {}
                                            onRotate()
                                            lastTapTime = now
                                        }
                                    }
                                }
                                break
                            }

                            val currentPos = change.position
                            val diffX = currentPos.x - startPos.x
                            val diffY = currentPos.y - startPos.y

                            val touchSlop = 8f * density
                            if (!isDragDetected) {
                                if (kotlin.math.abs(diffX) > touchSlop || kotlin.math.abs(diffY) > touchSlop) {
                                    isDragDetected = true
                                }
                            }

                            if (isDragDetected && !hasHardDropped) {
                                val deltaX = currentPos.x - lastMoveX
                                val deltaY = currentPos.y - lastMoveY

                                // Proportional cell-by-cell movement steps for continuous swipes
                                val horizontalStep = 22f * density
                                val verticalStep = 14f * density

                                if (deltaX >= horizontalStep) {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                    onMoveRight()
                                    lastMoveX = currentPos.x
                                    
                                    val moveRipple = TouchRippleState(currentPos.x, currentPos.y, world.accentColor.copy(alpha = 0.5f))
                                    touchRipples.add(moveRipple)
                                    scope.launch {
                                        moveRipple.animate()
                                        touchRipples.remove(moveRipple)
                                    }
                                } else if (deltaX <= -horizontalStep) {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                    onMoveLeft()
                                    lastMoveX = currentPos.x
                                    
                                    val moveRipple = TouchRippleState(currentPos.x, currentPos.y, world.accentColor.copy(alpha = 0.5f))
                                    touchRipples.add(moveRipple)
                                    scope.launch {
                                        moveRipple.animate()
                                        touchRipples.remove(moveRipple)
                                    }
                                }

                                if (deltaY >= verticalStep) {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                    onSoftDrop()
                                    lastMoveY = currentPos.y
                                }
                            }

                            // Long Press Detection -> Hold current piece
                            if (!isDragDetected && !hasHoldTriggered) {
                                val elapsed = System.currentTimeMillis() - downTime
                                if (elapsed > 400) {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (_: Exception) {}
                                    onHold()
                                    hasHoldTriggered = true
                                }
                            }

                            change.consume()
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalCols = gridState.columns
            val totalRows = gridState.rows

            val availableWidth = size.width
            val availableHeight = size.height

            val cellWidth = availableWidth / totalCols
            val cellHeight = availableHeight / totalRows
            val cellSize = minOf(cellWidth, cellHeight)

            val gridWidth = cellSize * totalCols
            val gridHeight = cellSize * totalRows

            val startX = (availableWidth - gridWidth) / 2f
            val startY = (availableHeight - gridHeight) / 2f

            val cornerRadiusPx = cellSize * 0.18f
            val gap = cellSize * 0.08f

            val clearingRows = gridState.clearingRows

            // 1. Draw Grid Base & Locked Cells
            for (r in 0 until totalRows) {
                val isRowClearing = r in clearingRows
                for (c in 0 until totalCols) {
                    val x = startX + c * cellSize + gap / 2f
                    val y = startY + r * cellSize + gap / 2f
                    val innerSize = cellSize - gap

                    val cell = gridState.gridMatrix[r][c]

                    val cellColor = if (isRowClearing) Color.White else cell.color
                    val borderColor = if (isRowClearing) Color(0xFFFFD700) else cell.borderColor

                    // Draw Cell Fill
                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(x, y),
                        size = Size(innerSize, innerSize),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )

                    // Special Block Canvas Overlays
                    if (cell.isOccupied && !isRowClearing && cell.specialType != SpecialBlockType.NONE) {
                        drawSpecialBlockDetails(
                            cell = cell,
                            x = x,
                            y = y,
                            size = innerSize,
                            cornerRadiusPx = cornerRadiusPx
                        )
                    }

                    // Draw Cell Border
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(x, y),
                        size = Size(innerSize, innerSize),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        style = Stroke(width = if (isRowClearing) 2.5f else 1.2f)
                    )
                }
            }

            // 2. Draw Ghost Piece Landing Projection
            if (activePiece != null) {
                val matrix = activePiece.matrix
                val matrixSize = matrix.size

                for (r in 0 until matrixSize) {
                    for (c in 0 until matrixSize) {
                        if (matrix[r][c]) {
                            val boardX = activePiece.x + c
                            val boardY = ghostY + r

                            if (boardX in 0 until totalCols && boardY in 0 until totalRows) {
                                val x = startX + boardX * cellSize + gap / 2f
                                val y = startY + boardY * cellSize + gap / 2f
                                val innerSize = cellSize - gap

                                drawRoundRect(
                                    color = activePiece.color.copy(alpha = 0.15f),
                                    topLeft = Offset(x, y),
                                    size = Size(innerSize, innerSize),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                                )

                                drawRoundRect(
                                    color = activePiece.color.copy(alpha = 0.6f),
                                    topLeft = Offset(x, y),
                                    size = Size(innerSize, innerSize),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                    style = Stroke(width = 1.8f)
                                )
                            }
                        }
                    }
                }

                // 3. Draw Active Falling Piece
                for (r in 0 until matrixSize) {
                    for (c in 0 until matrixSize) {
                        if (matrix[r][c]) {
                            val boardX = activePiece.x + c
                            val boardY = activePiece.y + r

                            if (boardX in 0 until totalCols && boardY in 0 until totalRows) {
                                val x = startX + boardX * cellSize + gap / 2f
                                val y = startY + boardY * cellSize + gap / 2f
                                val innerSize = cellSize - gap

                                val specType = activePiece.specialMatrix.getOrNull(r)?.getOrNull(c) ?: SpecialBlockType.NONE
                                val specObj = SpecialBlockRegistry.getSpecialBlock(specType)

                                val pieceColor = specObj?.fillColor ?: activePiece.color
                                val pieceBorderColor = specObj?.borderColor ?: Color.White.copy(alpha = 0.8f)

                                drawRoundRect(
                                    color = pieceColor,
                                    topLeft = Offset(x, y),
                                    size = Size(innerSize, innerSize),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                                )

                                if (specType != SpecialBlockType.NONE) {
                                    val dummyCell = BlockGridCell(
                                        row = boardY,
                                        column = boardX,
                                        isOccupied = true,
                                        color = pieceColor,
                                        specialType = specType
                                    )
                                    drawSpecialBlockDetails(dummyCell, x, y, innerSize, cornerRadiusPx)
                                }

                                drawRoundRect(
                                    color = pieceBorderColor,
                                    topLeft = Offset(x, y),
                                    size = Size(innerSize, innerSize),
                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Draw Active Touch Ripples
            touchRipples.forEach { ripple ->
                val radius = cellSize * 2f * ripple.scale.value
                val currentAlpha = ripple.alpha.value
                if (currentAlpha > 0f) {
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha * 0.35f),
                        radius = radius,
                        center = Offset(ripple.x, ripple.y)
                    )
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha * 0.6f),
                        radius = radius,
                        center = Offset(ripple.x, ripple.y),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpecialBlockDetails(
    cell: BlockGridCell,
    x: Float,
    y: Float,
    size: Float,
    cornerRadiusPx: Float
) {
    val center = Offset(x + size / 2f, y + size / 2f)

    when (cell.specialType) {
        SpecialBlockType.BOMB -> {
            // Inner explosive core
            drawCircle(color = Color(0xFF1F1212), radius = size * 0.32f, center = center)
            drawCircle(color = Color(0xFFFF9100), radius = size * 0.18f, center = center)
            drawCircle(color = Color.White, radius = size * 0.08f, center = center)
        }
        SpecialBlockType.ICE -> {
            // Ice crack lines if cracked
            if (cell.isCracked || cell.iceHitPoints == 1) {
                val path = Path().apply {
                    moveTo(x + size * 0.2f, y + size * 0.2f)
                    lineTo(x + size * 0.5f, y + size * 0.5f)
                    lineTo(x + size * 0.35f, y + size * 0.8f)
                    moveTo(x + size * 0.5f, y + size * 0.5f)
                    lineTo(x + size * 0.85f, y + size * 0.3f)
                }
                drawPath(path = path, color = Color.White, style = Stroke(width = size * 0.08f))
            } else {
                // Frosted shine
                drawCircle(color = Color.White.copy(alpha = 0.4f), radius = size * 0.25f, center = center)
            }
        }
        SpecialBlockType.STEEL -> {
            // Corner metallic rivets
            val rivetRadius = size * 0.07f
            val offsetVal = size * 0.2f
            drawCircle(color = Color(0xFFCFD8DC), radius = rivetRadius, center = Offset(x + offsetVal, y + offsetVal))
            drawCircle(color = Color(0xFFCFD8DC), radius = rivetRadius, center = Offset(x + size - offsetVal, y + offsetVal))
            drawCircle(color = Color(0xFFCFD8DC), radius = rivetRadius, center = Offset(x + offsetVal, y + size - offsetVal))
            drawCircle(color = Color(0xFFCFD8DC), radius = rivetRadius, center = Offset(x + size - offsetVal, y + size - offsetVal))
        }
        SpecialBlockType.LIGHTNING -> {
            // Electric bolt center icon
            val path = Path().apply {
                moveTo(x + size * 0.55f, y + size * 0.15f)
                lineTo(x + size * 0.25f, y + size * 0.55f)
                lineTo(x + size * 0.52f, y + size * 0.55f)
                lineTo(x + size * 0.45f, y + size * 0.88f)
                lineTo(x + size * 0.75f, y + size * 0.45f)
                lineTo(x + size * 0.48f, y + size * 0.45f)
                close()
            }
            drawPath(path = path, color = Color.White)
        }
        SpecialBlockType.RAINBOW -> {
            // Iridescent ring
            drawCircle(color = Color.White.copy(alpha = 0.5f), radius = size * 0.35f, center = center, style = Stroke(width = size * 0.1f))
            drawCircle(color = Color(0xFFFF007F), radius = size * 0.2f, center = center)
        }
        SpecialBlockType.COIN -> {
            // Golden coin circle
            drawCircle(color = Color(0xFFFFF59D), radius = size * 0.32f, center = center)
            drawCircle(color = Color(0xFFFFD700), radius = size * 0.22f, center = center)
        }
        SpecialBlockType.TIME -> {
            // Time hourglass dot
            drawCircle(color = Color.White, radius = size * 0.28f, center = center, style = Stroke(width = size * 0.08f))
            drawCircle(color = Color(0xFF29B6F6), radius = size * 0.15f, center = center)
        }
        SpecialBlockType.MYSTERY -> {
            // Mystery center dot
            drawCircle(color = Color.White, radius = size * 0.28f, center = center)
            drawCircle(color = Color(0xFFD500F9), radius = size * 0.16f, center = center)
        }
        SpecialBlockType.NONE -> {}
    }
}
