package com.myplaywin.app.ludo.data.model

data class GridCoord(val row: Float, val col: Float)

object LudoBoardLayout {
    // 52 Common Track cells
    val commonTrackCoords: List<GridCoord> = listOf(
        GridCoord(6f, 1f),  // 0  Red Start / Star
        GridCoord(6f, 2f),  // 1
        GridCoord(6f, 3f),  // 2
        GridCoord(6f, 4f),  // 3
        GridCoord(6f, 5f),  // 4
        GridCoord(5f, 6f),  // 5
        GridCoord(4f, 6f),  // 6
        GridCoord(3f, 6f),  // 7
        GridCoord(2f, 6f),  // 8  Safe Star
        GridCoord(1f, 6f),  // 9
        GridCoord(0f, 6f),  // 10
        GridCoord(0f, 7f),  // 11 Green Home Entry
        GridCoord(0f, 8f),  // 12
        GridCoord(1f, 8f),  // 13 Green Start / Star
        GridCoord(2f, 8f),  // 14
        GridCoord(3f, 8f),  // 15
        GridCoord(4f, 8f),  // 16
        GridCoord(5f, 8f),  // 17
        GridCoord(6f, 9f),  // 18
        GridCoord(6f, 10f), // 19
        GridCoord(6f, 11f), // 20
        GridCoord(6f, 12f), // 21 Safe Star
        GridCoord(6f, 13f), // 22
        GridCoord(6f, 14f), // 23
        GridCoord(7f, 14f), // 24 Yellow Home Entry
        GridCoord(8f, 14f), // 25
        GridCoord(8f, 13f), // 26 Yellow Start / Star
        GridCoord(8f, 12f), // 27
        GridCoord(8f, 11f), // 28
        GridCoord(8f, 10f), // 29
        GridCoord(8f, 9f),  // 30
        GridCoord(9f, 8f),  // 31
        GridCoord(10f, 8f), // 32
        GridCoord(11f, 8f), // 33
        GridCoord(12f, 8f), // 34 Safe Star
        GridCoord(13f, 8f), // 35
        GridCoord(14f, 8f), // 36
        GridCoord(14f, 7f), // 37 Blue Home Entry
        GridCoord(14f, 6f), // 38
        GridCoord(13f, 6f), // 39 Blue Start / Star
        GridCoord(12f, 6f), // 40
        GridCoord(11f, 6f), // 41
        GridCoord(10f, 6f), // 42
        GridCoord(9f, 6f),  // 43
        GridCoord(8f, 5f),  // 44
        GridCoord(8f, 4f),  // 45
        GridCoord(8f, 3f),  // 46
        GridCoord(8f, 2f),  // 47 Safe Star
        GridCoord(8f, 1f),  // 48
        GridCoord(8f, 0f),  // 49
        GridCoord(7f, 0f),  // 50 Red Home Entry
        GridCoord(6f, 0f)   // 51
    )

    // Safe star indices on 52-cell track
    val safeStarIndices: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Player Home Paths (5 cells each)
    val redHomePath = listOf(
        GridCoord(7f, 1f), GridCoord(7f, 2f), GridCoord(7f, 3f), GridCoord(7f, 4f), GridCoord(7f, 5f)
    )
    val greenHomePath = listOf(
        GridCoord(1f, 7f), GridCoord(2f, 7f), GridCoord(3f, 7f), GridCoord(4f, 7f), GridCoord(5f, 7f)
    )
    val yellowHomePath = listOf(
        GridCoord(7f, 13f), GridCoord(7f, 12f), GridCoord(7f, 11f), GridCoord(7f, 10f), GridCoord(7f, 9f)
    )
    val blueHomePath = listOf(
        GridCoord(13f, 7f), GridCoord(12f, 7f), GridCoord(11f, 7f), GridCoord(10f, 7f), GridCoord(9f, 7f)
    )

    // Final Center Home Coords
    val redFinishCoord = GridCoord(7f, 6f)
    val greenFinishCoord = GridCoord(6f, 7f)
    val yellowFinishCoord = GridCoord(7f, 8f)
    val blueFinishCoord = GridCoord(8f, 7f)

    // Yard Coords for each player (token 0..3)
    val redYardCoords = listOf(
        GridCoord(1.5f, 1.5f), GridCoord(1.5f, 3.5f), GridCoord(3.5f, 1.5f), GridCoord(3.5f, 3.5f)
    )
    val greenYardCoords = listOf(
        GridCoord(1.5f, 10.5f), GridCoord(1.5f, 12.5f), GridCoord(3.5f, 10.5f), GridCoord(3.5f, 12.5f)
    )
    val yellowYardCoords = listOf(
        GridCoord(10.5f, 10.5f), GridCoord(10.5f, 12.5f), GridCoord(12.5f, 10.5f), GridCoord(12.5f, 12.5f)
    )
    val blueYardCoords = listOf(
        GridCoord(10.5f, 1.5f), GridCoord(10.5f, 3.5f), GridCoord(12.5f, 1.5f), GridCoord(12.5f, 3.5f)
    )

    /**
     * Get exact grid coordinate for a token based on its color, token index, and step count (0..57)
     */
    fun getTokenCoordinate(color: LudoColor, tokenIndex: Int, stepCount: Int): GridCoord {
        if (stepCount <= 0) {
            return when (color) {
                LudoColor.RED -> redYardCoords[tokenIndex % 4]
                LudoColor.GREEN -> greenYardCoords[tokenIndex % 4]
                LudoColor.YELLOW -> yellowYardCoords[tokenIndex % 4]
                LudoColor.BLUE -> blueYardCoords[tokenIndex % 4]
            }
        }

        // On common track (1..51)
        if (stepCount <= 51) {
            val trackIndex = (color.startIndex + stepCount - 1) % 52
            return commonTrackCoords[trackIndex]
        }

        // On home path (52..56)
        if (stepCount <= 56) {
            val homePathIndex = stepCount - 52
            return when (color) {
                LudoColor.RED -> redHomePath[homePathIndex]
                LudoColor.GREEN -> greenHomePath[homePathIndex]
                LudoColor.YELLOW -> yellowHomePath[homePathIndex]
                LudoColor.BLUE -> blueHomePath[homePathIndex]
            }
        }

        // Finished (>= 57)
        return when (color) {
            LudoColor.RED -> redFinishCoord
            LudoColor.GREEN -> greenFinishCoord
            LudoColor.YELLOW -> yellowFinishCoord
            LudoColor.BLUE -> blueFinishCoord
        }
    }

    /**
     * Get absolute common track index (0..51) if the token is currently on the common track,
     * or -1 if the token is in Yard, Home Path, or Finished.
     */
    fun getCommonTrackIndex(color: LudoColor, stepCount: Int): Int {
        if (stepCount in 1..51) {
            return (color.startIndex + stepCount - 1) % 52
        }
        return -1
    }

    /**
     * Check if a common track index is a safe star cell.
     */
    fun isSafeCell(commonTrackIndex: Int): Boolean {
        return commonTrackIndex in safeStarIndices
    }
}
