package com.aven.app.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * ==============================================================================
 * OPTIONAL FEATURE: Calm Harmonic Audio Synthesizer
 * ==============================================================================
 * WHAT THIS DOES:
 * Synthesizes serene, harmonic audio cues (432Hz / 528Hz singing bowl resonant chords)
 * dynamically in memory via Android's native AudioTrack API without bundling any
 * external audio files or sound assets.
 *
 * OPTIONAL STATUS & MUTED DEFAULT:
 * This audio synthesizer is strictly optional and MUTED BY DEFAULT across the app.
 * Aven's visual breath counter, tactile haptics, garden visualizer, and core
 * habit intervention mechanics provide complete feedback on their own.
 *
 * SAFETY GUARANTEE:
 * Audio playback is wrapped in thorough exception handling and hardware checks.
 * If AudioTrack fails to initialize (e.g. on emulators, devices with restricted
 * audio sinks, or background tasks), it fails silently without crashing.
 *
 * HOW TO REMOVE OR DISABLE:
 * To remove this feature and decrease APK footprint:
 * 1. Delete this file (CalmAudioSynthesizer.kt).
 * 2. Remove references from InterventionViewModel.kt and SettingsViewModel.kt.
 * ==============================================================================
 */
object CalmAudioSynthesizer {

    private val synthScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    /**
     * MUTED BY DEFAULT: Audio is completely disabled unless explicitly unmuted by the user.
     */
    @Volatile
    var isAudioMuted: Boolean = true

    /**
     * Resonant singing bowl chime played when the pause breathing countdown finishes.
     */
    fun playPauseCompleteChime(force: Boolean = false) {
        if (isAudioMuted && !force) return
        synthScope.launch {
            playHarmonicBell(
                frequencies = doubleArrayOf(528.0, 1056.0),
                weights = doubleArrayOf(0.7, 0.3),
                durationSeconds = 1.6,
                decayRate = 2.8
            )
        }
    }

    /**
     * Soft rising tone signaling the start of the pause or breathing expansion.
     */
    fun playBreatheCue(force: Boolean = false) {
        if (isAudioMuted && !force) return
        synthScope.launch {
            playHarmonicBell(
                frequencies = doubleArrayOf(432.0),
                weights = doubleArrayOf(1.0),
                durationSeconds = 1.0,
                decayRate = 2.0
            )
        }
    }

    /**
     * Uplifting harmonic chord played when an intentional decision nurtures the garden.
     */
    fun playWorldGrowthChime(force: Boolean = false) {
        if (isAudioMuted && !force) return
        synthScope.launch {
            playHarmonicBell(
                frequencies = doubleArrayOf(528.0, 660.0),
                weights = doubleArrayOf(0.6, 0.4),
                durationSeconds = 1.8,
                decayRate = 2.5
            )
        }
    }

    private fun playHarmonicBell(
        frequencies: DoubleArray,
        weights: DoubleArray,
        durationSeconds: Double,
        decayRate: Double
    ) {
        var audioTrack: AudioTrack? = null
        try {
            val numSamples = (durationSeconds * SAMPLE_RATE).toInt()
            if (numSamples <= 0) return

            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / SAMPLE_RATE
                // Exponential decay envelope with subtle smooth attack
                val attack = if (time < 0.05) time / 0.05 else 1.0
                val envelope = attack * exp(-decayRate * time)

                var sampleValue = 0.0
                for (f in frequencies.indices) {
                    val freq = frequencies[f]
                    val weight = weights[f]
                    sampleValue += sin(2.0 * PI * freq * time) * weight
                }

                val finalSample = (sampleValue * envelope * 0.4 * Short.MAX_VALUE).toInt()
                samples[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val bufferSize = numSamples * 2
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
                // Audio hardware sink unavailable; fail silently
                audioTrack.release()
                return
            }

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()

            // Wait for duration then cleanly stop and release
            Thread.sleep((durationSeconds * 1000).toLong() + 50)
            try {
                if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.stop()
                }
            } catch (ignored: Throwable) {
            }
        } catch (t: Throwable) {
            // Audio synthesis failures on restrictive devices/emulators fail silently and never crash
        } finally {
            try {
                audioTrack?.release()
            } catch (ignored: Throwable) {
            }
        }
    }
}
