package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplaywin.app.blockmaster.cosmetics.CosmeticCategory
import com.myplaywin.app.blockmaster.cosmetics.CosmeticItem
import com.myplaywin.app.blockmaster.cosmetics.CosmeticRegistry

@Composable
fun BlockMasterCollectionsDialog(
    unlockedCosmeticIds: Set<String>,
    equippedSkin: String,
    equippedTheme: String,
    equippedFrame: String,
    equippedBackground: String,
    equippedTitle: String,
    onEquipCosmetic: (CosmeticCategory, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CosmeticCategory.BLOCK_SKIN) }
    val categoryItems = remember(selectedCategory) { CosmeticRegistry.getCosmeticsByCategory(selectedCategory) }
    val completionPercent = remember(unlockedCosmeticIds) { CosmeticRegistry.calculateCompletionPercentage(unlockedCosmeticIds) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1433), Color(0xFF0F0B1C))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFA855F7),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "COSMETIC COLLECTION",
                                color = Color(0xFFA855F7),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Completion: $completionPercent%",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Completion Progress Bar
                LinearProgressIndicator(
                    progress = { completionPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFFA855F7),
                    trackColor = Color.DarkGray
                )

                // Category Tabs Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CosmeticCategory.values().take(4).forEach { cat ->
                        val isSel = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFFA855F7).copy(alpha = 0.3f) else Color(0xFF161126),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFFA855F7) else Color.DarkGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.displayName.split(" ").first()}",
                                    color = if (isSel) Color.White else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Items Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categoryItems) { item ->
                        val isUnlocked = item.isUnlockedByDefault || unlockedCosmeticIds.contains(item.id)
                        val isEquipped = when (item.category) {
                            CosmeticCategory.BLOCK_SKIN -> equippedSkin == item.id
                            CosmeticCategory.GRID_THEME -> equippedTheme == item.id
                            CosmeticCategory.BOARD_FRAME -> equippedFrame == item.id
                            CosmeticCategory.BACKGROUND -> equippedBackground == item.id
                            CosmeticCategory.TITLE -> equippedTitle == item.name
                            else -> false
                        }

                        CosmeticItemCard(
                            item = item,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            onEquip = { onEquipCosmetic(item.category, item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CosmeticItemCard(
    item: CosmeticItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF19132D) else Color(0xFF110D20)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isEquipped) Color(0xFFFFD700) else if (isUnlocked) Color(item.rarityColorHex).copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(item.rarityColorHex).copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = item.iconEmoji, fontSize = 20.sp)
                }
            }

            Text(
                text = item.name,
                color = if (isUnlocked) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Text(
                text = item.rarity,
                color = Color(item.rarityColorHex),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )

            if (isUnlocked) {
                if (isEquipped) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFD700)
                    ) {
                        Text(
                            text = "EQUIPPED ✓",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onEquip,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(22.dp)
                    ) {
                        Text(text = "EQUIP", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                Text(text = "LOCKED 🔒", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
