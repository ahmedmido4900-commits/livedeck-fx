package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveVisualEffect
import com.example.model.VisualEffectType
import com.example.ui.theme.*

@Composable
fun VisualEffectsSection(
    activeEffect: ActiveVisualEffect?,
    onTriggerEffect: (VisualEffectType) -> Unit,
    onOpenOverlayStage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Stage Mode Launcher Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, TikTokCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(TikTokCyan.copy(alpha = 0.15f), DarkCard)
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "شاشة العرض والمؤثرات (Overlay Stage) 📺",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "افتح الشاشة بالحجم الكامل أو وضع الكروما الخضراء لعرض المؤثرات والهدايا أمام كاميرا البث أو OBS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onOpenOverlayStage,
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_overlay_stage_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("عرض الشاشة", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Active Effect Status Banner
        if (activeEffect != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, TikTokPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(activeEffect.iconEmoji ?: "✨", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المؤثر المرئي النشط الآن:",
                            fontSize = 11.sp,
                            color = TikTokPink,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeEffect.messageAr ?: activeEffect.type.titleAr,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Visual Effects Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            items(VisualEffectType.values(), key = { it.name }) { effect ->
                val isActive = activeEffect?.type == effect
                VisualEffectCard(
                    effect = effect,
                    isActive = isActive,
                    onClick = { onTriggerEffect(effect) }
                )
            }
        }
    }
}

@Composable
fun VisualEffectCard(
    effect: VisualEffectType,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val effectColor = when (effect) {
        VisualEffectType.DISCO_STROBE -> TikTokPurple
        VisualEffectType.CONFETTI -> TikTokPink
        VisualEffectType.GOLD_STARS -> NeonGold
        VisualEffectType.HEART_RAIN -> TikTokPink
        VisualEffectType.FIRE_FLAMES -> NeonOrange
        VisualEffectType.RED_ALERT -> DangerRed
        VisualEffectType.LASER_BEAMS -> TikTokCyan
        VisualEffectType.NEON_PULSE -> NeonGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick)
            .testTag("fx_${effect.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(if (isActive) 2.dp else 1.dp, if (isActive) effectColor else DarkCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            effectColor.copy(alpha = if (isActive) 0.35f else 0.12f),
                            DarkCard
                        )
                    )
                )
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(effect.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = effect.titleAr,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isActive) "نشط الآن..." else "اضغط للإطلاق",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) effectColor else TextMuted,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
