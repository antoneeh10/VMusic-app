/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SpatialAudioProcessorTest {
    @Test
    fun `spatial processor reuses its direct output buffer`() {
        val processor = SpatialAudioProcessor().apply {
            setEnabled(true)
            setStrength(50)
        }
        processor.configure(STEREO_16BIT_FORMAT)
        processor.flush()

        processor.queueInput(inputBuffer16Bit())
        val firstOutput = processor.output
        val firstBytes = ByteArray(firstOutput.remaining()).also(firstOutput::get)

        processor.queueInput(inputBuffer16Bit())
        val secondOutput = processor.output
        val secondBytes = ByteArray(secondOutput.remaining()).also(secondOutput::get)

        assertSame(firstOutput, secondOutput)
        assertArrayEquals(firstBytes, secondBytes)
    }

    @Test
    fun `mode OFF passes audio unchanged bit-exact`() {
        val processor = SpatialAudioProcessor().apply {
            setEnabled(false)
        }
        processor.configure(STEREO_16BIT_FORMAT)
        processor.flush()

        val input = inputBuffer16Bit()
        val inputBytes = ByteArray(input.remaining()).also { input.get(it); input.rewind() }

        processor.queueInput(input)
        val output = processor.output
        val outputBytes = ByteArray(output.remaining()).also(output::get)

        assertArrayEquals(inputBytes, outputBytes)
    }

    @Test
    fun `strength at zero passes audio unchanged bit-exact`() {
        val processor = SpatialAudioProcessor().apply {
            setEnabled(true)
            setStrength(0)
        }
        processor.configure(STEREO_16BIT_FORMAT)
        processor.flush()

        val input = inputBuffer16Bit()
        val inputBytes = ByteArray(input.remaining()).also { input.get(it); input.rewind() }

        processor.queueInput(input)
        val output = processor.output
        val outputBytes = ByteArray(output.remaining()).also(output::get)

        assertArrayEquals(inputBytes, outputBytes)
    }

    @Test
    fun `mono signal remains unchanged even when spatial mode is ON`() {
        val processor = SpatialAudioProcessor().apply {
            setEnabled(true)
            setStrength(75)
        }
        val monoFormat = AudioProcessor.AudioFormat(48_000, 1, C.ENCODING_PCM_16BIT)
        processor.configure(monoFormat)
        processor.flush()

        val input = ByteBuffer.allocateDirect(10).order(ByteOrder.nativeOrder()).apply {
            putShort(100)
            putShort(200)
            putShort(300)
            putShort(400)
            putShort(500)
            flip()
        }
        val inputBytes = ByteArray(input.remaining()).also { input.get(it); input.rewind() }

        processor.queueInput(input)
        val output = processor.output
        val outputBytes = ByteArray(output.remaining()).also(output::get)

        assertArrayEquals(inputBytes, outputBytes)
    }

    @Test
    fun `mode ON with stereo 16bit alters samples with channel difference and avoids overflow`() {
        val processor = SpatialAudioProcessor().apply {
            setEnabled(true)
            setStrength(50)
        }
        processor.configure(STEREO_16BIT_FORMAT)
        processor.flush()

        val input = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).apply {
            putShort(1000)
            putShort(-1000)
            putShort(Short.MAX_VALUE)
            putShort(Short.MIN_VALUE)
            flip()
        }

        processor.queueInput(input)
        processor.queueEndOfStream()
        val output = processor.output
        
        val outL1 = output.short
        val outR1 = output.short
        val outL2 = output.short
        val outR2 = output.short

        assertEquals(1399.toShort(), outL1)
        assertEquals((-1400).toShort(), outR1)
        assertEquals(Short.MAX_VALUE, outL2)
        assertEquals(Short.MIN_VALUE, outR2)
    }

    @Test
    fun `mode ON with float format processes correctly and clamps`() {
        val processor = SpatialAudioProcessor().apply {
            setEnabled(true)
            setStrength(50)
        }
        val floatFormat = AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_FLOAT)
        processor.configure(floatFormat)
        processor.flush()

        val input = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()).apply {
            putFloat(0.5f)
            putFloat(-0.5f)
            putFloat(2.0f) // should be clamped
            putFloat(-2.0f) // should be clamped
            flip()
        }

        processor.queueInput(input)
        processor.queueEndOfStream()
        val output = processor.output

        val outL1 = output.float
        val outR1 = output.float
        val outL2 = output.float
        val outR2 = output.float

        assertEquals(0.7f, outL1, 0.0001f)
        assertEquals(-0.7f, outR1, 0.0001f)
        assertEquals(1.0f, outL2, 0.0001f)
        assertEquals(-1.0f, outR2, 0.0001f)
    }

    private fun inputBuffer16Bit(): ByteBuffer =
        ByteBuffer.allocateDirect(BUFFER_SIZE)
            .order(ByteOrder.nativeOrder())
            .apply {
                repeat(BUFFER_SIZE / Short.SIZE_BYTES) { putShort(it.toShort()) }
                flip()
            }

    private companion object {
        const val BUFFER_SIZE = 1024
        val STEREO_16BIT_FORMAT = AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_16BIT)
    }
}
