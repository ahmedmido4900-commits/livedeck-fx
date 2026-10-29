package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import com.example.model.BackgroundMusicMode
import com.example.model.SoundEffectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class LiveSoundEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var volume: Float = 0.9f
    private val activeTracks = ConcurrentHashMap<Long, AudioTrack>()
    private var backgroundJob: Job? = null
    private var activeCustomPlayer: MediaPlayer? = null

    fun setMasterVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        activeTracks.values.forEach { track ->
            try {
                track.setVolume(volume)
            } catch (_: Exception) {}
        }
        activeCustomPlayer?.setVolume(volume, volume)
    }

    fun getMasterVolume(): Float = volume

    fun stopAll() {
        backgroundJob?.cancel()
        backgroundJob = null

        activeTracks.values.forEach { track ->
            try {
                track.pause()
                track.flush()
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
        activeTracks.clear()

        try {
            activeCustomPlayer?.stop()
            activeCustomPlayer?.release()
            activeCustomPlayer = null
        } catch (_: Exception) {}
    }

    fun playSoundEffect(type: SoundEffectType, onFinished: (() -> Unit)? = null) {
        scope.launch {
            val samples = generateSamplesForType(type)
            playPcmAudio(samples, onFinished)
        }
    }

    fun playCustomAudio(filePath: String, onFinished: (() -> Unit)? = null) {
        scope.launch(Dispatchers.Main) {
            try {
                activeCustomPlayer?.stop()
                activeCustomPlayer?.release()
                activeCustomPlayer = null

                val file = File(filePath)
                if (!file.exists()) return@launch

                val player = MediaPlayer.create(context, Uri.fromFile(file)) ?: return@launch
                player.setVolume(volume, volume)
                activeCustomPlayer = player
                player.setOnCompletionListener {
                    onFinished?.invoke()
                    player.release()
                    if (activeCustomPlayer == player) {
                        activeCustomPlayer = null
                    }
                }
                player.start()
            } catch (e: Exception) {
                onFinished?.invoke()
            }
        }
    }

    fun setBackgroundMusic(mode: BackgroundMusicMode) {
        backgroundJob?.cancel()
        backgroundJob = null

        if (mode == BackgroundMusicMode.NONE) return

        backgroundJob = scope.launch {
            while (isActive) {
                val loopSamples = generateBackgroundLoop(mode)
                val track = createTrack(loopSamples.size)
                val trackId = System.nanoTime()
                activeTracks[trackId] = track

                try {
                    track.play()
                    track.write(loopSamples, 0, loopSamples.size)
                    while (track.playState == AudioTrack.PLAYSTATE_PLAYING && isActive) {
                        delay(50)
                    }
                } catch (_: Exception) {
                } finally {
                    try {
                        track.stop()
                        track.release()
                    } catch (_: Exception) {}
                    activeTracks.remove(trackId)
                }
            }
        }
    }

    private fun playPcmAudio(samples: ShortArray, onFinished: (() -> Unit)?) {
        val track = createTrack(samples.size)
        val trackId = System.nanoTime()
        activeTracks[trackId] = track

        try {
            track.play()
            track.write(samples, 0, samples.size)
            // Wait until played
            val durationMs = (samples.size * 1000L) / SAMPLE_RATE
            Thread.sleep(durationMs.coerceAtLeast(100L))
        } catch (_: Exception) {
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
            activeTracks.remove(trackId)
            onFinished?.invoke()
        }
    }

    private fun createTrack(bufferSizeSamples: Int): AudioTrack {
        val sampleRate = SAMPLE_RATE
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = (bufferSizeSamples * 2).coerceAtLeast(minBufferSize)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        track.setVolume(volume)
        return track
    }

    private fun generateSamplesForType(type: SoundEffectType): ShortArray {
        return when (type) {
            SoundEffectType.AIRHORN -> generateAirhorn()
            SoundEffectType.APPLAUSE -> generateApplause()
            SoundEffectType.DRUMROLL -> generateDrumroll()
            SoundEffectType.LAUGH_TRACK -> generateLaughTrack()
            SoundEffectType.VICTORY_FANFARE -> generateVictoryFanfare()
            SoundEffectType.LION_ROAR -> generateLionRoar()
            SoundEffectType.GALAXY_CHIME -> generateGalaxyChime()
            SoundEffectType.ROSE_POP -> generateRosePop()
            SoundEffectType.CHA_CHING -> generateChaChing()
            SoundEffectType.BUZZER_WRONG -> generateWrongBuzzer()
            SoundEffectType.SUSPENSE_HEARTBEAT -> generateHeartbeat()
            SoundEffectType.WOW_SOUND -> generateWowSound()
            SoundEffectType.CRICKET_CHIRP -> generateCricketChirps()
            SoundEffectType.BOING_SPRING -> generateBoingSpring()
            SoundEffectType.SIREN_ALERT -> generateSiren()
            SoundEffectType.LASER_BURST -> generateLaserBurst()
            SoundEffectType.MAGIC_SPARKLE -> generateMagicSparkle()
            SoundEffectType.FIRE_BURST -> generateFireBurst()
        }
    }

    // --- Procedural Synthesis Recipes ---

    // DJ Airhorn: 3 rapid bursts (short, short, long triumphant blast with horn chord)
    private fun generateAirhorn(): ShortArray {
        val bursts = listOf(
            0.12f to 0.04f,
            0.12f to 0.04f,
            0.40f to 0.05f
        )
        val totalDuration = bursts.sumOf { (it.first + it.second).toDouble() }.toFloat()
        val totalSamples = (SAMPLE_RATE * totalDuration).toInt()
        val result = ShortArray(totalSamples)

        var offset = 0
        val hornFreqs = doubleArrayOf(466.16, 622.25, 783.99, 932.33) // Bb chord

        for ((burstDuration, silenceDuration) in bursts) {
            val burstSamples = (SAMPLE_RATE * burstDuration).toInt()
            for (i in 0 until burstSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val attack = (i.toDouble() / (SAMPLE_RATE * 0.02)).coerceAtMost(1.0)
                val release = ((burstSamples - i).toDouble() / (SAMPLE_RATE * 0.03)).coerceIn(0.0, 1.0)
                val env = attack * release

                // Add slight vibrato & pitch bend at start
                val pitchBend = 1.0 + 0.05 * exp(-t * 20.0)
                var sample = 0.0
                for (f in hornFreqs) {
                    val phase = 2.0 * PI * (f * pitchBend) * t
                    // Rich brass sawtooth approximation
                    val saw = (2.0 * ((phase / (2.0 * PI)) % 1.0) - 1.0)
                    sample += saw * 0.25
                }
                val sampleShort = (sample * env * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
                if (offset + i < totalSamples) {
                    result[offset + i] = sampleShort
                }
            }
            offset += burstSamples + (SAMPLE_RATE * silenceDuration).toInt()
        }
        return result
    }

    // Applause: Filtered noise bursts simulating hands clapping and crowd cheering
    private fun generateApplause(): ShortArray {
        val duration = 2.2f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)
        val rand = Random(42)

        var lastNoise = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = when {
                t < 0.3 -> t / 0.3
                t > 1.7 -> (2.2 - t) / 0.5
                else -> 1.0
            }

            // Crowd rumble noise
            val white = rand.nextDouble(-1.0, 1.0)
            // Low-pass filter for roar
            lastNoise = lastNoise * 0.75 + white * 0.25

            // Random individual sharp claps
            val clapImpulse = if (rand.nextInt(100) < 5) rand.nextDouble(-1.0, 1.0) * 1.8 else 0.0

            val sample = (lastNoise * 0.6 + clapImpulse * 0.4) * env
            result[i] = (sample * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Drumroll with cymbal crash
    private fun generateDrumroll(): ShortArray {
        val duration = 2.0f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)
        val rand = Random(1337)

        val snareEndTime = 1.4
        val crashStartTime = 1.4

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            if (t < snareEndTime) {
                // Accelerating snare drum roll
                val rollRate = 12.0 + (t / snareEndTime) * 28.0 // 12 to 40 hits/sec
                val hitPhase = (t * rollRate) % 1.0
                val hitEnv = exp(-hitPhase * 12.0)
                val noise = rand.nextDouble(-1.0, 1.0)
                val drumTone = sin(2.0 * PI * 180.0 * t)
                val snare = (noise * 0.7 + drumTone * 0.3) * hitEnv * (0.3 + 0.7 * (t / snareEndTime))
                sample += snare
            }

            if (t >= crashStartTime) {
                // Crash cymbal
                val crashT = t - crashStartTime
                val crashEnv = exp(-crashT * 3.5)
                val cymbalNoise = rand.nextDouble(-1.0, 1.0) * crashEnv
                sample += cymbalNoise * 0.9
            }

            result[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Laugh track: Multi-tone vocal "Ha-ha-ha"
    private fun generateLaughTrack(): ShortArray {
        val duration = 1.8f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        val chuckles = 6
        val chuckleDuration = duration / chuckles

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val chuckleIdx = (t / chuckleDuration).toInt().coerceIn(0, chuckles - 1)
            val localT = t % chuckleDuration

            // Pitch swoops down in each chuckle (420Hz down to 260Hz)
            val freq = 420.0 - 150.0 * (localT / chuckleDuration)
            val env = sin((localT / chuckleDuration) * PI).coerceAtLeast(0.0)

            // Voice harmonics
            val s1 = sin(2.0 * PI * freq * t)
            val s2 = sin(4.0 * PI * freq * t) * 0.5
            val s3 = sin(6.0 * PI * freq * t) * 0.25

            val sample = (s1 + s2 + s3) * env * (0.8 + 0.2 * sin(t * 15.0))
            result[i] = (sample * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Victory Fanfare: Brass celebratory chords
    private fun generateVictoryFanfare(): ShortArray {
        val duration = 2.0f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        // Notes: C4, E4, G4, C5 (held)
        val noteSeq = listOf(
            0.0 to (0.2 to 261.63), // C4
            0.2 to (0.2 to 329.63), // E4
            0.4 to (0.2 to 392.00), // G4
            0.6 to (1.4 to 523.25)  // C5 long triumph
        )

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            for ((start, note) in noteSeq) {
                val (dur, freq) = note
                if (t >= start && t < start + dur) {
                    val localT = t - start
                    val env = (localT / 0.04).coerceAtMost(1.0) * exp(-localT * 0.9)
                    val s1 = sin(2.0 * PI * freq * localT)
                    val s2 = sin(4.0 * PI * freq * localT) * 0.5
                    val s3 = sin(6.0 * PI * freq * localT) * 0.3
                    sample += (s1 + s2 + s3) * env
                }
            }

            result[i] = (sample * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Lion Roar: Sub-bass resonance + modulated growl for TikTok Lion gift
    private fun generateLionRoar(): ShortArray {
        val duration = 2.5f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)
        val rand = Random(999)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = when {
                t < 0.4 -> t / 0.4
                t > 1.8 -> (2.5 - t) / 0.7
                else -> 1.0
            }

            // Growl frequency modulates between 70Hz and 130Hz
            val growlFreq = 85.0 + 35.0 * sin(2.0 * PI * 8.0 * t)
            val sub = sin(2.0 * PI * growlFreq * t)
            val throatNoise = rand.nextDouble(-1.0, 1.0) * sin(2.0 * PI * 35.0 * t)
            val shimmer = sin(2.0 * PI * 1800.0 * t) * 0.15 * (t / duration)

            val raw = (sub * 0.65 + throatNoise * 0.4 + shimmer) * env
            // Soft clipping for punchy presence
            val clipped = raw.coerceIn(-1.0, 1.0)
            result[i] = (clipped * 28000.0).toInt().toShort()
        }
        return result
    }

    // Galaxy Chime: Ascending cosmic pentatonic bells
    private fun generateGalaxyChime(): ShortArray {
        val duration = 1.6f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        val freqs = doubleArrayOf(659.25, 783.99, 987.77, 1174.66, 1318.51, 1567.98, 1975.53, 2349.32)
        val noteInterval = duration / (freqs.size + 1)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            for (idx in freqs.indices) {
                val start = idx * noteInterval
                if (t >= start) {
                    val localT = t - start
                    val env = exp(-localT * 4.0)
                    val freq = freqs[idx]
                    val bell = sin(2.0 * PI * freq * localT) + 0.3 * sin(2.0 * PI * freq * 2.76 * localT)
                    sample += bell * env * 0.35
                }
            }
            result[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Rose Pop: Hollow bubble pop + harp chime
    private fun generateRosePop(): ShortArray {
        val duration = 0.7f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            // 1. Initial hollow pop (pitch drops rapidly)
            if (t < 0.12) {
                val popFreq = 950.0 - (t / 0.12) * 650.0
                val popEnv = exp(-t * 28.0)
                sample += sin(2.0 * PI * popFreq * t) * popEnv * 0.8
            }

            // 2. Sweet chime follow-up
            if (t >= 0.05) {
                val chimeT = t - 0.05
                val chimeEnv = exp(-chimeT * 6.0)
                val harpTone = sin(2.0 * PI * 1046.50 * chimeT) + 0.4 * sin(2.0 * PI * 1567.98 * chimeT)
                sample += harpTone * chimeEnv * 0.45
            }

            result[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Cash register cha-ching
    private fun generateChaChing(): ShortArray {
        val duration = 1.2f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)
        val rand = Random(777)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            // Lever/strike
            if (t < 0.08) {
                val strike = rand.nextDouble(-1.0, 1.0) * exp(-t * 40.0)
                sample += strike * 0.7
            }

            // High bell ding (2400Hz)
            if (t >= 0.05) {
                val bellT = t - 0.05
                val bellEnv = exp(-bellT * 4.5)
                sample += sin(2.0 * PI * 2489.0 * bellT) * bellEnv * 0.6
                sample += sin(2.0 * PI * 4978.0 * bellT) * bellEnv * 0.25
            }

            // Coin jingles
            val coinTimes = doubleArrayOf(0.18, 0.28, 0.38, 0.48)
            for (coinT in coinTimes) {
                if (t >= coinT) {
                    val localT = t - coinT
                    val coinEnv = exp(-localT * 18.0)
                    val coinFreq = 3200.0 + (coinT * 500.0)
                    sample += sin(2.0 * PI * coinFreq * localT) * coinEnv * 0.25
                }
            }

            result[i] = (sample * 25000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Wrong Buzzer: Dissonant low frequency double-buzz
    private fun generateWrongBuzzer(): ShortArray {
        val duration = 0.8f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        val burstDuration = 0.32
        val gap = 0.1

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val inBurst1 = t < burstDuration
            val inBurst2 = t >= (burstDuration + gap) && t < (2 * burstDuration + gap)

            if (inBurst1 || inBurst2) {
                val phase1 = (t * 140.0) % 1.0
                val phase2 = (t * 149.0) % 1.0
                val saw1 = 2.0 * phase1 - 1.0
                val saw2 = 2.0 * phase2 - 1.0
                val sample = (saw1 + saw2) * 0.5
                result[i] = (sample * 26000.0).toInt().toShort()
            } else {
                result[i] = 0
            }
        }
        return result
    }

    // Heartbeat: Lub-Dub cardiac pulse
    private fun generateHeartbeat(): ShortArray {
        val duration = 1.1f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        val hits = listOf(0.0 to 0.15, 0.22 to 0.18)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            for ((start, dur) in hits) {
                if (t >= start && t < start + dur) {
                    val localT = t - start
                    val freq = 55.0 - (localT / dur) * 15.0
                    val env = sin((localT / dur) * PI)
                    sample += sin(2.0 * PI * freq * localT) * env * 0.95
                }
            }
            result[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Wow Sound: Whistle pitch drop then swoop up
    private fun generateWowSound(): ShortArray {
        val duration = 0.9f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = when {
                t < 0.3 -> 300.0 + (t / 0.3) * 500.0
                else -> 800.0 + sin((t - 0.3) * 12.0) * 80.0
            }
            val env = sin((t / duration) * PI)
            val sample = sin(2.0 * PI * freq * t) * env
            result[i] = (sample * 24000.0).toInt().toShort()
        }
        return result
    }

    // Cricket Chirps: Awkward silence chirps
    private fun generateCricketChirps(): ShortArray {
        val duration = 1.4f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        val chirps = listOf(0.1, 0.18, 0.26, 0.7, 0.78, 0.86)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            for (start in chirps) {
                if (t >= start && t < start + 0.05) {
                    val localT = t - start
                    val env = sin((localT / 0.05) * PI)
                    sample += sin(2.0 * PI * 4600.0 * localT) * env * 0.7
                }
            }
            result[i] = (sample * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Boing Spring cartoon sound
    private fun generateBoingSpring(): ShortArray {
        val duration = 0.8f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 4.0)
            val mod = sin(2.0 * PI * 35.0 * t)
            val freq = 220.0 + (t / duration) * 450.0 + mod * 40.0
            val sample = sin(2.0 * PI * freq * t) * env
            result[i] = (sample * 25000.0).toInt().toShort()
        }
        return result
    }

    // Siren Alert: Wailing emergency siren
    private fun generateSiren(): ShortArray {
        val duration = 1.5f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // 600Hz to 1100Hz modulation at 2 Hz
            val freq = 850.0 + 250.0 * sin(2.0 * PI * 2.5 * t)
            val sample = sin(2.0 * PI * freq * t)
            result[i] = (sample * 24000.0).toInt().toShort()
        }
        return result
    }

    // Laser Burst: Fast downward chirp
    private fun generateLaserBurst(): ShortArray {
        val duration = 0.35f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 9.0)
            val freq = 2800.0 * exp(-t * 12.0) + 180.0
            val sample = sin(2.0 * PI * freq * t) * env
            result[i] = (sample * 26000.0).toInt().toShort()
        }
        return result
    }

    // Magic Sparkle: Twinkling glockenspiel
    private fun generateMagicSparkle(): ShortArray {
        val duration = 1.2f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)
        val rand = Random(555)

        val notes = doubleArrayOf(1567.98, 1760.00, 1975.53, 2349.32, 2637.02, 3135.96)
        val noteCount = 14

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            for (n in 0 until noteCount) {
                val start = n * (duration / noteCount)
                if (t >= start && t < start + 0.15) {
                    val localT = t - start
                    val env = exp(-localT * 22.0)
                    val freq = notes[(n * 3) % notes.size]
                    sample += sin(2.0 * PI * freq * localT) * env * 0.35
                }
            }
            result[i] = (sample * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Fire Burst: Explosive low rumble & crackle
    private fun generateFireBurst(): ShortArray {
        val duration = 1.3f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)
        val rand = Random(888)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 2.8)
            val rumble = sin(2.0 * PI * 65.0 * t) * 0.6
            val crackle = if (rand.nextInt(100) < 12) rand.nextDouble(-1.0, 1.0) * 0.8 else 0.0
            val sample = (rumble + crackle) * env
            result[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Background Beats Loops (continuous loops)
    private fun generateBackgroundLoop(mode: BackgroundMusicMode): ShortArray {
        return when (mode) {
            BackgroundMusicMode.HYPE_BEAT -> generateHypeBeatLoop()
            BackgroundMusicMode.LOFI_CHILL -> generateLofiChillLoop()
            BackgroundMusicMode.SUSPENSE_COUNTDOWN -> generateSuspenseLoop()
            BackgroundMusicMode.NONE -> ShortArray(0)
        }
    }

    // 128 BPM 4-bar kick & synth groove
    private fun generateHypeBeatLoop(): ShortArray {
        val bpm = 128.0
        val beatDuration = 60.0 / bpm
        val loopBeats = 8
        val duration = (beatDuration * loopBeats).toFloat()
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatTime = t % beatDuration
            var sample = 0.0

            // Punchy electronic kick on each beat
            if (beatTime < 0.18) {
                val kickFreq = 140.0 * exp(-beatTime * 30.0) + 45.0
                val kickEnv = exp(-beatTime * 18.0)
                sample += sin(2.0 * PI * kickFreq * beatTime) * kickEnv * 0.8
            }

            // Synth bassline (A1 to D2)
            val barIdx = (t / (beatDuration * 2)).toInt() % 4
            val bassFreq = when (barIdx) {
                0 -> 110.0 // A2
                1 -> 130.81 // C3
                2 -> 146.83 // D3
                else -> 98.0 // G2
            }
            val bassSaw = 2.0 * ((t * bassFreq) % 1.0) - 1.0
            sample += bassSaw * 0.25

            result[i] = (sample * 20000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Mellow warm Lo-Fi chords
    private fun generateLofiChillLoop(): ShortArray {
        val duration = 4.0f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        val chords = listOf(
            doubleArrayOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
            doubleArrayOf(220.00, 261.63, 329.63, 392.00), // Am7
            doubleArrayOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
            doubleArrayOf(196.00, 246.94, 293.66, 349.23)  // G7
        )

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val chordIdx = (t / 1.0).toInt().coerceIn(0, 3)
            val chord = chords[chordIdx]
            val localT = t % 1.0
            val env = exp(-localT * 1.5)

            var sample = 0.0
            for (f in chord) {
                sample += (sin(2.0 * PI * f * localT) + 0.3 * sin(4.0 * PI * f * localT)) * 0.2
            }
            result[i] = (sample * env * 18000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    // Dramatic Suspense Heartbeat & clock tick loop
    private fun generateSuspenseLoop(): ShortArray {
        val duration = 2.0f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val result = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            // Dark drone
            sample += sin(2.0 * PI * 55.0 * t) * 0.4
            sample += sin(2.0 * PI * 58.0 * t) * 0.3 // Beating dissonance

            // Clock tick every 0.5s
            val tickTime = t % 0.5
            if (tickTime < 0.03) {
                val tickEnv = exp(-tickTime * 120.0)
                sample += sin(2.0 * PI * 1800.0 * tickTime) * tickEnv * 0.5
            }

            result[i] = (sample * 20000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return result
    }

    companion object {
        const val SAMPLE_RATE = 44100
    }
}
