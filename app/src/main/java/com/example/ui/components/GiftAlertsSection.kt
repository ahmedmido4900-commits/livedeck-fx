package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveGiftAnimation
import com.example.model.GiftType
import com.example.ui.theme.*

@Composable
fun GiftAlertsSection(
    activeGift: ActiveGiftAnimation?,
    onTriggerGift: (GiftType, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var senderName by remember { mutableStateOf("فاعل خير") }
    var selectedCombo by remember { mutableStateOf(1) }
    val quickSenders = listOf("فاعل خير", "أبو فهد 🦁", "الملك 👑", "الداعم الذهبي ⭐", "سارة 💖")
    val combos = listOf(1, 10, 50, 100)

    Column(modifier = modifier.fillMaxWidth()) {
        // Active Gift Broadcast Alert Banner (if triggered)
        AnimatedVisibility(
            visible = activeGift != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            activeGift?.let { gift ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(2.dp, NeonGold)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        TikTokPink.copy(alpha = 0.3f),
                                        NeonGold.copy(alpha = 0.4f),
                                        TikTokCyan.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(gift.giftType.iconEmoji, fontSize = 42.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${gift.giftType.titleAr} (${gift.giftType.coins} عملة)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NeonGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "الداعم: ${gift.senderName}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                }
                            }

                            if (gift.comboCount > 1) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = TikTokPink),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "x${gift.comboCount}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Configuration Card (Donor Name & Combo multiplier)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "إعدادات إشعار الهدية في اللايف:",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("اسم الداعم / المرسل") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TikTokCyan)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TikTokCyan,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sender_name_input")
                )

                // Quick Names Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSenders.forEach { name ->
                        AssistChip(
                            onClick = { senderName = name },
                            label = { Text(name, fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        )
                    }
                }

                // Combo Multipliers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("المضاعف (Combo):", color = TextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        combos.forEach { combo ->
                            FilterChip(
                                selected = selectedCombo == combo,
                                onClick = { selectedCombo = combo },
                                label = { Text("x$combo", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TikTokPink,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextPrimary
                                ),
                                border = BorderStroke(1.dp, if (selectedCombo == combo) TikTokPink else DarkCardBorder)
                            )
                        }
                    }
                }
            }
        }

        // Gifts Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            items(GiftType.values(), key = { it.name }) { gift ->
                GiftCard(
                    gift = gift,
                    combo = selectedCombo,
                    onClick = { onTriggerGift(gift, senderName, selectedCombo) }
                )
            }
        }
    }
}

@Composable
fun GiftCard(
    gift: GiftType,
    combo: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick)
            .testTag("gift_${gift.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            when (gift) {
                                GiftType.LION -> NeonGold.copy(alpha = 0.2f)
                                GiftType.GALAXY -> TikTokPurple.copy(alpha = 0.25f)
                                GiftType.UNIVERSE -> TikTokPink.copy(alpha = 0.2f)
                                GiftType.ROSE -> TikTokPink.copy(alpha = 0.15f)
                                else -> TikTokCyan.copy(alpha = 0.15f)
                            },
                            DarkCard
                        )
                    )
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(gift.iconEmoji, fontSize = 30.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "💎 ${gift.coins}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = gift.titleAr,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إشعار + صوت + شاشة",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = TikTokPink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
