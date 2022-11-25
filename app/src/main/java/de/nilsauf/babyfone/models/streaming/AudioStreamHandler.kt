package de.nilsauf.babyfone.models.streaming

import android.app.NotificationManager
import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.nilsauf.babyfone.R
import de.nilsauf.babyfone.data.StreamingData
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.extensions.createStreamingChannel
import de.nilsauf.babyfone.extensions.observeConnections
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.net.ServerSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioStreamHandler @Inject constructor(
    private val notificationManager: NotificationManager,
    @ApplicationContext appContext: Context
) {
    private val streamingDisposable = SerialDisposable()
    private val streamingStateSubject = BehaviorSubject.createDefault(StreamingState.NotStreaming)
    private val streamingNotification = NotificationCompat.Builder(appContext, "Streaming")
        .setContentText("Streaming for Baby...")
        .setContentTitle("Streaming")
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .build()

    val streamingState: Observable<StreamingState>
        get() { return this.streamingStateSubject.distinctUntilChanged() }

    init {
        notificationManager.createStreamingChannel()
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    fun startStream(){
        this.stopStream()

        val serverSocket = ServerSocket(StreamingData.port)
        val serverSocketScheduler = Schedulers.io()

        val frequency = StreamingData.frequency
        val channelConfiguration = StreamingData.channelInConfiguration
        val audioEncoding = StreamingData.audioEncoding

        val bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding)

        streamingStateSubject.onNext(StreamingState.ReadyToStream)

        val compositeDisposable = CompositeDisposable()

        compositeDisposable.addAll(
            streamingState.filter { it == StreamingState.Streaming }
                .take(1)
                .subscribe {
                    notificationManager.notify(101, this.streamingNotification)
                },

            serverSocket.observeConnections()
                .subscribeOn(serverSocketScheduler)
                .take(1)
                .doAfterNext { streamingStateSubject.onNext(StreamingState.Streaming) }
                .map { socket -> socket.getOutputStream() }
                .flatMap { stream ->  createAndConnectToAudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize)
                    .observeOn(serverSocketScheduler)
                    .map { recordPair ->
                        stream.write(recordPair.first, 0, recordPair.second)
                        recordPair.first
                    }
                }
                .doFinally {
                    streamingStateSubject.onNext(StreamingState.NotStreaming)
                    if(!serverSocket.isClosed) serverSocket.close()
                }
                .subscribe(),

            streamingState.filter { it == StreamingState.NotStreaming }
                .take(1)
                .subscribe { notificationManager.cancelAll() }
        )

        streamingDisposable.set(compositeDisposable)
    }

    fun stopStream(){
        this.streamingDisposable.set(Disposable.empty())
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    private fun createAndConnectToAudioRecord(
        audioSource : Int,
        sampleRateInHz : Int,
        channelConfig : Int,
        audioFormat : Int,
        bufferSizeInBytes : Int,
        scheduler: Scheduler = Schedulers.io()
    ): Observable<Pair<ByteArray, Int>> {
        return Observable.using<Pair<ByteArray, Int>, AudioRecord>(
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
                            obs.onNext(Pair(buffer.clone(), read))
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