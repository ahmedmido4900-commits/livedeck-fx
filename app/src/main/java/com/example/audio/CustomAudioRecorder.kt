package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class CustomAudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var isRecording = false
    private var recordStartTime = 0L

    fun isCurrentlyRecording(): Boolean = isRecording

    fun startRecording(fileNamePrefix: String): Boolean {
        if (isRecording) return false
        try {
            val outputDir = File(context.filesDir, "custom_sounds")
            if (!outputDir.exists()) outputDir.mkdirs()

            val file = File(outputDir, "${fileNamePrefix}_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = file

            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            rec.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recorder = rec
            isRecording = true
            recordStartTime = System.currentTimeMillis()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            return false
        }
    }

    fun stopRecording(): Pair<String, Long>? {
        if (!isRecording) return null
        val duration = System.currentTimeMillis() - recordStartTime
        val filePath = currentRecordingFile?.absolutePath
        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cleanup()
        }

        return if (filePath != null && File(filePath).exists() && duration > 300) {
            Pair(filePath, duration)
        } else {
            null
        }
    }

    fun cancelRecording() {
        try {
            recorder?.stop()
        } catch (_: Exception) {}
        cleanup()
        currentRecordingFile?.delete()
    }

    private fun cleanup() {
        try {
            recorder?.reset()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        isRecording = false
    }
}
