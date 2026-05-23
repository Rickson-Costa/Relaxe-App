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
    private val trackLock = Any()
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile var targetVolume: Float = 0.0f
    @Volatile var targetFrequency: Float = 140.0f

    private var currentVolume = 0.0f
    private var currentFrequency = 140.0f

    fun start() {
        synchronized(trackLock) {
            if (audioTrack != null) return
            
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                val track = AudioTrack.Builder()
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

                track.play()
                audioTrack = track
            } catch (e: Throwable) {
                e.printStackTrace()
                return
            }
        }

        job = scope.launch {
            val shortBuffer = ShortArray(1024)
            var phase = 0.0
            val random = java.util.Random()
            var lastNoiseOut = 0.0f
            val sampleRate = 44100
            
            while (isActive) {
                // Smoothly slide current values to target values to prevent clicks/snaps
                currentVolume += (targetVolume - currentVolume) * 0.04f
                currentFrequency += (targetFrequency - currentFrequency) * 0.04f

                for (i in shortBuffer.indices) {
                    phase += 2.0 * Math.PI * currentFrequency / sampleRate
                    if (phase > 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI
                    }
                    
                    // 1. Deep relaxing background wave (singing bowl / ocean undertone)
                    val primaryWave = sin(phase)
                    val secondaryWave = 0.3 * sin(phase * 1.5)
                    val toneSample = (primaryWave + secondaryWave) / 1.3
                    
                    // 2. Earthy rain rustle (low-passed white noise)
                    val whiteNoise = random.nextFloat() * 2f - 1f
                    lastNoiseOut = lastNoiseOut + 0.12f * (whiteNoise - lastNoiseOut)
                    
                    // 3. Blend: 40% tone guidance, 60% gentle rain cascade
                    val mixedSample = (0.4f * toneSample) + (0.6f * lastNoiseOut)
                    
                    val shortVal = (mixedSample * currentVolume * 32767.0).toInt()
                    shortBuffer[i] = shortVal.coerceIn(-32767, 32767).toShort()
                }

                var trackBytesWritten = 0
                synchronized(trackLock) {
                    val track = audioTrack
                    if (track != null && isActive && track.state == AudioTrack.STATE_INITIALIZED) {
                        try {
                            trackBytesWritten = track.write(shortBuffer, 0, shortBuffer.size)
                        } catch (e: Throwable) {
                            trackBytesWritten = -1
                        }
                    } else {
                        trackBytesWritten = -1
                    }
                }
                
                if (trackBytesWritten < 0) {
                    break
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        
        synchronized(trackLock) {
            try {
                audioTrack?.let { track ->
                    if (track.state == AudioTrack.STATE_INITIALIZED) {
                        try {
                            track.stop()
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                    track.release()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            audioTrack = null
        }
        currentVolume = 0.0f
    }

    fun setParams(volume: Float, frequency: Float) {
        targetVolume = volume.coerceIn(0.0f, 0.4f) // Cap volume at 0.4 to keep it soothing and safe
        targetFrequency = frequency.coerceIn(80.0f, 350.0f)
    }
}
