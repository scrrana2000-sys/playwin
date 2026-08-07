package com.myplaywin.app.blockmaster.engine

import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import com.myplaywin.app.blockmaster.grid.BlockGridState

object BlockMasterCollisionEngine {

    fun isValidPosition(
        piece: TetrominoBlock,
        targetX: Int,
        targetY: Int,
        matrix: List<List<Boolean>>,
        gridState: BlockGridState
    ): Boolean {
        val size = matrix.size
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (matrix[r][c]) {
                    val boardX = targetX + c
                    val boardY = targetY + r

                    // Check horizontal boundaries
                    if (boardX < 0 || boardX >= gridState.columns) {
                        return false
                    }

                    // Check vertical bottom boundary
                    if (boardY >= gridState.rows) {
                        return false
                    }

                    // Top boundary check (allowed if above row 0 for spawn, but check grid if >= 0)
                    if (boardY >= 0) {
                        if (gridState.isOccupied(boardY, boardX)) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    // Standard Super Rotation System (SRS) Wall Kick Offsets
    private val STANDARD_WALL_KICK_OFFSETS = listOf(
        Pair(0, 0),
        Pair(-1, 0),
        Pair(1, 0),
        Pair(0, -1),
        Pair(-1, -1),
        Pair(1, -1),
        Pair(-2, 0),
        Pair(2, 0)
    )

    fun tryRotationWithWallKick(
        piece: TetrominoBlock,
        gridState: BlockGridState
    ): TetrominoBlock? {
        val rotatedMatrix = piece.rotateClockwiseMatrix()
        val rotatedSpecialMatrix = piece.rotateClockwiseSpecialMatrix()
        for (offset in STANDARD_WALL_KICK_OFFSETS) {
            val testX = piece.x + offset.first
            val testY = piece.y + offset.second
            if (isValidPosition(piece, testX, testY, rotatedMatrix, gridState)) {
                return piece.copy(
                    matrix = rotatedMatrix,
                    specialMatrix = rotatedSpecialMatrix,
                    rotationIndex = (piece.rotationIndex + 1) % 4,
                    x = testX,
                    y = testY
                )
            }
        }
        return null // Rotation blocked
    }

    fun calculateGhostY(
        piece: TetrominoBlock,
        gridState: BlockGridState
    ): Int {
        var testY = piece.y
        while (isValidPosition(piece, piece.x, testY + 1, piece.matrix, gridState)) {
            testY++
        }
        return testY
    }
}
