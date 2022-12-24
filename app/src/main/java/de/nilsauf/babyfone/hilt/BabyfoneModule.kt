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
import de.nilsauf.babyfone.models.streaming.BabyfoneAudioRecordData
import de.nilsauf.babyfone.models.streaming.BabyfoneStreamWriter
import de.nilsauf.babyfone.models.streaming.StreamType
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.kotlin.cast
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.ServerSocket

@Module
@InstallIn(SingletonComponent::class)
object BabyfoneModule {

    @Provides
    fun provideServerStream() : Observable<BabyfoneStreamWriter> {
        val streamType = StreamType.Socket
        return when (streamType) {
            StreamType.Socket -> this.createTcpStream()
            StreamType.WebSocket -> throw NotImplementedError("Web Socket not yet implemented")
        }
    }

    private fun createTcpStream() : Observable<BabyfoneStreamWriter> {
        val serverSocket = ServerSocket(StreamingData.port)
        val serverSocketScheduler = Schedulers.io()

        return serverSocket.observeConnections()
            .subscribeOn(serverSocketScheduler)
            .take(1)
            .map { socket -> socket.getOutputStream() }
            .map { stream -> BabyfoneStreamWriter(stream, serverSocketScheduler)}
            .doFinally {
                if(!serverSocket.isClosed)
                    serverSocket.close()
            }
            .cast()
    }

    @Provides
    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    fun provideAudioRecordObservable() : Observable<BabyfoneAudioRecordData> {
        val frequency = StreamingData.frequency
        val channelConfiguration = StreamingData.channelInConfiguration
        val audioEncoding = StreamingData.audioEncoding

        val bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding)

        return createAndConnectToAudioRecord(
            MediaRecorder.AudioSource.MIC,
            frequency,
            channelConfiguration,
            audioEncoding,
            bufferSize)
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    private fun createAndConnectToAudioRecord(
        audioSource : Int,
        sampleRateInHz : Int,
        channelConfig : Int,
        audioFormat : Int,
        bufferSizeInBytes : Int,
        scheduler: Scheduler = Schedulers.computation()
    ): Observable<BabyfoneAudioRecordData> {
        return Observable.using<BabyfoneAudioRecordData, AudioRecord>(
            {
                AudioRecord(
                    audioSource,
                    sampleRateInHz,
                    channelConfig,
                    audioFormat,
                    bufferSizeInBytes)
            },
            {
                val byteBufferSize = bufferSizeInBytes * 2
                val buffer = ByteArray(byteBufferSize)
                it.startRecording()

                Observable.create { obs ->
                    try {
                        while(!obs.isDisposed) {
                            val read = it.read(buffer, 0, bufferSizeInBytes)
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