package de.nilsauf.babyfone.hilt

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.nilsauf.babyfone.extensions.connectToMinAudioTrackBufferSize
import de.nilsauf.babyfone.models.listening.streamreader.BaseStreamReader
import de.nilsauf.babyfone.models.listening.streamreader.InputStreamReader
import de.nilsauf.babyfone.models.preferences.DataStoreManager
import de.nilsauf.babyfone.models.preferences.StreamType
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.cast
import io.reactivex.rxjava3.kotlin.mergeAll
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.net.Socket

@Module
@InstallIn(SingletonComponent::class)
object ListeningModule {

    @Provides
    fun provideClientStream(dataStoreManager: DataStoreManager) : Maybe<BaseStreamReader> {
        return dataStoreManager.connectToStreamType()
            .firstElement()
            .flatMap {
                when(it) {
                    StreamType.Socket -> this.createTcpStream(dataStoreManager)
                    StreamType.WebSocket -> throw NotImplementedError("Web Socket not yet implemented")
                }
            }
    }

    private fun createTcpStream(dataStoreManager: DataStoreManager) : Maybe<BaseStreamReader> {
        val ioScheduler = Schedulers.io()
        return Observable.combineLatest(
            dataStoreManager.connectToServerIpAddress(),
            dataStoreManager.connectToServerPort()
        ) { serverIp, serverPort ->
            Observable.using(
                { Socket(serverIp, serverPort) },
                { Observable.just(InputStreamReader(it.getInputStream(), ioScheduler)) },
                { it.close() })
                .retry { throwable -> throwable is IOException }
                .subscribeOn(ioScheduler)
        }
            .mergeAll()
            .firstElement()
            .cast()
    }

    @Provides
    fun provideAudioTrack(dataStoreManager: DataStoreManager) : Maybe<AudioTrack> {
        return Observable.combineLatest(
            dataStoreManager.connectToAudioEncoding(),
            dataStoreManager.connectToChannelConfigurationOut(),
            dataStoreManager.connectToFrequency()
        ) { audioEncoding, channelConfiguration, frequency ->
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build())
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(audioEncoding)
                        .setChannelMask(channelConfiguration)
                        .setSampleRate(frequency)
                        .build())
                .setTransferMode(AudioTrack.MODE_STREAM)
        }
            .firstElement()
            .flatMap { audioTrackBuilder ->
                dataStoreManager.connectToMinAudioTrackBufferSize()
                    .firstElement()
                    .map { audioTrackBuilder.setBufferSizeInBytes(it) }}
            .map { it.build() }
            .flatMap { audioTrack ->
                dataStoreManager.connectToAudioVolume()
                    .firstElement()
                    .map {
                        audioTrack.setVolume(it)
                        audioTrack
                    }
            }

    }
}