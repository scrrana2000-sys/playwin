package com.myplaywin.app.blockmaster.grid

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import com.myplaywin.app.blockmaster.constants.BlockMasterConstants
import com.myplaywin.app.blockmaster.special.SpecialBlockRegistry
import com.myplaywin.app.blockmaster.special.SpecialBlockType
import kotlin.random.Random

data class BlockGridCell(
    val row: Int,
    val column: Int,
    val isOccupied: Boolean = false,
    val color: Color = BlockMasterConstants.CellEmptyFill,
    val borderColor: Color = BlockMasterConstants.CellBorderColor,
    val glowColor: Color = BlockMasterConstants.CellGlowColor,
    val specialType: SpecialBlockType = SpecialBlockType.NONE,
    val iceHitPoints: Int = 2,
    val isCracked: Boolean = false
)

data class BombExplosionResult(
    val destroyedCellsCount: Int,
    val steelBlocksDestroyed: Int,
    val coinsEarned: Int,
    val chainReactionCount: Int
)

data class SpecialClearResult(
    val coinsEarned: Int,
    val scoreBonus: Int,
    val bombsTriggered: List<Pair<Int, Int>>,
    val lightningTriggered: Boolean,
    val timeSlowTriggered: Boolean,
    val iceShatteredCount: Int,
    val steelDestroyedCount: Int,
    val chainReactionMax: Int
)

class BlockGridState(
    val columns: Int = BlockMasterConstants.GRID_COLUMNS,
    val rows: Int = BlockMasterConstants.GRID_ROWS
) {
    // 2D grid matrix [row][column] using Jetpack Compose State with thread-safe copy-on-write
    var gridMatrix by mutableStateOf(Array(rows) { r ->
        Array(columns) { c ->
            BlockGridCell(row = r, column = c)
        }
    })
        private set

    var clearingRows by mutableStateOf<Set<Int>>(emptySet())

    fun isOccupied(row: Int, column: Int): Boolean {
        if (row !in 0 until rows || column !in 0 until columns) return true
        return gridMatrix[row][column].isOccupied
    }

    fun isCellEmpty(row: Int, column: Int): Boolean {
        return !isOccupied(row, column)
    }

    private fun getMatrixCopy(): Array<Array<BlockGridCell>> {
        return Array(rows) { r ->
            Array(columns) { c ->
                gridMatrix[r][c]
            }
        }
    }

    fun lockPiece(piece: TetrominoBlock) {
        val size = piece.matrix.size
        val newMatrix = getMatrixCopy()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (piece.matrix[r][c]) {
                    val boardX = piece.x + c
                    val boardY = piece.y + r
                    if (boardY in 0 until rows && boardX in 0 until columns) {
                        var specType = piece.specialMatrix.getOrNull(r)?.getOrNull(c) ?: SpecialBlockType.NONE

                        // If MYSTERY block, resolve randomly now
                        if (specType == SpecialBlockType.MYSTERY) {
                            val outcomes = listOf(
                                SpecialBlockType.BOMB,
                                SpecialBlockType.COIN,
                                SpecialBlockType.LIGHTNING,
                                SpecialBlockType.RAINBOW,
                                SpecialBlockType.TIME,
                                SpecialBlockType.NONE
                            )
                            specType = outcomes.random()
                        }

                        val specObj = SpecialBlockRegistry.getSpecialBlock(specType)
                        val cellColor = specObj?.fillColor ?: piece.color
                        val borderCol = specObj?.borderColor ?: piece.color.copy(alpha = 0.8f)
                        val glowCol = specObj?.glowColor ?: piece.color.copy(alpha = 0.4f)

                        newMatrix[boardY][boardX] = BlockGridCell(
                            row = boardY,
                            column = boardX,
                            isOccupied = true,
                            color = cellColor,
                            borderColor = borderCol,
                            glowColor = glowCol,
                            specialType = specType,
                            iceHitPoints = if (specType == SpecialBlockType.ICE) 2 else 0,
                            isCracked = false
                        )
                    }
                }
            }
        }
        gridMatrix = newMatrix
    }

    fun getCompletedRows(): List<Int> {
        val completed = mutableListOf<Int>()
        for (r in 0 until rows) {
            var isFull = true
            for (c in 0 until columns) {
                if (!gridMatrix[r][c].isOccupied) {
                    isFull = false
                    break
                }
            }
            if (isFull) {
                completed.add(r)
            }
        }
        return completed
    }

    /**
     * Process line clears considering special block behaviors:
     * - ICE requires 2 hits: 1st hit cracks ice, cell stays occupied!
     * - STEEL blocks resist line clear unless destroyed by Bomb.
     * - BOMB, LIGHTNING, COIN, TIME, RAINBOW execute their special effects.
     */
    fun processAndClearRows(rowsToClear: List<Int>): SpecialClearResult {
        if (rowsToClear.isEmpty()) {
            return SpecialClearResult(0, 0, emptyList(), false, false, 0, 0, 0)
        }

        var totalCoins = 0
        var totalScoreBonus = 0
        var lightningTriggered = false
        var timeSlowTriggered = false
        var iceShatteredCount = 0
        var steelDestroyedCount = 0
        var maxChainCount = 0

        val bombsToTrigger = mutableListOf<Pair<Int, Int>>()

        val newMatrix = getMatrixCopy()

        // First pass: scan completed rows for special block triggers
        for (r in rowsToClear) {
            for (c in 0 until columns) {
                val cell = newMatrix[r][c]
                if (!cell.isOccupied) continue

                when (cell.specialType) {
                    SpecialBlockType.BOMB -> {
                        bombsToTrigger.add(Pair(r, c))
                    }
                    SpecialBlockType.LIGHTNING -> {
                        lightningTriggered = true
                        totalScoreBonus += 300
                    }
                    SpecialBlockType.COIN -> {
                        totalCoins += 25
                        totalScoreBonus += 150
                    }
                    SpecialBlockType.TIME -> {
                        timeSlowTriggered = true
                        totalScoreBonus += 100
                    }
                    SpecialBlockType.RAINBOW -> {
                        totalScoreBonus += 250
                    }
                    SpecialBlockType.ICE -> {
                        if (cell.iceHitPoints > 1) {
                            // First hit: crack the ice, don't destroy yet
                            newMatrix[r][c] = cell.copy(
                                iceHitPoints = 1,
                                isCracked = true,
                                color = cell.color.copy(alpha = 0.7f)
                            )
                        } else {
                            iceShatteredCount++
                            totalScoreBonus += 100
                        }
                    }
                    else -> {}
                }
            }
        }

        gridMatrix = newMatrix

        // Execute bomb explosions if any bombs were cleared
        if (bombsToTrigger.isNotEmpty()) {
            val bombResult = executeBombChainExplosion(bombsToTrigger)
            totalCoins += bombResult.coinsEarned
            steelDestroyedCount += bombResult.steelBlocksDestroyed
            maxChainCount = maxOf(maxChainCount, bombResult.chainReactionCount)
        }

        // Execute Lightning clear if triggered
        if (lightningTriggered) {
            executeLightningEffect()
        }

        // Second pass: actual grid shifting for all completed rows.
        // This ensures NO completed rows are skipped or left on the board.
        if (rowsToClear.isNotEmpty()) {
            clearRowsInternal(rowsToClear)
        }

        return SpecialClearResult(
            coinsEarned = totalCoins,
            scoreBonus = totalScoreBonus,
            bombsTriggered = bombsToTrigger,
            lightningTriggered = lightningTriggered,
            timeSlowTriggered = timeSlowTriggered,
            iceShatteredCount = iceShatteredCount,
            steelDestroyedCount = steelDestroyedCount,
            chainReactionMax = maxChainCount
        )
    }

    /**
     * Executes a 3x3 explosion centered at bomb positions, handling recursive chain reactions.
     */
    fun executeBombChainExplosion(initialBombs: List<Pair<Int, Int>>): BombExplosionResult {
        val explodedSet = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.addAll(initialBombs)

        var totalDestroyed = 0
        var steelDestroyed = 0
        var coinsEarned = 0
        var chainCount = 0

        val newMatrix = getMatrixCopy()

        while (queue.isNotEmpty()) {
            val (br, bc) = queue.removeFirst()
            if (br !in 0 until rows || bc !in 0 until columns) continue
            if (explodedSet.contains(Pair(br, bc))) continue

            explodedSet.add(Pair(br, bc))
            chainCount++

            // Destroy 3x3 grid around bomb
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val targetR = br + dr
                    val targetC = bc + dc
                    if (targetR in 0 until rows && targetC in 0 until columns) {
                        val cell = newMatrix[targetR][targetC]
                        if (cell.isOccupied) {
                            totalDestroyed++

                            if (cell.specialType == SpecialBlockType.STEEL) {
                                steelDestroyed++
                            } else if (cell.specialType == SpecialBlockType.COIN) {
                                coinsEarned += 20
                            }

                            // If another Bomb block in explosion range, add to chain queue
                            if (cell.specialType == SpecialBlockType.BOMB && !explodedSet.contains(Pair(targetR, targetC))) {
                                queue.add(Pair(targetR, targetC))
                            }

                            // Clear target cell
                            newMatrix[targetR][targetC] = BlockGridCell(row = targetR, column = targetC)
                        }
                    }
                }
            }
        }

        gridMatrix = newMatrix
        return BombExplosionResult(
            destroyedCellsCount = totalDestroyed,
            steelBlocksDestroyed = steelDestroyed,
            coinsEarned = coinsEarned,
            chainReactionCount = chainCount
        )
    }

    /**
     * Executes Lightning effect: randomly clears an entire row or column.
     */
    fun executeLightningEffect() {
        val isHorizontal = Random.nextBoolean()
        val newMatrix = getMatrixCopy()
        if (isHorizontal) {
            // Pick a random occupied row
            val occupiedRows = (0 until rows).filter { r -> (0 until columns).any { c -> gridMatrix[r][c].isOccupied } }
            if (occupiedRows.isNotEmpty()) {
                val targetRow = occupiedRows.random()
                for (c in 0 until columns) {
                    newMatrix[targetRow][c] = BlockGridCell(row = targetRow, column = c)
                }
            }
        } else {
            // Pick a random column
            val targetCol = Random.nextInt(columns)
            for (r in 0 until rows) {
                newMatrix[r][targetCol] = BlockGridCell(row = r, column = targetCol)
            }
        }
        gridMatrix = newMatrix
    }

    // --- POWER-UP IMPLEMENTATIONS ---

    fun applyPowerUpClearRow(): Boolean {
        // Find bottom-most occupied row
        for (r in (rows - 1) downTo 0) {
            if ((0 until columns).any { c -> gridMatrix[r][c].isOccupied }) {
                val newMatrix = getMatrixCopy()
                for (c in 0 until columns) {
                    newMatrix[r][c] = BlockGridCell(row = r, column = c)
                }
                gridMatrix = newMatrix
                clearRowsInternal(listOf(r))
                return true
            }
        }
        return false
    }

    fun applyPowerUpClearColumn(): Boolean {
        val targetCols = listOf(columns / 2 - 1, columns / 2)
        val newMatrix = getMatrixCopy()
        for (c in targetCols) {
            for (r in 0 until rows) {
                newMatrix[r][c] = BlockGridCell(row = r, column = c)
            }
        }
        gridMatrix = newMatrix
        return true
    }

    fun applyPowerUpDestroyBlock(): Boolean {
        // Find highest occupied block on grid
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (gridMatrix[r][c].isOccupied) {
                    val newMatrix = getMatrixCopy()
                    newMatrix[r][c] = BlockGridCell(row = r, column = c)
                    gridMatrix = newMatrix
                    return true
                }
            }
        }
        return false
    }

    private fun clearRowsInternal(rowsToClear: List<Int>) {
        if (rowsToClear.isEmpty()) return

        val newMatrix = Array(rows) { r ->
            Array(columns) { c ->
                BlockGridCell(row = r, column = c)
            }
        }

        var newRowIndex = rows - 1
        for (oldRowIndex in (rows - 1) downTo 0) {
            if (oldRowIndex !in rowsToClear) {
                for (c in 0 until columns) {
                    val oldCell = gridMatrix[oldRowIndex][c]
                    newMatrix[newRowIndex][c] = oldCell.copy(row = newRowIndex)
                }
                newRowIndex--
            }
        }

        gridMatrix = newMatrix
        clearingRows = emptySet()
    }

    fun isBoardEmpty(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (gridMatrix[r][c].isOccupied) return false
            }
        }
        return true
    }

    fun clearBottomRows(count: Int) {
        val rowsToClear = ((rows - count) until rows).filter { it >= 0 }
        clearRowsInternal(rowsToClear)
    }

    fun resetGrid() {
        gridMatrix = Array(rows) { r ->
            Array(columns) { c ->
                BlockGridCell(row = r, column = c)
            }
        }
        clearingRows = emptySet()
    }
}
