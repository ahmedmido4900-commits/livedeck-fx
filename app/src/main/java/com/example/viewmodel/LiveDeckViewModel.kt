package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CustomAudioRecorder
import com.example.audio.LiveSoundEngine
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LiveDeckViewModel(application: Application) : AndroidViewModel(application) {

    val soundEngine = LiveSoundEngine(application.applicationContext)
    private val audioRecorder = CustomAudioRecorder(application.applicationContext)

    private val _currentMode = MutableStateFlow(StreamDeckMode.DECK)
    val currentMode: StateFlow<StreamDeckMode> = _currentMode.asStateFlow()

    private val _stageBackground = MutableStateFlow(StageBackgroundStyle.DARK_STUDIO)
    val stageBackground: StateFlow<StageBackgroundStyle> = _stageBackground.asStateFlow()

    private val _selectedCategory = MutableStateFlow(SoundCategory.ALL)
    val selectedCategory: StateFlow<SoundCategory> = _selectedCategory.asStateFlow()

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.9f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _backgroundMusic = MutableStateFlow(BackgroundMusicMode.NONE)
    val backgroundMusic: StateFlow<BackgroundMusicMode> = _backgroundMusic.asStateFlow()

    private val _activeVisualEffect = MutableStateFlow<ActiveVisualEffect?>(null)
    val activeVisualEffect: StateFlow<ActiveVisualEffect?> = _activeVisualEffect.asStateFlow()

    private val _activeGiftAnimation = MutableStateFlow<ActiveGiftAnimation?>(null)
    val activeGiftAnimation: StateFlow<ActiveGiftAnimation?> = _activeGiftAnimation.asStateFlow()

    private val _pkBattleState = MutableStateFlow(PKBattleState())
    val pkBattleState: StateFlow<PKBattleState> = _pkBattleState.asStateFlow()

    private val _goals = MutableStateFlow<List<LiveGoal>>(createDefaultGoals())
    val goals: StateFlow<List<LiveGoal>> = _goals.asStateFlow()

    private val _tickerText = MutableStateFlow("🔥 أهلاً بكم في البث المباشر! كبسوا على الشاشة وشاركوا البث 🚀 هدفنا اليوم 100K لايك والوصول للتوب 1 👑")
    val tickerText: StateFlow<String> = _tickerText.asStateFlow()

    private val _isTickerActive = MutableStateFlow(true)
    val isTickerActive: StateFlow<Boolean> = _isTickerActive.asStateFlow()

    private val _soundTiles = MutableStateFlow<List<SoundTile>>(createDefaultSoundTiles())
    val soundTiles: StateFlow<List<SoundTile>> = _soundTiles.asStateFlow()

    private val _customClips = MutableStateFlow<List<CustomVoiceClip>>(emptyList())
    val customClips: StateFlow<List<CustomVoiceClip>> = _customClips.asStateFlow()

    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _activityFeed = MutableStateFlow<List<String>>(
        listOf("جاهز للبث المباشر 🔴", "اضغط على أي مؤثر صوتي لتشغيله فوراً")
    )
    val activityFeed: StateFlow<List<String>> = _activityFeed.asStateFlow()

    private val penaltyList = mutableListOf(
        "ارسم شوارب بلحية ومعجون أسنان على الوجه 🖌️",
        "10 ضغط سريع مع التكبيس الحماسي 💪",
        "تكلم باللغة العربية الفصحى طوال الجولة القادمة 📜",
        "تذوق شريحة ليمون كاملة بدون أي تعبير 🍋",
        "غني أغنية أطفال بنبرة روبوت حماسية 🤖",
        "اعترف بأغرب موقف محرج حصل لك في حياتك 🙈",
        "البس نظارة شمسية مقلوبة طوال البث 🕶️",
        "علق كمعلق رياضي حماسي لمدة دقيقة كاملة 🎙️",
        "تحدي الثبات كالصخرة بدون ابتسامة لمدة 60 ثانية 🗿",
        "حط ثلجة في يدك وانتظر حتى تذوب ❄️",
        "ارسل تحية لخمسة من أكبر الداعمين بأسلوب شعري 👑"
    )

    private var pkJob: Job? = null
    private var visualClearJob: Job? = null
    private var giftClearJob: Job? = null

    init {
        soundEngine.setMasterVolume(_masterVolume.value)
    }

    fun setMode(mode: StreamDeckMode) {
        _currentMode.value = mode
    }

    fun setStageBackground(style: StageBackgroundStyle) {
        _stageBackground.value = style
    }

    fun selectCategory(category: SoundCategory) {
        _selectedCategory.value = category
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _masterVolume.value = clamped
        soundEngine.setMasterVolume(clamped)
    }

    fun setBackgroundMusicMode(mode: BackgroundMusicMode) {
        _backgroundMusic.value = mode
        soundEngine.setBackgroundMusic(mode)
        logActivity("الموسيقى الخلفية: ${mode.titleAr}")
    }

    fun stopAll() {
        soundEngine.stopAll()
        _currentlyPlayingId.value = null
        _backgroundMusic.value = BackgroundMusicMode.NONE
        _activeVisualEffect.value = null
        _activeGiftAnimation.value = null
        logActivity("تم إيقاف جميع المؤثرات والموسيقى ⏹️")
    }

    fun triggerSound(tile: SoundTile) {
        _currentlyPlayingId.value = tile.id
        if (tile.isCustom && tile.customAudioPath != null) {
            soundEngine.playCustomAudio(tile.customAudioPath) {
                if (_currentlyPlayingId.value == tile.id) {
                    _currentlyPlayingId.value = null
                }
            }
        } else {
            soundEngine.playSoundEffect(tile.type) {
                if (_currentlyPlayingId.value == tile.id) {
                    _currentlyPlayingId.value = null
                }
            }
        }
        logActivity("تم تشغيل: ${tile.titleAr} ${tile.iconEmoji}")
    }

    fun triggerVisual(type: VisualEffectType, message: String? = null, emoji: String? = null) {
        visualClearJob?.cancel()
        val effect = ActiveVisualEffect(
            type = type,
            durationMs = 3500L,
            messageAr = message ?: type.titleAr,
            iconEmoji = emoji ?: type.iconEmoji
        )
        _activeVisualEffect.value = effect
        logActivity("مؤثر مرئي: ${effect.messageAr} ${effect.iconEmoji}")

        visualClearJob = viewModelScope.launch {
            delay(3500L)
            if (_activeVisualEffect.value?.id == effect.id) {
                _activeVisualEffect.value = null
            }
        }
    }

    fun triggerGift(gift: GiftType, senderName: String = "فاعل خير الداعم", combo: Int = 1) {
        // 1. Play dedicated sound
        soundEngine.playSoundEffect(gift.soundEffect)

        // 2. Trigger visual overlay
        triggerVisual(gift.visualEffect, "${gift.titleAr} من $senderName", gift.iconEmoji)

        // 3. Set dedicated Gift Animation Card
        giftClearJob?.cancel()
        val giftAnim = ActiveGiftAnimation(
            giftType = gift,
            senderName = senderName,
            comboCount = combo
        )
        _activeGiftAnimation.value = giftAnim

        // 4. Update gift goal
        incrementGoal("goal_gifts", gift.coins)

        logActivity("🎁 هدية $combo x ${gift.titleAr} (${gift.coins} عملة) من $senderName")

        giftClearJob = viewModelScope.launch {
            delay(4500L)
            if (_activeGiftAnimation.value?.id == giftAnim.id) {
                _activeGiftAnimation.value = null
            }
        }
    }

    // --- PK Battle Logic ---
    fun startPKBattle(seconds: Int = 300) {
        pkJob?.cancel()
        _pkBattleState.value = PKBattleState(
            isActive = true,
            isPaused = false,
            remainingSeconds = seconds,
            totalSeconds = seconds,
            redScore = 0,
            blueScore = 0,
            activePenalty = null
        )
        soundEngine.playSoundEffect(SoundEffectType.SIREN_ALERT)
        triggerVisual(VisualEffectType.RED_ALERT, "بدء جولة التحدي PK الحماسية!", "🔥")
        logActivity("بدأت جولة تحدي PK جديدة (${seconds / 60} دقائق) ⚔️")

        pkJob = viewModelScope.launch {
            while (_pkBattleState.value.remainingSeconds > 0) {
                delay(1000L)
                if (!_pkBattleState.value.isPaused) {
                    _pkBattleState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }

                    // Sound cues in last 5 seconds
                    if (_pkBattleState.value.remainingSeconds in 1..5) {
                        soundEngine.playSoundEffect(SoundEffectType.SUSPENSE_HEARTBEAT)
                    }
                }
            }
            // Finished!
            soundEngine.playSoundEffect(SoundEffectType.VICTORY_FANFARE)
            triggerVisual(VisualEffectType.CONFETTI, "انتهت الجولة! حان وقت تنفيذ الحكم 🏆", "👑")
            logActivity("انتهت الجولة! النتيجة: أحمر (${_pkBattleState.value.redScore}) مقابل أزرق (${_pkBattleState.value.blueScore})")
        }
    }

    fun pauseResumePK() {
        _pkBattleState.update { it.copy(isPaused = !it.isPaused) }
        logActivity(if (_pkBattleState.value.isPaused) "تم إيقاف جولة PK مؤقتاً ⏸️" else "تم استئناف جولة PK ▶️")
    }

    fun resetPK() {
        pkJob?.cancel()
        _pkBattleState.value = PKBattleState(isActive = false)
        logActivity("تم إنهاء وتصفير جولة PK 🔄")
    }

    fun adjustPKScore(isRed: Boolean, delta: Int) {
        _pkBattleState.update { current ->
            if (isRed) {
                current.copy(redScore = (current.redScore + delta).coerceAtLeast(0))
            } else {
                current.copy(blueScore = (current.blueScore + delta).coerceAtLeast(0))
            }
        }
        if (delta >= 500) {
            soundEngine.playSoundEffect(SoundEffectType.CHA_CHING)
        }
    }

    fun spinRandomPenalty() {
        val randomPenalty = penaltyList.random()
        _pkBattleState.update { it.copy(activePenalty = randomPenalty) }
        soundEngine.playSoundEffect(SoundEffectType.DRUMROLL) {
            soundEngine.playSoundEffect(SoundEffectType.LAUGH_TRACK)
        }
        triggerVisual(VisualEffectType.DISCO_STROBE, "الحكم العشوائي: $randomPenalty", "⚖️")
        logActivity("الحكم: $randomPenalty")
    }

    // --- Live Goals Logic ---
    fun incrementGoal(goalId: String, delta: Int) {
        _goals.update { list ->
            list.map { goal ->
                if (goal.id == goalId) {
                    val newCurrent = goal.current + delta
                    if (newCurrent >= goal.target && goal.current < goal.target) {
                        // Goal achieved fanfare!
                        soundEngine.playSoundEffect(SoundEffectType.VICTORY_FANFARE)
                        triggerVisual(VisualEffectType.GOLD_STARS, "تم تحقيق ${goal.titleAr}! كفووو 🥳", "🎉")
                    }
                    goal.copy(current = newCurrent)
                } else goal
            }
        }
    }

    fun resetGoal(goalId: String, newTarget: Int? = null) {
        _goals.update { list ->
            list.map { goal ->
                if (goal.id == goalId) {
                    goal.copy(current = 0, target = newTarget ?: goal.target)
                } else goal
            }
        }
    }

    // --- Ticker Logic ---
    fun updateTicker(text: String) {
        _tickerText.value = text
        logActivity("تم تحديث الشريط الإعلاني 📢")
    }

    fun toggleTicker() {
        _isTickerActive.value = !_isTickerActive.value
    }

    // --- Custom Audio Recording ---
    fun startVoiceRecording(): Boolean {
        val started = audioRecorder.startRecording("stream_shout")
        if (started) {
            _isRecordingAudio.value = true
            logActivity("جاري تسجيل صوت مخصص... تحدث الآن 🎙️")
        }
        return started
    }

    fun stopVoiceRecording(title: String) {
        val result = audioRecorder.stopRecording()
        _isRecordingAudio.value = false
        if (result != null) {
            val (path, dur) = result
            val clip = CustomVoiceClip(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "صوتي المخصص ${customClips.value.size + 1}" },
                filePath = path,
                durationMs = dur
            )
            _customClips.update { listOf(clip) + it }

            // Add tile to soundboard
            val newTile = SoundTile(
                id = clip.id,
                titleAr = clip.title,
                titleEn = "Custom Shout",
                type = SoundEffectType.WOW_SOUND,
                category = SoundCategory.CUSTOM,
                colorHex = 0xFFFF5722,
                iconEmoji = "🎙️",
                isCustom = true,
                customAudioPath = path
            )
            _soundTiles.update { listOf(newTile) + it }
            soundEngine.playSoundEffect(SoundEffectType.ROSE_POP)
            logActivity("تم حفظ المقطع الصوتي: ${clip.title} (${dur / 1000} ث)")
        }
    }

    fun cancelVoiceRecording() {
        audioRecorder.cancelRecording()
        _isRecordingAudio.value = false
    }

    fun deleteCustomClip(id: String) {
        val clip = _customClips.value.find { it.id == id }
        clip?.let {
            File(it.filePath).delete()
        }
        _customClips.update { it.filterNot { c -> c.id == id } }
        _soundTiles.update { it.filterNot { t -> t.id == id } }
        logActivity("تم حذف المقطع الصوتي 🗑️")
    }

    private fun logActivity(msg: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _activityFeed.update { listOf("[$timeStr] $msg") + it.take(15) }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stopAll()
        audioRecorder.cancelRecording()
    }

    private fun createDefaultGoals(): List<LiveGoal> {
        return listOf(
            LiveGoal("goal_likes", GoalType.LIKES, "هدف التكبيس (100K لايك)", 34200, 100000),
            LiveGoal("goal_followers", GoalType.FOLLOWERS, "متابعون جدد في اللايف", 145, 500),
            LiveGoal("goal_gifts", GoalType.GIFTS, "صندوق الدعم اليومي (الماس)", 8400, 25000)
        )
    }

    private fun createDefaultSoundTiles(): List<SoundTile> {
        return listOf(
            // TikTok Gifts & Special
            SoundTile("s_lion", "زئير الأسد", "Lion Roar", SoundEffectType.LION_ROAR, SoundCategory.TIKTOK_GIFTS, 0xFFFFD700, "🦁"),
            SoundTile("s_galaxy", "رنة المجرة", "Galaxy Chime", SoundEffectType.GALAXY_CHIME, SoundCategory.TIKTOK_GIFTS, 0xFF9B51E0, "🌌"),
            SoundTile("s_rose", "نغمة الوردة", "Rose Pop", SoundEffectType.ROSE_POP, SoundCategory.TIKTOK_GIFTS, 0xFFFE2C55, "🌹"),
            SoundTile("s_cash", "كاش ودعم", "Cha-Ching", SoundEffectType.CHA_CHING, SoundCategory.TIKTOK_GIFTS, 0xFF00E676, "💰"),

            // Hype & Cheering
            SoundTile("s_airhorn", "بوق الحماس", "Airhorn Blast", SoundEffectType.AIRHORN, SoundCategory.HYPE, 0xFFFF3366, "📣"),
            SoundTile("s_applause", "تصفيق الجمهور", "Crowd Applause", SoundEffectType.APPLAUSE, SoundCategory.HYPE, 0xFF25F4EE, "👏"),
            SoundTile("s_victory", "نغمة الفوز", "Victory Fanfare", SoundEffectType.VICTORY_FANFARE, SoundCategory.HYPE, 0xFFFFBE0B, "🏆"),
            SoundTile("s_sparkle", "بريق سحري", "Magic Sparkle", SoundEffectType.MAGIC_SPARKLE, SoundCategory.HYPE, 0xFF00E5FF, "✨"),

            // Funny & Comedy
            SoundTile("s_laugh", "ضحك مسرحي", "Laugh Track", SoundEffectType.LAUGH_TRACK, SoundCategory.FUNNY, 0xFFFF9100, "😂"),
            SoundTile("s_boing", "قفزة مضحكة", "Cartoon Boing", SoundEffectType.BOING_SPRING, SoundCategory.FUNNY, 0xFF76FF03, "🤹"),
            SoundTile("s_cricket", "صرصور الليل (إحراج)", "Crickets", SoundEffectType.CRICKET_CHIRP, SoundCategory.FUNNY, 0xFF9E9E9E, "🦗"),
            SoundTile("s_wow", "صوت وااو", "Wow", SoundEffectType.WOW_SOUND, SoundCategory.FUNNY, 0xFF00B0FF, "😲"),

            // PK Battles & Tension
            SoundTile("s_drumroll", "قرع طبول حاسم", "Drumroll", SoundEffectType.DRUMROLL, SoundCategory.PK_BATTLE, 0xFFE040FB, "🥁"),
            SoundTile("s_heartbeat", "نبضات قلب وترقب", "Heartbeat", SoundEffectType.SUSPENSE_HEARTBEAT, SoundCategory.PK_BATTLE, 0xFFFF1744, "💓"),
            SoundTile("s_siren", "إنذار طوارئ", "Emergency Siren", SoundEffectType.SIREN_ALERT, SoundCategory.PK_BATTLE, 0xFFFF5252, "🚨"),
            SoundTile("s_buzzer", "بوزر الخطأ", "Wrong Buzzer", SoundEffectType.BUZZER_WRONG, SoundCategory.PK_BATTLE, 0xFFD50000, "❌"),
            SoundTile("s_laser", "طلقة ليزر", "Laser Zap", SoundEffectType.LASER_BURST, SoundCategory.PK_BATTLE, 0xFF00E5FF, "⚡"),
            SoundTile("s_fire", "انفجار ناري", "Fire Flame", SoundEffectType.FIRE_BURST, SoundCategory.PK_BATTLE, 0xFFFF6D00, "🔥")
        )
    }
}
