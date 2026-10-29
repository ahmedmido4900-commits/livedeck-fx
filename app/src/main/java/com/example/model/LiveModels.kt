package com.example.model

enum class SoundCategory(val titleAr: String) {
    ALL("الكل"),
    TIKTOK_GIFTS("هدايا التيك توك"),
    HYPE("حماس وتشجيع"),
    FUNNY("ضحك ومقالب"),
    PK_BATTLE("تحديات PK"),
    CUSTOM("أصواتي المسجلة")
}

enum class SoundEffectType {
    AIRHORN,
    APPLAUSE,
    DRUMROLL,
    LAUGH_TRACK,
    VICTORY_FANFARE,
    LION_ROAR,
    GALAXY_CHIME,
    ROSE_POP,
    CHA_CHING,
    BUZZER_WRONG,
    SUSPENSE_HEARTBEAT,
    WOW_SOUND,
    CRICKET_CHIRP,
    BOING_SPRING,
    SIREN_ALERT,
    LASER_BURST,
    MAGIC_SPARKLE,
    FIRE_BURST
}

enum class BackgroundMusicMode(val titleAr: String) {
    NONE("إيقاف"),
    HYPE_BEAT("إيقاع حماسي 128 BPM"),
    LOFI_CHILL("موسيقى هادئة Lo-Fi"),
    SUSPENSE_COUNTDOWN("نبض وترقب درامي")
}

data class SoundTile(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val type: SoundEffectType,
    val category: SoundCategory,
    val colorHex: Long,
    val iconEmoji: String,
    val isCustom: Boolean = false,
    val customAudioPath: String? = null
)

enum class GiftType(
    val titleAr: String,
    val coins: Int,
    val iconEmoji: String,
    val soundEffect: SoundEffectType,
    val visualEffect: VisualEffectType
) {
    LION("الأسد الأسطوري", 29999, "🦁", SoundEffectType.LION_ROAR, VisualEffectType.GOLD_STARS),
    GALAXY("المجرة الفضائية", 1000, "🌌", SoundEffectType.GALAXY_CHIME, VisualEffectType.NEON_PULSE),
    UNIVERSE("كون التيك توك", 34999, "👑", SoundEffectType.VICTORY_FANFARE, VisualEffectType.CONFETTI),
    ROSE("عاصفة الورود", 1, "🌹", SoundEffectType.ROSE_POP, VisualEffectType.HEART_RAIN),
    SUNGLASSES("النظارات الفخمة", 199, "🕶️", SoundEffectType.CHA_CHING, VisualEffectType.DISCO_STROBE),
    HEART_ME("قلوب الدعم (Heart Me)", 10, "💖", SoundEffectType.ROSE_POP, VisualEffectType.HEART_RAIN),
    CAP("كاب البث (TikTok Cap)", 99, "🧢", SoundEffectType.WOW_SOUND, VisualEffectType.CONFETTI),
    DRAGON("التنين الناري", 26999, "🐉", SoundEffectType.FIRE_BURST, VisualEffectType.FIRE_FLAMES)
}

enum class VisualEffectType(val titleAr: String, val iconEmoji: String) {
    DISCO_STROBE("ديسكو ملون", "✨"),
    CONFETTI("قصاصات واحتفال", "🎉"),
    GOLD_STARS("مطر ذهبي وبريق", "⭐"),
    HEART_RAIN("مطر القلوب للتكبيس", "💖"),
    FIRE_FLAMES("نيران التحدي", "🔥"),
    RED_ALERT("إنذار أحمر طوارئ", "🚨"),
    LASER_BEAMS("أشعة ليزر حماسية", "⚡"),
    NEON_PULSE("نبض نيون متوهج", "💫")
}

data class ActiveVisualEffect(
    val id: Long = System.currentTimeMillis(),
    val type: VisualEffectType,
    val durationMs: Long = 3000L,
    val messageAr: String? = null,
    val iconEmoji: String? = null
)

data class ActiveGiftAnimation(
    val id: Long = System.currentTimeMillis(),
    val giftType: GiftType,
    val senderName: String,
    val comboCount: Int = 1
)

enum class GoalType(val titleAr: String, val iconEmoji: String) {
    LIKES("التكبيس (Likes)", "❤️"),
    FOLLOWERS("متابعون جدد", "👥"),
    GIFTS("الماس والدعم", "💎")
}

data class LiveGoal(
    val id: String,
    val type: GoalType,
    val titleAr: String,
    val current: Int,
    val target: Int
)

data class PKBattleState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 300, // 5 min default
    val totalSeconds: Int = 300,
    val redScore: Int = 1250,
    val blueScore: Int = 980,
    val redName: String = "الفريق الأحمر",
    val blueName: String = "الفريق الأزرق",
    val activePenalty: String? = null
)

data class CustomVoiceClip(
    val id: String,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val createdAt: Long = System.currentTimeMillis()
)

enum class StreamDeckMode {
    DECK,           // Comprehensive Streamer Dashboard
    OVERLAY_STAGE,  // Fullscreen Stage / Green Screen Chroma Overlay
    QUICK_PAD       // Quick 1-Thumb Trigger Pad
}

enum class StageBackgroundStyle(val titleAr: String) {
    CHROMA_GREEN("كروما خضراء (#00FF00)"),
    DARK_STUDIO("استوديو مظلم شفاف"),
    NEON_GRADIENT("تدرج نيون سينمائي")
}
