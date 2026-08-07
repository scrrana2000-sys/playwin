package com.myplaywin.app.blockmaster.blocks

import androidx.compose.ui.graphics.Color
import com.myplaywin.app.blockmaster.special.SpecialBlockType
import kotlin.random.Random

enum class BlockType {
    I, J, L, O, S, T, Z
}

data class TetrominoBlock(
    val type: BlockType,
    val color: Color,
    val matrix: List<List<Boolean>>,
    val specialMatrix: List<List<SpecialBlockType>> = List(matrix.size) { List(matrix.size) { SpecialBlockType.NONE } },
    val rotationIndex: Int = 0,
    val x: Int = 3,
    val y: Int = 0
) {
    fun rotateClockwiseMatrix(): List<List<Boolean>> {
        val size = matrix.size
        val result = MutableList(size) { MutableList(size) { false } }
        for (r in 0 until size) {
            for (c in 0 until size) {
                result[c][size - 1 - r] = matrix[r][c]
            }
        }
        return result
    }

    fun rotateClockwiseSpecialMatrix(): List<List<SpecialBlockType>> {
        val size = specialMatrix.size
        val result = MutableList(size) { MutableList(size) { SpecialBlockType.NONE } }
        for (r in 0 until size) {
            for (c in 0 until size) {
                result[c][size - 1 - r] = specialMatrix[r][c]
            }
        }
        return result
    }
}

object BlockFactory {

    val COLOR_I = Color(0xFF00E5FF) // Neon Cyan
    val COLOR_J = Color(0xFF2979FF) // Electric Blue
    val COLOR_L = Color(0xFFFF9100) // Neon Orange
    val COLOR_O = Color(0xFFFFEA00) // Bright Yellow
    val COLOR_S = Color(0xFF00E676) // Neon Green
    val COLOR_T = Color(0xFFD500F9) // Electric Purple
    val COLOR_Z = Color(0xFFFF1744) // Bright Red

    fun getColor(type: BlockType): Color = when (type) {
        BlockType.I -> COLOR_I
        BlockType.J -> COLOR_J
        BlockType.L -> COLOR_L
        BlockType.O -> COLOR_O
        BlockType.S -> COLOR_S
        BlockType.T -> COLOR_T
        BlockType.Z -> COLOR_Z
    }

    fun getInitialMatrix(type: BlockType): List<List<Boolean>> = when (type) {
        BlockType.I -> listOf(
            listOf(false, false, false, false),
            listOf(true,  true,  true,  true),
            listOf(false, false, false, false),
            listOf(false, false, false, false)
        )
        BlockType.O -> listOf(
            listOf(true, true),
            listOf(true, true)
        )
        BlockType.T -> listOf(
            listOf(false, true,  false),
            listOf(true,  true,  true),
            listOf(false, false, false)
        )
        BlockType.L -> listOf(
            listOf(false, false, true),
            listOf(true,  true,  true),
            listOf(false, false, false)
        )
        BlockType.J -> listOf(
            listOf(true,  false, false),
            listOf(true,  true,  true),
            listOf(false, false, false)
        )
        BlockType.S -> listOf(
            listOf(false, true,  true),
            listOf(true,  true,  false),
            listOf(false, false, false)
        )
        BlockType.Z -> listOf(
            listOf(true,  true,  false),
            listOf(false, true,  true),
            listOf(false, false, false)
        )
    }

    fun createPiece(type: BlockType): TetrominoBlock {
        val matrix = getInitialMatrix(type)
        val initialX = when (type) {
            BlockType.I -> 3
            BlockType.O -> 4
            else -> 3
        }
        val initialY = 0
        return TetrominoBlock(
            type = type,
            color = getColor(type),
            matrix = matrix,
            specialMatrix = List(matrix.size) { List(matrix.size) { SpecialBlockType.NONE } },
            rotationIndex = 0,
            x = initialX,
            y = initialY
        )
    }

    // 7-Bag Randomizer generator to ensure fair, official piece distribution
    class SevenBagGenerator {
        private val currentBag = mutableListOf<BlockType>()

        fun nextPieceType(): BlockType {
            if (currentBag.isEmpty()) {
                currentBag.addAll(BlockType.values().toList().shuffled())
            }
            return currentBag.removeAt(0)
        }
    }
}
