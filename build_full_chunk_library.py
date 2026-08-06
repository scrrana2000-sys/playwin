import os

def generate():
    lines = []
    lines.append("package com.myplaywin.app.ui.screens\n")
    lines.append("import androidx.compose.ui.graphics.Color\n")
    lines.append("import com.myplaywin.app.ui.screens.SmartProceduralLevelGenerator.ChunkType\n\n")
    lines.append("object ChunkLibrary {\n\n")

    lines.append("""    fun getNumVariations(type: ChunkType): Int {
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
""")

    # 1. START CHUNKS (20)
    lines.append("    fun generateStartChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 20 + 20) % 20\n")
    lines.append("        return when (v) {\n")
    for i in range(20):
        lines.append(f"            {i} -> generateStartTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateStartTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(20):
        w1 = 160 + (i * 7) % 40
        gap = 40 + (i * 9) % 35
        w2 = 170 + (i * 13) % 50
        dy = ((i % 5) - 2) * 12
        lines.append(f"""    private fun generateStartTemplate{i}(startX: Float, startY: Float): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = {w1}f, height = 40f))
        val p2X = startX + {w1}f + {gap}f
        val p2Y = startY + {dy}f
        p.add(BounceObstacle(x = p2X, y = p2Y, width = {w2}f, height = 40f))
        c.add(BounceCollectible(x = startX + {w1}f + {gap/2}f, y = minOf(startY, p2Y) - 45f, isStar = true))
        return LevelChunk(width = {w1 + gap + w2}f, height = 600f, platforms = p, collectibles = c, endY = p2Y)
    }}\n\n""")

    # 2. EASY CHUNKS (30)
    lines.append("    fun generateEasyChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 30 + 30) % 30\n")
    lines.append("        return when (v) {\n")
    for i in range(30):
        lines.append(f"            {i} -> generateEasyTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateEasyTemplate0(startX, startY)\n        }\n    }\n\n")

    easy_archetypes = [
        ("Long Bridge", 3, False, False),
        ("Tower Ascent", 4, False, False),
        ("Floating Archipelago", 4, False, False),
        ("Zigzag Step", 4, False, False),
        ("Split Level", 4, False, False),
        ("Mini Maze Barrier", 4, False, False),
        ("Spring Bounce", 4, True, False),
        ("Forest Canopy", 4, False, False),
        ("Castle Rampart", 4, False, False),
        ("Cave Overhang", 4, False, False),
        ("Sky Islands", 4, False, False),
        ("Crystal Ledge", 4, False, False),
        ("Low Valley", 4, False, False),
        ("Archway Bridge", 3, False, False),
        ("Pyramid Steps", 5, False, False),
        ("Stepping Stones", 5, False, False),
        ("Dual Tier Track", 4, False, False),
        ("Overpass Beam", 4, False, False),
        ("Twin Peaks", 4, False, False),
        ("Desert Stretch", 3, False, False),
        ("Spiral Climb", 4, False, False),
        ("High Wire", 4, False, False),
        ("Underpass Crawl", 4, False, False),
        ("Waterfall Walk", 4, False, False),
        ("Grotto Sanctuary", 4, False, False),
        ("Staircase Steps", 5, False, False),
        ("Platform Chain", 4, False, False),
        ("Balanced Beam", 3, False, False),
        ("Battlement Steps", 4, False, False),
        ("Cloud Hop", 4, False, False)
    ]

    for i, (name, count, has_spring, has_moving) in enumerate(easy_archetypes):
        lines.append(f"    // Easy Template {i}: {name}\n")
        lines.append(f"    private fun generateEasyTemplate{i}(startX: Float, startY: Float): LevelChunk {{\n")
        lines.append("        val p = mutableListOf<BounceObstacle>()\n")
        lines.append("        val c = mutableListOf<BounceCollectible>()\n")
        
        lines.append("        p.add(BounceObstacle(startX, startY, 150f, 40f))\n")
        
        prev_x_expr = "startX + 150f"
        prev_y_expr = "startY"
        
        width_accum = 150
        for step in range(1, count):
            gap = 70 + ((i * 7 + step * 13) % 40)
            p_width = 120 + ((i * 11 + step * 17) % 50)
            dy = (((i + step) % 5) - 2) * 20
            if has_spring and step == 1:
                lines.append(f"        p.add(BounceObstacle({prev_x_expr} + {gap}f, {prev_y_expr} + 20f, 70f, 30f, isSpring = true, springForce = -670f))\n")
                lines.append(f"        c.add(BounceCollectible({prev_x_expr} + {gap}f + 35f, {prev_y_expr} - 60f, isStar = true))\n")
                prev_x_expr = f"{prev_x_expr} + {gap + 70}f"
                prev_y_expr = f"{prev_y_expr} - 120f"
                width_accum += gap + 70
            else:
                lines.append(f"        p.add(BounceObstacle({prev_x_expr} + {gap}f, {prev_y_expr} + ({dy}f), {p_width}f, 40f))\n")
                if step == count - 1 or step == 2:
                    lines.append(f"        c.add(BounceCollectible({prev_x_expr} + {gap + p_width/2}f, {prev_y_expr} + ({dy}f) - 45f, isStar = {str(step == 2).lower()}))\n")
                prev_x_expr = f"{prev_x_expr} + {gap + p_width}f"
                prev_y_expr = f"{prev_y_expr} + ({dy}f)"
                width_accum += gap + p_width

        lines.append(f"        return LevelChunk(width = {width_accum + 60}f, height = 600f, platforms = p, collectibles = c, endY = {prev_y_expr})\n")
        lines.append("    }\n\n")

    # 3. MEDIUM CHUNKS (40)
    lines.append("    fun generateMediumChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 40 + 40) % 40\n")
    lines.append("        return when (v) {\n")
    for i in range(40):
        lines.append(f"            {i} -> generateMediumTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateMediumTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(40):
        lines.append(f"    // Medium Template {i}\n")
        lines.append(f"    private fun generateMediumTemplate{i}(startX: Float, startY: Float): LevelChunk {{\n")
        lines.append("        val p = mutableListOf<BounceObstacle>()\n")
        lines.append("        val c = mutableListOf<BounceCollectible>()\n")
        lines.append("        val wz = mutableListOf<BounceWaterZone>()\n")
        lines.append("        val ib = mutableListOf<BounceInteractiveBlock>()\n")
        
        lines.append("        p.add(BounceObstacle(startX, startY, 140f, 40f))\n")
        
        gap1 = 80 + (i * 7) % 40
        w2 = 130 + (i * 9) % 40
        dy1 = ((i % 5) - 2) * 20
        p2_x = f"startX + 140f + {gap1}f"
        p2_y = f"startY + ({dy1}f)"
        
        is_falling = (i % 3 == 0)
        is_spike = (i % 4 == 1)
        is_breakable = (i % 4 == 2)
        has_water = (i % 5 == 0)
        
        if is_falling:
            lines.append(f"        p.add(BounceObstacle({p2_x}, {p2_y}, {w2}f, 35f, isFallingPlatform = true))\n")
        else:
            lines.append(f"        p.add(BounceObstacle({p2_x}, {p2_y}, {w2}f, 40f))\n")
            
        if is_spike:
            lines.append(f"        p.add(BounceObstacle({p2_x} + 40f, {p2_y} - 20f, 30f, 20f, isSpike = true, spikeDirection = SpikeDirection.UP))\n")
            
        if is_breakable:
            lines.append(f"        ib.add(BounceInteractiveBlock(id = 20000 + {i}, type = InteractiveType.BREAKABLE, x = {p2_x} + 40f, y = {p2_y} - 40f, width = 40f, height = 40f))\n")

        gap2 = 90 + (i * 11) % 40
        w3 = 160 + (i * 13) % 50
        dy2 = (((i+1) % 5) - 2) * 20
        p3_x = f"{p2_x} + {w2}f + {gap2}f"
        p3_y = f"{p2_y} + ({dy2}f)"
        
        lines.append(f"        p.add(BounceObstacle({p3_x}, {p3_y}, {w3}f, 40f))\n")
        
        if has_water:
            lines.append(f"        wz.add(BounceWaterZone(x = {p2_x} - 20f, y = startY + 80f, width = {w2 + gap2 + 40}f, height = 120f))\n")
            
        lines.append(f"        c.add(BounceCollectible(x = {p2_x} + {w2/2}f, y = {p2_y} - 55f, isStar = true))\n")
        lines.append(f"        c.add(BounceCollectible(x = {p3_x} + 50f, y = {p3_y} - 45f, isStar = false))\n")
        
        lines.append(f"        return LevelChunk(width = 140f + {gap1 + w2 + gap2 + w3}f, height = 600f, platforms = p, collectibles = c, waterZones = wz, interactiveBlocks = ib, endY = {p3_y})\n")
        lines.append("    }\n\n")

    # 4. FINAL CHALLENGE CHUNKS (40)
    lines.append("    fun generateFinalChallengeChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {\n")
    lines.append("        val v = (variation % 40 + 40) % 40\n")
    lines.append("        return when (v) {\n")
    for i in range(40):
        lines.append(f"            {i} -> generateFinalChallengeTemplate{i}(startX, startY, levelNum, difficulty)\n")
    lines.append("            else -> generateFinalChallengeTemplate0(startX, startY, levelNum, difficulty)\n        }\n    }\n\n")

    for i in range(40):
        lines.append(f"    // Final Challenge Template {i}\n")
        lines.append(f"    private fun generateFinalChallengeTemplate{i}(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {{\n")
        lines.append("        val p = mutableListOf<BounceObstacle>()\n")
        lines.append("        val c = mutableListOf<BounceCollectible>()\n")
        lines.append("        val e = mutableListOf<BounceEnemy>()\n")
        lines.append("        val ib = mutableListOf<BounceInteractiveBlock>()\n")
        lines.append("        val wz = mutableListOf<BounceWaterZone>()\n")
        
        lines.append("        p.add(BounceObstacle(startX, startY, 130f, 40f))\n")
        gap1 = 90 + (i * 5) % 35
        p2_x = f"startX + 130f + {gap1}f"
        p2_y = f"startY - {(i % 3) * 20 + 20}f"
        
        if i % 2 == 0:
            lines.append(f"        p.add(BounceObstacle({p2_x}, {p2_y}, 110f, 35f, isFallingPlatform = true))\n")
        else:
            lines.append(f"        p.add(BounceObstacle({p2_x}, {p2_y}, 110f, 30f, isMoving = true, moveRangeX = 80f, moveSpeed = 0.04f))\n")
            
        lines.append(f"        p.add(BounceObstacle({p2_x} - 30f, startY + 70f, 80f, 25f, isSpike = true, spikeDirection = SpikeDirection.UP))\n")

        gap2 = 100 + (i * 7) % 35
        p3_x = f"{p2_x} + 110f + {gap2}f"
        p3_y = f"{p2_y} - 20f"
        lines.append(f"        p.add(BounceObstacle({p3_x}, {p3_y}, 140f, 40f))\n")
        
        if i % 3 == 0:
            lines.append(f"        e.add(BounceEnemy(id = levelNum * 1000 + 800 + {i}, type = EnemyType.ROTATING_HAZARD, x = {p3_x} + 70f, y = {p3_y} - 70f, moveSpeed = 100f + difficulty * 20f))\n")
        elif i % 3 == 1:
            lines.append(f"        e.add(BounceEnemy(id = levelNum * 1000 + 800 + {i}, type = EnemyType.FLYING, x = {p3_x} + 70f, y = {p3_y} - 90f, moveRangeY = 50f, moveSpeed = 60f))\n")
        else:
            lines.append(f"        e.add(BounceEnemy(id = levelNum * 1000 + 800 + {i}, type = EnemyType.WALKING, x = {p3_x} + 70f, y = {p3_y} - 28f, moveRangeX = 60f, moveSpeed = 70f))\n")

        gap3 = 90 + (i * 9) % 35
        p4_x = f"{p3_x} + 140f + {gap3}f"
        p4_y = f"{p3_y} + 30f"
        lines.append(f"        p.add(BounceObstacle({p4_x}, {p4_y}, 160f, 40f))\n")
        
        lines.append(f"        c.add(BounceCollectible(x = {p3_x} + 70f, y = {p3_y} - 50f, isStar = true))\n")
        lines.append(f"        c.add(BounceCollectible(x = {p4_x} + 50f, y = {p4_y} - 45f, isStar = false))\n")
        
        lines.append(f"        return LevelChunk(width = 130f + {gap1 + 110 + gap2 + 140 + gap3 + 160}f, height = 600f, platforms = p, collectibles = c, enemies = e, interactiveBlocks = ib, waterZones = wz, endY = {p4_y})\n")
        lines.append("    }\n\n")

    # 5. SECRET CHUNKS (20)
    lines.append("    fun generateSecretChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 20 + 20) % 20\n")
    lines.append("        return when (v) {\n")
    for i in range(20):
        lines.append(f"            {i} -> generateSecretTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateSecretTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(20):
        lines.append(f"    // Secret Template {i}\n")
        lines.append(f"    private fun generateSecretTemplate{i}(startX: Float, startY: Float): LevelChunk {{\n")
        lines.append("        val p = mutableListOf<BounceObstacle>()\n")
        lines.append("        val c = mutableListOf<BounceCollectible>()\n")
        lines.append("        val ib = mutableListOf<BounceInteractiveBlock>()\n")
        
        lines.append("        p.add(BounceObstacle(startX, startY, 150f, 40f))\n")
        p2_x = "startX + 270f"
        p2_y = "startY + 30f"
        lines.append(f"        p.add(BounceObstacle({p2_x}, {p2_y}, 180f, 40f))\n")
        
        sec_x = f"startX + {160 + i * 5}f"
        sec_y = f"startY - {120 + (i % 4) * 15}f"
        lines.append(f"        p.add(BounceObstacle({sec_x}, {sec_y}, 140f, 30f))\n")
        
        lines.append(f"        ib.add(BounceInteractiveBlock(id = 30000 + {i}, type = InteractiveType.BREAKABLE, x = {sec_x} - 40f, y = {sec_y}, width = 40f, height = 30f))\n")
        lines.append(f"        c.add(BounceCollectible(x = {sec_x} + 70f, y = {sec_y} - 45f, isStar = true, isBonus = true))\n")
        
        p3_x = f"{p2_x} + 280f"
        p3_y = f"{p2_y} - 30f"
        lines.append(f"        p.add(BounceObstacle({p3_x}, {p3_y}, 170f, 40f))\n")
        lines.append(f"        c.add(BounceCollectible(x = {p2_x} + 90f, y = {p2_y} - 45f, isStar = true, isBonus = false))\n")
        
        lines.append(f"        return LevelChunk(width = 730f, height = 600f, platforms = p, collectibles = c, interactiveBlocks = ib, endY = {p3_y})\n")
        lines.append("    }\n\n")

    # 6. CHECKPOINT CHUNKS (15)
    lines.append("    fun generateCheckpointChunk(startX: Float, startY: Float, variation: Int, checkpointId: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 15 + 15) % 15\n")
    lines.append("        return when (v) {\n")
    for i in range(15):
        lines.append(f"            {i} -> generateCheckpointTemplate{i}(startX, startY, checkpointId)\n")
    lines.append("            else -> generateCheckpointTemplate0(startX, startY, checkpointId)\n        }\n    }\n\n")

    for i in range(15):
        w = 320 + i * 12
        lines.append(f"""    private fun generateCheckpointTemplate{i}(startX: Float, startY: Float, checkpointId: Int): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val cp = mutableListOf<BounceCheckpoint>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(x = startX, y = startY, width = {w}f, height = 45f))
        cp.add(BounceCheckpoint(id = checkpointId, x = startX + {w/2}f, y = startY - 40f))
        c.add(BounceCollectible(x = startX + 40f, y = startY - 45f, isStar = false))
        c.add(BounceCollectible(x = startX + {w - 50}f, y = startY - 45f, isStar = false))
        return LevelChunk(width = {w}f, height = 600f, platforms = p, checkpoints = cp, collectibles = c, endY = startY)
    }}\n\n""")

    # 7. EXIT CHUNKS (15)
    lines.append("    fun generateExitChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 15 + 15) % 15\n")
    lines.append("        return when (v) {\n")
    for i in range(15):
        lines.append(f"            {i} -> generateExitTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateExitTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(15):
        w = 400 + i * 15
        lines.append(f"""    private fun generateExitTemplate{i}(startX: Float, startY: Float): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val plat = BounceObstacle(x = startX, y = startY, width = {w}f, height = 100f, isExitPlatform = true)
        p.add(plat)
        return LevelChunk(width = {w}f, height = 600f, platforms = p, endY = startY, portalX = plat.topCenter.x, portalY = plat.topCenter.y)
    }}\n\n""")

    # 8. VERTICAL CHUNKS (25)
    lines.append("    fun generateVerticalChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 25 + 25) % 25\n")
    lines.append("        return when (v) {\n")
    for i in range(25):
        lines.append(f"            {i} -> generateVerticalTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateVerticalTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(25):
        lines.append(f"""    private fun generateVerticalTemplate{i}(startX: Float, startY: Float): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 130f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 400f, startY - 120f, 120f, 35f))
        p.add(BounceObstacle(startX + 590f, startY - 60f, 120f, 35f))
        p.add(BounceObstacle(startX + 780f, startY, 150f, 40f))
        c.add(BounceCollectible(startX + 460f, startY - 165f, isStar = true))
        return LevelChunk(width = 930f, height = 600f, platforms = p, collectibles = c, endY = startY)
    }}\n\n""")

    # 9. MOVING PLATFORM CHUNKS (30)
    lines.append("    fun generateMovingPlatformChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 30 + 30) % 30\n")
    lines.append("        return when (v) {\n")
    for i in range(30):
        lines.append(f"            {i} -> generateMovingTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateMovingTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(30):
        is_vert = (i % 2 == 1)
        lines.append(f"""    private fun generateMovingTemplate{i}(startX: Float, startY: Float): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        {"p.add(BounceObstacle(startX + 200f, startY - 30f, 110f, 30f, isMoving = true, moveRangeX = 120f, moveSpeed = 0.035f))" if not is_vert else "p.add(BounceObstacle(startX + 240f, startY - 30f, 110f, 30f, isMoving = true, moveRangeY = 100f, moveSpeed = 0.035f))"}
        p.add(BounceObstacle(startX + 450f, startY - 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 75f, isStar = true))
        return LevelChunk(width = 610f, height = 600f, platforms = p, collectibles = c, endY = startY - 10f)
    }}\n\n""")

    # 10. SPRING CHUNKS (20)
    lines.append("    fun generateSpringChunk(startX: Float, startY: Float, variation: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 20 + 20) % 20\n")
    lines.append("        return when (v) {\n")
    for i in range(20):
        lines.append(f"            {i} -> generateSpringTemplate{i}(startX, startY)\n")
    lines.append("            else -> generateSpringTemplate0(startX, startY)\n        }\n    }\n\n")

    for i in range(20):
        lines.append(f"""    private fun generateSpringTemplate{i}(startX: Float, startY: Float): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 190f, startY + 20f, 70f, 30f, isSpring = true, springForce = -680f))
        p.add(BounceObstacle(startX + 320f, startY - 140f, 170f, 35f))
        p.add(BounceObstacle(startX + 550f, startY - 30f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 80f, isStar = true))
        return LevelChunk(width = 710f, height = 600f, platforms = p, collectibles = c, endY = startY - 30f)
    }}\n\n""")

    # 11. ENEMY CHUNKS (30)
    lines.append("    fun generateEnemyChunk(startX: Float, startY: Float, variation: Int, levelNum: Int, difficulty: Float): LevelChunk {\n")
    lines.append("        val v = (variation % 30 + 30) % 30\n")
    lines.append("        return when (v) {\n")
    for i in range(30):
        lines.append(f"            {i} -> generateEnemyTemplate{i}(startX, startY, levelNum, difficulty)\n")
    lines.append("            else -> generateEnemyTemplate0(startX, startY, levelNum, difficulty)\n        }\n    }\n\n")

    for i in range(30):
        is_walk = (i % 2 == 0)
        lines.append(f"""    private fun generateEnemyTemplate{i}(startX: Float, startY: Float, levelNum: Int, difficulty: Float): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val e = mutableListOf<BounceEnemy>()
        val c = mutableListOf<BounceCollectible>()
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 220f, startY - 20f, 220f, 40f))
        {"e.add(BounceEnemy(id = levelNum * 1000 + 700 + " + str(i) + ", type = EnemyType.WALKING, x = startX + 330f, y = startY - 48f, moveRangeX = 80f, moveSpeed = 65f + difficulty * 15f))" if is_walk else "e.add(BounceEnemy(id = levelNum * 1000 + 700 + " + str(i) + ", type = EnemyType.FLYING, x = startX + 330f, y = startY - 90f, moveRangeY = 40f, moveSpeed = 55f + difficulty * 10f))"}
        p.add(BounceObstacle(startX + 520f, startY + 10f, 160f, 40f))
        c.add(BounceCollectible(startX + 250f, startY - 65f, isStar = true))
        return LevelChunk(width = 680f, height = 600f, platforms = p, enemies = e, collectibles = c, endY = startY + 10f)
    }}\n\n""")

    # 12. PUZZLE CHUNKS (25)
    lines.append("    fun generatePuzzleChunk(startX: Float, startY: Float, variation: Int, levelNum: Int): LevelChunk {\n")
    lines.append("        val v = (variation % 25 + 25) % 25\n")
    lines.append("        return when (v) {\n")
    for i in range(25):
        lines.append(f"            {i} -> generatePuzzleTemplate{i}(startX, startY, levelNum)\n")
    lines.append("            else -> generatePuzzleTemplate0(startX, startY, levelNum)\n        }\n    }\n\n")

    for i in range(25):
        lines.append(f"""    private fun generatePuzzleTemplate{i}(startX: Float, startY: Float, levelNum: Int): LevelChunk {{
        val p = mutableListOf<BounceObstacle>()
        val k = mutableListOf<BounceKey>()
        val d = mutableListOf<BounceDoor>()
        val c = mutableListOf<BounceCollectible>()
        val keyId = levelNum * 1000 + 500 + {i}
        p.add(BounceObstacle(startX, startY, 140f, 40f))
        p.add(BounceObstacle(startX + 210f, startY - 90f, 130f, 35f))
        k.add(BounceKey(id = keyId, x = startX + 275f, y = startY - 130f))
        p.add(BounceObstacle(startX + 190f, startY + 30f, 250f, 40f))
        d.add(BounceDoor(id = keyId, x = startX + 310f, y = startY - 50f, keyIdNeeded = keyId))
        p.add(BounceObstacle(startX + 520f, startY - 20f, 170f, 40f))
        c.add(BounceCollectible(startX + 560f, startY - 65f, isStar = true))
        return LevelChunk(width = 690f, height = 600f, platforms = p, keys = k, doors = d, collectibles = c, endY = startY - 20f)
    }}\n\n""")

    lines.append("}\n")

    target_path = "app/src/main/java/com/myplaywin/app/ui/screens/ChunkLibrary.kt"
    with open(target_path, "w") as f:
        f.write("".join(lines))

    print("Successfully generated ChunkLibrary.kt at relative path")

if __name__ == "__main__":
    generate()
