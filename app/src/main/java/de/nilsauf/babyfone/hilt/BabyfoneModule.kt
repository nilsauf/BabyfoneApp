package de.nilsauf.babyfone.hilt

import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.nilsauf.babyfone.data.StreamingData
import de.nilsauf.babyfone.extensions.observeConnections
import de.nilsauf.babyfone.models.preferences.DataStoreManager
import de.nilsauf.babyfone.models.streaming.BabyfoneAudioRecordData
import de.nilsauf.babyfone.models.streaming.streamwriter.BaseStreamWriter
import de.nilsauf.babyfone.models.streaming.StreamType
import de.nilsauf.babyfone.models.streaming.streamwriter.OutputStreamWriter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.kotlin.cast
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.ServerSocket

@Module
@InstallIn(SingletonComponent::class)
object BabyfoneModule {

    @Provides
    fun provideServerStream() : Observable<BaseStreamWriter> {
        val streamType = StreamType.Socket
        return when (streamType) {
            StreamType.Socket -> this.createTcpStream()
            StreamType.WebSocket -> throw NotImplementedError("Web Socket not yet implemented")
        }
    }

    private fun createTcpStream() : Observable<BaseStreamWriter> {
        val serverSocket = ServerSocket(StreamingData.port)
        val serverSocketScheduler = Schedulers.io()

        return serverSocket.observeConnections()
            .subscribeOn(serverSocketScheduler)
            .take(1)
            .map { socket -> socket.getOutputStream() }
            .map { stream -> OutputStreamWriter(stream, serverSocketScheduler) }
            .doFinally {
                if(!serverSocket.isClosed)
                    serverSocket.close()
            }
            .cast()
    }

    private data class AudioRecordConfigurationData(val audioSource: Int, val frequency: Int, val channelConfig: Int, val audioEncoding: Int, val bufferSize: Int)

    @Provides
    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    fun provideAudioRecordObservable(dataStoreManager: DataStoreManager) : Observable<BabyfoneAudioRecordData> {
        return Observable.combineLatest(
            dataStoreManager.connectToFrequency(),
            dataStoreManager.connectToChannelConfigurationIn(),
            dataStoreManager.connectToAudioEncoding()
            ) { frequency, channelConfig, audioEncoding ->
                AudioRecordConfigurationData(
                    MediaRecorder.AudioSource.MIC,
                    frequency,
                    channelConfig,
                    audioEncoding,
                    AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding)
                )
            }
            .take(1)
            .flatMap { createAndConnectToAudioRecord(it) }
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    private fun createAndConnectToAudioRecord(
        audioRecordConfigurationData: AudioRecordConfigurationData,
        scheduler: Scheduler = Schedulers.computation()
    ): Observable<BabyfoneAudioRecordData> {
        return Observable.using<BabyfoneAudioRecordData, AudioRecord>(
            {
                AudioRecord(
                    audioRecordConfigurationData.audioSource,
                    audioRecordConfigurationData.frequency,
                    audioRecordConfigurationData.channelConfig,
                    audioRecordConfigurationData.audioEncoding,
                    audioRecordConfigurationData.bufferSize)
            },
            {
                val byteBufferSize = audioRecordConfigurationData.bufferSize * 2
                val buffer = ByteArray(byteBufferSize)
                it.startRecording()

                Observable.create { obs ->
                    try {
                        while(!obs.isDisposed) {
                            val read = it.read(buffer, 0, audioRecordConfigurationData.bufferSize)
                            obs.onNext(BabyfoneAudioRecordData(buffer.clone(), read))
                        }
                        obs.onComplete()
                    } catch (ex: Exception){
                        obs.onError(ex)
                    }
                }
            },
            {
                it.stop()
                it.release()
            }
        ).subscribeOn(scheduler)
    }
}