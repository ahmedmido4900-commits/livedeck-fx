package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PKBattleState
import com.example.ui.theme.*

@Composable
fun PKBattleSection(
    state: PKBattleState,
    onStartPK: (Int) -> Unit,
    onPauseResume: () -> Unit,
    onResetPK: () -> Unit,
    onAdjustScore: (Boolean, Int) -> Unit,
    onSpinPenalty: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDuration by remember { mutableStateOf(300) } // 5 minutes default
    val durations = listOf(60 to "1 دقيقة", 180 to "3 دقائق", 300 to "5 دقائق")

    val totalScore = (state.redScore + state.blueScore).coerceAtLeast(1)
    val redRatio = (state.redScore.toFloat() / totalScore).coerceIn(0.05f, 0.95f)

    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Main PK Scoreboard Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Timer & Controls Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.isActive && !state.isPaused) DangerRed else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!state.isActive) "جولة التحدي (PK Battle) ⚔️"
                            else if (state.isPaused) "الجولة متوقفة مؤقتاً ⏸️"
                            else "الجولة جارية الآن! 🔥",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    // Timer Display
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.remainingSeconds in 1..10 && state.isActive) DangerRed else DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Red vs Blue Teams Score Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(state.redName, color = RedTeam, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${state.redScore}", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    }

                    Text("VS", color = NeonGold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)

                    Column(horizontalAlignment = Alignment.End) {
                        Text(state.blueName, color = BlueTeam, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${state.blueScore}", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tug-of-War Score Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(redRatio)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(listOf(RedTeam, RedTeam.copy(alpha = 0.8f)))
                            )
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f - redRatio)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(listOf(BlueTeam.copy(alpha = 0.8f), BlueTeam))
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score Adjustment Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Red Team Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(50, 500, 1000).forEach { amount ->
                            Button(
                                onClick = { onAdjustScore(true, amount) },
                                colors = ButtonDefaults.buttonColors(containerColor = RedTeam.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("pk_red_$amount")
                            ) {
                                Text("+$amount", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Blue Team Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(50, 500, 1000).forEach { amount ->
                            Button(
                                onClick = { onAdjustScore(false, amount) },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueTeam.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("pk_blue_$amount")
                            ) {
                                Text("+$amount", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Match Duration & Timer Control Buttons
                if (!state.isActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            durations.forEach { (sec, label) ->
                                FilterChip(
                                    selected = selectedDuration == sec,
                                    onClick = { selectedDuration = sec },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TikTokPink,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onStartPK(selectedDuration) },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("start_pk_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بدء الجولة", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPauseResume,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (state.isPaused) "استئناف" else "إيقاف مؤقت")
                        }

                        Button(
                            onClick = onResetPK,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إنهاء الجولة")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Penalty Wheel / الحكم العشوائي Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, NeonGold.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "عجلة الأحكام العشوائية ⚖️",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGold
                        )
                        Text(
                            text = "اضغط لاختيار حكم عشوائي ومحرج للخاسر في الجولة!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onSpinPenalty,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("spin_penalty_button")
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سحب حكم", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }

                // Active Penalty Reveal Box
                AnimatedVisibility(visible = state.activePenalty != null) {
                    state.activePenalty?.let { penalty ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = BorderStroke(1.dp, TikTokPink),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "الحكم المطلوب تنفيذه:",
                                    fontSize = 11.sp,
                                    color = TikTokPink,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = penalty,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
