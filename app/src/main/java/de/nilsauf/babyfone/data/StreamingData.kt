package de.nilsauf.babyfone.data

import android.media.AudioFormat

class StreamingData {
    companion object {
        const val frequency = 44100
        const val channelInConfiguration = AudioFormat.CHANNEL_IN_MONO
        const val channelOutConfiguration = AudioFormat.CHANNEL_OUT_MONO
        const val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        const val port = 10000
        const val serverIpAddress = "192.168.178.20"
    }
}