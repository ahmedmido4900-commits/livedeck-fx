package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveGoal
import com.example.ui.theme.*

@Composable
fun GoalsAndTickerSection(
    goals: List<LiveGoal>,
    tickerText: String,
    isTickerActive: Boolean,
    activityLog: List<String>,
    onIncrementGoal: (String, Int) -> Unit,
    onResetGoal: (String) -> Unit,
    onUpdateTicker: (String) -> Unit,
    onToggleTicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditTickerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Live Marquee Ticker Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, TikTokPink.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = TikTokPink, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الشريط الإعلاني المتحرك (Ticker) 📢",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    Switch(
                        checked = isTickerActive,
                        onCheckedChange = { onToggleTicker() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TikTokPink
                        ),
                        modifier = Modifier.testTag("toggle_ticker_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ticker Preview Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isTickerActive) tickerText else "(الشريط متوقف حالياً)",
                        color = if (isTickerActive) TikTokCyan else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showEditTickerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_ticker_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل نص الشريط المتحرك", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Goals Header
        Text(
            text = "أهداف البث المباشر (Goals) 🎯",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = "اضغط على الأزرار السريعة لزيادة عداد التكبيس والدعم أثناء البث.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Goals List
        goals.forEach { goal ->
            GoalCard(
                goal = goal,
                onIncrement = { amount -> onIncrementGoal(goal.id, amount) },
                onReset = { onResetGoal(goal.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Activity Feed Log
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "سجل الأحداث المباشرة (Live Feed) ⚡",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                ) {
                    items(activityLog) { log ->
                        Text(
                            text = log,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditTickerDialog) {
        var draftText by remember { mutableStateOf(tickerText) }
        AlertDialog(
            onDismissRequest = { showEditTickerDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("تعديل نص الشريط المتحرك 📢", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column {
                    Text(
                        "اكتب الرسالة التي تريد أن تظهر وتتحرك في أعلى شاشة البث (ترحيب، شكر للداعمين، حسابات التواصل، إلخ):",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = TikTokPink,
                            unfocusedBorderColor = DarkCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTicker(draftText)
                        showEditTickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink)
                ) {
                    Text("حفظ وتحديث", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTickerDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: LiveGoal,
    onIncrement: (Int) -> Unit,
    onReset: () -> Unit
) {
    val progress = (goal.current.toFloat() / goal.target.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkCardBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.type.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = goal.titleAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${goal.current} / ${goal.target}",
                        fontWeight = FontWeight.Bold,
                        color = if (progress >= 1f) NeonGreen else TikTokCyan,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "تصفير العداد",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress >= 1f) NeonGreen else TikTokPink,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Increments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val increments = when (goal.type) {
                    com.example.model.GoalType.LIKES -> listOf(100, 500, 1000, 5000)
                    com.example.model.GoalType.FOLLOWERS -> listOf(5, 10, 25, 50)
                    com.example.model.GoalType.GIFTS -> listOf(100, 500, 1000, 2500)
                }

                increments.forEach { amt ->
                    Button(
                        onClick = { onIncrement(amt) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+$amt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}
