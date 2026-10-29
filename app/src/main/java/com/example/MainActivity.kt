package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.StreamDeckMode
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.LiveDeckViewModel

enum class DeckTab(val titleAr: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SOUNDBOARD("صوتيات", Icons.Default.VolumeUp),
    GIFTS("الهدايا", Icons.Default.CardGiftcard),
    VISUALS("مرئيات", Icons.Default.AutoAwesome),
    PK_BATTLE("جولة PK", Icons.Default.SportsKabaddi),
    GOALS_TICKER("الأهداف", Icons.Default.Campaign)
}

class MainActivity : ComponentActivity() {

    private val viewModel: LiveDeckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                LiveDeckApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveDeckApp(viewModel: LiveDeckViewModel) {
    val context = LocalContext.current

    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val stageBackground by viewModel.stageBackground.collectAsStateWithLifecycle()
    val soundTiles by viewModel.soundTiles.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val currentlyPlayingId by viewModel.currentlyPlayingId.collectAsStateWithLifecycle()
    val masterVolume by viewModel.masterVolume.collectAsStateWithLifecycle()
    val backgroundMusic by viewModel.backgroundMusic.collectAsStateWithLifecycle()
    val activeVisualEffect by viewModel.activeVisualEffect.collectAsStateWithLifecycle()
    val activeGiftAnimation by viewModel.activeGiftAnimation.collectAsStateWithLifecycle()
    val pkState by viewModel.pkBattleState.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val tickerText by viewModel.tickerText.collectAsStateWithLifecycle()
    val isTickerActive by viewModel.isTickerActive.collectAsStateWithLifecycle()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsStateWithLifecycle()
    val activityFeed by viewModel.activityFeed.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(DeckTab.SOUNDBOARD) }

    // Permission launcher for custom voice shoutouts
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            Toast.makeText(context, "يلزم إذن الميكروفون لتسجيل الصوت المخصص", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle back button when in Stage Mode
    BackHandler(enabled = currentMode == StreamDeckMode.OVERLAY_STAGE) {
        viewModel.setMode(StreamDeckMode.DECK)
    }

    if (currentMode == StreamDeckMode.OVERLAY_STAGE) {
        // Fullscreen Overlay / Chroma Stage Mode
        OverlayStageView(
            activeEffect = activeVisualEffect,
            activeGift = activeGiftAnimation,
            pkState = pkState,
            tickerText = tickerText,
            isTickerActive = isTickerActive,
            stageBackground = stageBackground,
            onBackgroundChange = { viewModel.setStageBackground(it) },
            onExitStage = { viewModel.setMode(StreamDeckMode.DECK) }
        )
    } else {
        // Standard Streamer Control Deck
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "LiveDeck FX",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "مؤثرات وإضافات لايف التيك توك 🔴",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TikTokPink
                                )
                            }
                        }
                    },
                    actions = {
                        // Stage Mode Screen Launcher
                        IconButton(
                            onClick = { viewModel.setMode(StreamDeckMode.OVERLAY_STAGE) },
                            modifier = Modifier.testTag("top_open_stage_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "عرض الشاشة الكاملة / كروما",
                                tint = TikTokCyan
                            )
                        }

                        // Stop All Button
                        IconButton(
                            onClick = { viewModel.stopAll() },
                            modifier = Modifier.testTag("top_stop_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "إيقاف كل الأصوات والمؤثرات",
                                tint = DangerRed
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    DeckTab.values().forEach { tab ->
                        val isSelected = activeTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.titleAr,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.titleAr,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TikTokPink,
                                selectedTextColor = TikTokPink,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = TikTokPink.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content"
                ) { targetTab ->
                    when (targetTab) {
                        DeckTab.SOUNDBOARD -> {
                            SoundboardSection(
                                soundTiles = soundTiles,
                                selectedCategory = selectedCategory,
                                currentlyPlayingId = currentlyPlayingId,
                                masterVolume = masterVolume,
                                backgroundMusic = backgroundMusic,
                                isRecordingAudio = isRecordingAudio,
                                onCategorySelected = { viewModel.selectCategory(it) },
                                onSoundTriggered = { viewModel.triggerSound(it) },
                                onVolumeChanged = { viewModel.setVolume(it) },
                                onBackgroundMusicSelected = { viewModel.setBackgroundMusicMode(it) },
                                onStopAll = { viewModel.stopAll() },
                                onStartRecording = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startVoiceRecording()
                                    } else {
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onStopRecording = { title -> viewModel.stopVoiceRecording(title) },
                                onCancelRecording = { viewModel.cancelVoiceRecording() }
                            )
                        }
                        DeckTab.GIFTS -> {
                            GiftAlertsSection(
                                activeGift = activeGiftAnimation,
                                onTriggerGift = { gift, sender, combo ->
                                    viewModel.triggerGift(gift, sender, combo)
                                }
                            )
                        }
                        DeckTab.VISUALS -> {
                            VisualEffectsSection(
                                activeEffect = activeVisualEffect,
                                onTriggerEffect = { viewModel.triggerVisual(it) },
                                onOpenOverlayStage = { viewModel.setMode(StreamDeckMode.OVERLAY_STAGE) }
                            )
                        }
                        DeckTab.PK_BATTLE -> {
                            PKBattleSection(
                                state = pkState,
                                onStartPK = { viewModel.startPKBattle(it) },
                                onPauseResume = { viewModel.pauseResumePK() },
                                onResetPK = { viewModel.resetPK() },
                                onAdjustScore = { isRed, delta -> viewModel.adjustPKScore(isRed, delta) },
                                onSpinPenalty = { viewModel.spinRandomPenalty() }
                            )
                        }
                        DeckTab.GOALS_TICKER -> {
                            GoalsAndTickerSection(
                                goals = goals,
                                tickerText = tickerText,
                                isTickerActive = isTickerActive,
                                activityLog = activityFeed,
                                onIncrementGoal = { id, delta -> viewModel.incrementGoal(id, delta) },
                                onResetGoal = { id -> viewModel.resetGoal(id) },
                                onUpdateTicker = { viewModel.updateTicker(it) },
                                onToggleTicker = { viewModel.toggleTicker() }
                            )
                        }
                    }
                }
            }
        }
    }
}
