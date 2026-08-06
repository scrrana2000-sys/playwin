package com.myplaywin.app.ui.screens

import androidx.compose.ui.graphics.Color

object ChunkLibrary {

    fun getNumVariations(type: SmartProceduralLevelGenerator.ChunkType): Int {
        return when (type) {
            SmartProceduralLevelGenerator.ChunkType.START -> 20
            SmartProceduralLevelGenerator.ChunkType.EASY -> 30
            SmartProceduralLevelGenerator.ChunkType.MEDIUM -> 40
            SmartProceduralLevelGenerator.ChunkType.VERTICAL -> 25
            SmartProceduralLevelGenerator.ChunkType.MOVING_PLATFORM -> 30
            SmartProceduralLevelGenerator.ChunkType.SPRING -> 20
            SmartProceduralLevelGenerator.ChunkType.ENEMY -> 30
            SmartProceduralLevelGenerator.ChunkType.SECRET -> 20
            SmartProceduralLevelGenerator.ChunkType.PUZZLE -> 25
            SmartProceduralLevelGenerator.ChunkType.CHECKPOINT -> 15
            SmartProceduralLevelGenerator.ChunkType.FINAL_CHALLENGE -> 40 // Hard
            SmartProceduralLevelGenerator.ChunkType.EXIT -> 20
        }
    }

    fun generateStartChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 20
        val width = 350f + varIndex * 15f
        
        // Base platform
        val plat1Width = 180f + (varIndex * 5f)
        val plat1Height = 120f
        platforms.add(BounceObstacle(x = startX, y = startY, width = plat1Width, height = plat1Height))
        
        // Secondary platform connected smoothly
        val gap = 40f + (varIndex % 3) * 10f
        val plat2X = startX + plat1Width + gap
        val plat2Width = width - plat1Width - gap
        val plat2Y = startY + (if (varIndex % 2 == 0) 10f else -10f) * (varIndex % 4)
        platforms.add(BounceObstacle(x = plat2X, y = plat2Y, width = plat2Width, height = plat1Height))
        
        // Collectible star
        val starX = startX + plat1Width + gap / 2f
        val starY = startY - 50f - (varIndex % 3) * 15f
        collectibles.add(BounceCollectible(x = starX, y = starY, isStar = true, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            endY = plat2Y
        )
    }

    fun generateEasyChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 30
        val width = 650f + varIndex * 12f
        
        // Platform 1 (Entry)
        val p1Width = 150f + (varIndex % 5) * 10f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Platform 2 (Mid-air safe step)
        val gap1 = 100f + (varIndex % 4) * 15f
        val p2X = startX + p1Width + gap1
        val p2Y = startY + (if (varIndex % 2 == 0) -30f else 20f)
        val p2Width = 160f + (varIndex % 3) * 15f
        platforms.add(BounceObstacle(x = p2X, y = p2Y, width = p2Width, height = 40f))
        
        // Platform 3 (Exit connection)
        val gap2 = 110f + (varIndex % 3) * 15f
        val p3X = p2X + p2Width + gap2
        val p3Y = p2Y + (if (varIndex % 3 == 0) -20f else 15f)
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        // Reachable star
        val starX = p2X + p2Width / 2f
        val starY = p2Y - 55f
        collectibles.add(BounceCollectible(x = starX, y = starY, isStar = true, isBonus = false))
        
        // Standard extra coin
        val coinX = p3X + 40f
        val coinY = p3Y - 45f
        collectibles.add(BounceCollectible(x = coinX, y = coinY, isStar = false, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            endY = p3Y
        )
    }

    fun generateMediumChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()
        val waterZones = mutableListOf<BounceWaterZone>()
        val interactiveBlocks = mutableListOf<BounceInteractiveBlock>()

        val varIndex = variation % 40
        val width = 750f + varIndex * 10f
        
        // Platform 1 (Entry)
        val p1Width = 140f + (varIndex % 4) * 10f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Platform 2 (Hazardous / Interactive area)
        val gap1 = 110f + (varIndex % 3) * 12f
        val p2X = startX + p1Width + gap1
        val p2Y = startY + (if (varIndex % 2 == 0) -40f else 30f)
        val p2Width = 150f
        
        if (varIndex % 3 == 0) {
            platforms.add(BounceObstacle(x = p2X, y = p2Y, width = p2Width, height = 35f, isFallingPlatform = true))
        } else {
            platforms.add(BounceObstacle(x = p2X, y = p2Y, width = p2Width, height = 40f))
        }
        
        // Optional spike hazard
        if (varIndex % 4 == 1) {
            platforms.add(BounceObstacle(x = p2X + 60f, y = p2Y - 20f, width = 30f, height = 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        }
        
        // Optional Breakable Block
        if (varIndex % 4 == 2) {
            interactiveBlocks.add(
                BounceInteractiveBlock(
                    id = 20000 + varIndex,
                    type = InteractiveType.BREAKABLE,
                    x = p2X + 50f,
                    y = p2Y - 40f,
                    width = 40f,
                    height = 40f
                )
            )
        }

        // Platform 3 (Exit)
        val gap2 = 110f + (varIndex % 4) * 10f
        val p3X = p2X + p2Width + gap2
        val p3Y = p2Y + (if (varIndex % 3 == 1) -30f else 20f)
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        // Optional Water zone under the gap
        if (varIndex % 5 == 0) {
            waterZones.add(BounceWaterZone(x = p2X - 50f, y = 500f, width = gap2 + 100f, height = 100f))
        }

        // Collectibles
        collectibles.add(BounceCollectible(x = p2X + p2Width / 2f, y = p2Y - 75f, isStar = true, isBonus = false))
        collectibles.add(BounceCollectible(x = p3X + 40f, y = p3Y - 45f, isStar = false, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            waterZones = waterZones,
            interactiveBlocks = interactiveBlocks,
            endY = p3Y
        )
    }

    fun generateFinalChallengeChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()
        val enemies = mutableListOf<BounceEnemy>()
        val interactiveBlocks = mutableListOf<BounceInteractiveBlock>()

        val varIndex = variation % 40
        val width = 850f + varIndex * 10f
        
        // Platform 1 (Entry)
        val p1Width = 130f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Platform 2 (Falling/Tiny step over spike)
        val gap1 = 120f
        val p2X = startX + p1Width + gap1
        val p2Y = startY - 30f
        platforms.add(BounceObstacle(x = p2X - 40f, y = startY + 60f, width = 60f, height = 30f, isSpike = true, spikeDirection = SpikeDirection.UP))
        
        if (varIndex % 2 == 0) {
            platforms.add(BounceObstacle(x = p2X, y = p2Y, width = 100f, height = 35f, isFallingPlatform = true))
        } else {
            platforms.add(BounceObstacle(x = p2X, y = p2Y, width = 100f, height = 40f))
        }

        // Platform 3 (Spike tunnel or Rotating Hazard area)
        val gap2 = 120f
        val p3X = p2X + 100f + gap2
        val p3Y = p2Y - 40f
        val p3Width = 120f
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        if (varIndex % 3 == 0) {
            enemies.add(
                BounceEnemy(
                    id = levelNum * 1000 + 300 + varIndex,
                    type = EnemyType.ROTATING_HAZARD,
                    x = p3X + 60f,
                    y = p3Y - 80f,
                    moveSpeed = 120f + difficulty * 30f
                )
            )
        } else if (varIndex % 3 == 1) {
            enemies.add(
                BounceEnemy(
                    id = levelNum * 1000 + 300 + varIndex,
                    type = EnemyType.FLYING,
                    x = p3X + 60f,
                    y = p3Y - 100f,
                    moveRangeY = 60f,
                    moveSpeed = 70f
                )
            )
        } else {
            platforms.add(BounceObstacle(x = p3X + 45f, y = p3Y - 20f, width = 30f, height = 20f, isSpike = true, spikeDirection = SpikeDirection.UP))
        }

        // Platform 4 (Exit step)
        val gap3 = 110f
        val p4X = p3X + p3Width + gap3
        val p4Y = p3Y + 30f
        val p4Width = width - (p4X - startX)
        platforms.add(BounceObstacle(x = p4X, y = p4Y, width = p4Width, height = 40f))

        // Star on risky spot
        collectibles.add(BounceCollectible(x = p3X + p3Width / 2f, y = p3Y - 50f, isStar = true, isBonus = false))
        collectibles.add(BounceCollectible(x = p4X + 50f, y = p4Y - 45f, isStar = false, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            enemies = enemies,
            interactiveBlocks = interactiveBlocks,
            endY = p4Y
        )
    }

    fun generateSecretChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()
        val interactiveBlocks = mutableListOf<BounceInteractiveBlock>()

        val varIndex = variation % 20
        val width = 680f + varIndex * 12f
        
        // Base low path
        val p1Width = 150f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        val gap1 = 120f
        val p2X = startX + p1Width + gap1
        val p2Y = startY + 40f
        val p2Width = 180f
        platforms.add(BounceObstacle(x = p2X, y = p2Y, width = p2Width, height = 40f))
        
        // Secret high path
        val secretPlatX = startX + 180f
        val secretPlatY = startY - 140f
        val secretPlatWidth = 140f
        platforms.add(BounceObstacle(x = secretPlatX, y = secretPlatY, width = secretPlatWidth, height = 30f))
        
        // Hidden / Breakable blocks protecting secret
        if (varIndex % 2 == 0) {
            interactiveBlocks.add(
                BounceInteractiveBlock(
                    id = 30000 + varIndex,
                    type = InteractiveType.BREAKABLE,
                    x = secretPlatX - 45f,
                    y = secretPlatY,
                    width = 40f,
                    height = 30f
                )
            )
        }

        // Secret bonus star
        collectibles.add(BounceCollectible(x = secretPlatX + secretPlatWidth / 2f, y = secretPlatY - 45f, isStar = true, isBonus = true))

        // Normal path exit
        val gap2 = 110f
        val p3X = p2X + p2Width + gap2
        val p3Y = p2Y - 40f
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        // Normal star on lower path
        collectibles.add(BounceCollectible(x = p2X + p2Width / 2f, y = p2Y - 45f, isStar = true, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            interactiveBlocks = interactiveBlocks,
            endY = p3Y
        )
    }

    fun generateCheckpointChunk(startX: Float, startY: Float, variation: Int, checkpointId: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val checkpoints = mutableListOf<BounceCheckpoint>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 15
        val width = 320f + varIndex * 15f
        
        // Single flat platform
        platforms.add(BounceObstacle(x = startX, y = startY, width = width, height = 45f))
        
        // Checkpoint in the middle
        checkpoints.add(BounceCheckpoint(id = checkpointId, x = startX + width / 2f, y = startY - 40f))
        
        // A couple of comforting coins
        collectibles.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false, isBonus = false))
        collectibles.add(BounceCollectible(x = startX + width - 60f, y = startY - 45f, isStar = false, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            checkpoints = checkpoints,
            collectibles = collectibles,
            endY = startY
        )
    }

    fun generatePuzzleChunk(startX: Float, startY: Float, variation: Int, levelNum: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val keys = mutableListOf<BounceKey>()
        val doors = mutableListOf<BounceDoor>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 25
        val width = 780f + varIndex * 10f
        
        val keyId = levelNum * 1000 + 500 + varIndex
        
        // Platform 1 (Entry)
        val p1Width = 140f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Platform 2 (High branch for key)
        val keyPlatX = startX + 220f
        val keyPlatY = startY - 90f
        val keyPlatWidth = 120f
        platforms.add(BounceObstacle(x = keyPlatX, y = keyPlatY, width = keyPlatWidth, height = 35f))
        keys.add(BounceKey(id = keyId, x = keyPlatX + keyPlatWidth / 2f, y = keyPlatY - 40f))
        
        // Platform 3 (Lower main branch blocked by door)
        val doorPlatX = startX + 200f
        val doorPlatY = startY + 30f
        val doorPlatWidth = 240f
        platforms.add(BounceObstacle(x = doorPlatX, y = doorPlatY, width = doorPlatWidth, height = 40f))
        
        // Locked door blocking lower path
        doors.add(BounceDoor(id = keyId, x = doorPlatX + 120f, y = doorPlatY - 80f, keyIdNeeded = keyId))

        // Platform 4 (Exit connection)
        val gap2 = 110f
        val p4X = doorPlatX + doorPlatWidth + gap2
        val p4Y = doorPlatY - 30f
        val p4Width = width - (p4X - startX)
        platforms.add(BounceObstacle(x = p4X, y = p4Y, width = p4Width, height = 40f))
        
        // Collectible star inside puzzle path
        collectibles.add(BounceCollectible(x = p4X + 40f, y = p4Y - 45f, isStar = true, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            keys = keys,
            doors = doors,
            collectibles = collectibles,
            endY = p4Y
        )
    }

    fun generateEnemyChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val enemies = mutableListOf<BounceEnemy>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 30
        val width = 720f + varIndex * 15f
        
        // Platform 1 (Entry)
        val p1Width = 140f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Platform 2 (Enemy Patrol zone)
        val gap1 = 110f
        val p2X = startX + p1Width + gap1
        val p2Y = startY - 20f
        val p2Width = 220f
        platforms.add(BounceObstacle(x = p2X, y = p2Y, width = p2Width, height = 40f))
        
        if (varIndex % 2 == 0) {
            enemies.add(
                BounceEnemy(
                    id = levelNum * 1000 + 700 + varIndex,
                    type = EnemyType.WALKING,
                    x = p2X + p2Width / 2f,
                    y = p2Y - 28f,
                    moveRangeX = 80f,
                    moveSpeed = 60f + difficulty * 20f
                )
            )
        } else {
            enemies.add(
                BounceEnemy(
                    id = levelNum * 1000 + 700 + varIndex,
                    type = EnemyType.FLYING,
                    x = p2X + p2Width / 2f,
                    y = p2Y - 80f,
                    moveRangeY = 40f,
                    moveSpeed = 50f + difficulty * 15f
                )
            )
        }

        // Platform 3 (Exit connection)
        val gap2 = 110f
        val p3X = p2X + p2Width + gap2
        val p3Y = p2Y + 20f
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        collectibles.add(BounceCollectible(x = p2X + 30f, y = p2Y - 45f, isStar = true, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            enemies = enemies,
            collectibles = collectibles,
            endY = p3Y
        )
    }

    fun generateMovingPlatformChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 30
        val width = 740f + varIndex * 12f
        
        // Platform 1 (Entry)
        val p1Width = 140f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Moving platform
        val movingX = startX + p1Width + 60f
        val movingY = startY - 30f
        if (varIndex % 2 == 0) {
            platforms.add(
                BounceObstacle(
                    x = movingX,
                    y = movingY,
                    width = 110f,
                    height = 30f,
                    isMoving = true,
                    moveRangeX = 120f + (varIndex % 3) * 15f,
                    moveSpeed = 0.03f + (varIndex % 3) * 0.005f
                )
            )
        } else {
            platforms.add(
                BounceObstacle(
                    x = movingX + 40f,
                    y = movingY,
                    width = 110f,
                    height = 30f,
                    isMoving = true,
                    moveRangeY = 100f + (varIndex % 3) * 15f,
                    moveSpeed = 0.03f + (varIndex % 3) * 0.005f
                )
            )
        }

        // Platform 3 (Exit)
        val p3X = startX + p1Width + 300f
        val p3Y = startY - 20f
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        collectibles.add(BounceCollectible(x = movingX + 50f, y = movingY - 45f, isStar = true, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            endY = p3Y
        )
    }

    fun generateSpringChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 20
        val width = 680f + varIndex * 15f
        
        // Platform 1 (Entry)
        val p1Width = 140f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Spring platform
        val springX = startX + p1Width + 50f
        val springY = startY + 20f
        platforms.add(BounceObstacle(x = springX, y = springY, width = 70f, height = 30f, isSpring = true, springForce = -670f - (varIndex % 3) * 20f))
        
        // High Platform to land on
        val highX = springX + 120f
        val highY = startY - 140f
        val highWidth = 180f
        platforms.add(BounceObstacle(x = highX, y = highY, width = highWidth, height = 35f))
        
        // Exit Platform
        val p3X = highX + highWidth + 80f
        val p3Y = startY - 40f
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        // Airborne collectibles along the trajectory
        collectibles.add(BounceCollectible(x = springX + 60f, y = startY - 80f, isStar = true, isBonus = false))
        collectibles.add(BounceCollectible(x = highX + 40f, y = highY - 45f, isStar = false, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            endY = p3Y
        )
    }

    fun generateVerticalChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()
        val collectibles = mutableListOf<BounceCollectible>()

        val varIndex = variation % 25
        val width = 820f + varIndex * 10f
        
        // Platform 1 (Entry)
        val p1Width = 130f
        platforms.add(BounceObstacle(x = startX, y = startY, width = p1Width, height = 40f))
        
        // Step 1 Up
        val gap1 = 90f
        val s1X = startX + p1Width + gap1
        val s1Y = startY - 50f
        platforms.add(BounceObstacle(x = s1X, y = s1Y, width = 120f, height = 35f))
        
        // Step 2 Up (peak)
        val gap2 = 90f
        val s2X = s1X + 120f + gap2
        val s2Y = s1Y - 50f
        platforms.add(BounceObstacle(x = s2X, y = s2Y, width = 120f, height = 35f))
        
        // Step 3 Down
        val gap3 = 100f
        val s3X = s2X + 120f + gap3
        val s3Y = s2Y + 50f
        platforms.add(BounceObstacle(x = s3X, y = s3Y, width = 120f, height = 35f))
        
        // Exit Connection
        val gap4 = 90f
        val p3X = s3X + 120f + gap4
        val p3Y = s3Y + 50f
        val p3Width = width - (p3X - startX)
        platforms.add(BounceObstacle(x = p3X, y = p3Y, width = p3Width, height = 40f))
        
        collectibles.add(BounceCollectible(x = s2X + 60f, y = s2Y - 45f, isStar = true, isBonus = false))

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            collectibles = collectibles,
            endY = p3Y
        )
    }

    fun generateExitChunk(startX: Float, startY: Float, variation: Int): LevelChunk {
        val platforms = mutableListOf<BounceObstacle>()

        val varIndex = variation % 20
        val width = 420f + varIndex * 15f
        
        // Final Exit Platform
        val platform = BounceObstacle(x = startX, y = startY, width = width, height = 100f)
        platforms.add(platform)

        // Portal coordinates on top of final platform according to placement rules
        val useCenterX = (variation % 2 == 0)
        val pX = if (useCenterX) {
            platform.x + platform.width * 0.5f
        } else {
            platform.x + platform.width - 80f
        }
        val pY = platform.y - 32f

        return LevelChunk(
            width = width,
            height = 600f,
            platforms = platforms,
            endY = startY,
            portalX = pX,
            portalY = pY
        )
    }
}
