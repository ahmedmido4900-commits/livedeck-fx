package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackgroundMusicMode
import com.example.model.SoundCategory
import com.example.model.SoundTile
import com.example.ui.theme.*

@Composable
fun SoundboardSection(
    soundTiles: List<SoundTile>,
    selectedCategory: SoundCategory,
    currentlyPlayingId: String?,
    masterVolume: Float,
    backgroundMusic: BackgroundMusicMode,
    isRecordingAudio: Boolean,
    onCategorySelected: (SoundCategory) -> Unit,
    onSoundTriggered: (SoundTile) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onBackgroundMusicSelected: (BackgroundMusicMode) -> Unit,
    onStopAll: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: (String) -> Unit,
    onCancelRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecordDialog by remember { mutableStateOf(false) }

    val filteredTiles = remember(soundTiles, selectedCategory) {
        if (selectedCategory == SoundCategory.ALL) soundTiles
        else soundTiles.filter { it.category == selectedCategory }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Master Audio Controls Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "الصوت العام",
                            tint = TikTokCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مستوى الصوت العام: ${(masterVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    // Emergency Stop Button
                    Button(
                        onClick = onStopAll,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("stop_all_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "إيقاف", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إيقاف الكل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Slider(
                    value = masterVolume,
                    onValueChange = onVolumeChanged,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = TikTokCyan,
                        activeTrackColor = TikTokCyan,
                        inactiveTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("master_volume_slider")
                )

                // Background Ambient Loop Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الموسيقى الخلفية:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BackgroundMusicMode.values().forEach { mode ->
                            val isSelected = backgroundMusic == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { onBackgroundMusicSelected(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            BackgroundMusicMode.NONE -> "إيقاف ⏹️"
                                            BackgroundMusicMode.HYPE_BEAT -> "حماسي ⚡"
                                            BackgroundMusicMode.LOFI_CHILL -> "Lo-Fi ☕"
                                            BackgroundMusicMode.SUSPENSE_COUNTDOWN -> "ترقب 💓"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TikTokPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) TikTokPurple else DarkCardBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Tabs & Record Shoutout Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScrollableTabRow(
                selectedTabIndex = SoundCategory.values().indexOf(selectedCategory),
                edgePadding = 4.dp,
                containerColor = Color.Transparent,
                contentColor = TikTokPink,
                indicator = {},
                divider = {},
                modifier = Modifier.weight(1f)
            ) {
                SoundCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(category.titleAr, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TikTokPink,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, if (isSelected) TikTokPink else DarkCardBorder),
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            // Record Custom Shoutout Button
            IconButton(
                onClick = { showRecordDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .size(42.dp)
                    .testTag("record_audio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "تسجيل صوت مخصص",
                    tint = TikTokPink,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Soundboard Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            items(filteredTiles, key = { it.id }) { tile ->
                val isPlaying = currentlyPlayingId == tile.id
                SoundTilePad(
                    tile = tile,
                    isPlaying = isPlaying,
                    onClick = { onSoundTriggered(tile) }
                )
            }
        }
    }

    if (showRecordDialog) {
        RecordAudioDialog(
            isRecording = isRecordingAudio,
            onStartRecording = onStartRecording,
            onStopRecording = { title ->
                onStopRecording(title)
                showRecordDialog = false
            },
            onCancel = {
                onCancelRecording()
                showRecordDialog = false
            }
        )
    }
}

@Composable
fun SoundTilePad(
    tile: SoundTile,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val tileColor = Color(tile.colorHex)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val scale = if (isPlaying) pulseScale else 1f
    val borderColor by animateColorAsState(
        targetValue = if (isPlaying) tileColor else DarkCardBorder,
        label = "border"
    )

    Card(
        modifier = Modifier
            .scale(scale)
            .height(105.dp)
            .clickable(onClick = onClick)
            .testTag("sound_tile_${tile.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(if (isPlaying) 2.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tileColor.copy(alpha = if (isPlaying) 0.35f else 0.12f),
                            DarkCard
                        )
                    )
                )
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tile.iconEmoji,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tile.titleAr,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tile.titleEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(TikTokPink)
                )
            }
        }
    }
}

@Composable
fun RecordAudioDialog(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: (String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("صوت كبسوا يا شباب") }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = DarkSurface,
        title = {
            Text("تسجيل صوت مخصص للايف 🎙️", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "سجل بصوتك عبارة ترحيبية أو تشجيعية (مثل: 'كبسوا كبسوا!' أو 'شكراً على الدعم!') واستخدمها كزر مباشر في لوحة المؤثرات.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الزر / المؤثر") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TikTokPink,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Big Recording Mic Button
                val infinite = rememberInfiniteTransition(label = "rec")
                val recScale by infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "recScale"
                )

                IconButton(
                    onClick = {
                        if (isRecording) {
                            onStopRecording(title)
                        } else {
                            onStartRecording()
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isRecording) recScale else 1f)
                        .clip(CircleShape)
                        .background(if (isRecording) DangerRed else TikTokPink)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "تسجيل",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    if (isRecording) "جاري التسجيل... اضغط لإيقاف وحفظ الصوت" else "اضغط على الميكروفون لبدء التسجيل",
                    color = if (isRecording) DangerRed else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("إلغاء", color = TextSecondary)
            }
        }
    )
}
