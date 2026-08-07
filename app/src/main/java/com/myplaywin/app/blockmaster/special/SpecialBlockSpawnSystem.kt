package com.myplaywin.app.blockmaster.special

import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import kotlin.random.Random

class SpecialBlockSpawnSystem {

    fun attachSpecialBlockIfEligible(piece: TetrominoBlock, level: Int): TetrominoBlock {
        val lvl = maxOf(1, level)

        // Calculate spawn chance based on level (10% at level 1 up to 22% at high levels)
        val spawnChance = (0.08f + (lvl * 0.001f)).coerceIn(0.08f, 0.22f)
        if (Random.nextFloat() > spawnChance) {
            return piece
        }

        val availableTypes = mutableListOf<SpecialBlockType>()

        // Base special blocks available from early levels
        availableTypes.add(SpecialBlockType.COIN)
        availableTypes.add(SpecialBlockType.TIME)

        // Progression unlocks
        if (lvl >= SpecialBlockRegistry.BOMB.minLevelUnlock) availableTypes.add(SpecialBlockType.BOMB)
        if (lvl >= SpecialBlockRegistry.ICE.minLevelUnlock) availableTypes.add(SpecialBlockType.ICE)
        if (lvl >= SpecialBlockRegistry.STEEL.minLevelUnlock) availableTypes.add(SpecialBlockType.STEEL)
        if (lvl >= SpecialBlockRegistry.LIGHTNING.minLevelUnlock) availableTypes.add(SpecialBlockType.LIGHTNING)
        if (lvl >= SpecialBlockRegistry.RAINBOW.minLevelUnlock) availableTypes.add(SpecialBlockType.RAINBOW)
        if (lvl >= SpecialBlockRegistry.MYSTERY.minLevelUnlock) availableTypes.add(SpecialBlockType.MYSTERY)

        if (availableTypes.isEmpty()) return piece

        val chosenType = availableTypes.random()

        // Find filled cells in the piece matrix
        val size = piece.matrix.size
        val filledCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (piece.matrix[r][c]) {
                    filledCells.add(Pair(r, c))
                }
            }
        }

        if (filledCells.isEmpty()) return piece

        // Pick 1 cell in the piece to attach the special block
        val targetCell = filledCells.random()

        val newSpecialMatrix = piece.specialMatrix.map { row -> row.toMutableList() }
        newSpecialMatrix[targetCell.first][targetCell.second] = chosenType

        return piece.copy(specialMatrix = newSpecialMatrix)
    }

    fun resolveMysteryBlock(): SpecialBlockType {
        val outcomes = listOf(
            SpecialBlockType.BOMB,
            SpecialBlockType.COIN,
            SpecialBlockType.LIGHTNING,
            SpecialBlockType.RAINBOW,
            SpecialBlockType.TIME,
            SpecialBlockType.NONE
        )
        return outcomes.random()
    }
}
