package com.playwin.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.playwin.app.data.model.FirebaseScratchCardReward
import com.playwin.app.data.model.FirebaseScratchCardSettings
import com.playwin.app.ui.viewmodel.PlayWinViewModel

// Define app color scheme variables
private val DarkBg = Color(0xFF0F0E17)
private val CardDark = Color(0xFF1F1D2B)
private val PrimaryDark = Color(0xFF7F5AF0)
private val GoldCoin = Color(0xFFFFD700)
private val TextWhite = Color(0xFFFFFFFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuckyScratchAdminScreen(viewModel: PlayWinViewModel, onBack: () -> Unit) {
    val settings by viewModel.scratchCardSettingsState.collectAsState()
    val rewards by viewModel.scratchCardRewardsState.collectAsState()

    var showAddEditDialog by remember { mutableStateOf<FirebaseScratchCardReward?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Settings Text Field States
    var dailyLimitStr by remember(settings) { mutableStateOf(settings.dailyLimit.toString()) }
    var cooldownMinutesStr by remember(settings) { mutableStateOf(settings.cooldownMinutes.toString()) }
    var minimumLevelStr by remember(settings) { mutableStateOf(settings.minimumLevel.toString()) }
    var gameEnabled by remember(settings) { mutableStateOf(settings.enabled) }
    var adRequired by remember(settings) { mutableStateOf(settings.rewardAdRequired) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scratch Card Admin Console", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Settings
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Global Game Controls",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryDark,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Scratch Card Enabled", color = TextWhite, fontWeight = FontWeight.SemiBold)
                                Text("Toggle the entire feature on/off for users", color = Color.Gray, fontSize = 12.sp)
                            }
                            Switch(
                                checked = gameEnabled,
                                onCheckedChange = { newVal ->
                                    gameEnabled = newVal
                                    val updatedSettings = settings.copy(enabled = newVal)
                                    viewModel.updateScratchCardSettings(updatedSettings)
                                },
                                modifier = Modifier.testTag("admin_enabled_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rewarded Ad Required", color = TextWhite, fontWeight = FontWeight.SemiBold)
                                Text("Require users to watch ads after free scratch", color = Color.Gray, fontSize = 12.sp)
                            }
                            Switch(
                                checked = adRequired,
                                onCheckedChange = { newVal ->
                                    adRequired = newVal
                                    val updatedSettings = settings.copy(rewardAdRequired = newVal)
                                    viewModel.updateScratchCardSettings(updatedSettings)
                                },
                                modifier = Modifier.testTag("admin_ad_switch")
                            )
                        }

                        OutlinedTextField(
                            value = dailyLimitStr,
                            onValueChange = { newVal ->
                                dailyLimitStr = newVal
                                val limit = newVal.toIntOrNull()
                                if (limit != null && limit >= 0) {
                                    val updatedSettings = settings.copy(dailyLimit = limit)
                                    viewModel.updateScratchCardSettings(updatedSettings)
                                }
                            },
                            label = { Text("Daily Scratch Limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = PrimaryDark,
                                focusedBorderColor = PrimaryDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_daily_limit_input")
                        )

                        OutlinedTextField(
                            value = cooldownMinutesStr,
                            onValueChange = { newVal ->
                                cooldownMinutesStr = newVal
                                val cooldown = newVal.toIntOrNull()
                                if (cooldown != null && cooldown >= 0) {
                                    val updatedSettings = settings.copy(cooldownMinutes = cooldown)
                                    viewModel.updateScratchCardSettings(updatedSettings)
                                }
                            },
                            label = { Text("Cooldown Duration (Minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = PrimaryDark,
                                focusedBorderColor = PrimaryDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_cooldown_input")
                        )

                        OutlinedTextField(
                            value = minimumLevelStr,
                            onValueChange = { newVal ->
                                minimumLevelStr = newVal
                                val minLevel = newVal.toIntOrNull()
                                if (minLevel != null && minLevel >= 0) {
                                    val updatedSettings = settings.copy(minimumLevel = minLevel)
                                    viewModel.updateScratchCardSettings(updatedSettings)
                                }
                            },
                            label = { Text("Minimum Level Requirement") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = PrimaryDark,
                                focusedBorderColor = PrimaryDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_min_level_input")
                        )

                        Button(
                            onClick = {
                                val limit = dailyLimitStr.toIntOrNull() ?: settings.dailyLimit
                                val cooldown = cooldownMinutesStr.toIntOrNull() ?: settings.cooldownMinutes
                                val minLevel = minimumLevelStr.toIntOrNull() ?: settings.minimumLevel
                                val newSettings = FirebaseScratchCardSettings(
                                    enabled = gameEnabled,
                                    dailyLimit = limit,
                                    cooldownMinutes = cooldown,
                                    rewardAdRequired = adRequired,
                                    minimumLevel = minLevel
                                )
                                viewModel.updateScratchCardSettings(newSettings) { success ->
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Settings saved to Firebase!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to save settings.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                            modifier = Modifier.fillMaxWidth().testTag("admin_save_settings_btn")
                        ) {
                            Text("Save Global Settings", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: Rewards Header & Add Reward
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Reward Pools",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_add_reward_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Reward", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 3: Rewards list
            if (rewards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No rewards in pool. Defaulting seed...", color = Color.Gray)
                    }
                }
            } else {
                val sortedRewards = rewards.sortedBy { it.displayOrder }
                itemsIndexed(sortedRewards) { index: Int, reward: FirebaseScratchCardReward ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reward Icon & color highlight
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            try {
                                                Color(android.graphics.Color.parseColor(reward.color))
                                            } catch (e: Exception) {
                                                PrimaryDark
                                            }.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(reward.icon, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Name and Probability detail
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reward.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(reward.type, fontSize = 11.sp, color = GoldCoin, fontWeight = FontWeight.Bold)
                                        Text("•", color = Color.Gray, fontSize = 11.sp)
                                        Text("Weight: ${reward.probabilityWeight}", fontSize = 11.sp, color = Color.LightGray)
                                        Text("•", color = Color.Gray, fontSize = 11.sp)
                                        Text("Order: ${reward.displayOrder}", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                }
                            }

                            Divider(color = Color.Gray.copy(alpha = 0.15f), thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Enable/Disable directly in list row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Switch(
                                        checked = reward.active,
                                        onCheckedChange = { activeState ->
                                            viewModel.saveScratchCardReward(reward.copy(active = activeState))
                                        }
                                    )
                                    Text(
                                        text = if (reward.active) "Active" else "Disabled",
                                        color = if (reward.active) Color(0xFF00E676) else Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                // Reordering Actions (Up / Down) & Edit/Delete
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val prevReward = sortedRewards[index - 1]
                                                val currentOrder = reward.displayOrder
                                                val prevOrder = prevReward.displayOrder
                                                viewModel.saveScratchCardReward(reward.copy(displayOrder = prevOrder))
                                                viewModel.saveScratchCardReward(prevReward.copy(displayOrder = currentOrder))
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) PrimaryDark else Color.Gray
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < sortedRewards.size - 1) {
                                                val nextReward = sortedRewards[index + 1]
                                                val currentOrder = reward.displayOrder
                                                val nextOrder = nextReward.displayOrder
                                                viewModel.saveScratchCardReward(reward.copy(displayOrder = nextOrder))
                                                viewModel.saveScratchCardReward(nextReward.copy(displayOrder = currentOrder))
                                            }
                                        },
                                        enabled = index < sortedRewards.size - 1,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = if (index < sortedRewards.size - 1) PrimaryDark else Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { showAddEditDialog = reward },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Cyan)
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteScratchCardReward(reward.id) { success ->
                                                if (success) {
                                                    android.widget.Toast.makeText(context, "Reward deleted from Firebase!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Reward Dialog
    if (showAddDialog) {
        RewardEditDialog(
            reward = FirebaseScratchCardReward(id = "reward_${System.currentTimeMillis()}"),
            onDismiss = { showAddDialog = false },
            onSave = { rewardToSave ->
                viewModel.saveScratchCardReward(rewardToSave) { success ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Reward added to Firebase!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Reward Dialog
    showAddEditDialog?.let { currentReward ->
        RewardEditDialog(
            reward = currentReward,
            onDismiss = { showAddEditDialog = null },
            onSave = { rewardToSave ->
                viewModel.saveScratchCardReward(rewardToSave) { success ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Reward updated in Firebase!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    showAddEditDialog = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardEditDialog(
    reward: FirebaseScratchCardReward,
    onDismiss: () -> Unit,
    onSave: (FirebaseScratchCardReward) -> Unit
) {
    var name by remember { mutableStateOf(reward.name) }
    var type by remember { mutableStateOf(reward.type) }
    var value by remember { mutableStateOf(reward.value) }
    var weightStr by remember { mutableStateOf(reward.probabilityWeight.toString()) }
    var displayOrderStr by remember { mutableStateOf(reward.displayOrder.toString()) }
    var active by remember { mutableStateOf(reward.active) }
    var icon by remember { mutableStateOf(reward.icon) }
    var color by remember { mutableStateOf(reward.color) }

    val rewardTypes = listOf("Coins", "Coupon", "Retry Scratch", "Better Luck Next Time")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (reward.name.isEmpty()) "Add New Reward" else "Edit Reward Pool",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = PrimaryDark),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_reward_name")
                )

                // Type Dropdown (Simple implementation)
                Column {
                    Text("Reward Type", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rewardTypes.forEach { t ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (type == t) PrimaryDark else Color.DarkGray)
                                    .clickable { type = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t.split(" ").first(),
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value (Coin amount or Coupon code)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = PrimaryDark),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_reward_value")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = PrimaryDark),
                        modifier = Modifier.weight(1f).testTag("dialog_reward_weight")
                    )
                    OutlinedTextField(
                        value = displayOrderStr,
                        onValueChange = { displayOrderStr = it },
                        label = { Text("Order") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = PrimaryDark),
                        modifier = Modifier.weight(1f).testTag("dialog_reward_order")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("Emoji Icon") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = PrimaryDark),
                        modifier = Modifier.weight(1f).testTag("dialog_reward_icon")
                    )
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Hex Color") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = PrimaryDark),
                        modifier = Modifier.weight(1f).testTag("dialog_reward_color")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Is Pool Active", color = TextWhite)
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val finalWeight = weightStr.toIntOrNull() ?: 10
                            val finalOrder = displayOrderStr.toIntOrNull() ?: 0
                            onSave(
                                reward.copy(
                                    name = name,
                                    type = type,
                                    value = value,
                                    probabilityWeight = finalWeight,
                                    displayOrder = finalOrder,
                                    active = active,
                                    icon = icon,
                                    color = color
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                        modifier = Modifier.testTag("dialog_save_reward")
                    ) {
                        Text("Save Reward", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
