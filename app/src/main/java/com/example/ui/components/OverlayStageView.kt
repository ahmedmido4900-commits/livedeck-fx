package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun OverlayStageView(
    activeEffect: ActiveVisualEffect?,
    activeGift: ActiveGiftAnimation?,
    pkState: PKBattleState,
    tickerText: String,
    isTickerActive: Boolean,
    stageBackground: StageBackgroundStyle,
    onBackgroundChange: (StageBackgroundStyle) -> Unit,
    onExitStage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (stageBackground) {
        StageBackgroundStyle.CHROMA_GREEN -> ChromaGreen
        StageBackgroundStyle.DARK_STUDIO -> DarkBackground
        StageBackgroundStyle.NEON_GRADIENT -> DarkBackground
    }

    var showBgMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (stageBackground == StageBackgroundStyle.NEON_GRADIENT) {
                    Brush.radialGradient(
                        colors = listOf(DarkSurfaceVariant, DarkBackground, Color.Black)
                    )
                } else {
                    Brush.linearGradient(listOf(bgColor, bgColor))
                }
            )
    ) {
        // Dynamic Particle Canvas (Confetti, Hearts, Fire, Stars)
        if (activeEffect != null) {
            ParticleAnimationCanvas(effectType = activeEffect.type)
        }

        // Top Content: Scrolling Marquee Ticker
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            if (isTickerActive) {
                ContinuousMarqueeTicker(text = tickerText)
            }

            // PK Battle Mini HUD (if active)
            if (pkState.isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                PKBattleStageHUD(pkState = pkState)
            }
        }

        // Center Content: Active Gift Animation or Effect Announcement
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Gift Mega Alert Card
            AnimatedVisibility(
                visible = activeGift != null,
                enter = scaleIn(tween(400, easing = OvershootInterpolator(2f)::getInterpolation)) + fadeIn(),
                exit = scaleOut(tween(300)) + fadeOut()
            ) {
                activeGift?.let { gift ->
                    GiftMegaAnnouncement(gift = gift)
                }
            }

            // Visual Effect Announcement (if no gift active)
            if (activeGift == null && activeEffect != null && activeEffect.messageAr != null) {
                EffectAnnouncementBanner(effect = activeEffect)
            }
        }

        // Top Floating Control Bar (Exit button & Chroma background picker)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Background Picker Button
            Box {
                IconButton(
                    onClick = { showBgMenu = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "تغيير الخلفية",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showBgMenu,
                    onDismissRequest = { showBgMenu = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    StageBackgroundStyle.values().forEach { style ->
                        DropdownMenuItem(
                            text = { Text(style.titleAr, color = TextPrimary) },
                            onClick = {
                                onBackgroundChange(style)
                                showBgMenu = false
                            }
                        )
                    }
                }
            }

            // Exit Stage Mode Button
            IconButton(
                onClick = onExitStage,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .testTag("exit_stage_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "الرجوع للوحة التحكم",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Overshoot interpolator helper for bounce animations
class OvershootInterpolator(private val tension: Float = 2.0f) {
    fun getInterpolation(t: Float): Float {
        val tMinusOne = t - 1.0f
        return tMinusOne * tMinusOne * ((tension + 1) * tMinusOne + tension) + 1.0f
    }
}

@Composable
fun GiftMegaAnnouncement(gift: ActiveGiftAnimation) {
    val infiniteTransition = rememberInfiniteTransition(label = "gift_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.95f)),
        border = BorderStroke(3.dp, NeonGold)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NeonGold.copy(alpha = 0.35f),
                            TikTokPink.copy(alpha = 0.3f),
                            DarkCard
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Giant Animated Emoji
                Text(
                    text = gift.giftType.iconEmoji,
                    fontSize = 72.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Gift Name & Badge
                Text(
                    text = gift.giftType.titleAr,
                    style = MaterialTheme.typography.displayLarge,
                    color = NeonGold,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Donor Name Tag
                Card(
                    colors = CardDefaults.cardColors(containerColor = TikTokPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "الداعم: ${gift.senderName}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💎 ${gift.giftType.coins} عملة",
                        color = TikTokCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (gift.comboCount > 1) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NeonGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "COMBO x${gift.comboCount}",
                                color = DarkBackground,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EffectAnnouncementBanner(effect: ActiveVisualEffect) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.9f)),
        border = BorderStroke(2.dp, TikTokPink)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(effect.iconEmoji ?: "✨", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = effect.messageAr ?: effect.type.titleAr,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PKBattleStageHUD(pkState: PKBattleState) {
    val total = (pkState.redScore + pkState.blueScore).coerceAtLeast(1)
    val redRatio = (pkState.redScore.toFloat() / total).coerceIn(0.05f, 0.95f)

    val minutes = pkState.remainingSeconds / 60
    val seconds = pkState.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔴 ${pkState.redScore}", color = RedTeam, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(timeFormatted, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("${pkState.blueScore} 🔵", color = BlueTeam, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Score Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(redRatio)
                        .fillMaxHeight()
                        .background(RedTeam)
                )
                Box(
                    modifier = Modifier
                        .weight(1f - redRatio)
                        .fillMaxHeight()
                        .background(BlueTeam)
                )
            }
        }
    }
}

@Composable
fun ContinuousMarqueeTicker(text: String) {
    val transition = rememberInfiniteTransition(label = "marquee")
    val offsetProgress by transition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, TikTokPink.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📢 $text 🚀",
                color = TikTokCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Particle Canvas simulating Confetti, Hearts, Stars, Fire
@Composable
fun ParticleAnimationCanvas(effectType: VisualEffectType) {
    val particles = remember(effectType) {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.008f + 0.003f,
                size = Random.nextFloat() * 16f + 8f,
                color = when (effectType) {
                    VisualEffectType.CONFETTI -> listOf(TikTokPink, TikTokCyan, NeonGold, NeonGreen, TikTokPurple).random()
                    VisualEffectType.HEART_RAIN -> listOf(TikTokPink, DangerRed, Color(0xFFFF4081)).random()
                    VisualEffectType.GOLD_STARS -> listOf(NeonGold, Color(0xFFFFEE58), Color.White).random()
                    VisualEffectType.FIRE_FLAMES -> listOf(NeonOrange, DangerRed, NeonGold).random()
                    VisualEffectType.DISCO_STROBE -> listOf(TikTokPink, TikTokCyan, TikTokPurple).random()
                    else -> TikTokCyan
                },
                rotation = Random.nextFloat() * 360f,
                rotSpeed = (Random.nextFloat() - 0.5f) * 6f
            )
        }
    }

    var frameTick by remember { mutableStateOf(0L) }
    LaunchedEffect(effectType) {
        while (true) {
            delay(16) // ~60 FPS
            particles.forEach { p ->
                if (effectType == VisualEffectType.HEART_RAIN || effectType == VisualEffectType.FIRE_FLAMES) {
                    // Float upward
                    p.y -= p.speed
                    if (p.y < -0.1f) p.y = 1.1f
                } else {
                    // Fall downward
                    p.y += p.speed
                    if (p.y > 1.1f) p.y = -0.1f
                }
                p.rotation += p.rotSpeed
            }
            frameTick++
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val unused = frameTick // Trigger recomposition

        particles.forEach { p ->
            val px = p.x * w
            val py = p.y * h

            when (effectType) {
                VisualEffectType.CONFETTI -> {
                    rotate(p.rotation, pivot = Offset(px, py)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(px - p.size / 2, py - p.size / 4),
                            size = Size(p.size, p.size / 2)
                        )
                    }
                }
                VisualEffectType.HEART_RAIN -> {
                    drawCircle(
                        color = p.color,
                        radius = p.size / 2,
                        center = Offset(px, py)
                    )
                }
                VisualEffectType.GOLD_STARS -> {
                    rotate(p.rotation, pivot = Offset(px, py)) {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 3,
                            center = Offset(px, py)
                        )
                    }
                }
                VisualEffectType.FIRE_FLAMES -> {
                    drawCircle(
                        color = p.color.copy(alpha = 0.7f),
                        radius = p.size / 2,
                        center = Offset(px, py)
                    )
                }
                else -> {
                    drawCircle(
                        color = p.color.copy(alpha = 0.5f),
                        radius = p.size / 3,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

class Particle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var size: Float,
    var color: Color,
    var rotation: Float,
    var rotSpeed: Float
)
