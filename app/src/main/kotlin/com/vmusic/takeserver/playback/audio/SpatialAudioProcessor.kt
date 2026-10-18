/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SpatialAudioProcessor : BaseAudioProcessor() {
    @Volatile
    private var spatialEnabled = false

    @Volatile
    private var strength: Int = 50

    private var encoding = C.ENCODING_INVALID
    private var channelCount = 0

    fun setEnabled(enabled: Boolean) {
        spatialEnabled = enabled
    }

    fun setStrength(strengthVal: Int) {
        strength = strengthVal.coerceIn(0, 100)
    }

    fun getStrength(): Int = strength

    fun isEnabled(): Boolean = spatialEnabled

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        encoding = inputAudioFormat.encoding
        channelCount = inputAudioFormat.channelCount
        if (encoding == C.ENCODING_PCM_16BIT || encoding == C.ENCODING_PCM_FLOAT) {
            return inputAudioFormat
        }
        throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val output = replaceOutputBuffer(remaining)
        inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
        output.order(ByteOrder.LITTLE_ENDIAN)

        // Pass-through bit-exact if disabled, not stereo, or strength is 0%
        if (!spatialEnabled || channelCount != 2 || strength <= 0) {
            output.put(inputBuffer)
            output.flip()
            return
        }

        // Map strength (0..100) to widthFactor (1.0..1.8)
        // At 0% -> widthFactor = 1.0 (bit-exact pass-through)
        // At 50% -> widthFactor = 1.4
        // At 100% -> widthFactor = 1.8
        val widthFactor = 1.0f + (strength / 100.0f) * 0.8f

        if (encoding == C.ENCODING_PCM_16BIT) {
            val sampleCount = remaining / 2
            val frameCount = sampleCount / 2

            repeat(frameCount) {
                val sampleL = inputBuffer.getShort()
                val sampleR = inputBuffer.getShort()

                val left = sampleL / 32768.0f
                val right = sampleR / 32768.0f

                val mid = (left + right) * 0.5f
                val side = (left - right) * 0.5f
                val sideW = side * widthFactor

                val newLeft = mid + sideW
                val newRight = mid - sideW

                val outL = (newLeft.coerceIn(-1.0f, 1.0f) * if (newLeft < 0f) 32768.0f else 32767.0f)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
                val outR = (newRight.coerceIn(-1.0f, 1.0f) * if (newRight < 0f) 32768.0f else 32767.0f)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()

                output.putShort(outL)
                output.putShort(outR)
            }
            if (inputBuffer.hasRemaining()) {
                output.put(inputBuffer)
            }
        } else if (encoding == C.ENCODING_PCM_FLOAT) {
            val sampleCount = remaining / 4
            val frameCount = sampleCount / 2

            repeat(frameCount) {
                val left = inputBuffer.getFloat()
                val right = inputBuffer.getFloat()

                val mid = (left + right) * 0.5f
                val side = (left - right) * 0.5f
                val sideW = side * widthFactor

                val newLeft = (mid + sideW).coerceIn(-1.0f, 1.0f)
                val newRight = (mid - sideW).coerceIn(-1.0f, 1.0f)

                output.putFloat(newLeft)
                output.putFloat(newRight)
            }
            if (inputBuffer.hasRemaining()) {
                output.put(inputBuffer)
            }
        } else {
            output.put(inputBuffer)
        }

        output.flip()
    }

    override fun onReset() {
        encoding = C.ENCODING_INVALID
        channelCount = 0
    }
}
