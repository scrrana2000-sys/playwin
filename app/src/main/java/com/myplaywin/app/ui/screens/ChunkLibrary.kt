package com.myplaywin.app.ui.screens

import com.myplaywin.app.ui.screens.SmartProceduralLevelGenerator.ChunkType
import kotlin.math.min

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

    // --- 1. START CHUNK ---
    fun generateStartChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 20 + 20) % 20
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()

        val baseWidth = 160f + (v * 3f)
        val gap = 40f + (v % 5) * 6f
        val heightDiff = -20f + (v % 4) * 12f

        p.add(BounceObstacle(x = startX, y = startY, width = baseWidth, height = 40f))
        
        val p2X = startX + baseWidth + gap
        val p2Y = startY + heightDiff
        val p2Width = 170f + (v % 7) * 8f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = p2Width, height = 40f))

        // Collectibles
        c.add(BounceCollectible(x = startX + baseWidth + (gap / 2f), y = minOf(startY, p2Y) - 50f, isStar = true))
        if (v % 2 == 0) {
            c.add(BounceCollectible(x = p2X + 40f, y = p2Y - 45f, isStar = false))
        }

        val totalWidth = baseWidth + gap + p2Width
        return LevelChunk(width = totalWidth, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }

    // --- 2. EASY CHUNK ---
    fun generateEasyChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 30 + 30) % 30
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()

        var currentX = startX
        var currentY = startY

        val platformCount = 3 + (v % 3)
        p.add(BounceObstacle(currentX, currentY, 140f, 40f))

        for (i in 1..platformCount) {
            val stepGap = 70f + ((v + i * 7) % 6) * 10f
            val stepYOffset = -30f + ((v + i * 3) % 5) * 20f
            val platWidth = 120f + ((v + i) % 4) * 15f
            
            currentX += 140f + stepGap
            currentY += stepYOffset
            p.add(BounceObstacle(currentX, currentY, platWidth, 40f))

            c.add(BounceCollectible(x = currentX + (platWidth / 2f), y = currentY - 45f, isStar = (i % 2 == 0)))
        }

        return LevelChunk(width = (currentX - startX) + 150f, height = 600f, platforms = p, collectibles = c, endY = currentY)
    }

    // --- 3. MEDIUM CHUNK ---
    fun generateMediumChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 40 + 40) % 40
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val wz = mutableListOf<BounceWaterZone>()
        val ib = mutableListOf<BounceInteractiveBlock>()

        val plat1Width = 130f + (v % 4) * 10f
        p.add(BounceObstacle(startX, startY, plat1Width, 40f))

        val isFalling = v % 2 == 0
        val gap1 = 80f + (v % 5) * 8f
        val p2X = startX + plat1Width + gap1
        val p2Y = startY + if (v % 3 == 0) -40f else 20f
        
        p.add(BounceObstacle(p2X, p2Y, 130f, 35f, isFallingPlatform = isFalling))
        
        if (v % 4 == 0) {
            ib.add(BounceInteractiveBlock(id = 20000 + v, type = InteractiveType.BREAKABLE, x = p2X + 45f, y = p2Y - 40f, width = 40f, height = 40f))
        }

        val p3X = p2X + 130f + gap1
        val p3Y = p2Y - 30f
        p.add(BounceObstacle(p3X, p3Y, 160f, 40f))

        if (v % 3 == 1) {
            wz.add(BounceWaterZone(x = p2X - 20f, y = startY + 80f, width = 280f, height = 120f))
        }

        c.add(BounceCollectible(x = p2X + 65f, y = p2Y - 55f, isStar = true))
        c.add(BounceCollectible(x = p3X + 50f, y = p3Y - 45f, isStar = false))

        return LevelChunk(width = (p3X - startX) + 160f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = p3Y)
    }

    // --- 4. ENEMY CHUNK ---
    fun generateEnemyChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {
        val v = (variation % 30 + 30) % 30
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()

        val speed = 60f + (v % 5) * 10f + (difficulty * 12f)
        p.add(BounceObstacle(startX, startY, 140f, 40f))

        val p2X = startX + 190f + (v % 4) * 10f
        val p2Y = startY - 20f + (v % 3) * 15f
        val p2Width = 180f + (v % 6) * 10f
        p.add(BounceObstacle(p2X, p2Y, p2Width, 40f))

        when (v % 3) {
            0 -> e.add(BounceEnemy(id = levelNum * 1000 + 700 + v, type = EnemyType.WALKING, x = p2X + (p2Width / 2f), y = p2Y - 38f, moveRangeX = p2Width / 2.5f, moveSpeed = speed))
            1 -> e.add(BounceEnemy(id = levelNum * 1000 + 700 + v, type = EnemyType.FLYING, x = p2X + (p2Width / 2f), y = p2Y - 100f, moveRangeX = 60f, moveRangeY = 40f, moveSpeed = speed + 10f))
            else -> e.add(BounceEnemy(id = levelNum * 1000 + 700 + v, type = EnemyType.ROTATING_HAZARD, x = p2X + (p2Width / 2f), y = p2Y - 50f, moveSpeed = 150f + difficulty * 20f))
        }

        val p3X = p2X + p2Width + 80f
        val p3Y = p2Y + 10f
        p.add(BounceObstacle(p3X, p3Y, 150f, 40f))

        val starY = if (v % 3 == 2) p2Y - 145f else p2Y - 85f
        c.add(BounceCollectible(p2X + (p2Width / 2f), starY, isStar = true))

        return LevelChunk(width = (p3X - startX) + 150f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = p3Y)
    }

    // --- 5. MOVING PLATFORM CHUNK ---
    fun generateMovingPlatformChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 30 + 30) % 30
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()

        val isVerticalMoving = v % 2 == 1
        val moveRange = 80f + (v % 5) * 15f
        val moveSpeed = 0.03f + (v % 4) * 0.01f

        p.add(BounceObstacle(startX, startY, 140f, 40f))

        val p2X = startX + 200f + (v % 3) * 20f
        val p2Y = startY - 20f
        if (isVerticalMoving) {
            p.add(BounceObstacle(p2X, p2Y, 110f, 30f, isMoving = true, moveRangeY = moveRange, moveSpeed = moveSpeed))
        } else {
            p.add(BounceObstacle(p2X, p2Y, 110f, 30f, isMoving = true, moveRangeX = moveRange, moveSpeed = moveSpeed))
        }

        val p3X = p2X + 230f
        val p3Y = startY - 10f
        p.add(BounceObstacle(p3X, p3Y, 160f, 40f))

        c.add(BounceCollectible(p2X + 55f, p2Y - 75f, isStar = true))

        return LevelChunk(width = (p3X - startX) + 160f, height = 600f, platforms = p, collectibles = c, endY = p3Y)
    }

    // --- 6. VERTICAL CHUNK ---
    fun generateVerticalChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 25 + 25) % 25
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()

        var currX = startX
        var currY = startY

        p.add(BounceObstacle(currX, currY, 130f, 40f))

        val heightStep = 60f + (v % 4) * 10f
        val widthStep = 180f + (v % 3) * 15f

        currX += widthStep
        currY -= heightStep
        p.add(BounceObstacle(currX, currY, 120f, 35f))

        currX += widthStep
        currY -= heightStep
        p.add(BounceObstacle(currX, currY, 120f, 35f))
        c.add(BounceCollectible(currX + 60f, currY - 45f, isStar = true))

        currX += widthStep
        currY += heightStep
        p.add(BounceObstacle(currX, currY, 120f, 35f))

        currX += widthStep
        currY += heightStep
        p.add(BounceObstacle(currX, currY, 150f, 40f))

        return LevelChunk(width = (currX - startX) + 150f, height = 600f, platforms = p, collectibles = c, endY = currY)
    }

    // --- 7. SPRING CHUNK ---
    fun generateSpringChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 20 + 20) % 20
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()

        val springForce = -650f - (v % 5) * 20f

        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 180f, startY + 20f, 70f, 30f, isSpring = true, springForce = springForce))
        
        val highPlatY = startY - 140f - (v % 3) * 20f
        p.add(BounceObstacle(startX + 310f, highPlatY, 170f, 35f))
        p.add(BounceObstacle(startX + 540f, startY - 30f, 160f, 40f))

        c.add(BounceCollectible(startX + 215f, startY - 80f, isStar = true))
        c.add(BounceCollectible(startX + 395f, highPlatY - 45f, isStar = false))

        return LevelChunk(width = 700f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }

    // --- 8. PUZZLE CHUNK ---
    fun generatePuzzleChunk(startX: Float, startY: Float, variation: Int, levelNum: Int): LevelChunk {
        val v = (variation % 25 + 25) % 25
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()

        val keyId = levelNum * 1000 + 500 + v

        p.add(BounceObstacle(startX, startY, 140f, 40f))
        
        val keyPlatX = startX + 200f + (v % 4) * 10f
        val keyPlatY = startY - 80f - (v % 3) * 10f
        p.add(BounceObstacle(keyPlatX, keyPlatY, 130f, 35f))
        k.add(BounceKey(id = keyId, x = keyPlatX + 50f, y = keyPlatY - 40f))

        val doorPlatX = startX + 180f
        val doorPlatY = startY + 30f
        p.add(BounceObstacle(doorPlatX, doorPlatY, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = doorPlatX + 120f, y = doorPlatY - 50f, keyIdNeeded = keyId))

        val endX = doorPlatX + 310f
        val endY = startY - 20f
        p.add(BounceObstacle(endX, endY, 170f, 40f))
        c.add(BounceCollectible(endX + 50f, endY - 45f, isStar = true))

        return LevelChunk(width = (endX - startX) + 170f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = endY)
    }

    // --- 9. SECRET CHUNK ---
    fun generateSecretChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 20 + 20) % 20
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val ib = mutableListOf<BounceInteractiveBlock>()

        p.add(BounceObstacle(startX, startY, 150f, 40f))
        p.add(BounceObstacle(startX + 260f, startY + 30f, 180f, 40f))

        val secretX = startX + 150f + (v % 5) * 10f
        val secretY = startY - 120f - (v % 4) * 10f
        p.add(BounceObstacle(secretX, secretY, 140f, 30f))
        
        ib.add(BounceInteractiveBlock(id = 30000 + v, type = InteractiveType.BREAKABLE, x = secretX - 40f, y = secretY, width = 40f, height = 30f))
        c.add(BounceCollectible(x = secretX + 70f, y = secretY - 45f, isStar = true, isBonus = true))

        val endX = startX + 530f
        p.add(BounceObstacle(endX, startY, 170f, 40f))
        c.add(BounceCollectible(x = startX + 330f, y = startY - 15f, isStar = true, isBonus = false))

        return LevelChunk(width = (endX - startX) + 170f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = startY)
    }

    // --- 10. CHECKPOINT CHUNK ---
    fun generateCheckpointChunk(startX: Float, startY: Float, variation: Int, checkpointId: Int): LevelChunk {
        val v = (variation % 15 + 15) % 15
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()

        val platWidth = 320f + (v * 12f)
        p.add(BounceObstacle(x = startX, y = startY, width = platWidth, height = 45f))
        
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + (platWidth / 2f), y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + platWidth - 50f, y = startY - 45f, isStar = false))

        return LevelChunk(width = platWidth, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }

    // --- 11. FINAL CHALLENGE CHUNK ---
    fun generateFinalChallengeChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {
        val v = (variation % 40 + 40) % 40
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        val e = mutableListOf<BounceEnemy>()

        p.add(BounceObstacle(startX, startY, 130f, 40f))

        val p2X = startX + 210f
        val p2Y = startY - 30f
        p.add(BounceObstacle(p2X, p2Y, 110f, 35f, isFallingPlatform = (v % 2 == 0)))
        p.add(BounceObstacle(p2X - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))

        val p3X = p2X + 200f
        val p3Y = p2Y - 20f
        p.add(BounceObstacle(p3X, p3Y, 140f, 40f))

        val speed = 90f + difficulty * 25f
        if (v % 2 == 0) {
            e.add(BounceEnemy(id = levelNum * 1000 + 800 + v, type = EnemyType.ROTATING_HAZARD, x = p3X + 70f, y = p3Y - 70f, moveSpeed = speed))
        } else {
            e.add(BounceEnemy(id = levelNum * 1000 + 800 + v, type = EnemyType.FLYING, x = p3X + 70f, y = p3Y - 90f, moveRangeY = 50f, moveSpeed = speed))
        }

        val p4X = p3X + 210f
        val p4Y = p3Y + 30f
        p.add(BounceObstacle(p4X, p4Y, 160f, 40f))

        val bossStarY = if (v % 2 == 0) p3Y - 165f else p3Y - 45f
        c.add(BounceCollectible(x = p3X + 70f, y = bossStarY, isStar = true))
        c.add(BounceCollectible(x = p4X + 50f, y = p4Y - 45f, isStar = false))

        return LevelChunk(width = (p4X - startX) + 160f, height = 600f, platforms = p, collectibles = c, enemies = e, endY = p4Y)
    }

    // --- 12. EXIT CHUNK ---
    fun generateExitChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val v = (variation % 15 + 15) % 15
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()

        val platWidth = 400f + (v * 15f)
        val exitPlatform = BounceObstacle(x = startX, y = startY, width = platWidth, height = 100f, isExitPlatform = true)
        p.add(exitPlatform)

        c.add(BounceCollectible(x = exitPlatform.topCenter.x - 60f, y = exitPlatform.topCenter.y - 45f, isStar = true))
        c.add(BounceCollectible(x = exitPlatform.topCenter.x + 60f, y = exitPlatform.topCenter.y - 45f, isStar = true))

        return LevelChunk(
            width = platWidth,
            height = 600f,
            platforms = p,
            collectibles = c,
            endY = startY,
            portalX = exitPlatform.topCenter.x,
            portalY = exitPlatform.topCenter.y
        )
    }
}