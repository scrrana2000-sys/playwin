package com.myplaywin.app.ui.screens
import androidx.compose.ui.graphics.Color
import com.myplaywin.app.ui.screens.SmartProceduralLevelGenerator.ChunkType

object ChunkLibrary {

    fun getNumVariations(type: ChunkType): Int {
        return when (type) {
            ChunkType.START -> 20
            ChunkType.EASY -> 30
            ChunkType.MEDIUM -> 40
            ChunkType.VERTICAL -> 25
            ChunkType.MOVING_PLATFORM -> 30
            ChunkType.SPRING -> 20
            ChunkType.ENEMY -> 30
            ChunkType.SECRET -> 20
            ChunkType.PUZZLE -> 25
            ChunkType.CHECKPOINT -> 15
            ChunkType.FINAL_CHALLENGE -> 40
            ChunkType.EXIT -> 15
        }
    }
    fun generateStartChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 20 + 20) % 20
        return when (v) {
            0 -> generateStartTemplate0(startX, startY)
            1 -> generateStartTemplate1(startX, startY)
            2 -> generateStartTemplate2(startX, startY)
            3 -> generateStartTemplate3(startX, startY)
            4 -> generateStartTemplate4(startX, startY)
            5 -> generateStartTemplate5(startX, startY)
            6 -> generateStartTemplate6(startX, startY)
            7 -> generateStartTemplate7(startX, startY)
            8 -> generateStartTemplate8(startX, startY)
            9 -> generateStartTemplate9(startX, startY)
            10 -> generateStartTemplate10(startX, startY)
            11 -> generateStartTemplate11(startX, startY)
            12 -> generateStartTemplate12(startX, startY)
            13 -> generateStartTemplate13(startX, startY)
            14 -> generateStartTemplate14(startX, startY)
            15 -> generateStartTemplate15(startX, startY)
            16 -> generateStartTemplate16(startX, startY)
            17 -> generateStartTemplate17(startX, startY)
            18 -> generateStartTemplate18(startX, startY)
            19 -> generateStartTemplate19(startX, startY)
            else -> generateStartTemplate0(startX, startY)
        }
    }

    private fun generateStartTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 160f, height = 40f))
        val p2X = startX + 160f + 40f
        val p2Y = startY + -24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 170f, height = 40f))
        c.add(BounceCollectible(x = startX + 160f + 20.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 370f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 167f, height = 40f))
        val p2X = startX + 167f + 49f
        val p2Y = startY + -12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 183f, height = 40f))
        c.add(BounceCollectible(x = startX + 167f + 24.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 399f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 174f, height = 40f))
        val p2X = startX + 174f + 58f
        val p2Y = startY + 0f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 196f, height = 40f))
        c.add(BounceCollectible(x = startX + 174f + 29.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 428f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 181f, height = 40f))
        val p2X = startX + 181f + 67f
        val p2Y = startY + 12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 209f, height = 40f))
        c.add(BounceCollectible(x = startX + 181f + 33.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 457f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 188f, height = 40f))
        val p2X = startX + 188f + 41f
        val p2Y = startY + 24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 172f, height = 40f))
        c.add(BounceCollectible(x = startX + 188f + 20.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 401f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 195f, height = 40f))
        val p2X = startX + 195f + 50f
        val p2Y = startY + -24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 185f, height = 40f))
        c.add(BounceCollectible(x = startX + 195f + 25.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 430f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 162f, height = 40f))
        val p2X = startX + 162f + 59f
        val p2Y = startY + -12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 198f, height = 40f))
        c.add(BounceCollectible(x = startX + 162f + 29.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 419f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 169f, height = 40f))
        val p2X = startX + 169f + 68f
        val p2Y = startY + 0f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 211f, height = 40f))
        c.add(BounceCollectible(x = startX + 169f + 34.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 448f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 176f, height = 40f))
        val p2X = startX + 176f + 42f
        val p2Y = startY + 12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 174f, height = 40f))
        c.add(BounceCollectible(x = startX + 176f + 21.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 392f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 183f, height = 40f))
        val p2X = startX + 183f + 51f
        val p2Y = startY + 24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 187f, height = 40f))
        c.add(BounceCollectible(x = startX + 183f + 25.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 421f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 190f, height = 40f))
        val p2X = startX + 190f + 60f
        val p2Y = startY + -24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 200f, height = 40f))
        c.add(BounceCollectible(x = startX + 190f + 30.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 450f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 197f, height = 40f))
        val p2X = startX + 197f + 69f
        val p2Y = startY + -12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 213f, height = 40f))
        c.add(BounceCollectible(x = startX + 197f + 34.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 479f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 164f, height = 40f))
        val p2X = startX + 164f + 43f
        val p2Y = startY + 0f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 176f, height = 40f))
        c.add(BounceCollectible(x = startX + 164f + 21.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 383f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 171f, height = 40f))
        val p2X = startX + 171f + 52f
        val p2Y = startY + 12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 189f, height = 40f))
        c.add(BounceCollectible(x = startX + 171f + 26.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 412f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 178f, height = 40f))
        val p2X = startX + 178f + 61f
        val p2Y = startY + 24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 202f, height = 40f))
        c.add(BounceCollectible(x = startX + 178f + 30.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 441f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 185f, height = 40f))
        val p2X = startX + 185f + 70f
        val p2Y = startY + -24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 215f, height = 40f))
        c.add(BounceCollectible(x = startX + 185f + 35.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 470f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 192f, height = 40f))
        val p2X = startX + 192f + 44f
        val p2Y = startY + -12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 178f, height = 40f))
        c.add(BounceCollectible(x = startX + 192f + 22.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 414f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 199f, height = 40f))
        val p2X = startX + 199f + 53f
        val p2Y = startY + 0f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 191f, height = 40f))
        c.add(BounceCollectible(x = startX + 199f + 26.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 443f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 166f, height = 40f))
        val p2X = startX + 166f + 62f
        val p2Y = startY + 12f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 204f, height = 40f))
        c.add(BounceCollectible(x = startX + 166f + 31.0f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 432f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    private fun generateStartTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 173f, height = 40f))
        val p2X = startX + 173f + 71f
        val p2Y = startY + 24f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = 217f, height = 40f))
        c.add(BounceCollectible(x = startX + 173f + 35.5f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = 461f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    fun generateEasyChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 30 + 30) % 30
        return when (v) {
            0 -> generateEasyTemplate0(startX, startY)
            1 -> generateEasyTemplate1(startX, startY)
            2 -> generateEasyTemplate2(startX, startY)
            3 -> generateEasyTemplate3(startX, startY)
            4 -> generateEasyTemplate4(startX, startY)
            5 -> generateEasyTemplate5(startX, startY)
            6 -> generateEasyTemplate6(startX, startY)
            7 -> generateEasyTemplate7(startX, startY)
            8 -> generateEasyTemplate8(startX, startY)
            9 -> generateEasyTemplate9(startX, startY)
            10 -> generateEasyTemplate10(startX, startY)
            11 -> generateEasyTemplate11(startX, startY)
            12 -> generateEasyTemplate12(startX, startY)
            13 -> generateEasyTemplate13(startX, startY)
            14 -> generateEasyTemplate14(startX, startY)
            15 -> generateEasyTemplate15(startX, startY)
            16 -> generateEasyTemplate16(startX, startY)
            17 -> generateEasyTemplate17(startX, startY)
            18 -> generateEasyTemplate18(startX, startY)
            19 -> generateEasyTemplate19(startX, startY)
            20 -> generateEasyTemplate20(startX, startY)
            21 -> generateEasyTemplate21(startX, startY)
            22 -> generateEasyTemplate22(startX, startY)
            23 -> generateEasyTemplate23(startX, startY)
            24 -> generateEasyTemplate24(startX, startY)
            25 -> generateEasyTemplate25(startX, startY)
            26 -> generateEasyTemplate26(startX, startY)
            27 -> generateEasyTemplate27(startX, startY)
            28 -> generateEasyTemplate28(startX, startY)
            29 -> generateEasyTemplate29(startX, startY)
            else -> generateEasyTemplate0(startX, startY)
        }
    }

    // Easy Template 0: Long Bridge
    private fun generateEasyTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 83f, startY + (-20f), 137f, 40f))
        p.add(BounceObstacle(startX + 150f + 220f + 96f, startY + (-20f) + (0f), 154f, 40f))
        c.add(BounceCollectible(startX + 150f + 220f + 173.0f, startY + (-20f) + (0f) - 45f, isStar = true))
        return LevelChunk(width = 680f, height = 600f, platforms = p, collectibles = c, endY = startY + (-20f) + (0f))
    }

    // Easy Template 1: Tower Ascent
    private fun generateEasyTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 90f, startY + (0f), 148f, 40f))
        p.add(BounceObstacle(startX + 150f + 238f + 103f, startY + (0f) + (20f), 165f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 185.5f, startY + (0f) + (20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 238f + 268f + 76f, startY + (0f) + (20f) + (40f), 132f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 268f + 142.0f, startY + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 924f, height = 600f, platforms = p, collectibles = c, endY = startY + (0f) + (20f) + (40f))
    }

    // Easy Template 2: Floating Archipelago
    private fun generateEasyTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 97f, startY + (20f), 159f, 40f))
        p.add(BounceObstacle(startX + 150f + 256f + 70f, startY + (20f) + (40f), 126f, 40f))
        c.add(BounceCollectible(startX + 150f + 256f + 133.0f, startY + (20f) + (40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 256f + 196f + 83f, startY + (20f) + (40f) + (-40f), 143f, 40f))
        c.add(BounceCollectible(startX + 150f + 256f + 196f + 154.5f, startY + (20f) + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 888f, height = 600f, platforms = p, collectibles = c, endY = startY + (20f) + (40f) + (-40f))
    }

    // Easy Template 3: Zigzag Step
    private fun generateEasyTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 104f, startY + (40f), 120f, 40f))
        p.add(BounceObstacle(startX + 150f + 224f + 77f, startY + (40f) + (-40f), 137f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 145.5f, startY + (40f) + (-40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 224f + 214f + 90f, startY + (40f) + (-40f) + (-20f), 154f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 214f + 167.0f, startY + (40f) + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 892f, height = 600f, platforms = p, collectibles = c, endY = startY + (40f) + (-40f) + (-20f))
    }

    // Easy Template 4: Split Level
    private fun generateEasyTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 71f, startY + (-40f), 131f, 40f))
        p.add(BounceObstacle(startX + 150f + 202f + 84f, startY + (-40f) + (-20f), 148f, 40f))
        c.add(BounceCollectible(startX + 150f + 202f + 158.0f, startY + (-40f) + (-20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 202f + 232f + 97f, startY + (-40f) + (-20f) + (0f), 165f, 40f))
        c.add(BounceCollectible(startX + 150f + 202f + 232f + 179.5f, startY + (-40f) + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 906f, height = 600f, platforms = p, collectibles = c, endY = startY + (-40f) + (-20f) + (0f))
    }

    // Easy Template 5: Mini Maze Barrier
    private fun generateEasyTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 78f, startY + (-20f), 142f, 40f))
        p.add(BounceObstacle(startX + 150f + 220f + 91f, startY + (-20f) + (0f), 159f, 40f))
        c.add(BounceCollectible(startX + 150f + 220f + 170.5f, startY + (-20f) + (0f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 220f + 250f + 104f, startY + (-20f) + (0f) + (20f), 126f, 40f))
        c.add(BounceCollectible(startX + 150f + 220f + 250f + 167.0f, startY + (-20f) + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 910f, height = 600f, platforms = p, collectibles = c, endY = startY + (-20f) + (0f) + (20f))
    }

    // Easy Template 6: Spring Bounce
    private fun generateEasyTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 85f, startY + 20f, 70f, 30f, isSpring = true, springForce = -670f))
        c.add(BounceCollectible(startX + 150f + 85f + 35f, startY - 60f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 155f + 98f, startY - 120f + (20f), 120f, 40f))
        c.add(BounceCollectible(startX + 150f + 155f + 158.0f, startY - 120f + (20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 155f + 218f + 71f, startY - 120f + (20f) + (40f), 137f, 40f))
        c.add(BounceCollectible(startX + 150f + 155f + 218f + 139.5f, startY - 120f + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 791f, height = 600f, platforms = p, collectibles = c, endY = startY - 120f + (20f) + (40f))
    }

    // Easy Template 7: Forest Canopy
    private fun generateEasyTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 92f, startY + (20f), 164f, 40f))
        p.add(BounceObstacle(startX + 150f + 256f + 105f, startY + (20f) + (40f), 131f, 40f))
        c.add(BounceCollectible(startX + 150f + 256f + 170.5f, startY + (20f) + (40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 256f + 236f + 78f, startY + (20f) + (40f) + (-40f), 148f, 40f))
        c.add(BounceCollectible(startX + 150f + 256f + 236f + 152.0f, startY + (20f) + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 928f, height = 600f, platforms = p, collectibles = c, endY = startY + (20f) + (40f) + (-40f))
    }

    // Easy Template 8: Castle Rampart
    private fun generateEasyTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 99f, startY + (40f), 125f, 40f))
        p.add(BounceObstacle(startX + 150f + 224f + 72f, startY + (40f) + (-40f), 142f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 143.0f, startY + (40f) + (-40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 224f + 214f + 85f, startY + (40f) + (-40f) + (-20f), 159f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 214f + 164.5f, startY + (40f) + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 892f, height = 600f, platforms = p, collectibles = c, endY = startY + (40f) + (-40f) + (-20f))
    }

    // Easy Template 9: Cave Overhang
    private fun generateEasyTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 106f, startY + (-40f), 136f, 40f))
        p.add(BounceObstacle(startX + 150f + 242f + 79f, startY + (-40f) + (-20f), 153f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 155.5f, startY + (-40f) + (-20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 242f + 232f + 92f, startY + (-40f) + (-20f) + (0f), 120f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 232f + 152.0f, startY + (-40f) + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 896f, height = 600f, platforms = p, collectibles = c, endY = startY + (-40f) + (-20f) + (0f))
    }

    // Easy Template 10: Sky Islands
    private fun generateEasyTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 73f, startY + (-20f), 147f, 40f))
        p.add(BounceObstacle(startX + 150f + 220f + 86f, startY + (-20f) + (0f), 164f, 40f))
        c.add(BounceCollectible(startX + 150f + 220f + 168.0f, startY + (-20f) + (0f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 220f + 250f + 99f, startY + (-20f) + (0f) + (20f), 131f, 40f))
        c.add(BounceCollectible(startX + 150f + 220f + 250f + 164.5f, startY + (-20f) + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 910f, height = 600f, platforms = p, collectibles = c, endY = startY + (-20f) + (0f) + (20f))
    }

    // Easy Template 11: Crystal Ledge
    private fun generateEasyTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 80f, startY + (0f), 158f, 40f))
        p.add(BounceObstacle(startX + 150f + 238f + 93f, startY + (0f) + (20f), 125f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 155.5f, startY + (0f) + (20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 238f + 218f + 106f, startY + (0f) + (20f) + (40f), 142f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 218f + 177.0f, startY + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 914f, height = 600f, platforms = p, collectibles = c, endY = startY + (0f) + (20f) + (40f))
    }

    // Easy Template 12: Low Valley
    private fun generateEasyTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 87f, startY + (20f), 169f, 40f))
        p.add(BounceObstacle(startX + 150f + 256f + 100f, startY + (20f) + (40f), 136f, 40f))
        c.add(BounceCollectible(startX + 150f + 256f + 168.0f, startY + (20f) + (40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 256f + 236f + 73f, startY + (20f) + (40f) + (-40f), 153f, 40f))
        c.add(BounceCollectible(startX + 150f + 256f + 236f + 149.5f, startY + (20f) + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 928f, height = 600f, platforms = p, collectibles = c, endY = startY + (20f) + (40f) + (-40f))
    }

    // Easy Template 13: Archway Bridge
    private fun generateEasyTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 94f, startY + (40f), 130f, 40f))
        p.add(BounceObstacle(startX + 150f + 224f + 107f, startY + (40f) + (-40f), 147f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 180.5f, startY + (40f) + (-40f) - 45f, isStar = true))
        return LevelChunk(width = 688f, height = 600f, platforms = p, collectibles = c, endY = startY + (40f) + (-40f))
    }

    // Easy Template 14: Pyramid Steps
    private fun generateEasyTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 101f, startY + (-40f), 141f, 40f))
        p.add(BounceObstacle(startX + 150f + 242f + 74f, startY + (-40f) + (-20f), 158f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 153.0f, startY + (-40f) + (-20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 242f + 232f + 87f, startY + (-40f) + (-20f) + (0f), 125f, 40f))
        p.add(BounceObstacle(startX + 150f + 242f + 232f + 212f + 100f, startY + (-40f) + (-20f) + (0f) + (20f), 142f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 232f + 212f + 171.0f, startY + (-40f) + (-20f) + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 1138f, height = 600f, platforms = p, collectibles = c, endY = startY + (-40f) + (-20f) + (0f) + (20f))
    }

    // Easy Template 15: Stepping Stones
    private fun generateEasyTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 108f, startY + (-20f), 152f, 40f))
        p.add(BounceObstacle(startX + 150f + 260f + 81f, startY + (-20f) + (0f), 169f, 40f))
        c.add(BounceCollectible(startX + 150f + 260f + 165.5f, startY + (-20f) + (0f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 260f + 250f + 94f, startY + (-20f) + (0f) + (20f), 136f, 40f))
        p.add(BounceObstacle(startX + 150f + 260f + 250f + 230f + 107f, startY + (-20f) + (0f) + (20f) + (40f), 153f, 40f))
        c.add(BounceCollectible(startX + 150f + 260f + 250f + 230f + 183.5f, startY + (-20f) + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 1210f, height = 600f, platforms = p, collectibles = c, endY = startY + (-20f) + (0f) + (20f) + (40f))
    }

    // Easy Template 16: Dual Tier Track
    private fun generateEasyTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 75f, startY + (0f), 163f, 40f))
        p.add(BounceObstacle(startX + 150f + 238f + 88f, startY + (0f) + (20f), 130f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 153.0f, startY + (0f) + (20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 238f + 218f + 101f, startY + (0f) + (20f) + (40f), 147f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 218f + 174.5f, startY + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 914f, height = 600f, platforms = p, collectibles = c, endY = startY + (0f) + (20f) + (40f))
    }

    // Easy Template 17: Overpass Beam
    private fun generateEasyTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 82f, startY + (20f), 124f, 40f))
        p.add(BounceObstacle(startX + 150f + 206f + 95f, startY + (20f) + (40f), 141f, 40f))
        c.add(BounceCollectible(startX + 150f + 206f + 165.5f, startY + (20f) + (40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 206f + 236f + 108f, startY + (20f) + (40f) + (-40f), 158f, 40f))
        c.add(BounceCollectible(startX + 150f + 206f + 236f + 187.0f, startY + (20f) + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 918f, height = 600f, platforms = p, collectibles = c, endY = startY + (20f) + (40f) + (-40f))
    }

    // Easy Template 18: Twin Peaks
    private fun generateEasyTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 89f, startY + (40f), 135f, 40f))
        p.add(BounceObstacle(startX + 150f + 224f + 102f, startY + (40f) + (-40f), 152f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 178.0f, startY + (40f) + (-40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 224f + 254f + 75f, startY + (40f) + (-40f) + (-20f), 169f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 254f + 159.5f, startY + (40f) + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 932f, height = 600f, platforms = p, collectibles = c, endY = startY + (40f) + (-40f) + (-20f))
    }

    // Easy Template 19: Desert Stretch
    private fun generateEasyTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 96f, startY + (-40f), 146f, 40f))
        p.add(BounceObstacle(startX + 150f + 242f + 109f, startY + (-40f) + (-20f), 163f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 190.5f, startY + (-40f) + (-20f) - 45f, isStar = true))
        return LevelChunk(width = 724f, height = 600f, platforms = p, collectibles = c, endY = startY + (-40f) + (-20f))
    }

    // Easy Template 20: Spiral Climb
    private fun generateEasyTemplate20(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 103f, startY + (-20f), 157f, 40f))
        p.add(BounceObstacle(startX + 150f + 260f + 76f, startY + (-20f) + (0f), 124f, 40f))
        c.add(BounceCollectible(startX + 150f + 260f + 138.0f, startY + (-20f) + (0f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 260f + 200f + 89f, startY + (-20f) + (0f) + (20f), 141f, 40f))
        c.add(BounceCollectible(startX + 150f + 260f + 200f + 159.5f, startY + (-20f) + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 900f, height = 600f, platforms = p, collectibles = c, endY = startY + (-20f) + (0f) + (20f))
    }

    // Easy Template 21: High Wire
    private fun generateEasyTemplate21(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 70f, startY + (0f), 168f, 40f))
        p.add(BounceObstacle(startX + 150f + 238f + 83f, startY + (0f) + (20f), 135f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 150.5f, startY + (0f) + (20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 238f + 218f + 96f, startY + (0f) + (20f) + (40f), 152f, 40f))
        c.add(BounceCollectible(startX + 150f + 238f + 218f + 172.0f, startY + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 914f, height = 600f, platforms = p, collectibles = c, endY = startY + (0f) + (20f) + (40f))
    }

    // Easy Template 22: Underpass Crawl
    private fun generateEasyTemplate22(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 77f, startY + (20f), 129f, 40f))
        p.add(BounceObstacle(startX + 150f + 206f + 90f, startY + (20f) + (40f), 146f, 40f))
        c.add(BounceCollectible(startX + 150f + 206f + 163.0f, startY + (20f) + (40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 206f + 236f + 103f, startY + (20f) + (40f) + (-40f), 163f, 40f))
        c.add(BounceCollectible(startX + 150f + 206f + 236f + 184.5f, startY + (20f) + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 918f, height = 600f, platforms = p, collectibles = c, endY = startY + (20f) + (40f) + (-40f))
    }

    // Easy Template 23: Waterfall Walk
    private fun generateEasyTemplate23(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 84f, startY + (40f), 140f, 40f))
        p.add(BounceObstacle(startX + 150f + 224f + 97f, startY + (40f) + (-40f), 157f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 175.5f, startY + (40f) + (-40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 224f + 254f + 70f, startY + (40f) + (-40f) + (-20f), 124f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 254f + 132.0f, startY + (40f) + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 882f, height = 600f, platforms = p, collectibles = c, endY = startY + (40f) + (-40f) + (-20f))
    }

    // Easy Template 24: Grotto Sanctuary
    private fun generateEasyTemplate24(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 91f, startY + (-40f), 151f, 40f))
        p.add(BounceObstacle(startX + 150f + 242f + 104f, startY + (-40f) + (-20f), 168f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 188.0f, startY + (-40f) + (-20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 242f + 272f + 77f, startY + (-40f) + (-20f) + (0f), 135f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 272f + 144.5f, startY + (-40f) + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 936f, height = 600f, platforms = p, collectibles = c, endY = startY + (-40f) + (-20f) + (0f))
    }

    // Easy Template 25: Staircase Steps
    private fun generateEasyTemplate25(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 98f, startY + (-20f), 162f, 40f))
        p.add(BounceObstacle(startX + 150f + 260f + 71f, startY + (-20f) + (0f), 129f, 40f))
        c.add(BounceCollectible(startX + 150f + 260f + 135.5f, startY + (-20f) + (0f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 260f + 200f + 84f, startY + (-20f) + (0f) + (20f), 146f, 40f))
        p.add(BounceObstacle(startX + 150f + 260f + 200f + 230f + 97f, startY + (-20f) + (0f) + (20f) + (40f), 163f, 40f))
        c.add(BounceCollectible(startX + 150f + 260f + 200f + 230f + 178.5f, startY + (-20f) + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 1160f, height = 600f, platforms = p, collectibles = c, endY = startY + (-20f) + (0f) + (20f) + (40f))
    }

    // Easy Template 26: Platform Chain
    private fun generateEasyTemplate26(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 105f, startY + (0f), 123f, 40f))
        p.add(BounceObstacle(startX + 150f + 228f + 78f, startY + (0f) + (20f), 140f, 40f))
        c.add(BounceCollectible(startX + 150f + 228f + 148.0f, startY + (0f) + (20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 228f + 218f + 91f, startY + (0f) + (20f) + (40f), 157f, 40f))
        c.add(BounceCollectible(startX + 150f + 228f + 218f + 169.5f, startY + (0f) + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 904f, height = 600f, platforms = p, collectibles = c, endY = startY + (0f) + (20f) + (40f))
    }

    // Easy Template 27: Balanced Beam
    private fun generateEasyTemplate27(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 72f, startY + (20f), 134f, 40f))
        p.add(BounceObstacle(startX + 150f + 206f + 85f, startY + (20f) + (40f), 151f, 40f))
        c.add(BounceCollectible(startX + 150f + 206f + 160.5f, startY + (20f) + (40f) - 45f, isStar = true))
        return LevelChunk(width = 652f, height = 600f, platforms = p, collectibles = c, endY = startY + (20f) + (40f))
    }

    // Easy Template 28: Battlement Steps
    private fun generateEasyTemplate28(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 79f, startY + (40f), 145f, 40f))
        p.add(BounceObstacle(startX + 150f + 224f + 92f, startY + (40f) + (-40f), 162f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 173.0f, startY + (40f) + (-40f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 224f + 254f + 105f, startY + (40f) + (-40f) + (-20f), 129f, 40f))
        c.add(BounceCollectible(startX + 150f + 224f + 254f + 169.5f, startY + (40f) + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 922f, height = 600f, platforms = p, collectibles = c, endY = startY + (40f) + (-40f) + (-20f))
    }

    // Easy Template 29: Cloud Hop
    private fun generateEasyTemplate29(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 150f + 86f, startY + (-40f), 156f, 40f))
        p.add(BounceObstacle(startX + 150f + 242f + 99f, startY + (-40f) + (-20f), 123f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 160.5f, startY + (-40f) + (-20f) - 45f, isStar = true))
        p.add(BounceObstacle(startX + 150f + 242f + 222f + 72f, startY + (-40f) + (-20f) + (0f), 140f, 40f))
        c.add(BounceCollectible(startX + 150f + 242f + 222f + 142.0f, startY + (-40f) + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 886f, height = 600f, platforms = p, collectibles = c, endY = startY + (-40f) + (-20f) + (0f))
    }

    fun generateMediumChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 40 + 40) % 40
        return when (v) {
            0 -> generateMediumTemplate0(startX, startY)
            1 -> generateMediumTemplate1(startX, startY)
            2 -> generateMediumTemplate2(startX, startY)
            3 -> generateMediumTemplate3(startX, startY)
            4 -> generateMediumTemplate4(startX, startY)
            5 -> generateMediumTemplate5(startX, startY)
            6 -> generateMediumTemplate6(startX, startY)
            7 -> generateMediumTemplate7(startX, startY)
            8 -> generateMediumTemplate8(startX, startY)
            9 -> generateMediumTemplate9(startX, startY)
            10 -> generateMediumTemplate10(startX, startY)
            11 -> generateMediumTemplate11(startX, startY)
            12 -> generateMediumTemplate12(startX, startY)
            13 -> generateMediumTemplate13(startX, startY)
            14 -> generateMediumTemplate14(startX, startY)
            15 -> generateMediumTemplate15(startX, startY)
            16 -> generateMediumTemplate16(startX, startY)
            17 -> generateMediumTemplate17(startX, startY)
            18 -> generateMediumTemplate18(startX, startY)
            19 -> generateMediumTemplate19(startX, startY)
            20 -> generateMediumTemplate20(startX, startY)
            21 -> generateMediumTemplate21(startX, startY)
            22 -> generateMediumTemplate22(startX, startY)
            23 -> generateMediumTemplate23(startX, startY)
            24 -> generateMediumTemplate24(startX, startY)
            25 -> generateMediumTemplate25(startX, startY)
            26 -> generateMediumTemplate26(startX, startY)
            27 -> generateMediumTemplate27(startX, startY)
            28 -> generateMediumTemplate28(startX, startY)
            29 -> generateMediumTemplate29(startX, startY)
            30 -> generateMediumTemplate30(startX, startY)
            31 -> generateMediumTemplate31(startX, startY)
            32 -> generateMediumTemplate32(startX, startY)
            33 -> generateMediumTemplate33(startX, startY)
            34 -> generateMediumTemplate34(startX, startY)
            35 -> generateMediumTemplate35(startX, startY)
            36 -> generateMediumTemplate36(startX, startY)
            37 -> generateMediumTemplate37(startX, startY)
            38 -> generateMediumTemplate38(startX, startY)
            39 -> generateMediumTemplate39(startX, startY)
            else -> generateMediumTemplate0(startX, startY)
        }
    }

    // Medium Template 0
    private fun generateMediumTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 80f, startY + (-40f), 130f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 80f + 130f + 90f, startY + (-40f) + (-20f), 160f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 80f - 20f, y = startY + 80f, width = 260f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 80f + 65.0f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 80f + 130f + 90f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 460f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 1
    private fun generateMediumTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 87f, startY + (-20f), 139f, 40f))
        p.add(BounceObstacle(startX + 140f + 87f + 40f, startY + (-20f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 87f + 139f + 101f, startY + (-20f) + (0f), 173f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 87f + 69.5f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 87f + 139f + 101f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 500f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 2
    private fun generateMediumTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 94f, startY + (0f), 148f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 2, type = InteractiveType.BREAKABLE, x = startX + 140f + 94f + 40f, y = startY + (0f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 94f + 148f + 112f, startY + (0f) + (20f), 186f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 94f + 74.0f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 94f + 148f + 112f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 3
    private fun generateMediumTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 101f, startY + (20f), 157f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 101f + 157f + 123f, startY + (20f) + (40f), 199f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 101f + 78.5f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 101f + 157f + 123f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 580f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 4
    private fun generateMediumTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 108f, startY + (40f), 166f, 40f))
        p.add(BounceObstacle(startX + 140f + 108f + 166f + 94f, startY + (40f) + (-40f), 162f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 108f + 83.0f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 108f + 166f + 94f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 5
    private fun generateMediumTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 115f, startY + (-40f), 135f, 40f))
        p.add(BounceObstacle(startX + 140f + 115f + 40f, startY + (-40f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 115f + 135f + 105f, startY + (-40f) + (-20f), 175f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 115f - 20f, y = startY + 80f, width = 280f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 115f + 67.5f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 115f + 135f + 105f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 6
    private fun generateMediumTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 82f, startY + (-20f), 144f, 35f, isFallingPlatform = true))
        ib.add(BounceInteractiveBlock(id = 20000 + 6, type = InteractiveType.BREAKABLE, x = startX + 140f + 82f + 40f, y = startY + (-20f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 82f + 144f + 116f, startY + (-20f) + (0f), 188f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 82f + 72.0f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 82f + 144f + 116f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 7
    private fun generateMediumTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 89f, startY + (0f), 153f, 40f))
        p.add(BounceObstacle(startX + 140f + 89f + 153f + 127f, startY + (0f) + (20f), 201f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 89f + 76.5f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 89f + 153f + 127f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 570f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 8
    private fun generateMediumTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 96f, startY + (20f), 162f, 40f))
        p.add(BounceObstacle(startX + 140f + 96f + 162f + 98f, startY + (20f) + (40f), 164f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 96f + 81.0f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 96f + 162f + 98f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 520f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 9
    private fun generateMediumTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 103f, startY + (40f), 131f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 103f + 40f, startY + (40f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 103f + 131f + 109f, startY + (40f) + (-40f), 177f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 103f + 65.5f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 103f + 131f + 109f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 520f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 10
    private fun generateMediumTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 110f, startY + (-40f), 140f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 10, type = InteractiveType.BREAKABLE, x = startX + 140f + 110f + 40f, y = startY + (-40f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 110f + 140f + 120f, startY + (-40f) + (-20f), 190f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 110f - 20f, y = startY + 80f, width = 300f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 110f + 70.0f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 110f + 140f + 120f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 560f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 11
    private fun generateMediumTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 117f, startY + (-20f), 149f, 40f))
        p.add(BounceObstacle(startX + 140f + 117f + 149f + 91f, startY + (-20f) + (0f), 203f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 117f + 74.5f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 117f + 149f + 91f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 560f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 12
    private fun generateMediumTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 84f, startY + (0f), 158f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 84f + 158f + 102f, startY + (0f) + (20f), 166f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 84f + 79.0f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 84f + 158f + 102f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 510f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 13
    private fun generateMediumTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 91f, startY + (20f), 167f, 40f))
        p.add(BounceObstacle(startX + 140f + 91f + 40f, startY + (20f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 91f + 167f + 113f, startY + (20f) + (40f), 179f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 91f + 83.5f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 91f + 167f + 113f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 550f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 14
    private fun generateMediumTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 98f, startY + (40f), 136f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 14, type = InteractiveType.BREAKABLE, x = startX + 140f + 98f + 40f, y = startY + (40f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 98f + 136f + 124f, startY + (40f) + (-40f), 192f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 98f + 68.0f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 98f + 136f + 124f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 550f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 15
    private fun generateMediumTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 105f, startY + (-40f), 145f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 105f + 145f + 95f, startY + (-40f) + (-20f), 205f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 105f - 20f, y = startY + 80f, width = 280f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 105f + 72.5f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 105f + 145f + 95f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 550f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 16
    private fun generateMediumTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 112f, startY + (-20f), 154f, 40f))
        p.add(BounceObstacle(startX + 140f + 112f + 154f + 106f, startY + (-20f) + (0f), 168f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 112f + 77.0f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 112f + 154f + 106f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 17
    private fun generateMediumTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 119f, startY + (0f), 163f, 40f))
        p.add(BounceObstacle(startX + 140f + 119f + 40f, startY + (0f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 119f + 163f + 117f, startY + (0f) + (20f), 181f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 119f + 81.5f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 119f + 163f + 117f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 580f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 18
    private fun generateMediumTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 86f, startY + (20f), 132f, 35f, isFallingPlatform = true))
        ib.add(BounceInteractiveBlock(id = 20000 + 18, type = InteractiveType.BREAKABLE, x = startX + 140f + 86f + 40f, y = startY + (20f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 86f + 132f + 128f, startY + (20f) + (40f), 194f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 86f + 66.0f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 86f + 132f + 128f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 19
    private fun generateMediumTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 93f, startY + (40f), 141f, 40f))
        p.add(BounceObstacle(startX + 140f + 93f + 141f + 99f, startY + (40f) + (-40f), 207f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 93f + 70.5f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 93f + 141f + 99f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 20
    private fun generateMediumTemplate20(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 100f, startY + (-40f), 150f, 40f))
        p.add(BounceObstacle(startX + 140f + 100f + 150f + 110f, startY + (-40f) + (-20f), 170f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 100f - 20f, y = startY + 80f, width = 300f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 100f + 75.0f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 100f + 150f + 110f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 21
    private fun generateMediumTemplate21(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 107f, startY + (-20f), 159f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 107f + 40f, startY + (-20f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 107f + 159f + 121f, startY + (-20f) + (0f), 183f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 107f + 79.5f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 107f + 159f + 121f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 570f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 22
    private fun generateMediumTemplate22(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 114f, startY + (0f), 168f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 22, type = InteractiveType.BREAKABLE, x = startX + 140f + 114f + 40f, y = startY + (0f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 114f + 168f + 92f, startY + (0f) + (20f), 196f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 114f + 84.0f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 114f + 168f + 92f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 570f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 23
    private fun generateMediumTemplate23(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 81f, startY + (20f), 137f, 40f))
        p.add(BounceObstacle(startX + 140f + 81f + 137f + 103f, startY + (20f) + (40f), 209f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 81f + 68.5f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 81f + 137f + 103f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 24
    private fun generateMediumTemplate24(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 88f, startY + (40f), 146f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 88f + 146f + 114f, startY + (40f) + (-40f), 172f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 88f + 73.0f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 88f + 146f + 114f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 520f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 25
    private fun generateMediumTemplate25(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 95f, startY + (-40f), 155f, 40f))
        p.add(BounceObstacle(startX + 140f + 95f + 40f, startY + (-40f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 95f + 155f + 125f, startY + (-40f) + (-20f), 185f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 95f - 20f, y = startY + 80f, width = 320f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 95f + 77.5f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 95f + 155f + 125f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 560f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 26
    private fun generateMediumTemplate26(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 102f, startY + (-20f), 164f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 26, type = InteractiveType.BREAKABLE, x = startX + 140f + 102f + 40f, y = startY + (-20f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 102f + 164f + 96f, startY + (-20f) + (0f), 198f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 102f + 82.0f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 102f + 164f + 96f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 560f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 27
    private fun generateMediumTemplate27(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 109f, startY + (0f), 133f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 109f + 133f + 107f, startY + (0f) + (20f), 161f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 109f + 66.5f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 109f + 133f + 107f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 510f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 28
    private fun generateMediumTemplate28(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 116f, startY + (20f), 142f, 40f))
        p.add(BounceObstacle(startX + 140f + 116f + 142f + 118f, startY + (20f) + (40f), 174f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 116f + 71.0f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 116f + 142f + 118f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 550f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 29
    private fun generateMediumTemplate29(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 83f, startY + (40f), 151f, 40f))
        p.add(BounceObstacle(startX + 140f + 83f + 40f, startY + (40f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 83f + 151f + 129f, startY + (40f) + (-40f), 187f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 83f + 75.5f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 83f + 151f + 129f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 550f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 30
    private fun generateMediumTemplate30(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 90f, startY + (-40f), 160f, 35f, isFallingPlatform = true))
        ib.add(BounceInteractiveBlock(id = 20000 + 30, type = InteractiveType.BREAKABLE, x = startX + 140f + 90f + 40f, y = startY + (-40f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 90f + 160f + 100f, startY + (-40f) + (-20f), 200f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 90f - 20f, y = startY + 80f, width = 300f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 90f + 80.0f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 90f + 160f + 100f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 550f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 31
    private fun generateMediumTemplate31(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 97f, startY + (-20f), 169f, 40f))
        p.add(BounceObstacle(startX + 140f + 97f + 169f + 111f, startY + (-20f) + (0f), 163f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 97f + 84.5f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 97f + 169f + 111f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 32
    private fun generateMediumTemplate32(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 104f, startY + (0f), 138f, 40f))
        p.add(BounceObstacle(startX + 140f + 104f + 138f + 122f, startY + (0f) + (20f), 176f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 104f + 69.0f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 104f + 138f + 122f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 33
    private fun generateMediumTemplate33(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 111f, startY + (20f), 147f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 111f + 40f, startY + (20f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 111f + 147f + 93f, startY + (20f) + (40f), 189f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 111f + 73.5f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 111f + 147f + 93f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 540f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 34
    private fun generateMediumTemplate34(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 118f, startY + (40f), 156f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 34, type = InteractiveType.BREAKABLE, x = startX + 140f + 118f + 40f, y = startY + (40f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 118f + 156f + 104f, startY + (40f) + (-40f), 202f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 118f + 78.0f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 118f + 156f + 104f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 580f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    // Medium Template 35
    private fun generateMediumTemplate35(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 85f, startY + (-40f), 165f, 40f))
        p.add(BounceObstacle(startX + 140f + 85f + 165f + 115f, startY + (-40f) + (-20f), 165f, 40f))
        wz.add(BounceWaterZone(x = startX + 140f + 85f - 20f, y = startY + 80f, width = 320f, height = 120f))
        c.add(BounceCollectible(x = startX + 140f + 85f + 82.5f, y = startY + (-40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 85f + 165f + 115f + 50f, y = startY + (-40f) + (-20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-40f) + (-20f))
    }

    // Medium Template 36
    private fun generateMediumTemplate36(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 92f, startY + (-20f), 134f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 92f + 134f + 126f, startY + (-20f) + (0f), 178f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 92f + 67.0f, y = startY + (-20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 92f + 134f + 126f + 50f, y = startY + (-20f) + (0f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (-20f) + (0f))
    }

    // Medium Template 37
    private fun generateMediumTemplate37(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 99f, startY + (0f), 143f, 40f))
        p.add(BounceObstacle(startX + 140f + 99f + 40f, startY + (0f) - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 140f + 99f + 143f + 97f, startY + (0f) + (20f), 191f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 99f + 71.5f, y = startY + (0f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 99f + 143f + 97f + 50f, y = startY + (0f) + (20f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 530f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (0f) + (20f))
    }

    // Medium Template 38
    private fun generateMediumTemplate38(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 106f, startY + (20f), 152f, 40f))
        ib.add(BounceInteractiveBlock(id = 20000 + 38, type = InteractiveType.BREAKABLE, x = startX + 140f + 106f + 40f, y = startY + (20f) - 40f, width = 40f, height = 40f))
        p.add(BounceObstacle(startX + 140f + 106f + 152f + 108f, startY + (20f) + (40f), 204f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 106f + 76.0f, y = startY + (20f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 106f + 152f + 108f + 50f, y = startY + (20f) + (40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 570f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (20f) + (40f))
    }

    // Medium Template 39
    private fun generateMediumTemplate39(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 140f + 113f, startY + (40f), 161f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 140f + 113f + 161f + 119f, startY + (40f) + (-40f), 167f, 40f))
        c.add(BounceCollectible(x = startX + 140f + 113f + 80.5f, y = startY + (40f) - 55f, isStar = true))
        c.add(BounceCollectible(x = startX + 140f + 113f + 161f + 119f + 50f, y = startY + (40f) + (-40f) - 45f, isStar = false))
        return LevelChunk(width = 140f + 560f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = startY + (40f) + (-40f))
    }

    fun generateFinalChallengeChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {
        val v = (variation % 40 + 40) % 40
        return when (v) {
            0 -> generateFinalChallengeTemplate0(startX, startY, levelNum, difficulty)
            1 -> generateFinalChallengeTemplate1(startX, startY, levelNum, difficulty)
            2 -> generateFinalChallengeTemplate2(startX, startY, levelNum, difficulty)
            3 -> generateFinalChallengeTemplate3(startX, startY, levelNum, difficulty)
            4 -> generateFinalChallengeTemplate4(startX, startY, levelNum, difficulty)
            5 -> generateFinalChallengeTemplate5(startX, startY, levelNum, difficulty)
            6 -> generateFinalChallengeTemplate6(startX, startY, levelNum, difficulty)
            7 -> generateFinalChallengeTemplate7(startX, startY, levelNum, difficulty)
            8 -> generateFinalChallengeTemplate8(startX, startY, levelNum, difficulty)
            9 -> generateFinalChallengeTemplate9(startX, startY, levelNum, difficulty)
            10 -> generateFinalChallengeTemplate10(startX, startY, levelNum, difficulty)
            11 -> generateFinalChallengeTemplate11(startX, startY, levelNum, difficulty)
            12 -> generateFinalChallengeTemplate12(startX, startY, levelNum, difficulty)
            13 -> generateFinalChallengeTemplate13(startX, startY, levelNum, difficulty)
            14 -> generateFinalChallengeTemplate14(startX, startY, levelNum, difficulty)
            15 -> generateFinalChallengeTemplate15(startX, startY, levelNum, difficulty)
            16 -> generateFinalChallengeTemplate16(startX, startY, levelNum, difficulty)
            17 -> generateFinalChallengeTemplate17(startX, startY, levelNum, difficulty)
            18 -> generateFinalChallengeTemplate18(startX, startY, levelNum, difficulty)
            19 -> generateFinalChallengeTemplate19(startX, startY, levelNum, difficulty)
            20 -> generateFinalChallengeTemplate20(startX, startY, levelNum, difficulty)
            21 -> generateFinalChallengeTemplate21(startX, startY, levelNum, difficulty)
            22 -> generateFinalChallengeTemplate22(startX, startY, levelNum, difficulty)
            23 -> generateFinalChallengeTemplate23(startX, startY, levelNum, difficulty)
            24 -> generateFinalChallengeTemplate24(startX, startY, levelNum, difficulty)
            25 -> generateFinalChallengeTemplate25(startX, startY, levelNum, difficulty)
            26 -> generateFinalChallengeTemplate26(startX, startY, levelNum, difficulty)
            27 -> generateFinalChallengeTemplate27(startX, startY, levelNum, difficulty)
            28 -> generateFinalChallengeTemplate28(startX, startY, levelNum, difficulty)
            29 -> generateFinalChallengeTemplate29(startX, startY, levelNum, difficulty)
            30 -> generateFinalChallengeTemplate30(startX, startY, levelNum, difficulty)
            31 -> generateFinalChallengeTemplate31(startX, startY, levelNum, difficulty)
            32 -> generateFinalChallengeTemplate32(startX, startY, levelNum, difficulty)
            33 -> generateFinalChallengeTemplate33(startX, startY, levelNum, difficulty)
            34 -> generateFinalChallengeTemplate34(startX, startY, levelNum, difficulty)
            35 -> generateFinalChallengeTemplate35(startX, startY, levelNum, difficulty)
            36 -> generateFinalChallengeTemplate36(startX, startY, levelNum, difficulty)
            37 -> generateFinalChallengeTemplate37(startX, startY, levelNum, difficulty)
            38 -> generateFinalChallengeTemplate38(startX, startY, levelNum, difficulty)
            39 -> generateFinalChallengeTemplate39(startX, startY, levelNum, difficulty)
            else -> generateFinalChallengeTemplate0(startX, startY, levelNum, difficulty)
        }
    }

    // Final Challenge Template 0
    private fun generateFinalChallengeTemplate0(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 90f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 90f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 100f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 0, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 90f + 110f + 100f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 100f + 140f + 90f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 100f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 100f + 140f + 90f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 690f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 1
    private fun generateFinalChallengeTemplate1(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 95f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 95f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 107f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 1, type = EnemyType.FLYING, x = startX + 130f + 95f + 110f + 107f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 107f + 140f + 99f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 107f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 107f + 140f + 99f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 711f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 2
    private fun generateFinalChallengeTemplate2(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 100f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 100f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 114f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 2, type = EnemyType.WALKING, x = startX + 130f + 100f + 110f + 114f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 114f + 140f + 108f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 114f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 114f + 140f + 108f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 3
    private fun generateFinalChallengeTemplate3(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 105f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 105f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 121f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 3, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 105f + 110f + 121f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 121f + 140f + 117f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 121f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 121f + 140f + 117f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 753f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 4
    private fun generateFinalChallengeTemplate4(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 110f, startY - 40f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 110f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 128f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 4, type = EnemyType.FLYING, x = startX + 130f + 110f + 110f + 128f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 128f + 140f + 91f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 128f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 128f + 140f + 91f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 739f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 5
    private fun generateFinalChallengeTemplate5(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 115f, startY - 60f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 115f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 100f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 5, type = EnemyType.WALKING, x = startX + 130f + 115f + 110f + 100f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 100f + 140f + 100f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 100f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 100f + 140f + 100f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 725f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 6
    private fun generateFinalChallengeTemplate6(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 120f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 120f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 107f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 6, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 120f + 110f + 107f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 107f + 140f + 109f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 107f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 107f + 140f + 109f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 746f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 7
    private fun generateFinalChallengeTemplate7(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 90f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 90f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 114f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 7, type = EnemyType.FLYING, x = startX + 130f + 90f + 110f + 114f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 114f + 140f + 118f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 114f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 114f + 140f + 118f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 8
    private fun generateFinalChallengeTemplate8(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 95f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 95f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 121f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 8, type = EnemyType.WALKING, x = startX + 130f + 95f + 110f + 121f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 121f + 140f + 92f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 121f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 121f + 140f + 92f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 718f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 9
    private fun generateFinalChallengeTemplate9(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 100f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 100f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 128f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 9, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 100f + 110f + 128f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 128f + 140f + 101f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 128f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 128f + 140f + 101f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 739f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 10
    private fun generateFinalChallengeTemplate10(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 105f, startY - 40f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 105f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 100f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 10, type = EnemyType.FLYING, x = startX + 130f + 105f + 110f + 100f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 100f + 140f + 110f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 100f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 100f + 140f + 110f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 725f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 11
    private fun generateFinalChallengeTemplate11(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 110f, startY - 60f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 110f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 107f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 11, type = EnemyType.WALKING, x = startX + 130f + 110f + 110f + 107f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 107f + 140f + 119f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 107f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 107f + 140f + 119f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 746f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 12
    private fun generateFinalChallengeTemplate12(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 115f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 115f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 114f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 12, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 115f + 110f + 114f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 114f + 140f + 93f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 114f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 114f + 140f + 93f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 13
    private fun generateFinalChallengeTemplate13(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 120f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 120f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 121f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 13, type = EnemyType.FLYING, x = startX + 130f + 120f + 110f + 121f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 121f + 140f + 102f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 121f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 121f + 140f + 102f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 753f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 14
    private fun generateFinalChallengeTemplate14(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 90f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 90f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 128f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 14, type = EnemyType.WALKING, x = startX + 130f + 90f + 110f + 128f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 128f + 140f + 111f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 128f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 128f + 140f + 111f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 739f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 15
    private fun generateFinalChallengeTemplate15(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 95f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 95f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 100f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 15, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 95f + 110f + 100f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 100f + 140f + 120f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 100f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 100f + 140f + 120f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 725f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 16
    private fun generateFinalChallengeTemplate16(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 100f, startY - 40f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 100f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 107f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 16, type = EnemyType.FLYING, x = startX + 130f + 100f + 110f + 107f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 107f + 140f + 94f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 107f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 107f + 140f + 94f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 711f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 17
    private fun generateFinalChallengeTemplate17(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 105f, startY - 60f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 105f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 114f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 17, type = EnemyType.WALKING, x = startX + 130f + 105f + 110f + 114f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 114f + 140f + 103f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 114f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 114f + 140f + 103f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 18
    private fun generateFinalChallengeTemplate18(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 110f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 110f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 121f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 18, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 110f + 110f + 121f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 121f + 140f + 112f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 121f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 121f + 140f + 112f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 753f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 19
    private fun generateFinalChallengeTemplate19(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 115f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 115f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 128f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 19, type = EnemyType.FLYING, x = startX + 130f + 115f + 110f + 128f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 128f + 140f + 121f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 128f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 128f + 140f + 121f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 774f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 20
    private fun generateFinalChallengeTemplate20(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 120f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 120f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 100f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 20, type = EnemyType.WALKING, x = startX + 130f + 120f + 110f + 100f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 100f + 140f + 95f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 100f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 100f + 140f + 95f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 725f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 21
    private fun generateFinalChallengeTemplate21(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 90f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 90f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 107f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 21, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 90f + 110f + 107f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 107f + 140f + 104f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 107f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 107f + 140f + 104f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 711f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 22
    private fun generateFinalChallengeTemplate22(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 95f, startY - 40f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 95f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 114f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 22, type = EnemyType.FLYING, x = startX + 130f + 95f + 110f + 114f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 114f + 140f + 113f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 114f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 114f + 140f + 113f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 23
    private fun generateFinalChallengeTemplate23(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 100f, startY - 60f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 100f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 121f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 23, type = EnemyType.WALKING, x = startX + 130f + 100f + 110f + 121f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 121f + 140f + 122f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 121f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 121f + 140f + 122f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 753f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 24
    private fun generateFinalChallengeTemplate24(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 105f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 105f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 128f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 24, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 105f + 110f + 128f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 128f + 140f + 96f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 128f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 128f + 140f + 96f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 739f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 25
    private fun generateFinalChallengeTemplate25(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 110f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 110f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 100f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 25, type = EnemyType.FLYING, x = startX + 130f + 110f + 110f + 100f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 100f + 140f + 105f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 100f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 100f + 140f + 105f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 725f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 26
    private fun generateFinalChallengeTemplate26(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 115f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 115f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 107f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 26, type = EnemyType.WALKING, x = startX + 130f + 115f + 110f + 107f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 107f + 140f + 114f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 107f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 107f + 140f + 114f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 746f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 27
    private fun generateFinalChallengeTemplate27(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 120f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 120f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 114f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 27, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 120f + 110f + 114f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 114f + 140f + 123f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 114f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 114f + 140f + 123f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 767f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 28
    private fun generateFinalChallengeTemplate28(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 90f, startY - 40f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 90f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 121f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 28, type = EnemyType.FLYING, x = startX + 130f + 90f + 110f + 121f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 121f + 140f + 97f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 121f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 121f + 140f + 97f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 718f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 29
    private fun generateFinalChallengeTemplate29(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 95f, startY - 60f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 95f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 128f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 29, type = EnemyType.WALKING, x = startX + 130f + 95f + 110f + 128f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 128f + 140f + 106f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 128f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 128f + 140f + 106f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 739f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 30
    private fun generateFinalChallengeTemplate30(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 100f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 100f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 100f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 30, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 100f + 110f + 100f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 100f + 140f + 115f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 100f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 100f + 140f + 115f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 725f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 31
    private fun generateFinalChallengeTemplate31(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 105f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 105f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 107f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 31, type = EnemyType.FLYING, x = startX + 130f + 105f + 110f + 107f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 107f + 140f + 124f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 107f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 107f + 140f + 124f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 746f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 32
    private fun generateFinalChallengeTemplate32(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 110f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 110f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 114f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 32, type = EnemyType.WALKING, x = startX + 130f + 110f + 110f + 114f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 114f + 140f + 98f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 114f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 114f + 140f + 98f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 33
    private fun generateFinalChallengeTemplate33(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 115f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 115f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 121f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 33, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 115f + 110f + 121f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 115f + 110f + 121f + 140f + 107f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 121f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 115f + 110f + 121f + 140f + 107f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 753f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 34
    private fun generateFinalChallengeTemplate34(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 120f, startY - 40f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 120f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 128f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 34, type = EnemyType.FLYING, x = startX + 130f + 120f + 110f + 128f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 120f + 110f + 128f + 140f + 116f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 128f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 120f + 110f + 128f + 140f + 116f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 774f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 35
    private fun generateFinalChallengeTemplate35(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 90f, startY - 60f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 90f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 100f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 35, type = EnemyType.WALKING, x = startX + 130f + 90f + 110f + 100f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 90f + 110f + 100f + 140f + 90f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 100f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 90f + 110f + 100f + 140f + 90f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 690f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 36
    private fun generateFinalChallengeTemplate36(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 95f, startY - 20f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 95f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 107f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 36, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 95f + 110f + 107f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 95f + 110f + 107f + 140f + 99f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 107f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 95f + 110f + 107f + 140f + 99f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 711f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    // Final Challenge Template 37
    private fun generateFinalChallengeTemplate37(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 100f, startY - 40f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 100f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 114f, startY - 40f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 37, type = EnemyType.FLYING, x = startX + 130f + 100f + 110f + 114f + 70f, y = startY - 40f - 20f - 90f, moveRangeY = 50f, moveSpeed = 60f))
        p.add(BounceObstacle(startX + 130f + 100f + 110f + 114f + 140f + 108f, startY - 40f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 114f + 70f, y = startY - 40f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 100f + 110f + 114f + 140f + 108f + 50f, y = startY - 40f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 732f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 40f - 20f + 30f)
    }

    // Final Challenge Template 38
    private fun generateFinalChallengeTemplate38(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 105f, startY - 60f, 110f, 35f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 130f + 105f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 121f, startY - 60f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 38, type = EnemyType.WALKING, x = startX + 130f + 105f + 110f + 121f + 70f, y = startY - 60f - 20f - 28f, moveRangeX = 60f, moveSpeed = 70f))
        p.add(BounceObstacle(startX + 130f + 105f + 110f + 121f + 140f + 117f, startY - 60f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 121f + 70f, y = startY - 60f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 105f + 110f + 121f + 140f + 117f + 50f, y = startY - 60f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 753f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 60f - 20f + 30f)
    }

    // Final Challenge Template 39
    private fun generateFinalChallengeTemplate39(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        val wz = mutableListOf<BounceWaterZone>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 130f + 110f, startY - 20f, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))
        p.add(BounceObstacle(startX + 130f + 110f - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 128f, startY - 20f - 20f, 140f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 800 + 39, type = EnemyType.ROTATING_HAZARD, x = startX + 130f + 110f + 110f + 128f + 70f, y = startY - 20f - 20f - 70f, moveSpeed = 100f + difficulty * 20f))
        p.add(BounceObstacle(startX + 130f + 110f + 110f + 128f + 140f + 91f, startY - 20f - 20f + 30f, 160f, 40f))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 128f + 70f, y = startY - 20f - 20f - 50f, isStar = true))
        c.add(BounceCollectible(x = startX + 130f + 110f + 110f + 128f + 140f + 91f + 50f, y = startY - 20f - 20f + 30f - 45f, isStar = false))
        return LevelChunk(width = 130f + 739f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = startY - 20f - 20f + 30f)
    }

    fun generateSecretChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 20 + 20) % 20
        return when (v) {
            0 -> generateSecretTemplate0(startX, startY)
            1 -> generateSecretTemplate1(startX, startY)
            2 -> generateSecretTemplate2(startX, startY)
            3 -> generateSecretTemplate3(startX, startY)
            4 -> generateSecretTemplate4(startX, startY)
            5 -> generateSecretTemplate5(startX, startY)
            6 -> generateSecretTemplate6(startX, startY)
            7 -> generateSecretTemplate7(startX, startY)
            8 -> generateSecretTemplate8(startX, startY)
            9 -> generateSecretTemplate9(startX, startY)
            10 -> generateSecretTemplate10(startX, startY)
            11 -> generateSecretTemplate11(startX, startY)
            12 -> generateSecretTemplate12(startX, startY)
            13 -> generateSecretTemplate13(startX, startY)
            14 -> generateSecretTemplate14(startX, startY)
            15 -> generateSecretTemplate15(startX, startY)
            16 -> generateSecretTemplate16(startX, startY)
            17 -> generateSecretTemplate17(startX, startY)
            18 -> generateSecretTemplate18(startX, startY)
            19 -> generateSecretTemplate19(startX, startY)
            else -> generateSecretTemplate0(startX, startY)
        }
    }

    // Secret Template 0
    private fun generateSecretTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 160f, startY - 120f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 0, type = InteractiveType.BREAKABLE, x = startX + 160f - 40f, y = startY - 120f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 160f + 70f, y = startY - 120f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 1
    private fun generateSecretTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 165f, startY - 135f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 1, type = InteractiveType.BREAKABLE, x = startX + 165f - 40f, y = startY - 135f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 165f + 70f, y = startY - 135f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 2
    private fun generateSecretTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 170f, startY - 150f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 2, type = InteractiveType.BREAKABLE, x = startX + 170f - 40f, y = startY - 150f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 170f + 70f, y = startY - 150f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 3
    private fun generateSecretTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 175f, startY - 165f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 3, type = InteractiveType.BREAKABLE, x = startX + 175f - 40f, y = startY - 165f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 175f + 70f, y = startY - 165f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 4
    private fun generateSecretTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 120f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 4, type = InteractiveType.BREAKABLE, x = startX + 180f - 40f, y = startY - 120f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 180f + 70f, y = startY - 120f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 5
    private fun generateSecretTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 185f, startY - 135f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 5, type = InteractiveType.BREAKABLE, x = startX + 185f - 40f, y = startY - 135f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 185f + 70f, y = startY - 135f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 6
    private fun generateSecretTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 190f, startY - 150f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 6, type = InteractiveType.BREAKABLE, x = startX + 190f - 40f, y = startY - 150f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 190f + 70f, y = startY - 150f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 7
    private fun generateSecretTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 195f, startY - 165f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 7, type = InteractiveType.BREAKABLE, x = startX + 195f - 40f, y = startY - 165f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 195f + 70f, y = startY - 165f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 8
    private fun generateSecretTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 120f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 8, type = InteractiveType.BREAKABLE, x = startX + 200f - 40f, y = startY - 120f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 200f + 70f, y = startY - 120f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 9
    private fun generateSecretTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 205f, startY - 135f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 9, type = InteractiveType.BREAKABLE, x = startX + 205f - 40f, y = startY - 135f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 205f + 70f, y = startY - 135f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 10
    private fun generateSecretTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 150f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 10, type = InteractiveType.BREAKABLE, x = startX + 210f - 40f, y = startY - 150f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 210f + 70f, y = startY - 150f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 11
    private fun generateSecretTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 215f, startY - 165f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 11, type = InteractiveType.BREAKABLE, x = startX + 215f - 40f, y = startY - 165f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 215f + 70f, y = startY - 165f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 12
    private fun generateSecretTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 120f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 12, type = InteractiveType.BREAKABLE, x = startX + 220f - 40f, y = startY - 120f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 220f + 70f, y = startY - 120f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 13
    private fun generateSecretTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 225f, startY - 135f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 13, type = InteractiveType.BREAKABLE, x = startX + 225f - 40f, y = startY - 135f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 225f + 70f, y = startY - 135f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 14
    private fun generateSecretTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 230f, startY - 150f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 14, type = InteractiveType.BREAKABLE, x = startX + 230f - 40f, y = startY - 150f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 230f + 70f, y = startY - 150f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 15
    private fun generateSecretTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 235f, startY - 165f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 15, type = InteractiveType.BREAKABLE, x = startX + 235f - 40f, y = startY - 165f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 235f + 70f, y = startY - 165f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 16
    private fun generateSecretTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 120f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 16, type = InteractiveType.BREAKABLE, x = startX + 240f - 40f, y = startY - 120f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 240f + 70f, y = startY - 120f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 17
    private fun generateSecretTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 245f, startY - 135f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 17, type = InteractiveType.BREAKABLE, x = startX + 245f - 40f, y = startY - 135f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 245f + 70f, y = startY - 135f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 18
    private fun generateSecretTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 250f, startY - 150f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 18, type = InteractiveType.BREAKABLE, x = startX + 250f - 40f, y = startY - 150f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 250f + 70f, y = startY - 150f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    // Secret Template 19
    private fun generateSecretTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()
        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 270f, startY + 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 255f, startY - 165f, 140f, 30f))
        ib.add(BounceInteractiveBlock(id = 30000 + 19, type = InteractiveType.BREAKABLE, x = startX + 255f - 40f, y = startY - 165f, width = 40f, height = 30f))
        c.add(BounceCollectible(x = startX + 255f + 70f, y = startY - 165f - 45f, isStar = true, isBonus = true))
        p.add(BounceObstacle(startX + 270f + 280f, startY + 30f - 30f, 170f, 40f))
        c.add(BounceCollectible(x = startX + 270f + 90f, y = startY + 30f - 45f, isStar = true, isBonus = false))
        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY + 30f - 30f)
    }

    fun generateCheckpointChunk(startX: Float, startY: Float, variation: Int, checkpointId: Int): LevelChunk {
        val v = (variation % 15 + 15) % 15
        return when (v) {
            0 -> generateCheckpointTemplate0(startX, startY, checkpointId)
            1 -> generateCheckpointTemplate1(startX, startY, checkpointId)
            2 -> generateCheckpointTemplate2(startX, startY, checkpointId)
            3 -> generateCheckpointTemplate3(startX, startY, checkpointId)
            4 -> generateCheckpointTemplate4(startX, startY, checkpointId)
            5 -> generateCheckpointTemplate5(startX, startY, checkpointId)
            6 -> generateCheckpointTemplate6(startX, startY, checkpointId)
            7 -> generateCheckpointTemplate7(startX, startY, checkpointId)
            8 -> generateCheckpointTemplate8(startX, startY, checkpointId)
            9 -> generateCheckpointTemplate9(startX, startY, checkpointId)
            10 -> generateCheckpointTemplate10(startX, startY, checkpointId)
            11 -> generateCheckpointTemplate11(startX, startY, checkpointId)
            12 -> generateCheckpointTemplate12(startX, startY, checkpointId)
            13 -> generateCheckpointTemplate13(startX, startY, checkpointId)
            14 -> generateCheckpointTemplate14(startX, startY, checkpointId)
            else -> generateCheckpointTemplate0(startX, startY, checkpointId)
        }
    }

    private fun generateCheckpointTemplate0(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 320f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 160.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 270f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 320f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate1(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 332f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 166.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 282f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 332f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate2(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 344f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 172.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 294f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 344f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate3(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 356f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 178.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 306f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 356f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate4(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 368f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 184.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 318f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 368f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate5(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 380f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 190.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 330f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 380f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate6(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 392f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 196.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 342f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 392f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate7(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 404f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 202.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 354f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 404f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate8(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 416f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 208.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 366f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 416f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate9(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 428f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 214.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 378f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 428f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate10(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 440f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 220.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 390f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 440f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate11(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 452f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 226.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 402f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 452f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate12(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 464f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 232.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 414f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 464f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate13(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 476f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 238.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 426f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 476f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    private fun generateCheckpointTemplate14(startX: Float, startY: Float, checkpointId: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = 488f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + 244.0f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + 438f, y = startY - 45f, isStar = false))
        return LevelChunk(width = 488f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    fun generateExitChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 15 + 15) % 15
        return when (v) {
            0 -> generateExitTemplate0(startX, startY)
            1 -> generateExitTemplate1(startX, startY)
            2 -> generateExitTemplate2(startX, startY)
            3 -> generateExitTemplate3(startX, startY)
            4 -> generateExitTemplate4(startX, startY)
            5 -> generateExitTemplate5(startX, startY)
            6 -> generateExitTemplate6(startX, startY)
            7 -> generateExitTemplate7(startX, startY)
            8 -> generateExitTemplate8(startX, startY)
            9 -> generateExitTemplate9(startX, startY)
            10 -> generateExitTemplate10(startX, startY)
            11 -> generateExitTemplate11(startX, startY)
            12 -> generateExitTemplate12(startX, startY)
            13 -> generateExitTemplate13(startX, startY)
            14 -> generateExitTemplate14(startX, startY)
            else -> generateExitTemplate0(startX, startY)
        }
    }

    private fun generateExitTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 400f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 400f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 415f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 415f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 430f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 430f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 445f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 445f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 460f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 460f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 475f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 475f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 490f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 490f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 505f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 505f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 520f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 520f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 535f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 535f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 550f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 550f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 565f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 565f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 580f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 580f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 595f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 595f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    private fun generateExitTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = 610f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = 610f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }

    fun generateVerticalChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 25 + 25) % 25
        return when (v) {
            0 -> generateVerticalTemplate0(startX, startY)
            1 -> generateVerticalTemplate1(startX, startY)
            2 -> generateVerticalTemplate2(startX, startY)
            3 -> generateVerticalTemplate3(startX, startY)
            4 -> generateVerticalTemplate4(startX, startY)
            5 -> generateVerticalTemplate5(startX, startY)
            6 -> generateVerticalTemplate6(startX, startY)
            7 -> generateVerticalTemplate7(startX, startY)
            8 -> generateVerticalTemplate8(startX, startY)
            9 -> generateVerticalTemplate9(startX, startY)
            10 -> generateVerticalTemplate10(startX, startY)
            11 -> generateVerticalTemplate11(startX, startY)
            12 -> generateVerticalTemplate12(startX, startY)
            13 -> generateVerticalTemplate13(startX, startY)
            14 -> generateVerticalTemplate14(startX, startY)
            15 -> generateVerticalTemplate15(startX, startY)
            16 -> generateVerticalTemplate16(startX, startY)
            17 -> generateVerticalTemplate17(startX, startY)
            18 -> generateVerticalTemplate18(startX, startY)
            19 -> generateVerticalTemplate19(startX, startY)
            20 -> generateVerticalTemplate20(startX, startY)
            21 -> generateVerticalTemplate21(startX, startY)
            22 -> generateVerticalTemplate22(startX, startY)
            23 -> generateVerticalTemplate23(startX, startY)
            24 -> generateVerticalTemplate24(startX, startY)
            else -> generateVerticalTemplate0(startX, startY)
        }
    }

    private fun generateVerticalTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate20(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate21(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate22(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate23(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    private fun generateVerticalTemplate24(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }

    fun generateMovingPlatformChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 30 + 30) % 30
        return when (v) {
            0 -> generateMovingTemplate0(startX, startY)
            1 -> generateMovingTemplate1(startX, startY)
            2 -> generateMovingTemplate2(startX, startY)
            3 -> generateMovingTemplate3(startX, startY)
            4 -> generateMovingTemplate4(startX, startY)
            5 -> generateMovingTemplate5(startX, startY)
            6 -> generateMovingTemplate6(startX, startY)
            7 -> generateMovingTemplate7(startX, startY)
            8 -> generateMovingTemplate8(startX, startY)
            9 -> generateMovingTemplate9(startX, startY)
            10 -> generateMovingTemplate10(startX, startY)
            11 -> generateMovingTemplate11(startX, startY)
            12 -> generateMovingTemplate12(startX, startY)
            13 -> generateMovingTemplate13(startX, startY)
            14 -> generateMovingTemplate14(startX, startY)
            15 -> generateMovingTemplate15(startX, startY)
            16 -> generateMovingTemplate16(startX, startY)
            17 -> generateMovingTemplate17(startX, startY)
            18 -> generateMovingTemplate18(startX, startY)
            19 -> generateMovingTemplate19(startX, startY)
            20 -> generateMovingTemplate20(startX, startY)
            21 -> generateMovingTemplate21(startX, startY)
            22 -> generateMovingTemplate22(startX, startY)
            23 -> generateMovingTemplate23(startX, startY)
            24 -> generateMovingTemplate24(startX, startY)
            25 -> generateMovingTemplate25(startX, startY)
            26 -> generateMovingTemplate26(startX, startY)
            27 -> generateMovingTemplate27(startX, startY)
            28 -> generateMovingTemplate28(startX, startY)
            29 -> generateMovingTemplate29(startX, startY)
            else -> generateMovingTemplate0(startX, startY)
        }
    }

    private fun generateMovingTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate20(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate21(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate22(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate23(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate24(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate25(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate26(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate27(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate28(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    private fun generateMovingTemplate29(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }

    fun generateSpringChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 20 + 20) % 20
        return when (v) {
            0 -> generateSpringTemplate0(startX, startY)
            1 -> generateSpringTemplate1(startX, startY)
            2 -> generateSpringTemplate2(startX, startY)
            3 -> generateSpringTemplate3(startX, startY)
            4 -> generateSpringTemplate4(startX, startY)
            5 -> generateSpringTemplate5(startX, startY)
            6 -> generateSpringTemplate6(startX, startY)
            7 -> generateSpringTemplate7(startX, startY)
            8 -> generateSpringTemplate8(startX, startY)
            9 -> generateSpringTemplate9(startX, startY)
            10 -> generateSpringTemplate10(startX, startY)
            11 -> generateSpringTemplate11(startX, startY)
            12 -> generateSpringTemplate12(startX, startY)
            13 -> generateSpringTemplate13(startX, startY)
            14 -> generateSpringTemplate14(startX, startY)
            15 -> generateSpringTemplate15(startX, startY)
            16 -> generateSpringTemplate16(startX, startY)
            17 -> generateSpringTemplate17(startX, startY)
            18 -> generateSpringTemplate18(startX, startY)
            19 -> generateSpringTemplate19(startX, startY)
            else -> generateSpringTemplate0(startX, startY)
        }
    }

    private fun generateSpringTemplate0(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate1(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate2(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate3(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate4(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate5(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate6(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate7(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate8(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate9(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate10(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate11(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate12(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate13(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate14(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate15(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate16(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate17(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate18(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    private fun generateSpringTemplate19(startX: Float, startY: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    fun generateEnemyChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {
        val v = (variation % 30 + 30) % 30
        return when (v) {
            0 -> generateEnemyTemplate0(startX, startY, levelNum, difficulty)
            1 -> generateEnemyTemplate1(startX, startY, levelNum, difficulty)
            2 -> generateEnemyTemplate2(startX, startY, levelNum, difficulty)
            3 -> generateEnemyTemplate3(startX, startY, levelNum, difficulty)
            4 -> generateEnemyTemplate4(startX, startY, levelNum, difficulty)
            5 -> generateEnemyTemplate5(startX, startY, levelNum, difficulty)
            6 -> generateEnemyTemplate6(startX, startY, levelNum, difficulty)
            7 -> generateEnemyTemplate7(startX, startY, levelNum, difficulty)
            8 -> generateEnemyTemplate8(startX, startY, levelNum, difficulty)
            9 -> generateEnemyTemplate9(startX, startY, levelNum, difficulty)
            10 -> generateEnemyTemplate10(startX, startY, levelNum, difficulty)
            11 -> generateEnemyTemplate11(startX, startY, levelNum, difficulty)
            12 -> generateEnemyTemplate12(startX, startY, levelNum, difficulty)
            13 -> generateEnemyTemplate13(startX, startY, levelNum, difficulty)
            14 -> generateEnemyTemplate14(startX, startY, levelNum, difficulty)
            15 -> generateEnemyTemplate15(startX, startY, levelNum, difficulty)
            16 -> generateEnemyTemplate16(startX, startY, levelNum, difficulty)
            17 -> generateEnemyTemplate17(startX, startY, levelNum, difficulty)
            18 -> generateEnemyTemplate18(startX, startY, levelNum, difficulty)
            19 -> generateEnemyTemplate19(startX, startY, levelNum, difficulty)
            20 -> generateEnemyTemplate20(startX, startY, levelNum, difficulty)
            21 -> generateEnemyTemplate21(startX, startY, levelNum, difficulty)
            22 -> generateEnemyTemplate22(startX, startY, levelNum, difficulty)
            23 -> generateEnemyTemplate23(startX, startY, levelNum, difficulty)
            24 -> generateEnemyTemplate24(startX, startY, levelNum, difficulty)
            25 -> generateEnemyTemplate25(startX, startY, levelNum, difficulty)
            26 -> generateEnemyTemplate26(startX, startY, levelNum, difficulty)
            27 -> generateEnemyTemplate27(startX, startY, levelNum, difficulty)
            28 -> generateEnemyTemplate28(startX, startY, levelNum, difficulty)
            29 -> generateEnemyTemplate29(startX, startY, levelNum, difficulty)
            else -> generateEnemyTemplate0(startX, startY, levelNum, difficulty)
        }
    }

    private fun generateEnemyTemplate0(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 55f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 220f, 40f))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 700, type = EnemyType.WALKING, x = startX + 310f, y = startY - 58f, moveRangeX = 85f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 701, type = EnemyType.FLYING, x = startX + 440f, y = startY - 110f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp + 10f))
        c.add(BounceCollectible(startX + 310f, startY - 95f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate1(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 60f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 180f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.07f))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 702, type = EnemyType.FLYING, x = startX + 290f, y = startY - 90f, moveRangeX = 80f, moveRangeY = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 703, type = EnemyType.WALKING, x = startX + 540f, y = startY - 18f, moveRangeX = 50f, moveSpeed = sp - 10f))
        c.add(BounceCollectible(startX + 290f, startY - 140f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate2(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 65f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 220f, 40f))
        p.add(BounceObstacle(startX + 280f, startY - 40f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 490f, startY, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 704, type = EnemyType.WALKING, x = startX + 370f, y = startY - 48f, moveRangeX = 45f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 705, type = EnemyType.FLYING, x = startX + 170f, y = startY - 100f, moveRangeY = 50f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 370f, startY - 90f, isStar = true))
        return LevelChunk(width = 650f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY)
    }

    private fun generateEnemyTemplate3(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 50f, 160f, 40f))
        p.add(BounceObstacle(startX + 450f, startY - 120f, 140f, 35f))
        p.add(BounceObstacle(startX + 660f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 706, type = EnemyType.ROTATING_HAZARD, x = startX + 180f, y = startY - 20f, moveSpeed = 160f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 707, type = EnemyType.WALKING, x = startX + 300f, y = startY - 78f, moveRangeX = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 708, type = EnemyType.FLYING, x = startX + 520f, y = startY - 180f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 520f, startY - 165f, isStar = true))
        return LevelChunk(width = 820f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate4(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 65f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 10f, 140f, 30f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 400f, startY - 40f, 200f, 40f))
        p.add(BounceObstacle(startX + 660f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 709, type = EnemyType.FLYING, x = startX + 270f, y = startY - 80f, moveRangeX = 70f, moveRangeY = 40f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 710, type = EnemyType.WALKING, x = startX + 500f, y = startY - 68f, moveRangeX = 75f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 500f, startY - 110f, isStar = true))
        return LevelChunk(width = 820f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate5(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 180f, 40f))
        p.add(BounceObstacle(startX + 440f, startY - 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 680f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 711, type = EnemyType.WALKING, x = startX + 290f, y = startY - 48f, moveRangeX = 60f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 712, type = EnemyType.FLYING, x = startX + 410f, y = startY - 100f, moveRangeY = 60f, moveSpeed = sp + 5f))
        e.add(BounceEnemy(id = levelNum * 1000 + 713, type = EnemyType.ROTATING_HAZARD, x = startX + 650f, y = startY - 10f, moveSpeed = 170f + difficulty * 20f))
        c.add(BounceCollectible(startX + 530f, startY - 80f, isStar = true))
        return LevelChunk(width = 840f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate6(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 65f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 220f, startY + 30f, 40f, 30f, isSpike = true, isMoving = true, moveRangeX = 90f, moveSpeed = 0.08f))
        p.add(BounceObstacle(startX + 380f, startY - 20f, 220f, 40f))
        p.add(BounceObstacle(startX + 660f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 714, type = EnemyType.FLYING, x = startX + 260f, y = startY - 90f, moveRangeX = 80f, moveRangeY = 40f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 715, type = EnemyType.WALKING, x = startX + 490f, y = startY - 48f, moveRangeX = 80f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 490f, startY - 90f, isStar = true))
        return LevelChunk(width = 820f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate7(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 10f, 50f, 25f, isSpring = true, springForce = -700f))
        p.add(BounceObstacle(startX + 320f, startY - 180f, 200f, 35f))
        p.add(BounceObstacle(startX + 580f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 716, type = EnemyType.FLYING, x = startX + 280f, y = startY - 230f, moveRangeX = 70f, moveRangeY = 40f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 717, type = EnemyType.FLYING, x = startX + 440f, y = startY - 220f, moveRangeX = 60f, moveRangeY = 50f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 718, type = EnemyType.WALKING, x = startX + 420f, y = startY - 208f, moveRangeX = 60f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 420f, startY - 250f, isStar = true))
        return LevelChunk(width = 740f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate8(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 65f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY - 20f, 150f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.07f))
        p.add(BounceObstacle(startX + 420f, startY - 40f, 150f, 35f, isMoving = true, moveRangeY = 60f, moveSpeed = 0.08f))
        p.add(BounceObstacle(startX + 640f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 719, type = EnemyType.WALKING, x = startX + 265f, y = startY - 48f, moveRangeX = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 720, type = EnemyType.FLYING, x = startX + 495f, y = startY - 110f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 495f, startY - 150f, isStar = true))
        return LevelChunk(width = 800f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate9(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 460f, startY + 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 310f, startY - 80f, isStar = true))
        e.add(BounceEnemy(id = levelNum * 1000 + 721, type = EnemyType.FLYING, x = startX + 310f, y = startY - 95f, moveRangeX = 70f, moveRangeY = 50f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 722, type = EnemyType.WALKING, x = startX + 310f, y = startY - 58f, moveRangeX = 65f, moveSpeed = sp))
        return LevelChunk(width = 620f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate10(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 220f, 40f))
        p.add(BounceObstacle(startX + 250f, startY - 40f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 350f, startY - 40f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 723, type = EnemyType.ROTATING_HAZARD, x = startX + 310f, y = startY - 80f, moveSpeed = 180f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 724, type = EnemyType.FLYING, x = startX + 420f, y = startY - 100f, moveRangeY = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 310f, startY - 130f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate11(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 30f, 120f, 30f))
        p.add(BounceObstacle(startX + 340f, startY - 60f, 120f, 30f))
        p.add(BounceObstacle(startX + 500f, startY - 20f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 725, type = EnemyType.FLYING, x = startX + 260f, y = startY - 100f, moveRangeX = 70f, moveRangeY = 40f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 726, type = EnemyType.WALKING, x = startX + 400f, y = startY - 88f, moveRangeX = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 400f, startY - 120f, isStar = true))
        return LevelChunk(width = 660f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY - 20f)
    }

    private fun generateEnemyTemplate12(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 65f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 180f, 40f))
        p.add(BounceObstacle(startX + 440f, startY - 20f, 180f, 40f))
        p.add(BounceObstacle(startX + 680f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 727, type = EnemyType.WALKING, x = startX + 290f, y = startY - 48f, moveRangeX = 65f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 728, type = EnemyType.FLYING, x = startX + 410f, y = startY - 80f, moveRangeX = 50f, moveRangeY = 60f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 729, type = EnemyType.WALKING, x = startX + 530f, y = startY - 48f, moveRangeX = 65f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 410f, startY - 130f, isStar = true))
        return LevelChunk(width = 840f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate13(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 40f, 140f, 35f))
        p.add(BounceObstacle(startX + 360f, startY - 90f, 140f, 35f))
        p.add(BounceObstacle(startX + 560f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 730, type = EnemyType.WALKING, x = startX + 250f, y = startY - 68f, moveRangeX = 45f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 731, type = EnemyType.ROTATING_HAZARD, x = startX + 330f, y = startY - 40f, moveSpeed = 170f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 732, type = EnemyType.FLYING, x = startX + 430f, y = startY - 150f, moveRangeX = 50f, moveRangeY = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 430f, startY - 135f, isStar = true))
        return LevelChunk(width = 720f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate14(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 160f, 35f, isMoving = true, moveRangeX = 90f, moveSpeed = 0.08f))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 733, type = EnemyType.FLYING, x = startX + 280f, y = startY - 90f, moveRangeX = 90f, moveRangeY = 40f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 734, type = EnemyType.WALKING, x = startX + 540f, y = startY - 18f, moveRangeX = 55f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 280f, startY - 130f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate15(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY - 20f, 120f, 30f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 350f, startY - 40f, 120f, 30f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 520f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 735, type = EnemyType.FLYING, x = startX + 250f, y = startY - 80f, moveRangeX = 60f, moveRangeY = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 736, type = EnemyType.FLYING, x = startX + 410f, y = startY - 100f, moveRangeX = 60f, moveRangeY = 50f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 737, type = EnemyType.WALKING, x = startX + 580f, y = startY - 18f, moveRangeX = 50f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 410f, startY - 140f, isStar = true))
        return LevelChunk(width = 680f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate16(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 30f, 200f, 40f))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 738, type = EnemyType.ROTATING_HAZARD, x = startX + 180f, y = startY - 10f, moveSpeed = 170f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 739, type = EnemyType.WALKING, x = startX + 320f, y = startY - 58f, moveRangeX = 75f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 320f, startY - 100f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate17(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 180f, 40f))
        p.add(BounceObstacle(startX + 440f, startY + 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 350f, startY - 60f, isStar = true))
        e.add(BounceEnemy(id = levelNum * 1000 + 740, type = EnemyType.FLYING, x = startX + 350f, y = startY - 80f, moveRangeX = 50f, moveRangeY = 40f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 741, type = EnemyType.WALKING, x = startX + 270f, y = startY - 48f, moveRangeX = 55f, moveSpeed = sp))
        return LevelChunk(width = 600f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate18(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 10f, 160f, 35f))
        p.add(BounceObstacle(startX + 360f, startY - 70f, 160f, 35f))
        p.add(BounceObstacle(startX + 560f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 742, type = EnemyType.WALKING, x = startX + 260f, y = startY - 38f, moveRangeX = 60f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 743, type = EnemyType.FLYING, x = startX + 320f, y = startY - 120f, moveRangeX = 70f, moveRangeY = 40f, moveSpeed = sp + 5f))
        e.add(BounceEnemy(id = levelNum * 1000 + 744, type = EnemyType.WALKING, x = startX + 440f, y = startY - 98f, moveRangeX = 60f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 440f, startY - 140f, isStar = true))
        return LevelChunk(width = 720f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate19(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 10f, 50f, 25f, isSpring = true, springForce = -720f))
        p.add(BounceObstacle(startX + 320f, startY - 200f, 180f, 35f))
        p.add(BounceObstacle(startX + 560f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 745, type = EnemyType.FLYING, x = startX + 270f, y = startY - 240f, moveRangeX = 70f, moveRangeY = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 746, type = EnemyType.FLYING, x = startX + 410f, y = startY - 240f, moveRangeX = 70f, moveRangeY = 50f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 747, type = EnemyType.WALKING, x = startX + 410f, y = startY - 228f, moveRangeX = 60f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 410f, startY - 270f, isStar = true))
        return LevelChunk(width = 720f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate20(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY + 20f, 40f, 30f, isSpike = true, isMoving = true, moveRangeX = 80f, moveSpeed = 0.08f))
        p.add(BounceObstacle(startX + 360f, startY - 20f, 200f, 40f))
        p.add(BounceObstacle(startX + 620f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 748, type = EnemyType.WALKING, x = startX + 460f, y = startY - 48f, moveRangeX = 70f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 749, type = EnemyType.FLYING, x = startX + 240f, y = startY - 90f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 460f, startY - 90f, isStar = true))
        return LevelChunk(width = 780f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate21(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 20f, 220f, 40f))
        p.add(BounceObstacle(startX + 500f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 750, type = EnemyType.ROTATING_HAZARD, x = startX + 180f, y = startY - 20f, moveSpeed = 180f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 751, type = EnemyType.ROTATING_HAZARD, x = startX + 460f, y = startY - 10f, moveSpeed = 190f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 752, type = EnemyType.WALKING, x = startX + 330f, y = startY - 48f, moveRangeX = 80f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 330f, startY - 90f, isStar = true))
        return LevelChunk(width = 660f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate22(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY - 10f, 130f, 30f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 360f, startY - 30f, 180f, 40f))
        p.add(BounceObstacle(startX + 600f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 753, type = EnemyType.FLYING, x = startX + 250f, y = startY - 80f, moveRangeY = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 754, type = EnemyType.WALKING, x = startX + 450f, y = startY - 58f, moveRangeX = 60f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 450f, startY - 100f, isStar = true))
        return LevelChunk(width = 760f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate23(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 30f, 120f, 30f))
        p.add(BounceObstacle(startX + 340f, startY - 30f, 120f, 30f))
        p.add(BounceObstacle(startX + 500f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 755, type = EnemyType.FLYING, x = startX + 260f, y = startY - 90f, moveRangeX = 80f, moveRangeY = 40f, moveSpeed = sp + 5f))
        e.add(BounceEnemy(id = levelNum * 1000 + 756, type = EnemyType.WALKING, x = startX + 400f, y = startY - 58f, moveRangeX = 40f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 400f, startY - 95f, isStar = true))
        return LevelChunk(width = 660f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate24(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 20f, 240f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 40f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 340f, startY - 40f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 757, type = EnemyType.WALKING, x = startX + 280f, y = startY - 48f, moveRangeX = 25f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 758, type = EnemyType.FLYING, x = startX + 300f, y = startY - 110f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp + 5f))
        c.add(BounceCollectible(startX + 280f, startY - 140f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate25(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY - 10f, 50f, 25f, isSpring = true, springForce = -720f))
        p.add(BounceObstacle(startX + 320f, startY - 190f, 180f, 35f))
        p.add(BounceObstacle(startX + 560f, startY + 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 410f, startY - 260f, isStar = true))
        e.add(BounceEnemy(id = levelNum * 1000 + 759, type = EnemyType.FLYING, x = startX + 410f, y = startY - 275f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp + 10f))
        e.add(BounceEnemy(id = levelNum * 1000 + 760, type = EnemyType.WALKING, x = startX + 410f, y = startY - 218f, moveRangeX = 60f, moveSpeed = sp))
        return LevelChunk(width = 720f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate26(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 20f, 160f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.08f))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 761, type = EnemyType.ROTATING_HAZARD, x = startX + 380f, y = startY - 20f, moveSpeed = 180f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 762, type = EnemyType.WALKING, x = startX + 540f, y = startY - 18f, moveRangeX = 55f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 280f, startY - 90f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate27(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 75f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 200f, startY - 30f, 220f, 40f))
        p.add(BounceObstacle(startX + 280f, startY - 50f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 480f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 763, type = EnemyType.FLYING, x = startX + 240f, y = startY - 100f, moveRangeX = 60f, moveRangeY = 50f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 764, type = EnemyType.FLYING, x = startX + 380f, y = startY - 110f, moveRangeX = 60f, moveRangeY = 50f, moveSpeed = sp + 10f))
        c.add(BounceCollectible(startX + 360f, startY - 140f, isStar = true))
        return LevelChunk(width = 640f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate28(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 70f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY - 10f, 120f, 30f, isFallingPlatform = true))
        p.add(BounceObstacle(startX + 350f, startY - 30f, 200f, 40f))
        p.add(BounceObstacle(startX + 420f, startY - 50f, 35f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        p.add(BounceObstacle(startX + 610f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 765, type = EnemyType.WALKING, x = startX + 480f, y = startY - 58f, moveRangeX = 40f, moveSpeed = sp))
        e.add(BounceEnemy(id = levelNum * 1000 + 766, type = EnemyType.FLYING, x = startX + 250f, y = startY - 80f, moveRangeY = 50f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 480f, startY - 100f, isStar = true))
        return LevelChunk(width = 770f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    private fun generateEnemyTemplate29(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        val sp = 80f + difficulty * 15f
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY - 20f, 160f, 35f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.08f))
        p.add(BounceObstacle(startX + 450f, startY - 40f, 200f, 40f))
        p.add(BounceObstacle(startX + 710f, startY + 10f, 160f, 40f))
        e.add(BounceEnemy(id = levelNum * 1000 + 767, type = EnemyType.ROTATING_HAZARD, x = startX + 380f, y = startY - 20f, moveSpeed = 190f + difficulty * 20f))
        e.add(BounceEnemy(id = levelNum * 1000 + 768, type = EnemyType.FLYING, x = startX + 550f, y = startY - 110f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = sp + 5f))
        e.add(BounceEnemy(id = levelNum * 1000 + 769, type = EnemyType.WALKING, x = startX + 550f, y = startY - 68f, moveRangeX = 65f, moveSpeed = sp))
        c.add(BounceCollectible(startX + 550f, startY - 140f, isStar = true))
        return LevelChunk(width = 870f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }

    fun generatePuzzleChunk(startX: Float, startY: Float, variation: Int, levelNum: Int): LevelChunk {
        val v = (variation % 25 + 25) % 25
        return when (v) {
            0 -> generatePuzzleTemplate0(startX, startY, levelNum)
            1 -> generatePuzzleTemplate1(startX, startY, levelNum)
            2 -> generatePuzzleTemplate2(startX, startY, levelNum)
            3 -> generatePuzzleTemplate3(startX, startY, levelNum)
            4 -> generatePuzzleTemplate4(startX, startY, levelNum)
            5 -> generatePuzzleTemplate5(startX, startY, levelNum)
            6 -> generatePuzzleTemplate6(startX, startY, levelNum)
            7 -> generatePuzzleTemplate7(startX, startY, levelNum)
            8 -> generatePuzzleTemplate8(startX, startY, levelNum)
            9 -> generatePuzzleTemplate9(startX, startY, levelNum)
            10 -> generatePuzzleTemplate10(startX, startY, levelNum)
            11 -> generatePuzzleTemplate11(startX, startY, levelNum)
            12 -> generatePuzzleTemplate12(startX, startY, levelNum)
            13 -> generatePuzzleTemplate13(startX, startY, levelNum)
            14 -> generatePuzzleTemplate14(startX, startY, levelNum)
            15 -> generatePuzzleTemplate15(startX, startY, levelNum)
            16 -> generatePuzzleTemplate16(startX, startY, levelNum)
            17 -> generatePuzzleTemplate17(startX, startY, levelNum)
            18 -> generatePuzzleTemplate18(startX, startY, levelNum)
            19 -> generatePuzzleTemplate19(startX, startY, levelNum)
            20 -> generatePuzzleTemplate20(startX, startY, levelNum)
            21 -> generatePuzzleTemplate21(startX, startY, levelNum)
            22 -> generatePuzzleTemplate22(startX, startY, levelNum)
            23 -> generatePuzzleTemplate23(startX, startY, levelNum)
            24 -> generatePuzzleTemplate24(startX, startY, levelNum)
            else -> generatePuzzleTemplate0(startX, startY, levelNum)
        }
    }

    private fun generatePuzzleTemplate0(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 0
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate1(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 1
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate2(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 2
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate3(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 3
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate4(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 4
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate5(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 5
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate6(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 6
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate7(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 7
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate8(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 8
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate9(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 9
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate10(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 10
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate11(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 11
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate12(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 12
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate13(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 13
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate14(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 14
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate15(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 15
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate16(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 16
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate17(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 17
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate18(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 18
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate19(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 19
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate20(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 20
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate21(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 21
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate22(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 22
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate23(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 23
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

    private fun generatePuzzleTemplate24(startX: Float, startY: Float, levelNum: Int): LevelChunk {
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + 24
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }

}
