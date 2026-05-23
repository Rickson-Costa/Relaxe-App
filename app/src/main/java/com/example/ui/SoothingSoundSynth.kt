package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoothingSoundSynth {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile var targetVolume: Float = 0.0f
    @Volatile var targetFrequency: Float = 140.0f

    private var currentVolume = 0.0f
    private var currentFrequency = 140.0f

    fun start() {
        if (job?.isActive == true) return

        job = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).let { if (it < 2048) 2048 else it }

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Throwable) {
                e.printStackTrace()
                return@launch
            }

            try {
                track.play()
            } catch (e: Throwable) {
                e.printStackTrace()
                track.release()
                return@launch
            }

            val shortBuffer = ShortArray(1024)
            var phase = 0.0
            val random = java.util.Random()
            var lastNoiseOut = 0.0f

            try {
                while (isActive) {
                    currentVolume += (targetVolume - currentVolume) * 0.04f
                    currentFrequency += (targetFrequency - currentFrequency) * 0.04f

                    for (i in shortBuffer.indices) {
                        phase += 2.0 * Math.PI * currentFrequency / sampleRate
                        if (phase > 2.0 * Math.PI) {
                            phase -= 2.0 * Math.PI
                        }
                        
                        val primaryWave = sin(phase)
                        val secondaryWave = 0.3 * sin(phase * 1.5)
                        val toneSample = (primaryWave + secondaryWave) / 1.3
                        
                        val whiteNoise = random.nextFloat() * 2f - 1f
                        lastNoiseOut = lastNoiseOut + 0.12f * (whiteNoise - lastNoiseOut)
                        
                        val mixedSample = (0.4f * toneSample) + (0.6f * lastNoiseOut)
                        
                        val shortVal = (mixedSample * currentVolume * 32767.0).toInt()
                        shortBuffer[i] = shortVal.coerceIn(-32767, 32767).toShort()
                    }

                    if (track.state == AudioTrack.STATE_INITIALIZED) {
                        val trackBytesWritten = try {
                            track.write(shortBuffer, 0, shortBuffer.size)
                        } catch (e: Throwable) {
                            -1
                        }

                        if (trackBytesWritten < 0) {
                            break
                        } else if (trackBytesWritten == 0) {
                            kotlinx.coroutines.delay(10)
                        }
                    } else {
                        break
                    }
                }
            } finally {
                try {
                    track.stop()
                } catch (e: Throwable) {}
                try {
                    track.release()
                } catch (e: Throwable) {}
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        currentVolume = 0.0f
    }

    fun setParams(volume: Float, frequency: Float) {
        targetVolume = volume.coerceIn(0.0f, 0.4f)
        targetFrequency = frequency.coerceIn(80.0f, 350.0f)
    }
}
