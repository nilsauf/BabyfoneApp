package de.nilsauf.babyfone.extensions

import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import de.nilsauf.babyfone.models.preferences.DataStoreManager
import de.nilsauf.babyfone.models.streaming.AudioRecordConfigurationData
import io.reactivex.rxjava3.core.Observable

fun DataStoreManager.connectToAudioRecordConfigurationData() : Observable<AudioRecordConfigurationData> {
    return Observable.combineLatest(
        this.connectToFrequency(),
        this.connectToChannelConfigurationIn(),
        this.connectToAudioEncoding(),
        this.connectToMinAudioRecordBufferSize()
    ) { frequency, channelConfig, audioEncoding, bufferSize ->
        AudioRecordConfigurationData(
            MediaRecorder.AudioSource.MIC,
            frequency,
            channelConfig,
            audioEncoding,
            bufferSize
        )
    }
}

fun DataStoreManager.connectToMinAudioRecordBufferSize() : Observable<Int> {
    return Observable.combineLatest(
        this.connectToFrequency(),
        this.connectToChannelConfigurationIn(),
        this.connectToAudioEncoding()
    ) { frequency, channelConfig, audioEncoding ->
        AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding)
    }
}

fun DataStoreManager.connectToMinAudioTrackBufferSize() : Observable<Int> {
    return Observable.combineLatest(
        this.connectToFrequency(),
        this.connectToChannelConfigurationOut(),
        this.connectToAudioEncoding()
    ) { frequency, channelConfig, audioEncoding ->
        AudioTrack.getMinBufferSize(frequency, channelConfig, audioEncoding)
    }
}