package de.nilsauf.babyfone.models.streaming

import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.nilsauf.babyfone.R
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.extensions.createStreamingChannel
import de.nilsauf.babyfone.models.streaming.streamwriter.BaseStreamWriter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AudioStreamingHandler @Inject constructor(
    private val notificationManager: NotificationManager,
    private val serverStreamFactory: Provider<Observable<BaseStreamWriter>>,
    private val audioRecordDataFactory: Provider<Observable<BabyfoneAudioRecordData>>,
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

        streamingStateSubject.onNext(StreamingState.ReadyToStream)

        val compositeDisposable = CompositeDisposable()

        val audioRecordSubject = audioRecordDataFactory.get()
            .publish()
            .autoConnect()

        compositeDisposable.addAll(
            streamingState.filter { it == StreamingState.Streaming }
                .firstElement()
                .subscribe {
                    notificationManager.notify(101, this.streamingNotification)
                },

            serverStreamFactory.get()
                .doAfterNext { streamingStateSubject.onNext(StreamingState.Streaming) }
                .flatMap { streamWriter -> audioRecordSubject
                    .observeOn(streamWriter.StreamScheduler)
                    .map { recordData -> streamWriter.write(recordData.Data, recordData.Length) }
                }
                .doFinally { streamingStateSubject.onNext(StreamingState.NotStreaming) }
                .onErrorComplete()
                .subscribe(),

            streamingState.filter { it == StreamingState.NotStreaming }
                .firstElement()
                .subscribe { notificationManager.cancelAll() }
        )

        streamingDisposable.set(compositeDisposable)
    }

    fun stopStream(){
        this.streamingDisposable.set(Disposable.empty())
    }
}