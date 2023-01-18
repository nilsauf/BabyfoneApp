package de.nilsauf.babyfone.models.streaming

data class BabyfoneAudioRecordData(val Data: ByteArray, val Length: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BabyfoneAudioRecordData

        if (!Data.contentEquals(other.Data)) return false
        if (Length != other.Length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Data.contentHashCode()
        result = 31 * result + Length
        return result
    }
}

data class AudioRecordConfigurationData(val audioSource: Int, val frequency: Int, val channelConfig: Int, val audioEncoding: Int, val bufferSize: Int)