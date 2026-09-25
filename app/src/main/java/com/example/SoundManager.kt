package com.example

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    var isSoundEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playDodgeSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 120
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 450.0 + progress * 400.0 // Pitch slides up
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val envelope = 1.0 - progress // Fade out
                    buffer[i] = (sin(angle) * envelope * 0.4 * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playCelebrationSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                // Sweet arpeggio: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
                val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
                val noteDurationMs = 140
                val samplesPerNote = (noteDurationMs * sampleRate) / 1000
                val totalSamples = samplesPerNote * notes.size + (sampleRate / 4) // extra ring out
                val buffer = ShortArray(totalSamples)

                notes.forEachIndexed { noteIndex, freq ->
                    val start = noteIndex * samplesPerNote
                    val end = (start + samplesPerNote * 2).coerceAtMost(totalSamples)
                    val noteLen = end - start
                    for (i in 0 until noteLen) {
                        val bufferIdx = start + i
                        val progress = i.toDouble() / noteLen
                        val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                        val overtone = 2.0 * Math.PI * i / (sampleRate / (freq * 2))
                        val envelope = (1.0 - progress) * (1.0 - progress)
                        val sampleVal = (sin(angle) * 0.7 + sin(overtone) * 0.3) * envelope * 0.45 * Short.MAX_VALUE
                        val current = buffer[bufferIdx].toInt()
                        buffer[bufferIdx] = (current + sampleVal.toInt()).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playHeartPopSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 80
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)
                val freq = 880.0

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val envelope = (1.0 - progress)
                    buffer[i] = (sin(angle) * envelope * 0.3 * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        // Release after finished
        scope.launch {
            kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 100)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
